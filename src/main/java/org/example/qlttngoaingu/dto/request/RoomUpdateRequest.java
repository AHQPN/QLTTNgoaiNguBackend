package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {
    
    private String roomName;
    
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;
    
    @Pattern(regexp = "^(Sẵn sàng|Bảo trì)$", message = "Trạng thái phải là 'Sẵn sàng' hoặc 'Bảo trì'")
    private String status;
}
