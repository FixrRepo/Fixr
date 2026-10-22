package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.enums.ReportVoteType;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ReportFeedResponseDto {
    private UUID reportId;
    private String description;

    private LocationDto location;

    private MinimalFixrUserDetails reporter;
    private double distance;

    private List<MediaDto> mediaList;
    private ReportStatus status;

    private String category;

    private ZonedDateTime createdAt;
    private long upvoteCount;
    private long downvoteCount;
    private ReportVoteType viewerVote;
}
