package com.inspaya.fixr.mapper;

import com.inspaya.fixr.dtos.MediaDto;
import com.inspaya.fixr.entities.ReportMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaMapper {

    @Mapping(source = "url", target = "mediaUrl")
    @Mapping(source = "type", target = "mediaType")
    MediaDto toDto(ReportMedia media);

    List<MediaDto> toDto(List<ReportMedia> mediaList);
}
