package com.inspaya.fixr.controllers;

import com.inspaya.fixr.dtos.*;
import com.inspaya.fixr.services.ReportEmbeddingService;
import com.inspaya.fixr.services.ReportService;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.services.FixrUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fixr/user")
@RequiredArgsConstructor
public class FixrUserController {
    private final ReportService reportService;
    private final ReportEmbeddingService reportEmbeddingService;
    private final FixrUserService service;
    private final UserValidityService userValidityService;

    @PostMapping("/auth/signup")
    public ResponseEntity<FixrUserResponseDto> createAccount(@Valid @RequestBody CreateFixrUserDto userDto){
        return ResponseEntity.ok(service.createFixrUser(userDto));
    }

    @GetMapping
    public ResponseEntity<FixrUserResponseDto> getFixrUser(@AuthenticationPrincipal FixrUserSecurityDetail detail){
        return ResponseEntity.ok(service.mapUser(detail.getUser()));
    }

    @GetMapping("/validity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserValidityResponseDto> getCurrentUserValidity(
            @AuthenticationPrincipal FixrUserSecurityDetail detail) {
        return ResponseEntity.ok(userValidityService.getCurrentUserValidity(detail.getUser()));
    }

    @PatchMapping
    public ResponseEntity<FixrUserResponseDto> patchFixrUser(){
        return null;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal FixrUserSecurityDetail detail){
        return null;
    }

    @PostMapping("/reports")
    public ResponseEntity<UserReportResponseDto> createReport(@Valid @RequestBody CreateReportDto reportDto, @AuthenticationPrincipal FixrUserSecurityDetail details){
        return ResponseEntity.ok(reportService.createReport(reportDto, details.getUser()));
    }

    @GetMapping("/reports")
    public ResponseEntity<PagedModel<UserReportResponseDto>> getUserReports(@AuthenticationPrincipal FixrUserSecurityDetail detail,
                                                     @RequestParam(name = "pageNum", defaultValue = "0") int pageNum, @RequestParam(name = "pageSize", defaultValue = "20") int pageSize){
        return ResponseEntity.ok(reportService.getUserSubmittedReports(pageNum, pageSize, detail.getUser()));
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<UserReportResponseDto> getUserReport(@PathVariable(name = "reportId") String reportId, @AuthenticationPrincipal FixrUserSecurityDetail detail){
        return ResponseEntity.ok(reportService.getUserSubmittedReport(reportId, detail.getUser()));
    }

    @PatchMapping("/reports")
    public ResponseEntity<UserReportResponseDto> patchUserReport(@RequestParam(name = "reportId") String reportId, @AuthenticationPrincipal FixrUserSecurityDetail detail){
        return null;
    }

    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<Void> deleteUserReport(@PathVariable(name = "reportId") String reportId, @AuthenticationPrincipal FixrUserSecurityDetail detail){
        reportService.deleteReport(reportId, detail.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/{reportId}/duplicate-candidates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedModel<DuplicateCandidateResponseDto>> getDuplicateCandidates(
            @PathVariable String reportId, @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail,
            Pageable pageable) {
        reportService.ensureReportOwner(reportId, detail.getUser());
        return ResponseEntity.ok(reportEmbeddingService.getDuplicateCandidates(reportId, pageable));
    }

}
