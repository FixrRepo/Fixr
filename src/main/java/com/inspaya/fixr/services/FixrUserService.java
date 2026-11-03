package com.inspaya.fixr.services;

import com.inspaya.fixr.dtos.*;
import com.inspaya.fixr.encryption.BlindIndexService;
import com.inspaya.fixr.entities.location.City;
import com.inspaya.fixr.entities.location.Lga;
import com.inspaya.fixr.entities.location.State;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.exceptions.EntityNotFoundException;
import com.inspaya.fixr.exceptions.FixrServerError;
import com.inspaya.fixr.exceptions.InvalidTokenException;
import com.inspaya.fixr.mapper.UserMapper;
import com.inspaya.fixr.repositories.CredibilityEventRepo;
import com.inspaya.fixr.repositories.FixrUserFollowRepo;
import com.inspaya.fixr.repositories.FixrUserRepo;
import com.inspaya.fixr.repositories.UserValidityProfileRepo;
import com.inspaya.fixr.entities.UserValidityProfile;
import com.inspaya.fixr.services.email.EmailServiceProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.random.RandomGenerator;


@Service
@RequiredArgsConstructor
@Slf4j
public class FixrUserService {
    private final LocationService locationService;
    private final CredibilityEventRepo credibilityEventRepo;
    private final FixrUserFollowRepo fixrUserFollowRepo;
    private final FixrUserRepo fixrUserRepo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserValidityProfileRepo validityProfileRepo;
    private final EmailServiceProvider provider;
    private final VerificationService verificationService;
    private final LeaderboardProjectionService leaderboardProjectionService;

    private static final Map<String, List<String>> nameSet = loadNameSet();

    @Transactional
    public FixrUserResponseDto createFixrUser(@Validated CreateFixrUserDto userDto){

        State state = locationService.createOrReturnState(userDto.state());
        Lga lga = locationService.createOrReturnLga(userDto.lga(), state);
        City city = null;
        if(userDto.city() != null && StringUtils.hasText(userDto.city())){
            city = locationService.createOrReturnCity(userDto.city(), lga);
        }

        FixrUser user = new FixrUser(userDto);
        user.setState(state);
        user.setLga(lga);
        user.setCity(city);
        assert userDto.password() != null;
        user.setPassword(passwordEncoder.encode(userDto.password()));

        String username = generateUsername();
        while(fixrUserRepo.existsByUsername(username))
            username = generateUsername();

        user.setUsername(username);
        user.setEmailVerified(false);
        FixrUser savedUser = createOrReturnUser(user);
        validityProfileRepo.findById(savedUser.getUserId())
                .orElseGet(() -> validityProfileRepo.save(new UserValidityProfile(savedUser)));
        leaderboardProjectionService.enqueue(savedUser.getUserId());

        verificationService.generateAndSendEmailVerification(userDto.email());
        return mapper.toDto(savedUser);
    }

    public boolean verifyUserEmail(@Valid EmailVerificationDto emailVerificationDto) {
        FixrUser user = findUserByEmail(emailVerificationDto.email());
        if (user == null) return false;
        if (Boolean.TRUE.equals(user.getEmailVerified())) return true;

        final boolean isTokenVerified = verificationService.verifyEmailOtp(emailVerificationDto);
        if (isTokenVerified) {
            user.setEmailVerified(true);
            fixrUserRepo.save(user);
        }
        return isTokenVerified;
    }

    protected FixrUser findUserByEmail(String email){
        try{
            String blindIndex = generateBlindIndex(email);
            if(blindIndex == null)
                throw new FixrServerError("Something wrong went wrong on the server");

            return fixrUserRepo.findByBlindEmailIndex(blindIndex).orElseThrow(()-> new EntityNotFoundException("Invalid email"));
        } catch ( EntityNotFoundException e) {
            log.error("Failed to find Fixr user because {}", e.getLocalizedMessage());
        }

        return null;
    }

    @Transactional
    public void verifyAndResetPassword(@Valid PasswordResetDto passwordResetDto) {
        String blindIndex = generateBlindIndex(passwordResetDto.email());

        if(blindIndex == null)
            throw new FixrServerError("Something went wrong on the server");

        Optional<FixrUser> optionalFixrUser = fixrUserRepo.findByBlindEmailIndex(blindIndex);
        if(optionalFixrUser.isEmpty())
            return;

        if(!passwordResetDto.password().equals(passwordResetDto.rePassword()))
            throw new IllegalStateException("Password does not match");

        boolean isVerified = verificationService.verifyPasswordResetToken(passwordResetDto);
        if(!isVerified)
            throw new InvalidTokenException("Invalid token");

        FixrUser user = optionalFixrUser.get();
        user.setPassword(passwordEncoder.encode(passwordResetDto.password()));

        fixrUserRepo.save(user);
    }

    public FixrUserResponseDto getUser(String emailIndexHex){
        return mapper.toDto(fixrUserRepo.findByBlindEmailIndex(emailIndexHex).orElseThrow());
    }

    public FixrUserResponseDto mapUser(FixrUser user){
        return mapper.toDto(user);
    }
    private FixrUser createOrReturnUser(FixrUser user){
        return fixrUserRepo.findByBlindEmailIndex(user.getBlindEmailIndex())
                .orElseGet(()-> fixrUserRepo.save(user));
    }

    private String generateUsername(){
        Random random = Random.from(RandomGenerator.getDefault());
        String num = String.format("%03d", random.nextInt(0, 1000));

        int nounIndex = random.nextInt(nameSet.get("noun").size());
        int adjectiveIndex = random.nextInt(nameSet.get("adjective").size());
        String adj = nameSet.get("noun").get(nounIndex);
        String noun = nameSet.get("adjective").get(adjectiveIndex);

        return String.format("%s%s%s", adj, noun, num);
    }

    private String generateBlindIndex(String email){
        try{
            return BlindIndexService.generateBlindIndex(email);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate blind index because {}", e.getLocalizedMessage());
            return null;
        }
    }
    private static Map<String, List<String>> loadNameSet(){
        try(Reader reader = Files.newBufferedReader(Path.of("username-template.csv"))){
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            CSVParser parser = format.parse(reader);
            Set<String> s = new HashSet<>();


            Map<String, List<String>> data = new HashMap<>();
            List<String> nouns = new ArrayList<>();
            List<String> adjs = new ArrayList<>();
            for(CSVRecord record: parser){
                nouns.add(record.get("Noun").strip());
                adjs.add(record.get("Adjective").strip());
            }

            data.put("noun", nouns);
            data.put("adjective", adjs);

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPasswordResetToken(@Valid OtpRequestDto otpRequest) {
        try{
            String blindIndex = BlindIndexService.generateBlindIndex(otpRequest.email());
            boolean accountExists = fixrUserRepo.existsByBlindEmailIndex(blindIndex);
            if(accountExists)
                verificationService.generateAndSendPasswordResetToken(otpRequest.email());
        }catch ( NoSuchAlgorithmException e){
            log.error("Failed to send token because {}", e.getLocalizedMessage());
        }
    }
}
