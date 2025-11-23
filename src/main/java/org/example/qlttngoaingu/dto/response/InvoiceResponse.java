package org.example.qlttngoaingu.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// DTO trả về thông tin hóa đơn tổng quát
@Data
public class InvoiceResponse {
    private Integer invoiceId;
    private LocalDateTime dateCreated;
    private BigDecimal totalAmount;
    private Boolean status;

    private String studentName;
    private String studentId;
    private String paymentMethod;    // Tên phương thức thanh toán

    private List<InvoiceDetailResponse> details;


}