package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ReportVoteType;
import jakarta.validation.constraints.NotNull;

public record ReportVoteRequest(@NotNull ReportVoteType vote) {
}
