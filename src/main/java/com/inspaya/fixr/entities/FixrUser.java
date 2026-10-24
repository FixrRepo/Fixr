package com.inspaya.fixr.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inspaya.fixr.encryption.BlindIndexService;
import com.inspaya.fixr.converter.DataEncryptionConverter;
import com.inspaya.fixr.dtos.CreateFixrUserDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_active=true")
@Slf4j
public class FixrUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    private String username;
    @Convert(converter = DataEncryptionConverter.class)
    @JdbcTypeCode(SqlTypes.BINARY)
    private String firstname;
    @Convert(converter = DataEncryptionConverter.class)
    @JdbcTypeCode(SqlTypes.BINARY)
    private String middleName;
    @Convert(converter = DataEncryptionConverter.class)
    @JdbcTypeCode(SqlTypes.BINARY)
    private String lastname;

    @Convert(converter = DataEncryptionConverter.class)
    @JdbcTypeCode(SqlTypes.BINARY)
    private String email;

    @Column(unique = true, nullable = false)
    private String blindEmailIndex;

    @Convert(converter = DataEncryptionConverter.class)
    @JdbcTypeCode(SqlTypes.BINARY)
    private String password;

    @Convert(converter = DataEncryptionConverter.class)
    private String phoneNumber;

    @Column(name = "user_location", columnDefinition = "geometry(Point, 4326)")
    private Point location;
    @Column(name = "user_area", columnDefinition = "geometry(Polygon, 4326)")
    private Polygon geographicalArea;

    private double credibilityScore;

    private Boolean isInNigeria;
    private Boolean agreedToTerms;

    @ManyToOne
    @JsonIgnore
    private State state;
    @ManyToOne
    @JsonIgnore
    private Lga lga;
    @ManyToOne
    @JsonIgnore
    private City city;

    @OneToMany(mappedBy = "userFollow")
    private List<FixrUserFollow> following;

    @OneToMany(mappedBy = "userFollowed")
    private List<FixrUserFollow> followers;

    @Column(name = "is_active")
    private Boolean isActive;
    private ZonedDateTime deletedAt;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public FixrUser(CreateFixrUserDto userDto){
        this.firstname = userDto.firstname();
        this.lastname = userDto.lastname();
        this.middleName = userDto.middleName();
        this.email = userDto.email();
        this.phoneNumber = userDto.phoneNumber();
        this.isActive = true;
        this.credibilityScore = 10.0;
        this.isInNigeria = userDto.isInNigeria();
        this.agreedToTerms = userDto.agreedToTerms();

        try{
            this.blindEmailIndex = BlindIndexService.generateBlindIndex(email);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to create blind index for email");
        }
    }
    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

}
