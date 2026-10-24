package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.enums.ReportValidationStatus;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserReportResponseDto {
    private UUID reportId;
    private String description;
    private LocationDto location;
    private ReportStatus status;
    private ReportValidationStatus validationStatus;
    private List<MediaDto> reportMedia;
    private String category;
    private ZonedDateTime createdAt;
}
