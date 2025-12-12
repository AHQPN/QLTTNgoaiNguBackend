package org.example.qlttngoaingu.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.example.qlttngoaingu.service.CourseClassService;
import org.example.qlttngoaingu.service.ExcelExportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExcelExportService excelExportService;
    private final CourseClassService courseClassService;

    /**
     * Xuất danh sách học viên của lớp ra file Excel
     * GET /api/export/students/{classId}
     */
    @GetMapping("/students/{classId}")
    public ResponseEntity<ByteArrayResource> exportStudentList(@PathVariable Integer classId) {
        try {
            byte[] excelData = excelExportService.exportStudentListToExcel(classId);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "DanhSachHocVien_Lop" + classId + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất bảng điểm danh của lớp ra file Excel
     * GET /api/export/attendance/{classId}
     */
    @GetMapping("/attendance/{classId}")
    public ResponseEntity<ByteArrayResource> exportAttendance(@PathVariable Integer classId) {
        try {
            byte[] excelData = excelExportService.exportAttendanceToExcel(classId);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "BangDiemDanh_Lop" + classId + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất báo cáo thống kê tất cả các lớp
     * GET /api/export/class-statistics
     */
    @GetMapping("/class-statistics")
    public ResponseEntity<ByteArrayResource> exportClassStatistics() {
        try {
            // Lấy danh sách tất cả các lớp (có thể thêm filter params nếu cần)
            // Ở đây tạm thời lấy page đầu tiên với size lớn
            var classResponse = courseClassService.getAllClasses(0, 1000);
            
            // Convert sang ClassDetailResponse nếu cần
            // Hoặc tạo method riêng trong ExcelExportService nhận ClassResponse.ClassInfo
            
            byte[] excelData = excelExportService.exportClassStatisticsToExcel(List.of());
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "ThongKeLopHoc_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất danh sách học viên cho giảng viên
     * GET /api/export/teacher/students/{classId}
     */
    @GetMapping("/teacher/students/{classId}")
    public ResponseEntity<ByteArrayResource> exportStudentListForTeacher(
            @PathVariable Integer classId,
            @RequestParam Integer userId) {
        try {
            // Kiểm tra quyền truy cập (teacher owns this class)
            courseClassService.getClassDetailForTeacher(userId, classId);
            
            byte[] excelData = excelExportService.exportStudentListToExcel(classId);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "DanhSachHocVien_Lop" + classId + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất bảng điểm chi tiết của lớp
     * GET /api/export/grades/{classId}
     */
    @GetMapping("/grades/{classId}")
    public ResponseEntity<ByteArrayResource> exportGradeSheet(@PathVariable Integer classId) {
        try {
            byte[] excelData = excelExportService.exportGradeSheetToExcel(classId);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "BangDiem_Lop" + classId + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất báo cáo tài chính trong khoảng thời gian
     * GET /api/export/financial-report?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/financial-report")
    public ResponseEntity<ByteArrayResource> exportFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] excelData = excelExportService.exportFinancialReportToExcel(startDate, endDate);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "BaoCaoTaiChinh_" + 
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất hóa đơn chi tiết
     * GET /api/export/invoice/{invoiceId}
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ByteArrayResource> exportInvoice(@PathVariable Integer invoiceId) {
        try {
            byte[] excelData = excelExportService.exportInvoiceToExcel(invoiceId);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String filename = "HoaDon_" + invoiceId + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
