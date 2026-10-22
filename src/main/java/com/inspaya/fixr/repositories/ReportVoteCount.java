package com.inspaya.fixr.repositories;

import com.inspaya.fixr.enums.ReportVoteType;

import java.util.UUID;

public interface ReportVoteCount {
    UUID getReportId();
    ReportVoteType getVoteType();
    long getVoteCount();
}
