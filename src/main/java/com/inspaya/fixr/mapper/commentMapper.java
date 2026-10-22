package com.inspaya.fixr.mapper;

import com.inspaya.fixr.dtos.ReportCommentResponseDto;
import com.inspaya.fixr.entities.ReportComment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.web.bind.annotation.PostMapping;

@Mapper(componentModel = "spring")
public interface commentMapper {
    ReportCommentResponseDto toCommentDto(ReportComment comment);

}
