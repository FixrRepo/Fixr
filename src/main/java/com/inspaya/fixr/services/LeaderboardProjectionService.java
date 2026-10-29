package com.inspaya.fixr.services;

import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.entities.LeaderboardOutboxEvent;
import com.inspaya.fixr.entities.UserValidityProfile;
import com.inspaya.fixr.enums.ReportVoteType;
import com.inspaya.fixr.repositories.FixrUserRepo;
import com.inspaya.fixr.repositories.LeaderboardOutboxRepo;
import com.inspaya.fixr.repositories.ReportRepo;
import com.inspaya.fixr.repositories.ReportVoteRepo;
import com.inspaya.fixr.repositories.UserValidityProfileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaderboardProjectionService {
    private static final String GLOBAL_KEY = "fixr:leaderboard:score:global";
    private static final String STATE_KEY_PREFIX = "fixr:leaderboard:score:state:";
    private static final String LGA_KEY_PREFIX = "fixr:leaderboard:score:lga:";
    private static final String STATS_KEY_PREFIX = "fixr:leaderboard:stats:";

    private final StringRedisTemplate redis;
    private final FixrUserRepo userRepo;
    private final UserValidityProfileRepo profileRepo;
    private final ReportRepo reportRepo;
    private final ReportVoteRepo voteRepo;
    private final LeaderboardOutboxRepo outboxRepo;

    @Transactional
    public void enqueue(UUID userId) {
        outboxRepo.save(new LeaderboardOutboxEvent(userId));
    }

    @Transactional
    public void enqueueAllExistingUsers() {
        List<LeaderboardOutboxEvent> events = userRepo.findAll().stream()
                .map(user -> new LeaderboardOutboxEvent(user.getUserId()))
                .toList();
        outboxRepo.saveAll(events);
    }

    public void refreshUser(UUID userId) {
        String member = userId.toString();
        String statsKey = STATS_KEY_PREFIX + member;
        Map<?, ?> oldStats = redis.opsForHash().entries(statsKey);

        FixrUser user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            removeFromRegionalBoards(member, stringValue(oldStats.get("stateId")), stringValue(oldStats.get("lgaId")));
            redis.opsForZSet().remove(GLOBAL_KEY, member);
            redis.delete(statsKey);
            return;
        }

        UserValidityProfile profile = profileRepo.findById(userId).orElse(null);
        BigDecimal score = profile == null ? BigDecimal.valueOf(user.getCredibilityScore()) : profile.getValidityScore();
        String stateId = user.getState() == null ? null : user.getState().getStateId().toString();
        String lgaId = user.getLga() == null ? null : user.getLga().getLgaId().toString();
        removeChangedRegionalMemberships(member, stringValue(oldStats.get("stateId")),
                stringValue(oldStats.get("lgaId")), stateId, lgaId);

        Map<String, String> stats = new HashMap<>();
        stats.put("username", user.getUsername());
        stats.put("score", score.toPlainString());
        stats.put("reportCount", Long.toString(reportRepo.countByReporterUserId(userId)));
        stats.put("supportedReportCount", Long.toString(voteRepo.countByUserUserIdAndVoteType(userId, ReportVoteType.UPVOTE)));
        stats.put("disputedReportCount", Long.toString(voteRepo.countByUserUserIdAndVoteType(userId, ReportVoteType.DOWNVOTE)));
        stats.put("scoredAssessments", Integer.toString(profile == null ? 0 : profile.getScoredAssessments()));
        if (stateId != null) stats.put("stateId", stateId);
        if (lgaId != null) stats.put("lgaId", lgaId);

        redis.opsForZSet().add(GLOBAL_KEY, member, score.doubleValue());
        if (stateId != null) redis.opsForZSet().add(STATE_KEY_PREFIX + stateId, member, score.doubleValue());
        if (lgaId != null) redis.opsForZSet().add(LGA_KEY_PREFIX + lgaId, member, score.doubleValue());
        redis.opsForHash().delete(statsKey, "stateId", "lgaId");
        redis.opsForHash().putAll(statsKey, stats);
    }

    private void removeChangedRegionalMemberships(String userId, String oldStateId, String oldLgaId,
                                                   String newStateId, String newLgaId) {
        if (oldStateId != null && !oldStateId.equals(newStateId)) {
            redis.opsForZSet().remove(STATE_KEY_PREFIX + oldStateId, userId);
        }
        if (oldLgaId != null && !oldLgaId.equals(newLgaId)) {
            redis.opsForZSet().remove(LGA_KEY_PREFIX + oldLgaId, userId);
        }
    }

    private void removeFromRegionalBoards(String userId, String stateId, String lgaId) {
        if (stateId != null) redis.opsForZSet().remove(STATE_KEY_PREFIX + stateId, userId);
        if (lgaId != null) redis.opsForZSet().remove(LGA_KEY_PREFIX + lgaId, userId);
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
