package com.inspaya.fixr.controllers;

import com.inspaya.fixr.dtos.LeaderboardEntryDto;
import com.inspaya.fixr.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fixr/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<PagedModel<LeaderboardEntryDto>> getLeaderboard(
            @RequestParam(name = "stateId", required = false) Long stateId,
            @RequestParam(name = "lgaId", required = false) Long lgaId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(stateId, lgaId, pageNum, pageSize));
    }
}
