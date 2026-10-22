package com.inspaya.fixr.services;

import com.inspaya.fixr.controllers.PublicFeedController;
import com.inspaya.fixr.dtos.FeedSpecifications;
import com.inspaya.fixr.dtos.ReportFeedResponseDto;
import com.inspaya.fixr.entities.Report;
import com.inspaya.fixr.entities.ReportCategory;
import com.inspaya.fixr.entities.State;
import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.mapper.ReportMapper;
import com.inspaya.fixr.repositories.ReportCategoryRepo;
import com.inspaya.fixr.repositories.ReportRepo;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.PointBuilder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportRecommendationService {
    private final ReportRepo repo;
    private final LocationService locationService;
    private final ReportCategoryRepo categoryRepo;
    private final ReportMapper reportMapper;
    private final ReportService reportService;
    private Point areaWithMostReports;

//    @Scheduled(cron = "* */1 * * *")
    private void refreshAreaWithMostReport(){

    }

    // Recommend by location nearest to user
    public PagedModel<Report> recommendReportsByLocation(Point coordinates, Pageable pageable){
        return new PagedModel<>(repo.findReportsByLocation(coordinates, pageable));
    }
    // Recommend by report category sorted by location
    public PagedModel<Report> recommendReportsByCategory(Point coordinates, ReportCategory category, Pageable pageable){
        return new PagedModel<>(repo.findReportsByCategory(coordinates, category, pageable));
    }

    // Recommend by Latest report sorted by location

    // Recommend by status sorted by location

    // Blenched (System recommendation)
    @Transactional(readOnly = true)
    public @Nullable PagedModel<ReportFeedResponseDto> getRecommendation(FeedSpecifications specs) {

        Pageable pageable = PageRequest.of(specs.pageNumber(), specs.pageSize(), Sort.by("createdAt").descending());
        State state = Objects.equals(specs.state(),  "*")? null: locationService.getStateByName(specs.state());
        ReportCategory category =  Objects.equals(specs.category(), "*")? null : categoryRepo.createOrReturnReportCategory(specs.category());

        // Geo coordinates of location with the most reports
        Coordinate coordinate = new Coordinate(0, 0);
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        boolean guest = specs.detail() == null || specs.detail().getUser() == null
                || "GuestUser".equals(specs.detail().getUsername());
        Point referencePoint = !guest ? specs.detail().getUser().getLocation() : factory.createPoint(coordinate);

        if(referencePoint == null)
            referencePoint = factory.createPoint(coordinate);
        ReportStatus status = specs.status() == ReportStatus.ALL ? null : specs.status();
        Page<Report> reportPage = repo.findReportBySpecification(referencePoint, category,state, status, pageable);
        Point finalReferencePoint = referencePoint;
        reportPage.getContent().forEach(report->{
            Double distance = finalReferencePoint.distance(report.getCoordinate());
            DecimalFormat df = new DecimalFormat("0.00");
//            report.setDistance(String.format("%.2f m", distance));
            report.setDistance(df.format(distance));
        });
        List<ReportFeedResponseDto> reportFeed = reportMapper.convertToReportFeedResponses(reportPage.toList());
        reportService.populateVoteSummary(
                reportPage.toList(), reportFeed, guest ? null : specs.detail().getUser());

        return new PagedModel<>(new PageImpl<>(reportFeed, reportPage.getPageable(), reportPage.getTotalElements()));
    }
}
