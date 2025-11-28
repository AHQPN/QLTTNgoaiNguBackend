package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPaySignatureDebugResponse {
    private String data;
    private String providedHash;
    private String expectedHash;
    private boolean valid;
}
