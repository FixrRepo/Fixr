package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_name_lga", columnNames = {"name", "lga"}))
@NoArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cityId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "lga", nullable = false)
    private Lga lga;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    @PreUpdate
    private void setUpdatedAt(){
        this.createdAt =ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public City(String name, Lga lga){
        this.name = name;
        this.lga = lga;
    }
}
