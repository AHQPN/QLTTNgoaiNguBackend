# 📚 API DOCUMENTATION - HỆ THỐNG QUẢN LÝ TRUNG TÂM NGOẠI NGỮ

## 🚀 Giới thiệu

Hệ thống API RESTful cho việc quản lý trung tâm ngoại ngữ, được xây dựng với Spring Boot 3.5.6 và Java 17. API hỗ trợ đầy đủ các chức năng quản lý học viên, giảng viên, khóa học, lớp học, thanh toán và nhiều tính năng khác.

## 📖 Truy cập API Documentation

### Swagger UI (Interactive Documentation)

Sau khi khởi động ứng dụng, truy cập vào các đường dẫn sau:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI YAML**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

### Tính năng của Swagger UI

✅ **Giao diện tương tác** - Test API trực tiếp trên trình duyệt  
✅ **Authentication** - Tích hợp JWT Bearer Token  
✅ **Request/Response Examples** - Ví dụ chi tiết cho mỗi endpoint  
✅ **Schema Documentation** - Mô tả đầy đủ cấu trúc dữ liệu  
✅ **Try it out** - Thực thi API ngay lập tức  

---

## 🔐 XÁC THỰC (AUTHENTICATION)

### 1. Đăng ký tài khoản

```http
POST /auth/signup
Content-Type: application/json

{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "phoneNumber": "0123456789",
  "password": "password123",
  "address": "123 Đường ABC, Quận 1, TP.HCM",
  "gender": "Nam",
  "ngaySinh": "1990-01-01",
  "job": "Sinh viên"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "We have sent a verification email, please check your inbox"
}
```

### 2. Xác thực email

Kiểm tra email và click vào link xác thực. Link có dạng:
```
http://localhost:8080/auth/verify?code={uuid-code}&type=EMAIL_VERIFICATION
```

### 3. Đăng nhập

```http
POST /auth/login
Content-Type: application/json

{
  "identifier": "0123456789",  // Hoặc email
  "password": "password123"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Login Successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "role": "STUDENT",
    "userId": 1
  }
}
```

### 4. Sử dụng Access Token

Thêm token vào header của mỗi request cần authentication:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Cách thêm token trong Swagger UI:**

1. Click nút **"Authorize"** ở góc trên bên phải
2. Nhập: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (không cần thêm "Bearer")
3. Click **"Authorize"**
4. Tất cả request sau đó sẽ tự động thêm token

### 5. Refresh Token

Khi access token hết hạn (sau 24 giờ), sử dụng refresh token để lấy token mới:

```http
POST /auth/refreshtoken
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "New Refresh Token and Access Token are created Successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "660e8400-e29b-41d4-a716-446655440001"
  }
}
```

---

## 👥 PHÂN QUYỀN (AUTHORIZATION)

### Các vai trò trong hệ thống:

| Vai trò | Quyền hạn |
|---------|-----------|
| **STUDENT** | - Xem danh sách khóa học<br>- Đăng ký khóa học<br>- Xem thông tin cá nhân<br>- Thanh toán học phí |
| **TEACHER** | - Xem lớp học được phân công<br>- Điểm danh học viên<br>- Quản lý buổi học |
| **ADMIN** | - Toàn quyền quản lý hệ thống<br>- Quản lý người dùng, khóa học, lớp học<br>- Xem báo cáo, thống kê |

### Endpoints yêu cầu quyền:

- **Public (không cần đăng nhập):**
  - `GET /courses/**` - Xem khóa học
  - `GET /categories/**` - Xem danh mục
  - `POST /auth/**` - Xác thực

- **Authenticated (đã đăng nhập):**
  - `GET /users/me` - Xem thông tin cá nhân
  - `PUT /users/me` - Cập nhật thông tin

- **ADMIN only:**
  - `POST /courses` - Tạo khóa học mới
  - `PUT /courses/{id}` - Cập nhật khóa học
  - `POST /categories` - Tạo danh mục

---

## 📋 CÁC MODULE API

### 1. Authentication & Authorization (`/auth`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/auth/signup` | POST | Đăng ký tài khoản học viên | ❌ |
| `/auth/login` | POST | Đăng nhập | ❌ |
| `/auth/logout` | POST | Đăng xuất | ✅ |
| `/auth/refreshtoken` | POST | Làm mới access token | ❌ |
| `/auth/verify` | GET | Xác thực email | ❌ |
| `/auth/resend` | POST | Gửi lại mã xác thực | ❌ |
| `/auth/forgot-password` | POST | Quên mật khẩu | ❌ |
| `/auth/reset-password` | POST | Đặt lại mật khẩu | ❌ |
| `/auth/verify-reset-code` | GET | Kiểm tra mã reset | ❌ |

### 2. User Management (`/users`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/users/me` | GET | Xem thông tin cá nhân | ✅ |
| `/users/{id}` | GET | Xem thông tin user | ✅ |
| `/users/{id}` | PUT | Cập nhật thông tin | ✅ |

### 3. Course Management (`/courses`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/courses` | GET | Danh sách khóa học (phân trang) | ❌ |
| `/courses/activecourses` | GET | Khóa học đang hoạt động | ❌ |
| `/courses/{id}` | GET | Chi tiết khóa học | ❌ |
| `/courses` | POST | Tạo khóa học mới | ✅ ADMIN |
| `/courses/{id}` | PUT | Cập nhật khóa học | ✅ ADMIN |
| `/courses/status/{id}` | POST | Thay đổi trạng thái | ✅ ADMIN |
| `/courses/recommedcousres/{id}` | GET | Khóa học đề xuất | ❌ |

### 4. Class Management (`/courseclasses`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/courseclasses` | GET | Danh sách lớp học | ❌ |
| `/courseclasses/{id}` | GET | Chi tiết lớp học | ❌ |
| `/courseclasses` | POST | Tạo lớp học mới | ✅ ADMIN |
| `/courseclasses/{id}` | PUT | Cập nhật lớp học | ✅ ADMIN |

### 5. Payment (`/payment`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/payment/create-payment` | POST | Tạo thanh toán VNPay | ✅ |
| `/payment/vnpay-return` | GET | Callback từ VNPay | ❌ |

### 6. Order Management (`/orders`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/orders` | GET | Danh sách đơn hàng | ✅ |
| `/orders/{id}` | GET | Chi tiết đơn hàng | ✅ |
| `/orders` | POST | Tạo đơn hàng mới | ✅ |

### 7. Student Management (`/students`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/students` | GET | Danh sách học viên | ✅ |
| `/students/{id}` | GET | Chi tiết học viên | ✅ |
| `/students/{id}` | PUT | Cập nhật thông tin | ✅ |

### 8. Lecturer Management (`/lecturers`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/lecturers` | GET | Danh sách giảng viên | ❌ |
| `/lecturers/{id}` | GET | Chi tiết giảng viên | ❌ |
| `/lecturers` | POST | Thêm giảng viên | ✅ ADMIN |

### 9. Module Management (`/modules`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/modules` | GET | Danh sách module | ❌ |
| `/modules/{id}` | GET | Chi tiết module | ❌ |
| `/modules` | POST | Tạo module | ✅ ADMIN |
| `/modules/{id}` | PUT | Cập nhật module | ✅ ADMIN |

### 10. Category Management (`/categories`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/categories` | GET | Danh sách danh mục | ❌ |
| `/categories/{id}` | GET | Chi tiết danh mục | ❌ |
| `/categories` | POST | Tạo danh mục | ✅ ADMIN |

### 11. Room Management (`/rooms`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/rooms` | GET | Danh sách phòng học | ❌ |
| `/rooms/{id}` | GET | Chi tiết phòng | ❌ |

### 12. File Upload (`/files`)

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/files/upload` | POST | Upload file/ảnh | ✅ |
| `/files/{filename}` | GET | Lấy file | ❌ |

---

## 🔔 RESPONSE FORMAT

Tất cả API response đều có cấu trúc chuẩn:

### Success Response

```json
{
  "code": 1000,
  "message": "Success message",
  "data": {
    // Response data here
  }
}
```

### Error Response

```json
{
  "code": 1002,
  "message": "User not found"
}
```

---

## ⚠️ ERROR CODES

### Authentication & Authorization Errors

| Code | HTTP Status | Message |
|------|-------------|---------|
| 1002 | 404 | User not found |
| 1003 | 403 | User not verified, please check your email |
| 1005 | 400 | Phone number or email already exists |
| 10000 | 401 | Refresh token not found |
| 10001 | 401 | Refresh token expired |
| 10002 | 401 | Refresh token revoked |
| 11000 | 401 | Invalid authentication credentials |
| 11001 | 401 | Authentication token expired |
| 11002 | 401 | Invalid authentication token |
| 11003 | 403 | Access denied |
| 11004 | 401 | Unauthenticated |
| 11005 | 410 | Expired verification code |

### Validation Errors

| Code | HTTP Status | Message |
|------|-------------|---------|
| 12001 | 400 | Invalid email |
| 12002 | 400 | Invalid phone number |
| 12003 | 400 | Password must be at least 6 characters |
| 12004 | 400 | Invalid email or phone number |
| 12006 | 400 | Invalid verification code |
| 12007 | 400 | Password and confirm password do not match |

### Business Logic Errors

| Code | HTTP Status | Message |
|------|-------------|---------|
| 2000 | 404 | Course not found |
| 2005 | 404 | Class not found |
| 4000 | 404 | Genre not found |
| 6000 | 404 | Borrow slip not found |
| 8000 | 404 | Payment not found |
| 8001 | 400 | Payment failed |

---

## 💳 VNPAY INTEGRATION

### Tạo thanh toán

```http
POST /payment/create-payment
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 5000000,
  "orderInfo": "Thanh toán học phí khóa học IELTS",
  "orderId": "ORD123456"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
  }
}
```

### Luồng thanh toán:

1. Client gọi API tạo payment → Nhận payment URL
2. Redirect user đến VNPay payment URL
3. User thanh toán trên VNPay
4. VNPay redirect về `/payment/vnpay-return?vnp_ResponseCode=00&...`
5. Backend xử lý và redirect đến frontend với kết quả

---

## 🧪 TESTING VỚI SWAGGER UI

### Bước 1: Đăng ký và đăng nhập

1. Mở Swagger UI: http://localhost:8080/swagger-ui.html
2. Tìm endpoint `POST /auth/signup`
3. Click "Try it out"
4. Điền thông tin đăng ký
5. Click "Execute"
6. Kiểm tra email và xác thực
7. Sử dụng `POST /auth/login` để đăng nhập

### Bước 2: Lấy Access Token

Sau khi login thành công, copy `accessToken` từ response.

### Bước 3: Authorize trong Swagger

1. Click nút **"Authorize"** (biểu tượng ổ khóa) ở góc trên
2. Paste access token vào ô "Value"
3. Click "Authorize"
4. Click "Close"

### Bước 4: Test các API

Giờ bạn có thể test bất kỳ endpoint nào yêu cầu authentication!

---

## 📊 PAGINATION

Các endpoint trả về danh sách có hỗ trợ phân trang:

### Request Parameters:

- `page`: Số trang (bắt đầu từ 0), mặc định = 0
- `size`: Số items mỗi trang, mặc định = 15

### Example:

```http
GET /courses?page=0&size=10
```

### Response:

```json
{
  "code": 1000,
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0,
    "numberOfElements": 10,
    "first": true,
    "last": false
  }
}
```

---

## 🔧 CONFIGURATION

### Environment Variables

Cấu hình trong `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DATASOURCE_URL}
spring.datasource.username=${DATASOURCE_USERNAME}
spring.datasource.password=${DATASOURCE_PASSWORD}

# JWT
app.jwtSecret=${APP_JWT_SECRET}
app.jwtExpirationMs=86400000  # 24 hours
app.jwtRefreshExpirationMs=604800000  # 7 days

# Email
spring.mail.host=${GMAIL_SMTP_HOST}
spring.mail.port=${GMAIL_SMTP_PORT}
spring.mail.username=${GMAIL_USERNAME}
spring.mail.password=${GMAIL_PASSWORD}

# VNPay
vnpay.tmn-code=8FHCECWU
vnpay.hash-secret=Y5CH2TNSOR0VLOJ9I2QPHNHYF1ZKS0M6
vnpay.api-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=${VNPAY_RETURN_URL}

# OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## 🚀 KHỞI ĐỘNG ỨNG DỤNG

### Prerequisites:

- Java 17+
- Maven 3.6+
- SQL Server
- Gmail account (cho email verification)

### Build & Run:

```bash
# Clone repository
git clone https://github.com/AHQPN/QLTTNgoaiNguBackend.git
cd QLTTNgoaiNguBackend

# Build
mvn clean install

# Run
mvn spring-boot:run
```

### Hoặc chạy JAR file:

```bash
java -jar target/QLTTNgoaiNgu-0.0.1-SNAPSHOT.jar
```

### Verify:

- API Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/v3/api-docs

---

## 📚 ADDITIONAL RESOURCES

### Documentation Files:

- **FORGOT_PASSWORD_GUIDE.md** - Hướng dẫn chi tiết về chức năng quên mật khẩu
- **HELP.md** - Hướng dẫn chung về dự án
- **TestCases.csv** - Test cases cho hệ thống

### Technologies Used:

- **Spring Boot 3.5.6** - Framework chính
- **Spring Security** - Xác thực và phân quyền
- **JWT (jjwt 0.11.5)** - JSON Web Token
- **Spring Data JPA** - ORM
- **Liquibase** - Database migration
- **MapStruct 1.6.3** - Object mapping
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **JavaMailSender** - Email service
- **Lombok** - Boilerplate code reduction
- **Microsoft SQL Server** - Database

---

## 🤝 SUPPORT & CONTACT

### Issues & Bugs:

Nếu gặp vấn đề, vui lòng tạo issue trên GitHub:
- Repository: https://github.com/AHQPN/QLTTNgoaiNguBackend

### Contact:

- Email: support@qlttngoaingu.com
- GitHub: [@AHQPN](https://github.com/AHQPN)

---

## 📝 VERSION HISTORY

### Version 1.0.0 (Current)
- ✅ Authentication & Authorization with JWT
- ✅ User Management (Student, Teacher, Admin)
- ✅ Course & Class Management
- ✅ Payment Integration (VNPay)
- ✅ Email Verification
- ✅ Password Reset Feature
- ✅ Complete API Documentation with Swagger
- ✅ Module & Category Management
- ✅ Room & Lecturer Management
- ✅ File Upload Feature

---

## 📜 LICENSE

Copyright © 2025 QLTTNGOAINGU Team. All rights reserved.

Licensed under the Apache License, Version 2.0

---

**🎉 Happy Coding! Chúc bạn phát triển thành công!**
