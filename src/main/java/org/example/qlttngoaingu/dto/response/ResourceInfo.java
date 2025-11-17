package org.example.qlttngoaingu.dto.response;

import lombok.Data;

@Data
public class ResourceInfo {
    private Integer id;
    private String name;
    private String additionalInfo; // Ví dụ: "Phòng 50 chỗ", "Chuyên ngành AI"

    public ResourceInfo(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ResourceInfo(Integer id, String name, String additionalInfo) {
        this.id = id;
        this.name = name;
        this.additionalInfo = additionalInfo;
    }
}
