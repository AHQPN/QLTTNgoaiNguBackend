package org.example.qlttngoaingu.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CourseRegistrationRequest {
    private Integer studentId;
    private List<Integer> classIds;
    private Integer paymentMethodId;
}