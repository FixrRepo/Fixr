package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;

public record FeedSpecifications(
        String category,
        ReportStatus status,
        String state,
        FixrUserSecurityDetail detail,
        int pageNumber,
        int pageSize,
        String order
){}