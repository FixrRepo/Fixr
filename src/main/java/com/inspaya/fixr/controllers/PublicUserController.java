package com.inspaya.fixr.controllers;

import com.inspaya.fixr.dtos.ReportFeedResponseDto;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fixr/users")
public class PublicUserController {
    private static final String ANONYMOUS_USERNAME_PREFIX = "anonymous-";

    private final ReportService reportService;

    @GetMapping("/{username}/reports")
    public ResponseEntity<PagedModel<ReportFeedResponseDto>> getPublicUserReports(
            @PathVariable String username,
            @RequestParam(name = "pageNum", defaultValue = "0") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        if (username.toLowerCase(Locale.ROOT).startsWith(ANONYMOUS_USERNAME_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Public profile not found");
        }

        return ResponseEntity.ok(reportService.getPublicUserReports(
                username, pageNum, pageSize, detail == null ? null : detail.getUser()));
    }
}
