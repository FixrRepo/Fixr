package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(uniqueConstraints = @UniqueConstraint(name = "user_follow_user_following_unq", columnNames = {"follower_id", "following_id"}),
check = @CheckConstraint(name = "check_no_self_follow",constraint = "follower_id <> following_id"))
public class FixrUserFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private FixrUser userFollow;
    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private FixrUser userFollowed;

    private ZonedDateTime startedFollowingAt;

    @PrePersist
    private void setStartedFollowingAt(){
        this.startedFollowingAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public void createFollowing(FixrUser i, FixrUser follow){
        this.userFollow = i;
        this.userFollowed = follow;
    }
}
