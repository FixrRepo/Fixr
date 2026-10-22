package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long stateId;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "state", fetch = FetchType.LAZY)
    private List<Lga> stateLga;

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

    public State(String name){
        this.name = name;
    }
}
