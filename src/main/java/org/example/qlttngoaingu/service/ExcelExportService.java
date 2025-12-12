package org.example.qlttngoaingu.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.qlttngoaingu.dto.response.ClassDetailResponse;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.GradeSheet;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.example.qlttngoaingu.entity.Session;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.GradeSheetRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailRepository;
import org.example.qlttngoaingu.repository.InvoiceRepository;
import org.example.qlttngoaingu.repository.SessionRepository;
import org.example.qlttngoaingu.repository.StudentRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final InvoiceDetailRepository invoiceDetailRepository;
    private final SessionRepository sessionRepository;
    private final CourseClassRepository classRepository;
    private final GradeSheetRepository gradeSheetRepository;
    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;

    /**
     * Xuất danh sách học viên của lớp ra file Excel
     */
    public byte[] exportStudentListToExcel(Integer classId) throws IOException {
        CourseClass courseClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = invoiceDetailRepository.findStudentsByClassId(classId);

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách học viên");

        // Tạo style cho header
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // Tạo tiêu đề
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH HỌC VIÊN LỚP: " + courseClass.getClassName());
        titleCell.setCellStyle(headerStyle);

        // Merge cells cho tiêu đề
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

        // Thông tin lớp
        int rowNum = 2;
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Khóa học:");
        infoRow1.createCell(1).setCellValue(courseClass.getCourse().getCourseName());

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Giảng viên:");
        infoRow2.createCell(1).setCellValue(courseClass.getLecturer() != null ? 
                courseClass.getLecturer().getFullName() : "Chưa phân công");

        Row infoRow3 = sheet.createRow(rowNum++);
        infoRow3.createCell(0).setCellValue("Phòng học:");
        infoRow3.createCell(1).setCellValue(courseClass.getRoom().getRoomName());

        // Dòng trống
        rowNum++;

        // Header của bảng
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Mã học viên", "Họ tên", "Giới tính", "Email", "Số điện thoại"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu học viên
        int stt = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue("HV" + student.getId());
            row.createCell(2).setCellValue(student.getName());
            row.createCell(3).setCellValue(student.getGender() != null ? String.valueOf(student.getGender()) : "");
            row.createCell(4).setCellValue(student.getAccount() != null ? 
                    student.getAccount().getEmail() : "");
            row.createCell(5).setCellValue(student.getAccount() != null ? 
                    student.getAccount().getPhoneNumber() : "");

            // Apply data style
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Tổng số học viên
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        Cell totalCell = totalRow.createCell(0);
        totalCell.setCellValue("Tổng số học viên: " + students.size());
        totalCell.setCellStyle(headerStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi ra byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Xuất danh sách điểm danh của lớp
     */
    public byte[] exportAttendanceToExcel(Integer classId) throws IOException {
        CourseClass courseClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = invoiceDetailRepository.findStudentsByClassId(classId);
        List<Session> sessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(classId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Điểm danh");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // Tiêu đề
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BẢNG ĐIỂM DANH LỚP: " + courseClass.getClassName());
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, Math.max(5, sessions.size() + 2)));

        // Header
        int rowNum = 2;
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("STT");
        headerRow.createCell(1).setCellValue("Mã HV");
        headerRow.createCell(2).setCellValue("Họ tên");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        // Các cột là các buổi học
        int colIndex = 3;
        for (Session session : sessions) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue("Buổi " + (colIndex - 3) + "\n" + 
                    session.getSessionDate().format(formatter));
            cell.setCellStyle(headerStyle);
        }

        // Style header
        for (int i = 0; i <= colIndex; i++) {
            if (headerRow.getCell(i) != null) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
        }

        // Dữ liệu học viên (để trống các ô điểm danh để giáo viên có thể điền)
        int stt = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue("HV" + student.getId());
            row.createCell(2).setCellValue(student.getName());

            // Tạo các ô trống cho điểm danh
            for (int i = 3; i < colIndex; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(""); // Để trống cho giáo viên điền
                cell.setCellStyle(dataStyle);
            }
        }

        // Auto-size
        for (int i = 0; i < colIndex; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Xuất báo cáo thống kê các lớp học
     */
    public byte[] exportClassStatisticsToExcel(List<ClassDetailResponse> classes) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Thống kê lớp học");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // Tiêu đề
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO THỐNG KÊ LỚP HỌC");
        titleCell.setCellStyle(headerStyle);

        // Header
        int rowNum = 2;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "STT", "Tên lớp", "Khóa học", "Giảng viên", 
            "Phòng", "Số HV", "Sức chứa", "Tỷ lệ lấp đầy",
            "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu
        int stt = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (ClassDetailResponse cls : classes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(cls.getClassName());
            row.createCell(2).setCellValue(cls.getCourseName());
            row.createCell(3).setCellValue(cls.getInstructorName() != null ? 
                    cls.getInstructorName() : "Chưa phân công");
            row.createCell(4).setCellValue(cls.getRoomName());
            row.createCell(5).setCellValue(cls.getCurrentEnrollment());
            row.createCell(6).setCellValue(cls.getMaxCapacity());
            
            // Tính tỷ lệ lấp đầy
            double fillRate = cls.getMaxCapacity() > 0 ? 
                    (cls.getCurrentEnrollment() * 100.0 / cls.getMaxCapacity()) : 0;
            row.createCell(7).setCellValue(String.format("%.1f%%", fillRate));
            
            row.createCell(8).setCellValue(cls.getStartDate() != null ? 
                    cls.getStartDate().format(formatter) : "");
            row.createCell(9).setCellValue(cls.getEndDate() != null ? 
                    cls.getEndDate().format(formatter) : "");
            row.createCell(10).setCellValue("Đang học"); // hoặc lấy từ cls nếu có

            // Apply style
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Tạo style cho header
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Background color
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Border
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        
        return style;
    }

    /**
     * Tạo style cho data cells
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Border
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Alignment
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    /**
     * Xuất bảng điểm chi tiết của học viên trong lớp
     */
    public byte[] exportGradeSheetToExcel(Integer classId) throws IOException {
        CourseClass courseClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = invoiceDetailRepository.findStudentsByClassId(classId);
        List<GradeSheet> allGrades = gradeSheetRepository.findAllByClassId(classId);

        // Map grades theo studentId
        Map<Integer, List<GradeSheet>> gradesByStudentId = allGrades.stream()
                .collect(Collectors.groupingBy(g -> 
                    g.getEnrollment().getInvoice().getStudent().getId()
                ));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bảng điểm");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.cloneStyleFrom(dataStyle);
        numberStyle.setAlignment(HorizontalAlignment.CENTER);

        // Tiêu đề
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BẢNG ĐIỂM LỚP: " + courseClass.getClassName());
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        // Thông tin lớp
        int rowNum = 2;
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Khóa học:");
        infoRow1.createCell(1).setCellValue(courseClass.getCourse().getCourseName());

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Giảng viên:");
        infoRow2.createCell(1).setCellValue(courseClass.getLecturer() != null ? 
                courseClass.getLecturer().getFullName() : "Chưa phân công");

        rowNum++;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "STT", "Mã HV", "Họ tên", 
            "Điểm chuyên cần (10%)", "Điểm giữa kỳ (30%)", 
            "Điểm cuối kỳ (60%)", "Điểm trung bình"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu điểm
        int stt = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue("HV" + student.getId());
            row.createCell(2).setCellValue(student.getName());

            List<GradeSheet> studentGrades = gradesByStudentId.get(student.getId());
            
            BigDecimal attendance = null, midterm = null, finalScore = null;
            
            if (studentGrades != null) {
                for (GradeSheet grade : studentGrades) {
                    String gradeType = grade.getGradeType();
                    if (gradeType == null || grade.getScore() == null) continue;
                    
                    if (gradeType.contains("Chuyên cần") || gradeType.equalsIgnoreCase("Attendance")) {
                        attendance = grade.getScore();
                    } else if (gradeType.contains("Giữa kỳ") || gradeType.equalsIgnoreCase("Midterm")) {
                        midterm = grade.getScore();
                    } else if (gradeType.contains("Cuối kỳ") || gradeType.equalsIgnoreCase("Final")) {
                        finalScore = grade.getScore();
                    }
                }
            }

            // Fill điểm
            Cell attendanceCell = row.createCell(3);
            if (attendance != null) {
                attendanceCell.setCellValue(attendance.doubleValue());
            } else {
                attendanceCell.setCellValue("");
            }
            attendanceCell.setCellStyle(numberStyle);

            Cell midtermCell = row.createCell(4);
            if (midterm != null) {
                midtermCell.setCellValue(midterm.doubleValue());
            } else {
                midtermCell.setCellValue("");
            }
            midtermCell.setCellStyle(numberStyle);

            Cell finalCell = row.createCell(5);
            if (finalScore != null) {
                finalCell.setCellValue(finalScore.doubleValue());
            } else {
                finalCell.setCellValue("");
            }
            finalCell.setCellStyle(numberStyle);

            // Tính điểm trung bình
            Cell avgCell = row.createCell(6);
            if (attendance != null && midterm != null && finalScore != null) {
                double avg = attendance.doubleValue() * 0.1 + 
                           midterm.doubleValue() * 0.3 + 
                           finalScore.doubleValue() * 0.6;
                avgCell.setCellValue(Math.round(avg * 100.0) / 100.0);
            } else {
                avgCell.setCellValue("");
            }
            avgCell.setCellStyle(numberStyle);

            // Apply style cho các cell khác
            row.getCell(0).setCellStyle(numberStyle);
            row.getCell(1).setCellStyle(dataStyle);
            row.getCell(2).setCellStyle(dataStyle);
        }

        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Xuất báo cáo tài chính (doanh thu từ hóa đơn)
     */
    public byte[] exportFinancialReportToExcel(
            LocalDate startDate, 
            LocalDate endDate) throws IOException {
        
        List<Invoice> invoices = invoiceRepository.findByPaymentDateBetween(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo cáo tài chính");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.cloneStyleFrom(dataStyle);
        moneyStyle.setAlignment(HorizontalAlignment.RIGHT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Tiêu đề
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

        // Thời gian báo cáo
        int rowNum = 2;
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue(
            "Từ ngày " + startDate.format(formatter) + " đến " + endDate.format(formatter)
        );
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        rowNum++;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "STT", "Học viên", "Ngày thanh toán", 
            "Tổng tiền gốc", "Giảm giá", "Thành tiền"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu hóa đơn
        int stt = 1;
        BigDecimal totalOriginal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(invoice.getStudent().getName());
            row.createCell(2).setCellValue(
                invoice.getDateCreated() != null ? 
                invoice.getDateCreated().toLocalDate().format(formatter) : ""
            );

            // Tạm thời sử dụng totalAmount vì không có totalOriginalPrice
            BigDecimal originalPrice = invoice.getTotalAmount() != null ? 
                    invoice.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO; // Tạm thời = 0
            BigDecimal finalPrice = invoice.getTotalAmount() != null ? 
                    invoice.getTotalAmount() : BigDecimal.ZERO;

            row.createCell(3).setCellValue(originalPrice.doubleValue());
            row.createCell(4).setCellValue(discountAmount.doubleValue());
            row.createCell(5).setCellValue(finalPrice.doubleValue());

            totalOriginal = totalOriginal.add(originalPrice);
            totalDiscount = totalDiscount.add(discountAmount);
            totalFinal = totalFinal.add(finalPrice);

            // Apply styles
            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
            for (int i = 3; i < 6; i++) {
                row.getCell(i).setCellStyle(moneyStyle);
            }
        }

        // Tổng cộng
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("TỔNG CỘNG");
        totalLabelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum, rowNum, 0, 2));

        Cell totalOriginalCell = totalRow.createCell(3);
        totalOriginalCell.setCellValue(totalOriginal.doubleValue());
        totalOriginalCell.setCellStyle(headerStyle);

        Cell totalDiscountCell = totalRow.createCell(4);
        totalDiscountCell.setCellValue(totalDiscount.doubleValue());
        totalDiscountCell.setCellStyle(headerStyle);

        Cell totalFinalCell = totalRow.createCell(5);
        totalFinalCell.setCellValue(totalFinal.doubleValue());
        totalFinalCell.setCellStyle(headerStyle);

        // Thống kê
        rowNum += 2;
        Row statsRow = sheet.createRow(rowNum++);
        statsRow.createCell(0).setCellValue("Số hóa đơn: " + invoices.size());
        
        Row avgRow = sheet.createRow(rowNum);
        double avgAmount = invoices.isEmpty() ? 0 : 
                totalFinal.doubleValue() / invoices.size();
        avgRow.createCell(0).setCellValue(
            "Doanh thu trung bình/HĐ: " + Math.round(avgAmount) + " VNĐ"
        );

        // Auto-size
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Xuất hóa đơn chi tiết của học viên ra Excel
     * (Dùng tạm Excel, có thể chuyển sang PDF sau)
     */
    public byte[] exportInvoiceToExcel(Integer invoiceId) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Hóa đơn");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.cloneStyleFrom(dataStyle);
        moneyStyle.setAlignment(HorizontalAlignment.RIGHT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Header hóa đơn
        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HÓA ĐƠN THANH TOÁN");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

        rowNum++;

        // Thông tin trung tâm (hardcoded - có thể lấy từ config)
        Row centerRow1 = sheet.createRow(rowNum++);
        centerRow1.createCell(0).setCellValue("Trung tâm Ngoại ngữ ABC");
        
        Row centerRow2 = sheet.createRow(rowNum++);
        centerRow2.createCell(0).setCellValue("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM");
        
        Row centerRow3 = sheet.createRow(rowNum++);
        centerRow3.createCell(0).setCellValue("SĐT: 0123456789");

        rowNum++;

        // Thông tin hóa đơn
        Row invoiceIdRow = sheet.createRow(rowNum++);
        invoiceIdRow.createCell(0).setCellValue("Số hóa đơn:");
        invoiceIdRow.createCell(1).setCellValue("HD" + invoice.getInvoiceId());

        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Ngày lập:");
        dateRow.createCell(1).setCellValue(
            invoice.getDateCreated() != null ? 
            invoice.getDateCreated().toLocalDate().format(formatter) : ""
        );

        Row paymentDateRow = sheet.createRow(rowNum++);
        paymentDateRow.createCell(0).setCellValue("Trạng thái:");
        paymentDateRow.createCell(1).setCellValue(
            invoice.getStatus() != null && invoice.getStatus() ? 
            "Đã thanh toán" : "Chưa thanh toán"
        );

        rowNum++;

        // Thông tin học viên
        Row studentRow = sheet.createRow(rowNum++);
        studentRow.createCell(0).setCellValue("Học viên:");
        studentRow.createCell(1).setCellValue(invoice.getStudent().getName());

        Row emailRow = sheet.createRow(rowNum++);
        emailRow.createCell(0).setCellValue("Email:");
        emailRow.createCell(1).setCellValue(
            invoice.getStudent().getAccount() != null ? 
            invoice.getStudent().getAccount().getEmail() : ""
        );

        Row phoneRow = sheet.createRow(rowNum++);
        phoneRow.createCell(0).setCellValue("Số điện thoại:");
        phoneRow.createCell(1).setCellValue(
            invoice.getStudent().getAccount() != null ? 
            invoice.getStudent().getAccount().getPhoneNumber() : ""
        );

        rowNum++;

        // Chi tiết các khóa học
        Row detailHeaderRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Lớp học", "Khóa học", "Học phí", "Thành tiền"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = detailHeaderRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Chi tiết hóa đơn
        List<InvoiceDetail> details = invoice.getDetails();
        int stt = 1;
        for (InvoiceDetail detail : details) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(detail.getCourseClass().getClassName());
            row.createCell(2).setCellValue(detail.getCourseClass().getCourse().getCourseName());
            row.createCell(3).setCellValue(
                detail.getCourseClass().getCourse().getTuitionFee() != null ? 
                detail.getCourseClass().getCourse().getTuitionFee().doubleValue() : 0
            );
            row.createCell(4).setCellValue(
                detail.getCourseClass().getCourse().getTuitionFee() != null ? 
                detail.getCourseClass().getCourse().getTuitionFee().doubleValue() : 0
            );

            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
            row.getCell(3).setCellStyle(moneyStyle);
            row.getCell(4).setCellStyle(moneyStyle);
        }

        rowNum++;

        // Tổng tiền (không có totalOriginalPrice và discount nên tạm bỏ qua)
        rowNum++;

        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(2).setCellValue("THÀNH TIỀN:");
        totalRow.getCell(2).setCellStyle(headerStyle);
        Cell totalCell = totalRow.createCell(4);
        totalCell.setCellValue(
            invoice.getTotalAmount() != null ? 
            invoice.getTotalAmount().doubleValue() : 0
        );
        totalCell.setCellStyle(headerStyle);

        rowNum += 2;

        // Chữ ký
        Row signRow = sheet.createRow(rowNum);
        signRow.createCell(0).setCellValue("Người lập");
        signRow.createCell(3).setCellValue("Học viên");

        // Auto-size
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
