package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ReportEmbedding;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportEmbeddingRepo extends JpaRepository<ReportEmbedding, UUID> {

    @Query(value = """
            SELECT re.*, ST_Distance(re.coordinate, CAST(:point as geography)) as distance FROM report_embedding re
            WHERE ST_DWithin(re.coordinate, CAST(:point as geography), :distanceRange_m)
            """, nativeQuery = true)
    List<ReportEmbedding> findReportEmbeddingsByCoordinateWithin(@Param("point") Point point, @Param("distanceRange_m") double distanceRange);
}
