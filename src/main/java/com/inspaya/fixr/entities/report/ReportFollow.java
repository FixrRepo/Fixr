package com.inspaya.fixr.entities.report;

import com.inspaya.fixr.entities.FixrUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ReportFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String followId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id")
    private FixrUser follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id")
    private Report report;

    private ZonedDateTime startedFollowingAt;

    @PrePersist
    private void setStartedFollowingAt(){
        this.startedFollowingAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public ReportFollow(FixrUser follower, Report report){
        this.follower = follower;
        this.report = report;
    }
}
