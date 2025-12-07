package org.example.qlttngoaingu.service;

import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.example.qlttngoaingu.entity.Student;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceEmailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi email hóa đơn sau khi thanh toán thành công
     */
    public void sendInvoiceEmail(Invoice invoice) {
        if (invoice == null || invoice.getStudent() == null) {
            log.warn("Cannot send invoice email: invoice or student is null");
            return;
        }

        Student student = invoice.getStudent();
        if (student.getAccount() == null || student.getAccount().getEmail() == null ||
                student.getAccount().getEmail().isEmpty()) {
            log.info("Student {} has no email, skipping", student.getId());
            return;
        }

        String email = student.getAccount().getEmail();
        String studentName = student.getName() != null ? student.getName() : "Học viên";
        String subject = "Hóa đơn thanh toán #" + invoice.getInvoiceId() + " - Ipower IELTS";

        StringBuilder courses = new StringBuilder();
        BigDecimal originalTotal = BigDecimal.ZERO;
        if (invoice.getDetails() != null) {
            for (InvoiceDetail d : invoice.getDetails()) {
                CourseClass cc = d.getCourseClass();
                if (cc != null && cc.getCourse() != null) {
                    courses.append(String.format(
                            "<tr><td style='padding:8px;border-bottom:1px solid #e5e7eb'>%s - %s</td>" +
                                    "<td style='padding:8px;border-bottom:1px solid #e5e7eb;text-align:right'>%,.0fđ</td></tr>",
                            cc.getCourse().getCourseName(), cc.getClassName(), d.getAmount()));
                    originalTotal = originalTotal.add(BigDecimal.valueOf(cc.getCourse().getTuitionFee()));
                }
            }
        }

        BigDecimal discount = originalTotal.subtract(invoice.getTotalAmount());
        String date = invoice.getDateCreated() != null
                ? invoice.getDateCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "";
        String payMethod = invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().getName() : "Không xác định";

        String html = String.format(
                """
                        <div style='font-family:Arial;max-width:600px;margin:0 auto'>
                        <div style='background:#f97316;color:white;padding:20px;text-align:center'><h1 style='margin:0'>Thanh toán thành công</h1></div>
                        <div style='padding:30px;background:#f9fafb'>
                        <p>Xin chào <b>%s</b>,</p>
                        <p>Cảm ơn bạn đã đăng ký tại Ipower IELTS!</p>
                        <div style='background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:20px;margin:20px 0'>
                        <h3 style='margin-top:0;color:#f97316'>HÓA ĐƠN #%d</h3>
                        <p style='color:#6b7280'>Ngày: %s</p>
                        <table style='width:100%%;border-collapse:collapse'>
                        <thead><tr style='background:#f3f4f6'><th style='padding:10px;text-align:left'>Khóa học</th><th style='padding:10px;text-align:right'>Học phí</th></tr></thead>
                        <tbody>%s</tbody>
                        </table>
                        <div style='border-top:2px solid #e5e7eb;margin-top:15px;padding-top:15px'>
                        <p>Tổng gốc: <b>%,.0fđ</b></p>
                        <p style='color:#22c55e'>Giảm giá: <b>-%,.0fđ</b></p>
                        <p style='font-size:18px;color:#f97316'><b>TỔNG: %,.0fđ</b></p>
                        </div>
                        <p>PT thanh toán: <b>%s</b></p>
                        </div>
                        <p>Trân trọng,<br>Ipower IELTS</p>
                        </div></div>
                        """,
                studentName, invoice.getInvoiceId(), date, courses.toString(),
                originalTotal, discount, invoice.getTotalAmount(), payMethod);

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg);
            helper.setFrom("nguyenbro9721@gmail.com", "Ipower IELTS");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Invoice email sent to {} for invoice #{}", email, invoice.getInvoiceId());
        } catch (Exception e) {
            log.error("Failed to send invoice email: {}", e.getMessage());
        }
    }
}
