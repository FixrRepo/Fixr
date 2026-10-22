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
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_name_state", columnNames = {"name", "state"}))
public class Lga {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long lgaId;

    private String name;

    @OneToMany(mappedBy = "lga", fetch = FetchType.LAZY)
    private List<City> lgaAreas;

    @ManyToOne
    @JoinColumn(name = "state", nullable = false)
    private State state;

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

    public Lga(String name, State state){
        this.name = name;
        this.state = state;
    }
}

