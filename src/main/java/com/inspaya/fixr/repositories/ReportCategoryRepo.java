package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportCategoryRepo extends JpaRepository<ReportCategory, Long> {
    Optional<ReportCategory> findByName(String reportCategory);

    default ReportCategory createOrReturnReportCategory(String categoryName){
        return findByName(categoryName).orElseGet(()-> save(new ReportCategory(categoryName)));
    }
}
