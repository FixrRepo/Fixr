package com.inspaya.fixr.services;

import com.inspaya.fixr.dtos.LeaderboardEntryDto;
import com.inspaya.fixr.entities.Lga;
import com.inspaya.fixr.entities.State;
import com.inspaya.fixr.repositories.LgaRepo;
import com.inspaya.fixr.repositories.StateRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private static final String GLOBAL_KEY = "fixr:leaderboard:score:global";
    private static final String STATE_KEY_PREFIX = "fixr:leaderboard:score:state:";
    private static final String LGA_KEY_PREFIX = "fixr:leaderboard:score:lga:";
    private static final String STATS_KEY_PREFIX = "fixr:leaderboard:stats:";

    private final StringRedisTemplate redis;
    private final StateRepo stateRepo;
    private final LgaRepo lgaRepo;

    @Transactional(readOnly = true)
    public PagedModel<LeaderboardEntryDto> getLeaderboard(Long stateId, Long lgaId, int pageNum, int pageSize) {
        if (pageNum < 0 || pageSize < 1 || pageSize > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pageNum must be non-negative and pageSize must be between 1 and 100");
        }

        String key = GLOBAL_KEY;
        State state = null;
        if (stateId != null) {
            state = stateRepo.findById(stateId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));
            key = STATE_KEY_PREFIX + stateId;
        }
        if (lgaId != null) {
            Lga lga = lgaRepo.findById(lgaId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LGA not found"));
            if (state != null && !lga.getState().getStateId().equals(state.getStateId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "LGA does not belong to the selected state");
            }
            key = LGA_KEY_PREFIX + lgaId;
        }

        Long size = redis.opsForZSet().zCard(key);
        long total = size == null ? 0 : size;
        long start = (long) pageNum * pageSize;
        long end = start + pageSize - 1;
        Set<ZSetOperations.TypedTuple<String>> ranked = redis.opsForZSet().reverseRangeWithScores(key, start, end);
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        if (ranked != null) {
            long rank = start + 1;
            for (ZSetOperations.TypedTuple<String> tuple : ranked) {
                String userId = tuple.getValue();
                if (!StringUtils.hasText(userId)) continue;
                Map<?, ?> stats = redis.opsForHash().entries(STATS_KEY_PREFIX + userId);
                if (stats.isEmpty()) continue;
                entries.add(new LeaderboardEntryDto(rank++, stringValue(stats.get("username")),
                        decimalValue(stats.get("score"), tuple.getScore()),
                        longValue(stats.get("reportCount")), longValue(stats.get("supportedReportCount")),
                        longValue(stats.get("disputedReportCount")), (int) longValue(stats.get("scoredAssessments"))));
            }
        }

        return new PagedModel<>(new PageImpl<>(entries, PageRequest.of(pageNum, pageSize), total));
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private long longValue(Object value) {
        return value == null ? 0 : Long.parseLong(value.toString());
    }

    private BigDecimal decimalValue(Object value, Double fallback) {
        if (value != null) return new BigDecimal(value.toString());
        return fallback == null ? BigDecimal.ZERO : BigDecimal.valueOf(fallback);
    }
}
