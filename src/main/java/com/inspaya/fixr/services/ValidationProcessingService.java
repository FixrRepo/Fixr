package com.inspaya.fixr.services;

import com.inspaya.fixr.dtos.ReportValidationResponseDto;
import com.inspaya.fixr.exceptions.ValidationProcessingException;
import com.inspaya.fixr.entities.*;
import com.inspaya.fixr.enums.ReportValidationStatus;
import com.inspaya.fixr.enums.ReportVoteType;
import com.inspaya.fixr.enums.ValidityScoreReason;
import com.inspaya.fixr.repositories.*;
import com.inspaya.fixr.configurations.properties.ValidationPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidationProcessingService {
    private static final BigDecimal MAX_SCORE = new BigDecimal("100.00");

    private final ValidationOutboxRepo outboxRepo;
    private final ReportRepo reportRepo;
    private final ReportVoteRepo voteRepo;
    private final UserValidityProfileRepo profileRepo;
    private final ValidationSnapshotRepo snapshotRepo;
    private final ValidityScoreEventRepo scoreEventRepo;
    private final ValidationPolicyProperties policy;
    private final LeaderboardProjectionService leaderboardProjectionService;

    @Transactional
    public void processOne() {
        List<ValidationOutboxEvent> pending = outboxRepo.findPendingForUpdate(Instant.now(), PageRequest.of(0, 1));
        if (pending.isEmpty()) return;

        ValidationOutboxEvent event = pending.getFirst();
        try {
            Report report = reportRepo.findForValidation(event.getReportId()).orElse(null);
            if (report != null) recalculate(report);
            event.setProcessedAt(Instant.now());
            event.setLastError(null);
            outboxRepo.flush();
        } catch (RuntimeException exception) {
            throw new ValidationProcessingException(event.getId(), exception);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID outboxEventId, String error) {
        outboxRepo.findById(outboxEventId).ifPresent(event -> {
            int attempts = event.getAttempts() + 1;
            event.setAttempts(attempts);
            long delaySeconds = Math.min(3600L, 1L << Math.min(attempts, 12));
            event.setNextAttemptAt(Instant.now().plusSeconds(delaySeconds));
            event.setLastError(error == null ? "Unknown error" : error.substring(0, Math.min(error.length(), 2000)));
        });
    }

    @Transactional(readOnly = true)
    public ReportValidationResponseDto getReportValidation(UUID reportId) {
        Report report = reportRepo.findById(reportId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        ValidationSnapshot snapshot = snapshotRepo.findFirstByReportReportIdOrderByCreatedAtDesc(reportId).orElse(null);
        int count = snapshot == null ? 0 : snapshot.getEligibleVoteCount();
        BigDecimal up = snapshot == null ? BigDecimal.ZERO : snapshot.getUpvoteWeight();
        BigDecimal down = snapshot == null ? BigDecimal.ZERO : snapshot.getDownvoteWeight();
        BigDecimal ratio = snapshot == null ? null : snapshot.getSupportRatio();
        return new ReportValidationResponseDto(reportId, report.getValidationStatus(), count,
                up, down, ratio, report.getValidationStatus() == ReportValidationStatus.PROVISIONAL);
    }

    private void recalculate(Report report) {
        List<ReportVote> votes = voteRepo.findAllByReportReportId(report.getReportId());
        BigDecimal up = sumWeight(votes, ReportVoteType.UPVOTE);
        BigDecimal down = sumWeight(votes, ReportVoteType.DOWNVOTE);
        BigDecimal supportRatio = ratio(up, down);
        ReportValidationStatus status = determineStatus(votes.size(), supportRatio);

        ValidationSnapshot snapshot = snapshotRepo.save(new ValidationSnapshot(
                report, votes.size(), up, down, supportRatio, status, serializeVotes(votes)));
        report.setValidationStatus(status);

        if (status == ReportValidationStatus.SUPPORTED || status == ReportValidationStatus.DISPUTED) {
            evaluateVotes(votes, report, snapshot);
        }
    }

    private void evaluateVotes(List<ReportVote> votes, Report report, ValidationSnapshot snapshot) {
        for (ReportVote assessedVote : votes) {
            if (scoreEventRepo.existsByUserUserIdAndReportReportIdAndScoringVersion(
                    assessedVote.getUser().getUserId(), report.getReportId(), policy.scoringVersion())) continue;

            BigDecimal otherUp = sumWeightExcluding(votes, ReportVoteType.UPVOTE, assessedVote);
            BigDecimal otherDown = sumWeightExcluding(votes, ReportVoteType.DOWNVOTE, assessedVote);
            long otherVoters = votes.stream().filter(vote -> !vote.getId().equals(assessedVote.getId())).count();
            BigDecimal independentRatio = ratio(otherUp, otherDown);
            ReportValidationStatus independentResult = determineStatus((int) otherVoters, independentRatio);
            if (independentResult != ReportValidationStatus.SUPPORTED
                    && independentResult != ReportValidationStatus.DISPUTED) continue;

            boolean agrees = (assessedVote.getVoteType() == ReportVoteType.UPVOTE
                    && independentResult == ReportValidationStatus.SUPPORTED)
                    || (assessedVote.getVoteType() == ReportVoteType.DOWNVOTE
                    && independentResult == ReportValidationStatus.DISPUTED);
            BigDecimal adjustment = agrees ? policy.scoreAdjustment() : policy.scoreAdjustment().negate();
            UserValidityProfile profile = profileRepo.findById(assessedVote.getUser().getUserId())
                    .orElseGet(() -> profileRepo.save(new UserValidityProfile(assessedVote.getUser())));
            BigDecimal before = profile.getValidityScore();
            BigDecimal after = before.add(adjustment).max(BigDecimal.ZERO).min(MAX_SCORE);
            BigDecimal actualDelta = after.subtract(before);
            profile.setValidityScore(after);
            profile.setScoredAssessments(profile.getScoredAssessments() + 1);
            assessedVote.getUser().setCredibilityScore(after.doubleValue());
            scoreEventRepo.save(new ValidityScoreEvent(assessedVote.getUser(), report, snapshot,
                    actualDelta, after, agrees ? ValidityScoreReason.CONSENSUS_AGREEMENT
                    : ValidityScoreReason.CONSENSUS_DISAGREEMENT, policy.scoringVersion(),
                    (int) otherVoters, independentRatio));
            leaderboardProjectionService.enqueue(assessedVote.getUser().getUserId());
        }
    }

    private BigDecimal sumWeight(List<ReportVote> votes, ReportVoteType type) {
        return votes.stream().filter(vote -> vote.getVoteType() == type)
                .map(ReportVote::getWeightAtVote).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumWeightExcluding(List<ReportVote> votes, ReportVoteType type, ReportVote excluded) {
        return votes.stream().filter(vote -> vote != excluded && vote.getVoteType() == type)
                .map(ReportVote::getWeightAtVote).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal ratio(BigDecimal up, BigDecimal down) {
        BigDecimal total = up.add(down);
        return total.signum() == 0 ? null : up.divide(total, 6, RoundingMode.HALF_UP);
    }

    private ReportValidationStatus determineStatus(int voterCount, BigDecimal ratio) {
        if (voterCount < policy.minimumVoters() || ratio == null) return ReportValidationStatus.PROVISIONAL;
        if (ratio.compareTo(policy.supportThreshold()) >= 0) return ReportValidationStatus.SUPPORTED;
        if (ratio.compareTo(policy.disputeThreshold()) <= 0) return ReportValidationStatus.DISPUTED;
        return ReportValidationStatus.INCONCLUSIVE;
    }

    private String serializeVotes(List<ReportVote> votes) {
        return votes.stream().map(vote -> String.format(
                        "{\"voteId\":\"%s\",\"userId\":\"%s\",\"decision\":\"%s\",\"weight\":%s}",
                        vote.getId(), vote.getUser().getUserId(), vote.getVoteType(), vote.getWeightAtVote()))
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
}
