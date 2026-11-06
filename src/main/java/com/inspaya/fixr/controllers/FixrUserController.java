package com.inspaya.fixr.controllers;

import com.inspaya.fixr.dtos.*;
import com.inspaya.fixr.entities.token.OtpToken;
import com.inspaya.fixr.enums.DeliveryMedium;
import com.inspaya.fixr.enums.OtpTokenPurpose;
import com.inspaya.fixr.services.*;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.services.cloudinary.CloudinaryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/fixr/user")
@RequiredArgsConstructor
public class FixrUserController {
    private final ReportService reportService;
    private final ReportEmbeddingService reportEmbeddingService;
    private final FixrUserService service;
    private final UserValidityService userValidityService;
    private final VerificationService verificationService;
    private final CloudinaryService cloudinaryService;
    private final JwtTokenIssueManager tokenIssueManager;

    @PostMapping("/auth/signup")
    public ResponseEntity<FixrUserResponseDto> createAccount(@Valid @RequestBody CreateFixrUserDto userDto){
        return ResponseEntity.ok(service.createFixrUser(userDto));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<JwtTokenResponseDto> handeRefreshToken(
                                                                 HttpServletRequest request){
        if(request.getCookies() != null){
            final Optional<Cookie> optionalCookie = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("RefreshToken")).findFirst();
            final HttpHeaders headers = new HttpHeaders();

            if(optionalCookie.isPresent()){
                String token = optionalCookie.get().getValue();
                headers.addAll(tokenIssueManager.reIssueJwtTokensCookies(token));
                return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
            }
        }

        // Check authorization header for tokens
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authorization!=null && authorization.startsWith("Bearer ") && !authorization.substring(7).isEmpty()){
            String token = authorization.substring(7);
            return ResponseEntity.ok(tokenIssueManager.reIssueJwtTokens(token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logoutFixrUser(@AuthenticationPrincipal FixrUserSecurityDetail detail){
        tokenIssueManager.revokeTokenSession(detail.getSessionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/email/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerificationDto emailVerificationDto){
        boolean verified = service.verifyUserEmail(emailVerificationDto);
        return verified ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }

    @PostMapping("/auth/password/forgot")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody OtpRequestDto otpRequest){
        service.sendPasswordResetToken(otpRequest);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/auth/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto){
        service.verifyAndResetPassword(passwordResetDto);
        return ResponseEntity.accepted().build();
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

    @GetMapping("/reports/media/signature")
    public ResponseEntity<MediaUploadResponse> getMediaUploadSignature(){
        return ResponseEntity.ok(cloudinaryService.generateSignedUploadKey("report"));
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
