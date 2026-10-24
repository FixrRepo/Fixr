package com.inspaya.fixr.services;

import com.inspaya.fixr.dtos.UserValidityResponseDto;
import com.inspaya.fixr.dtos.ValidityScoreEventDto;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.entities.UserValidityProfile;
import com.inspaya.fixr.repositories.FixrUserRepo;
import com.inspaya.fixr.repositories.UserValidityProfileRepo;
import com.inspaya.fixr.repositories.ValidityScoreEventRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserValidityService {
    private final UserValidityProfileRepo profileRepo;
    private final ValidityScoreEventRepo scoreEventRepo;
    private final FixrUserRepo userRepo;

    @Transactional
    public UserValidityProfile profileFor(FixrUser user) {
        return profileRepo.findById(user.getUserId())
                .orElseGet(() -> profileRepo.save(new UserValidityProfile(user)));
    }

    @Transactional
    public UserValidityResponseDto getCurrentUserValidity(FixrUser user) {
        UUID userId = user.getUserId();
        UserValidityProfile profile = profileRepo.findById(userId)
                .orElseGet(() -> profileRepo.save(new UserValidityProfile(
                        userRepo.findById(userId).orElseThrow())));
        var history = scoreEventRepo.findAllByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(event -> new ValidityScoreEventDto(
                        event.getId(), event.getReport().getReportId(), event.getScoreDelta(),
                        event.getScoreAfter(), event.getReason(), event.getScoringVersion(),
                        event.getEvaluationEligibleVoters(), event.getEvaluationSupportRatio(), event.getCreatedAt()))
                .toList();
        return new UserValidityResponseDto(profile.getValidityScore(), profile.getScoredAssessments(), history);
    }
}
