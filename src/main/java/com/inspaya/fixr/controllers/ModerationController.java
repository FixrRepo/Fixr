package com.inspaya.fixr.controllers;

import com.inspaya.fixr.dtos.VotingAbuseFlagDto;
import com.inspaya.fixr.dtos.ReviewVotingAbuseFlagRequest;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.services.VotingAbuseDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fixr/moderation")
@RequiredArgsConstructor
public class ModerationController {
    private final VotingAbuseDetectionService abuseDetectionService;

    @GetMapping("/voting-abuse-flags")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public List<VotingAbuseFlagDto> getOpenVotingAbuseFlags() {
        return abuseDetectionService.getOpenFlags();
    }

    @PatchMapping("/voting-abuse-flags/{flagId}")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public ResponseEntity<Void> reviewVotingAbuseFlag(
            @PathVariable java.util.UUID flagId,
            @Valid @RequestBody ReviewVotingAbuseFlagRequest request,
            @AuthenticationPrincipal FixrUserSecurityDetail moderator) {
        abuseDetectionService.reviewFlag(flagId, moderator.getUser(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
