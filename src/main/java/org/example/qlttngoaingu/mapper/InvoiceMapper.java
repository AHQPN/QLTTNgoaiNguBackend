package org.example.qlttngoaingu.mapper;

import org.example.qlttngoaingu.dto.response.InvoiceDetailResponse;
import org.example.qlttngoaingu.dto.response.InvoiceResponse;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvoiceMapper {

    // --- MAP INVOICE ---
    @Mapping(target = "studentName", source = "student.name")
    // @Mapping(target = "studentCode", source = "student.studentCode")
    @Mapping(target = "paymentMethod", source = "paymentMethod.name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "details", source = "details")
    @Mapping(target = "studentId", source = "student.id")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    // --- MAP DETAIL ---
    @Mapping(target = "finalAmount", source = "amount")

    @Mapping(target = "courseName", source = "courseClass.course.courseName")

    @Mapping(target = "className", source = "courseClass.className")
    InvoiceDetailResponse toDetailResponse(InvoiceDetail detail);
}