package org.example.qlttngoaingu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${APP_SITE_URL:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Quản Lý Trung Tâm Ngoại Ngữ API")
                        .version("1.0.0")
                        .description("""
                                # API Documentation cho Hệ Thống Quản Lý Trung Tâm Ngoại Ngữ
                                
                                ## Giới thiệu
                                Đây là API documentation cho hệ thống quản lý trung tâm ngoại ngữ, bao gồm:
                                - Quản lý người dùng, học viên, giảng viên
                                - Quản lý khóa học, lớp học, module
                                - Xử lý thanh toán VNPay
                                - Quản lý lịch học, phòng học
                                - Quản lý điểm danh, bằng cấp
                                
                                ## Xác thực (Authentication)
                                Hệ thống sử dụng JWT (JSON Web Token) cho việc xác thực. 
                                
                                ### Cách lấy token:
                                1. Đăng ký tài khoản: `POST /auth/signup`
                                2. Xác thực email qua link gửi về email
                                3. Đăng nhập: `POST /auth/login`
                                4. Sử dụng `accessToken` trong response
                                
                                ### Cách sử dụng token:
                                - Thêm vào header: `Authorization: Bearer {accessToken}`
                                - Token hết hạn sau 24 giờ
                                - Sử dụng refresh token để lấy token mới: `POST /auth/refreshtoken`
                                
                                ## Refresh Token
                                - Refresh token được lưu trong cookie (HTTP-only)
                                - Thời hạn: 7 ngày
                                - Endpoint: `POST /auth/refreshtoken`
                                
                                ## Phân quyền (Authorization)
                                Hệ thống có các vai trò sau:
                                - **STUDENT**: Học viên - Có thể xem khóa học, đăng ký khóa học, xem thông tin cá nhân
                                - **TEACHER**: Giảng viên - Có thể quản lý lớp học được phân công, điểm danh học viên
                                - **ADMIN**: Quản trị viên - Có toàn quyền quản lý hệ thống
                                
                                ## Error Codes
                                API trả về các mã lỗi chuẩn:
                                - `1000`: Success
                                - `1002`: User not found
                                - `1003`: User not verified
                                - `2000`: Course not found
                                - `10000-10002`: Refresh token errors
                                - `11000-11004`: Authentication errors
                                - `12001-12009`: Validation errors
                                
                                ## Response Format
                                Tất cả response đều có format:
                                ```json
                                {
                                  "code": 1000,
                                  "message": "Success message",
                                  "data": { ... }
                                }
                                ```
                                
                                ## VNPay Integration
                                Hệ thống tích hợp cổng thanh toán VNPay Sandbox:
                                - TMN Code: 8FHCECWU
                                - Environment: Sandbox (Test)
                                - Currency: VND
                                
                                ## Contact
                                Nếu cần hỗ trợ, vui lòng liên hệ team phát triển.
                                """)
                        .contact(new Contact()
                                .name("API Support Team")
                                .email("support@qlttngoaingu.com")
                                .url("https://github.com/AHQPN"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url(serverUrl).description("Local Development Server"),
                        new Server().url("https://production-api.qlttngoaingu.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token. Lấy token từ endpoint `/auth/login`")));
    }
}
