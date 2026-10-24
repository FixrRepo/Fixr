package com.inspaya.fixr.services;

import com.inspaya.fixr.configurations.properties.ValidationPolicyProperties;
import com.inspaya.fixr.dtos.VotingAbuseFlagDto;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.entities.Report;
import com.inspaya.fixr.entities.ReportVote;
import com.inspaya.fixr.entities.VotingAbuseFlag;
import com.inspaya.fixr.enums.VotingAbusePattern;
import com.inspaya.fixr.enums.ReportVoteType;
import com.inspaya.fixr.enums.AbuseReviewOutcome;
import com.inspaya.fixr.dtos.ReviewVotingAbuseFlagRequest;
import com.inspaya.fixr.repositories.ReportVoteRepo;
import com.inspaya.fixr.repositories.VotingAbuseFlagRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VotingAbuseDetectionService {
    private final ReportVoteRepo voteRepo;
    private final VotingAbuseFlagRepo flagRepo;
    private final ValidationPolicyProperties policy;

    @Transactional
    public void inspectVote(Report report, FixrUser voter, ReportVote currentVote) {
        flagBurst(report, currentVote);
        flagRepeatedTargeting(report, voter);
        flagReciprocalPattern(report, voter);
    }

    @Transactional(readOnly = true)
    public List<VotingAbuseFlagDto> getOpenFlags() {
        return flagRepo.findAllByReviewedAtIsNullOrderByCreatedAtDesc().stream()
                .map(flag -> new VotingAbuseFlagDto(flag.getId(), flag.getReport().getReportId(),
                        flag.getFlaggedUser() == null ? null : flag.getFlaggedUser().getUserId(),
                        flag.getFlaggedUser() == null ? null : flag.getFlaggedUser().getUsername(),
                        flag.getPattern(), flag.getDetails(), flag.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void reviewFlag(java.util.UUID flagId, FixrUser moderator, ReviewVotingAbuseFlagRequest request) {
        VotingAbuseFlag flag = flagRepo.findById(flagId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Voting abuse flag not found"));
        if (flag.getReviewedAt() != null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Voting abuse flag has already been reviewed");
        }
        flag.setReviewedAt(java.time.Instant.now());
        flag.setReviewedBy(moderator);
        flag.setReviewOutcome(request.outcome());
        flag.setReviewNotes(request.notes());
    }

    private void flagBurst(Report report, ReportVote currentVote) {
        ZonedDateTime since = ZonedDateTime.now(ZoneId.of("Africa/Lagos"))
                .minusMinutes(policy.burstWindowMinutes());
        List<ReportVote> recentSameSide = voteRepo.findAllByReportReportId(report.getReportId()).stream()
                .filter(vote -> vote.getVoteType() == currentVote.getVoteType())
                .filter(vote -> effectiveTimestamp(vote).isAfter(since))
                .toList();
        if (recentSameSide.size() >= policy.burstVoteCount()) {
            String users = recentSameSide.stream().map(vote -> vote.getUser().getUserId().toString())
                    .distinct().sorted().collect(java.util.stream.Collectors.joining(","));
            createFlag(report, null, VotingAbusePattern.BURST_ACTIVITY,
                    "sameSide=" + currentVote.getVoteType() + "; voters=" + users);
        }
    }

    private void flagRepeatedTargeting(Report report, FixrUser voter) {
        ZonedDateTime since = ZonedDateTime.now(ZoneId.of("Africa/Lagos"))
                .minusDays(policy.repeatedTargetWindowDays());
        long reportsTargeted = voteRepo.countDistinctReportsByVoterAndReportOwnerSince(
                voter.getUserId(), report.getReporter().getUserId(), since);
        if (reportsTargeted >= policy.repeatedTargetReportCount()) {
            createFlag(report, voter, VotingAbusePattern.REPEATED_TARGETING,
                    "distinctReports=" + reportsTargeted + "; targetReporter=" + report.getReporter().getUserId());
        }
    }

    private void flagReciprocalPattern(Report report, FixrUser voter) {
        FixrUser reportOwner = report.getReporter();
        if (reportOwner.getUserId().equals(voter.getUserId())) return;
        ZonedDateTime since = ZonedDateTime.now(ZoneId.of("Africa/Lagos"))
                .minusDays(policy.reciprocalWindowDays());
        List<ReportVote> voterVotes = voteRepo.findAllByUserUserId(voter.getUserId());
        List<ReportVote> ownerVotes = voteRepo.findAllByUserUserId(reportOwner.getUserId());
        boolean reciprocalUpvoting = countForOwner(voterVotes, reportOwner.getUserId(), since, ReportVoteType.UPVOTE)
                >= policy.reciprocalReportsPerDirection()
                && countForOwner(ownerVotes, voter.getUserId(), since, ReportVoteType.UPVOTE)
                >= policy.reciprocalReportsPerDirection();
        boolean reciprocalDownvoting = countForOwner(voterVotes, reportOwner.getUserId(), since, ReportVoteType.DOWNVOTE)
                >= policy.reciprocalReportsPerDirection()
                && countForOwner(ownerVotes, voter.getUserId(), since, ReportVoteType.DOWNVOTE)
                >= policy.reciprocalReportsPerDirection();
        if (reciprocalUpvoting || reciprocalDownvoting) {
            createFlag(report, voter, VotingAbusePattern.RECIPROCAL_VOTING,
                    "sameSide=" + (reciprocalUpvoting ? "UPVOTE" : "DOWNVOTE")
                            + "; reportsEachDirection=" + policy.reciprocalReportsPerDirection()
                            + "; otherUser=" + reportOwner.getUserId());
        }
    }

    private long countForOwner(List<ReportVote> votes, java.util.UUID ownerId,
                               ZonedDateTime since, ReportVoteType type) {
        return votes.stream()
                .filter(vote -> vote.getReport().getReporter().getUserId().equals(ownerId))
                .filter(vote -> vote.getVoteType() == type)
                .filter(vote -> !effectiveTimestamp(vote).isBefore(since))
                .map(vote -> vote.getReport().getReportId())
                .distinct()
                .count();
    }

    private ZonedDateTime effectiveTimestamp(ReportVote vote) {
        return vote.getUpdatedAt() == null ? vote.getCreatedAt() : vote.getUpdatedAt();
    }

    private void createFlag(Report report, FixrUser user, VotingAbusePattern pattern, String details) {
        if (!flagRepo.existsByReportReportIdAndPattern(report.getReportId(), pattern)) {
            flagRepo.save(new VotingAbuseFlag(report, user, pattern, details));
        }
    }
}
