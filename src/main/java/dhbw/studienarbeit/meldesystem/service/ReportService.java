package dhbw.studienarbeit.meldesystem.service;

import dhbw.studienarbeit.meldesystem.dto.*;
import dhbw.studienarbeit.meldesystem.exceptions.BadRequestException;
import dhbw.studienarbeit.meldesystem.exceptions.ResourceNotFoundException;
import dhbw.studienarbeit.meldesystem.model.*;
import dhbw.studienarbeit.meldesystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final HelperRepository helperRepository;
    private final ReportHistoryRepository historyRepository;
    private final FileStorageService fileStorageService;
    private final AuthorityDispatcherService authorityDispatcherService;
    private final WebSocketNotificationService notificationService;
    private final GeocodingService geocodingService;

    public ReportDTO createReport(CreateReportRequest request, MultipartFile photo, Long userId) {
        log.info("Creating new report: {} by user: {}", request.getTitle(), userId);

        User creator = null;
        if (userId != null && !request.getIsAnonymous()) {
            creator = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = fileStorageService.storeFile(photo);
        }

        if (request.getCity() == null || request.getStreet() == null) {
            LocationDTO geocodedLocation = geocodingService.reverseGeocode(
                    request.getLatitude(),
                    request.getLongitude()
            );
            if (request.getCity() == null) request.setCity(geocodedLocation.getCity());
            if (request.getStreet() == null) request.setStreet(geocodedLocation.getStreet());
            if (request.getPostalCode() == null) request.setPostalCode(geocodedLocation.getPostalCode());
        }

        Report report = Report.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .photoUrl(photoUrl)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .street(request.getStreet())
                .postalCode(request.getPostalCode())
                .city(request.getCity())
                .locationDescription(request.getLocationDescription())
                .status(ReportStatus.NEW)
                .createdBy(creator)
                .isAnonymousReport(request.getIsAnonymous())
                .build();

        report = reportRepository.save(report);

        createHistoryEntry(report, ReportStatus.NEW, "System", "Report wurde erstellt");

        try {
            authorityDispatcherService.notifyAuthority(report);
            createHistoryEntry(report, ReportStatus.AUTHORITY_NOTIFIED, "System",
                    "Zuständige Behörde wurde informiert");
        } catch (Exception e) {
            log.error("Failed to notify authority for report {}", report.getId(), e);
        }

        notificationService.notifyNewReport(report);

        log.info("Report created successfully with ID: {}", report.getId());
        return mapToDTO(report, userId);
    }

    @Transactional(readOnly = true)
    public ReportDetailDTO getReportById(Long reportId, Long currentUserId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        return mapToDetailDTO(report, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDTO> searchReports(ReportSearchParams params, Long currentUserId) {
        Pageable pageable = PageRequest.of(
                params.getPage(),
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Report> reportPage;

        if (params.getMyReports() != null && params.getMyReports()) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            reportPage = reportRepository.findByCreatedBy(user, pageable);

        } else if (params.getHelpingReports() != null && params.getHelpingReports()) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            reportPage = helperRepository.findReportsByUser(user, pageable);

        } else if (params.getLatitude() != null && params.getLongitude() != null) {
            Integer radius = params.getRadius() != null ? params.getRadius() : 10; // Default 10km
            List<Report> reports = reportRepository.findReportsWithinRadiusPageable(
                    params.getLatitude(),
                    params.getLongitude(),
                    radius,
                    params.getSize(),
                    params.getPage() * params.getSize()
            );

            long total = reportRepository.findReportsWithinRadius(
                    params.getLatitude(), params.getLongitude(), radius
            ).size();

            List<ReportDTO> dtos = reports.stream()
                    .map(r -> mapToDTO(r, currentUserId))
                    .collect(Collectors.toList());

            return PageResponse.<ReportDTO>builder()
                    .content(dtos)
                    .pageNumber(params.getPage())
                    .pageSize(params.getSize())
                    .totalElements(total)
                    .totalPages((int) Math.ceil((double) total / params.getSize()))
                    .last(params.getPage() >= (total / params.getSize()))
                    .build();

        } else {
            reportPage = reportRepository.findByFilters(
                    params.getCategory(),
                    params.getStatus(),
                    pageable
            );
        }

        List<ReportDTO> dtos = reportPage.getContent().stream()
                .map(r -> mapToDTO(r, currentUserId))
                .collect(Collectors.toList());

        return PageResponse.<ReportDTO>builder()
                .content(dtos)
                .pageNumber(reportPage.getNumber())
                .pageSize(reportPage.getSize())
                .totalElements(reportPage.getTotalElements())
                .totalPages(reportPage.getTotalPages())
                .last(reportPage.isLast())
                .build();
    }

    public JoinReportResponse joinReport(Long reportId, Long userId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getIsAnonymous()) {
            throw new BadRequestException("Anonymous users cannot join as helpers");
        }

        if (helperRepository.existsByReportAndUser(report, user)) {
            throw new BadRequestException("You are already helping with this report");
        }

        Helper helper = Helper.builder()
                .report(report)
                .user(user)
                .build();

        helperRepository.save(helper);

        createHistoryEntry(report, ReportStatus.HELPER_FOUND, user.getNickname(),
                user.getNickname() + " hat sich als Helfer gemeldet");

        notificationService.notifyHelperJoined(report, user);

        Integer totalHelpers = helperRepository.countByReport(report);

        return JoinReportResponse.builder()
                .success(true)
                .message("Successfully joined as helper")
                .totalHelpers(totalHelpers)
                .build();
    }

    public void updateReportStatus(Long reportId, ReportStatus newStatus, String comment, String changedBy) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        report.setStatus(newStatus);
        reportRepository.save(report);

        createHistoryEntry(report, newStatus, changedBy, comment);

        notificationService.notifyStatusUpdate(report);
    }

    private void createHistoryEntry(Report report, ReportStatus status, String changedBy, String comment) {
        ReportHistory history = ReportHistory.builder()
                .report(report)
                .status(status)
                .changedBy(changedBy)
                .comment(comment)
                .build();

        historyRepository.save(history);
    }

    private ReportDTO mapToDTO(Report report, Long currentUserId) {
        Integer helperCount = helperRepository.countByReport(report);
        Boolean isHelper = false;

        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null) {
                isHelper = helperRepository.existsByReportAndUser(report, user);
            }
        }

        return ReportDTO.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .category(report.getCategory())
                .photoUrl(report.getPhotoUrl())
                .location(mapLocationToDTO(report))
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .createdBy(mapUserToBasicDTO(report.getCreatedBy(), report.getIsAnonymousReport()))
                .isAnonymousReport(report.getIsAnonymousReport())
                .helperCount(helperCount)
                .currentUserIsHelper(isHelper)
                .build();
    }

    private ReportDetailDTO mapToDetailDTO(Report report, Long currentUserId) {
        List<HelperDTO> helpers = helperRepository.findByReport(report).stream()
                .map(this::mapHelperToDTO)
                .collect(Collectors.toList());

        List<ReportHistoryDTO> history = historyRepository.findByReportOrderByTimestampDesc(report)
                .stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());

        Boolean isHelper = false;
        Boolean isCreator = false;

        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null) {
                isHelper = helperRepository.existsByReportAndUser(report, user);
                isCreator = report.getCreatedBy() != null &&
                        report.getCreatedBy().getId().equals(currentUserId);
            }
        }

        return ReportDetailDTO.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .category(report.getCategory())
                .photoUrl(report.getPhotoUrl())
                .location(mapLocationToDTO(report))
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .createdBy(mapUserToBasicDTO(report.getCreatedBy(), report.getIsAnonymousReport()))
                .isAnonymousReport(report.getIsAnonymousReport())
                .helpers(helpers)
                .history(history)
                .currentUserIsHelper(isHelper)
                .currentUserIsCreator(isCreator)
                .build();
    }

    private LocationDTO mapLocationToDTO(Report report) {
        String fullAddress = String.format("%s, %s %s",
                report.getStreet() != null ? report.getStreet() : "",
                report.getPostalCode() != null ? report.getPostalCode() : "",
                report.getCity() != null ? report.getCity() : ""
        ).trim();

        return LocationDTO.builder()
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .street(report.getStreet())
                .postalCode(report.getPostalCode())
                .city(report.getCity())
                .locationDescription(report.getLocationDescription())
                .fullAddress(fullAddress)
                .build();
    }

    private UserBasicDTO mapUserToBasicDTO(User user, Boolean isAnonymous) {
        if (isAnonymous || user == null) {
            return UserBasicDTO.builder()
                    .id(null)
                    .nickname("Anonym")
                    .isAnonymous(true)
                    .build();
        }

        return UserBasicDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .isAnonymous(user.getIsAnonymous())
                .build();
    }

    private HelperDTO mapHelperToDTO(Helper helper) {
        return HelperDTO.builder()
                .id(helper.getId())
                .user(mapUserToBasicDTO(helper.getUser(), false))
                .joinedAt(helper.getJoinedAt())
                .build();
    }

    private ReportHistoryDTO mapHistoryToDTO(ReportHistory history) {
        return ReportHistoryDTO.builder()
                .id(history.getId())
                .status(history.getStatus())
                .changedBy(history.getChangedBy())
                .timestamp(history.getTimestamp())
                .comment(history.getComment())
                .build();
    }
}