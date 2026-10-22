package com.inspaya.fixr.services;

import com.inspaya.fixr.dtos.*;
import com.inspaya.fixr.entities.*;
import com.inspaya.fixr.enums.ReportVoteType;
import com.inspaya.fixr.enums.EmbeddingTaskEvents;
import com.inspaya.fixr.enums.MediaType;
import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.exceptions.IllegalOperationException;
import com.inspaya.fixr.mapper.ReportMapper;
import com.inspaya.fixr.configurations.properties.RabbitConfigProperties;
import com.inspaya.fixr.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportEmbeddingRepo reportEmbeddingRepo;
    private final ReportMediaRepo mediaRepo;
    private final VideoEmbeddingRepo videoEmbeddingRepo;
    private final ReportRepo repo;
    private final ReportCategoryRepo categoryRepo;
    private final ReportCommentRepo commentRepo;
    private final ReportVoteRepo voteRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ReportMapper mapper;

    private final LocationService locationService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfigProperties properties;

    @Transactional
    public UserReportResponseDto createReport(CreateReportDto reportDto, FixrUser reporter){
        try{
            UUID report_id = UUID.randomUUID();
            Report report = new Report(reportDto, report_id, reporter);

            updateReport(report, reportDto);
            List<String> images = report.getReportMedia().stream()
                    .filter(media-> media.getType() == MediaType.Image)
                    .map(ReportMedia::getUrl).toList();

            List<String> videos = report.getReportMedia().stream()
                    .filter(media-> media.getType() == MediaType.Video)
                    .map(ReportMedia::getUrl).toList();

            EmbeddingTask task = new EmbeddingTask(
                    EmbeddingTaskEvents.EMBEDDING_REQUESTED,
                    UUID.randomUUID(),
                    report.getReportId(),
                    new EmbeddingTaskMedia(images, videos),
                    report.getDescription()
                    );
            report.setIdempotencyKey(reportDto.idempotencyKey());
            var savedReport = repo.findByIdempotencyKey(reportDto.idempotencyKey()).orElseGet(()->repo.save(report));
            rabbitTemplate.convertAndSend(properties.fixrExchangeName(), properties.requestQueueRk(), task);
            return mapper.convertToUserReportResponse(savedReport);
        } catch (AmqpException e) {
            log.error("Failed to publish task because {}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "notification")
    @Transactional
    public void handleReportDuplicateDetection(EmbeddingTaskResult result){
        Report report = repo.findById(result.reportId())
                .orElseThrow(()->new EntityNotFoundException("Failed to process report:"+result.reportId()+" because report does not exist"));

        if(result.event() == EmbeddingTaskEvents.EMBEDDING_FAILED)
            report.setStatus(ReportStatus.Published);
        if(result.event() == EmbeddingTaskEvents.EMBEDDING_COMPLETED){
            if(result.candidates()!=null && !result.candidates().isEmpty()){
                report.setStatus(ReportStatus.DuplicateDetected);
            }
            report.setStatus(ReportStatus.Published);
        }
        repo.saveAndFlush(report);
        // Notify user
    }

    public PagedModel<UserReportResponseDto> getUserSubmittedReports(int page, int pageSize, FixrUser user){
        PageRequest request = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"updatedAt"));
        Page<Report> reports = repo.findAllByReporter(user, request);
        System.out.println(reports.getContent().size());
        List<UserReportResponseDto> reportResponseDtos = mapper.convertToUserReportResponses(reports.getContent());
        return new PagedModel<>(new PageImpl<>(reportResponseDtos, reports.getPageable(), reports.getTotalElements()));
    }

    public @Nullable UserReportResponseDto getUserSubmittedReport(String reportId, FixrUser user) {
        Report report = repo.findByReportIdAndReporter(reportId, user)
                .orElseThrow(()->new EntityNotFoundException("Report does not belong to user"));
        return mapper.convertToUserReportResponse(report);
    }

    private void updateReport(Report report, CreateReportDto reportDto){
        LocationDto location = reportDto.locationDto();
        State state = locationService.createOrReturnState(location.state());
        Lga lga = locationService.createOrReturnLga(location.lga(), state);
        City city = null;
        if(location.city() != null && StringUtils.hasText(location.city())){
            city = locationService.createOrReturnCity(location.city(), lga);
        }

        ReportCategory category = categoryRepo.createOrReturnReportCategory(reportDto.category());

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate point = new Coordinate(location.coordinates().longitude(),
                location.coordinates().latitude());
        Point coordinate = geometryFactory.createPoint(point);

        report.setState(state);
        report.setLga(lga);
        report.setCity(city);
        report.setCategory(category);
        report.setCoordinate(coordinate);
        report.getReportEmbedding().setCoordinate(coordinate);
    }


    public void deleteReport(String reportId, FixrUser user) {
        Report report = repo.findById(UUID.fromString(reportId)).orElseThrow();
        if(report.getReporter().getUserId() == user.getUserId()) {
            report.setStatus(ReportStatus.Deleted);
            repo.save(report);
        }
        else throw new IllegalOperationException("Action is not allowed");
    }


    // Public/feed access
    @Transactional(readOnly = true)
    public ReportFeedResponseDto getReportFeed(String reportId, FixrUser viewer) {
        Report report = findReport(parseId(reportId, "reportId"));
        ReportFeedResponseDto response = mapper.convertToFeed(report);
        populateVoteSummary(List.of(report), List.of(response), viewer);
        return response;
    }

    @Transactional(readOnly = true)
    public void populateVoteSummary(List<Report> reports, List<ReportFeedResponseDto> responses, FixrUser viewer) {

        if (reports.isEmpty()) return;

        List<UUID> reportIds = reports.stream().map(Report::getReportId).toList();
        Map<UUID, ReportFeedResponseDto> responseById = responses.stream().collect(
                Collectors.toMap(ReportFeedResponseDto::getReportId, Function.identity()));

        for (ReportVoteCount count : voteRepo.countVotesByReportIds(reportIds)) {
            ReportFeedResponseDto response = responseById.get(count.getReportId());
            if (response == null) continue;

            if (count.getVoteType() == ReportVoteType.UPVOTE) {
                response.setUpvoteCount(count.getVoteCount());
            } else {
                response.setDownvoteCount(count.getVoteCount());
            }
        }

        if (viewer != null) {
            for (ReportVote vote : voteRepo.findAllByUserUserIdAndReportReportIdIn(
                    viewer.getUserId(), reportIds)) {
                ReportFeedResponseDto response = responseById.get(vote.getReport().getReportId());
                if (response != null) response.setViewerVote(vote.getVoteType());
            }
        }
    }

    @Transactional
    public ReportCommentResponseDto commentOnReport(String reportId, CommentRequest comment, FixrUser author) {
        Report report = findReport(reportId);
        String normalizedContent = comment.content() == null ? "" : comment.content().strip();
        if (normalizedContent.isEmpty() || normalizedContent.length() > 5000) {
            throw new IllegalStateException("Comment must contain 1 to 5000 characters");
        }
        ReportComment newReportComment = new ReportComment(report, author, normalizedContent);
        newReportComment.setIdempotencyKey(comment.idempotencyKey());

        ReportComment reportComment = commentRepo.findByIdempotencyKey(comment.idempotencyKey())
                .orElseGet(()->commentRepo.save(newReportComment));
        return toCommentResponse(reportComment);
    }

    @Transactional(readOnly = true)
    public PagedModel<ReportCommentResponseDto> getReportComments(
            String reportId, int pageNumber, int pageSize) {
        UUID reportUUID = parseId(reportId, "reportId");
        findReport(reportUUID);
        PageRequest request = commentPage(pageNumber, pageSize);
        Page<ReportCommentResponseDto> page = commentRepo
                .findAllByReport_ReportId(reportUUID, request)
                .map(this::toCommentResponse);
        return new PagedModel<>(page);
    }

    @Transactional
    public ReportCommentResponseDto editComment(
            String reportId, String commentId, String content, FixrUser author) {
        UUID reportUUID = parseId(reportId, "reportId");
        UUID commentUUID = parseId(commentId, "commentId");
        ReportComment comment = commentRepo.findByCommentIdAndReport_ReportId(commentUUID, reportUUID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getAuthor().getUserId().equals(author.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the comment author can edit it");
        }
        String normalizedContent = content == null ? "" : content.strip();
        if (normalizedContent.isEmpty() || normalizedContent.length() > 5000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must contain 1 to 5000 characters");
        }
        comment.setContent(normalizedContent);
        commentRepo.saveAndFlush(comment);
        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(String reportId, String commentId, FixrUser actor) {
        UUID reportUUID = parseId(reportId, "reportId");
        UUID commentUUID = parseId(commentId, "commentId");
        Report report = findReport(reportUUID);
        ReportComment comment = commentRepo.findByCommentIdAndReport_ReportId(commentUUID, reportUUID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        boolean author = comment.getAuthor().getUserId().equals(actor.getUserId());
        boolean reportOwner = report.getReporter().getUserId().equals(actor.getUserId());
        if (!author && !reportOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the comment author or report owner can delete it");
        }
        commentRepo.delete(comment);
    }

    @Transactional
    public ReportVoteResponseDto voteOnReport(String reportId, FixrUser user, ReportVoteType voteType) {
        if (voteType == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vote type is required");
        Report report = findReport(reportId);
        ReportVote vote = voteRepo.findByReportReportIdAndUserUserId(
                report.getReportId(), user.getUserId()).orElseGet(() -> new ReportVote(report, user, voteType));
        vote.setVoteType(voteType);
        voteRepo.saveAndFlush(vote);
        return voteSummary(report.getReportId(), voteType);
    }

    @Transactional
    public void removeVote(String reportId, FixrUser user) {
        UUID reportUUID = parseId(reportId, "reportId");
        findReport(reportUUID);
        voteRepo.findByReportReportIdAndUserUserId(reportUUID, user.getUserId())
                .ifPresent(voteRepo::delete);
    }

    public void ensureReportOwner(String reportId, FixrUser user) {
        Report report = findReport(reportId);
        if (!report.getReporter().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report does not belong to user");
        }
    }

    private ReportVoteResponseDto voteSummary(UUID reportId, ReportVoteType viewerVote) {
        return new ReportVoteResponseDto(
                viewerVote,
                voteRepo.countByReport_ReportIdAndVoteType(reportId, ReportVoteType.UPVOTE),
                voteRepo.countByReport_ReportIdAndVoteType(reportId, ReportVoteType.DOWNVOTE));
    }

    private Report findReport(String reportId) {
        return findReport(parseId(reportId, "reportId"));
    }

    private UUID parseId(String value, String name) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + name);
        }
    }

    private Report findReport(UUID reportId) {
        return repo.findById(reportId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, String.format("Report with reportId %s does not exist", reportId)));
    }

    private PageRequest commentPage(int pageNumber, int pageSize) {
        if (pageNumber < 0 || pageSize < 1 || pageSize > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Comment pagination must use page >= 0 and pageSize from 1 to 100");
        }
        return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private ReportCommentResponseDto toCommentResponse(ReportComment comment) {
        return new ReportCommentResponseDto(
                comment.getCommentId(), comment.getContent(), comment.getAuthor().getUsername(),
                comment.getCreatedAt(), comment.getUpdatedAt());
    }
}
