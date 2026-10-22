package com.inspaya.fixr.mapper;

import com.inspaya.fixr.dtos.CoordinateDto;
import com.inspaya.fixr.dtos.LocationDto;
import com.inspaya.fixr.dtos.ReportFeedResponseDto;
import com.inspaya.fixr.dtos.UserReportResponseDto;
import com.inspaya.fixr.entities.Report;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = {UserMapper.class, MediaMapper.class})
public interface ReportMapper {

    /**
     * Converts the Report entity into a DTO for account owner*/
    @Mapping(target = "category", ignore = true)
    UserReportResponseDto convertToUserReportResponse(Report report);

    /** Converts the Report entity into a feed that would be viewed publicly */
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "upvoteCount", ignore = true)
    @Mapping(target = "downvoteCount", ignore = true)
    @Mapping(target = "viewerVote", ignore = true)
    ReportFeedResponseDto convertToFeed(Report report);

    List<UserReportResponseDto> convertToUserReportResponses(List<Report> reports);
    List<ReportFeedResponseDto> convertToReportFeedResponses(List<Report> reports);

    @AfterMapping
    default void updateUserReportResponse(Report report, @MappingTarget UserReportResponseDto reportResponseDto){
        reportResponseDto.setLocation( convertToLocation(report));
        reportResponseDto.setCategory(report.getCategory().getName());
    }

    @AfterMapping
    default void updateFeedReportResponse(Report report, @MappingTarget ReportFeedResponseDto feedDto){
        feedDto.setLocation( convertToLocation(report) );
        feedDto.setCategory( report.getCategory().getName() );
    }

    default LocationDto convertToLocation(Report report){
        CoordinateDto coordinate = new CoordinateDto(report.getCoordinate().getCoordinate().y,
                report.getCoordinate().getCoordinate().x);
        return new LocationDto(coordinate, report.getState().getName(), report.getLga().getName(),
                Objects.requireNonNull(report.getCity()).getName());
    }
}
