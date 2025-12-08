package org.example.qlttngoaingu.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InvoicePageResponse {
    private List<InvoiceListResponse> invoices;
    private int currentPage;
    private long totalItems;
    private int totalPages;
}
