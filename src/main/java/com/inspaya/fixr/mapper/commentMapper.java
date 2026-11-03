package com.inspaya.fixr.mapper;

import com.inspaya.fixr.dtos.ReportCommentResponseDto;
import com.inspaya.fixr.entities.report.ReportComment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface commentMapper {
    ReportCommentResponseDto toCommentDto(ReportComment comment);

}
