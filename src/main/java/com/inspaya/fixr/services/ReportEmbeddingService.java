package com.inspaya.fixr.services;

import com.inspaya.fixr.dtos.EmbeddingCandidate;
import com.inspaya.fixr.dtos.EmbeddingTaskResult;
import com.inspaya.fixr.dtos.DuplicateCandidateResponseDto;
import com.inspaya.fixr.entities.DuplicateReportResult;
import com.inspaya.fixr.entities.Report;
import com.inspaya.fixr.entities.ReportEmbedding;
import com.inspaya.fixr.enums.EmbeddingStatus;
import com.inspaya.fixr.enums.EmbeddingTaskEvents;
import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.mapper.ReportMapper;
import com.inspaya.fixr.repositories.DuplicateReportResultRepo;
import com.inspaya.fixr.repositories.ReportEmbeddingRepo;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReportEmbeddingService {
    private final ReportEmbeddingRepo repo;
    private final DuplicateReportResultRepo duplicateReportResultRepo;
    private final EntityManager manager;
    private final ReportMapper mapper;

    @Async
    @RabbitListener(queues = "${fixr.rabbit-mq.completed-queue-rk}")
    @Transactional
    public void handleEmbeddingTask(EmbeddingTaskResult result) {
        if (result.event() == EmbeddingTaskEvents.EMBEDDING_FAILED) {
            handleFailedEmbedding(result);
            return;
        }
        if (result.event() != EmbeddingTaskEvents.EMBEDDING_COMPLETED) {
            throw new IllegalArgumentException("Unsupported embedding result event: " + result.event());
        }

        ReportEmbedding embedding = repo.findById(result.reportId()).orElseThrow();
        Report report = manager.getReference(Report.class, embedding.getId());

        List<EmbeddingCandidate> candidates =
                result.candidates() == null ? List.of() : result.candidates();
        duplicateReportResultRepo.deleteAllByReport_ReportId(result.reportId());
        List<DuplicateReportResult> duplicateReportResults = candidates.stream().map(candidate -> {
            Report duplicateReport = manager.getReference(Report.class, candidate.reportId());
            return new DuplicateReportResult(report, duplicateReport, candidate);
        }).toList();

        duplicateReportResultRepo.saveAll(duplicateReportResults);

        embedding.setStatus(EmbeddingStatus.Completed);

        repo.save(embedding);

        int candidateCount = result.candidates() == null ? 0 : result.candidates().size();
        log.info("Embedding job {} completed for report {} with {} candidates",
                result.jobId(), result.reportId(), candidateCount);
    }

    private void handleFailedEmbedding(EmbeddingTaskResult result) {
        ReportEmbedding embedding = repo.findById(result.reportId()).orElseThrow();
        embedding.setStatus(EmbeddingStatus.Failed);
        repo.save(embedding);
        log.error("Embedding job {} failed for report {}: {}",
                result.jobId(), result.reportId(), result.error());
    }

    @Transactional(readOnly = true)
    public PagedModel<DuplicateCandidateResponseDto> getDuplicateCandidates(
            String reportId, Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Candidate pagination must use page >= 0 and pageSize from 1 to 100");
        }
        java.util.UUID reportUUID;
        try {
            reportUUID = java.util.UUID.fromString(reportId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reportId");
        }

        Page<DuplicateCandidateResponseDto> page = duplicateReportResultRepo
                .findAllByReport_ReportIdOrderByRankAsc(reportUUID, pageable)
                .map(candidate -> new DuplicateCandidateResponseDto(
                        candidate.getDuplicateReport().getReportId(),
                        candidate.getDuplicateReport().getDescription(),
                        candidate.getRank(), candidate.getSimilarityScore(),
                        candidate.getDistanceMeters()));
        return new PagedModel<>(page);
    }
}
