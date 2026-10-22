package com.inspaya.fixr.services;

import com.inspaya.fixr.entities.City;
import com.inspaya.fixr.entities.Lga;
import com.inspaya.fixr.entities.State;
import com.inspaya.fixr.dtos.CreateFixrUserDto;
import com.inspaya.fixr.dtos.FixrUserResponseDto;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.mapper.UserMapper;
import com.inspaya.fixr.repositories.CredibilityEventRepo;
import com.inspaya.fixr.repositories.FixrUserFollowRepo;
import com.inspaya.fixr.repositories.FixrUserRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.random.RandomGenerator;


@Service
@RequiredArgsConstructor
public class FixrUserService {
    private final LocationService locationService;
    private final CredibilityEventRepo credibilityEventRepo;
    private final FixrUserFollowRepo fixrUserFollowRepo;
    private final FixrUserRepo fixrUserRepo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    private static final Map<String, List<String>> nameSet = loadNameSet();

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
        return mapper.toDto(createOrReturnUser(user));
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

}
