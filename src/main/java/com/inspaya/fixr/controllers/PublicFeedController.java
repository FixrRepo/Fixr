package com.inspaya.fixr.controllers;

import com.inspaya.fixr.dtos.*;
import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.services.ReportEmbeddingService;
import com.inspaya.fixr.services.ReportRecommendationService;
import com.inspaya.fixr.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fixr/feed")
public class PublicFeedController {
    private final ReportRecommendationService recommendationService;
    private final ReportService reportService;
    private final ReportEmbeddingService reportEmbeddingService;
    private final ValidationProcessingService validationProcessingService;

    @GetMapping
    public ResponseEntity<PagedModel<ReportFeedResponseDto>> getFeeds(
            @RequestParam(name = "category", defaultValue = "*") String category,
            @RequestParam(name = "status", defaultValue = "ALL") ReportStatus status,
            @RequestParam(name = "state", defaultValue = "*") String state,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail,
            @RequestParam(value = "pageNum", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "40") int pageSize,
            @RequestParam(value = "order", defaultValue = "desc") String order) {
        FeedSpecifications specification = new FeedSpecifications(
                category, status, state, detail, pageNumber, pageSize, order);
        return ResponseEntity.ok(recommendationService.getRecommendation(specification));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportFeedResponseDto> getFeed(
            @PathVariable String reportId,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        return ResponseEntity.ok(reportService.getReportFeed(
                reportId, detail == null ? null : detail.getUser()));
    }

    @GetMapping("/reports/{reportId}/validation")
    public ResponseEntity<ReportValidationResponseDto> getReportValidation(@PathVariable String reportId) {
        try {
            return ResponseEntity.ok(validationProcessingService.getReportValidation(java.util.UUID.fromString(reportId)));
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reportId");
        }
    }

    @PostMapping("/reports/{reportId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportCommentResponseDto> leaveCommentOnReport(
            @PathVariable String reportId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                reportService.commentOnReport(reportId, request, detail.getUser()));
    }

    @GetMapping("/reports/{reportId}/comments")
    public ResponseEntity<PagedModel<ReportCommentResponseDto>> getReportComments(
            @PathVariable String reportId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(reportService.getReportComments(reportId, pageNum, pageSize));
    }

    @PatchMapping("/reports/{reportId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportCommentResponseDto> editComment(
            @PathVariable String reportId, @PathVariable String commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        return ResponseEntity.ok(reportService.editComment(
                reportId, commentId, request.content(), detail.getUser()));
    }

    @DeleteMapping("/reports/{reportId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String reportId, @PathVariable String commentId,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        reportService.deleteComment(reportId, commentId, detail.getUser());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reports/{reportId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportVoteResponseDto> voteOnReport(
            @PathVariable String reportId, @Valid @RequestBody ReportVoteRequest request,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        return ResponseEntity.ok(reportService.voteOnReport(
                reportId, detail.getUser(), request.vote()));
    }

    @DeleteMapping("/reports/{reportId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeVote(
            @PathVariable String reportId,
            @AuthenticationPrincipal(errorOnInvalidType = false) FixrUserSecurityDetail detail) {
        reportService.removeVote(reportId, detail.getUser());
        return ResponseEntity.noContent().build();
    }


}
