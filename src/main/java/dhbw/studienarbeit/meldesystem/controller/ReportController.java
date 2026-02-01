package dhbw.studienarbeit.meldesystem.controller;


import dhbw.studienarbeit.meldesystem.dto.*;
import dhbw.studienarbeit.meldesystem.model.*;
import dhbw.studienarbeit.meldesystem.security.*;
import dhbw.studienarbeit.meldesystem.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReportDTO>> createReport(
            @Valid @ModelAttribute CreateReportRequest request,
            @RequestParam(required = false) MultipartFile photo,
            @CurrentUser UserPrincipal currentUser) {

        log.info("Creating report: {} by user: {}", request.getTitle(),
                currentUser != null ? currentUser.getId() : "anonymous");

        Long userId = currentUser != null ? currentUser.getId() : null;
        ReportDTO report = reportService.createReport(request, photo, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report created successfully", report));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDetailDTO>> getReport(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        Long userId = currentUser != null ? currentUser.getId() : null;
        ReportDetailDTO report = reportService.getReportById(id, userId);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReportDTO>>> searchReports(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean myReports,
            @RequestParam(required = false) Boolean helpingReports,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @CurrentUser UserPrincipal currentUser) {

        ReportSearchParams params = ReportSearchParams.builder()
                .latitude(lat)
                .longitude(lng)
                .radius(radius)
                .category(category != null ? Category.valueOf(category) : null)
                .status(status != null ? ReportStatus.valueOf(status) : null)
                .myReports(myReports)
                .helpingReports(helpingReports)
                .page(page)
                .size(size)
                .build();

        Long userId = currentUser != null ? currentUser.getId() : null;
        PageResponse<ReportDTO> reports = reportService.searchReports(params, userId);

        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated() and !@authService.isAnonymous(#currentUser)")
    public ResponseEntity<ApiResponse<JoinReportResponse>> joinReport(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        log.info("User {} joining report {}", currentUser.getId(), id);
        JoinReportResponse response = reportService.joinReport(id, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHORITY')")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            @CurrentUser UserPrincipal currentUser) {

        ReportStatus newStatus = ReportStatus.valueOf(status);
        reportService.updateReportStatus(id, newStatus, comment,
                currentUser.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", null));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<ReportHistoryDTO>>> getHistory(
            @PathVariable Long id) {

        ReportDetailDTO report = reportService.getReportById(id, null);
        return ResponseEntity.ok(ApiResponse.success(report.getHistory()));
    }
}