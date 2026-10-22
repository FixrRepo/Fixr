package com.inspaya.fixr.mapper;

import com.inspaya.fixr.dtos.FixrUserResponseDto;
import com.inspaya.fixr.entities.FixrUser;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    FixrUserResponseDto toDto (FixrUser user);
}
