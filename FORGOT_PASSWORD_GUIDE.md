# CHỨC NĂNG QUÊN MẬT KHẨU - TÀI LIỆU HƯỚNG DẪN

## 📁 CÁC FILE ĐÃ TẠO/CẬP NHẬT

### 1. **Files mới được tạo:**
- `ForgotPasswordRequest.java` - DTO cho request quên mật khẩu
- `ResetPasswordRequest.java` - DTO cho request đặt lại mật khẩu

### 2. **Files đã cập nhật:**
- `AuthController.java` - Thêm 3 endpoints mới
- `UserService.java` - Thêm 2 methods xử lý logic
- `ErrorCode.java` - Thêm error code `PASSWORD_NOT_MATCH`

---

## 🔌 CÁC API ENDPOINTS

### 1. **POST `/auth/forgot-password`** - Yêu cầu reset mật khẩu

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Password reset email has been sent. Please check your inbox."
}
```

**Mô tả:** User nhập email để nhận link reset password. Hệ thống sẽ gửi email chứa link reset password.

---

### 2. **GET `/auth/verify-reset-code?code={code}`** - Kiểm tra mã reset có hợp lệ

**Query Parameters:**
- `code`: Mã xác minh từ email

**Response (Success):**
```json
{
  "code": 1000,
  "message": "Valid reset code",
  "data": {
    "email": "user@example.com"
  }
}
```

**Response (Error - Code hết hạn):**
```json
{
  "code": 11005,
  "message": "Expired verification code"
}
```

**Response (Error - Code không hợp lệ):**
```json
{
  "code": 12006,
  "message": "Invalid verification code"
}
```

**Mô tả:** Endpoint này dùng để kiểm tra code có hợp lệ trước khi cho user nhập password mới (Optional - Frontend có thể bỏ qua bước này).

---

### 3. **POST `/auth/reset-password`** - Đặt lại mật khẩu mới

**Request Body:**
```json
{
  "code": "uuid-verification-code",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**Response (Success):**
```json
{
  "code": 1000,
  "message": "Password has been reset successfully. Please login with your new password."
}
```

**Response (Error - Mật khẩu không khớp):**
```json
{
  "code": 12007,
  "message": "Password and confirm password do not match"
}
```

**Mô tả:** User nhập mật khẩu mới và xác nhận mật khẩu. Hệ thống sẽ cập nhật mật khẩu mới cho user.

---

## 🔄 LUỒNG CHẠY CHI TIẾT

### **Bước 1: User quên mật khẩu**

```
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │ 1. User nhập email và click "Forgot Password"
       │ POST /auth/forgot-password
       │ { "email": "user@example.com" }
       ▼
┌──────────────────────────────────────────────────┐
│              AuthController                       │
│  @PostMapping("/forgot-password")                │
└──────┬───────────────────────────────────────────┘
       │ 2. Gọi userService.requestPasswordReset()
       ▼
┌──────────────────────────────────────────────────┐
│              UserService                          │
│  requestPasswordReset(email, siteUrl)            │
│                                                   │
│  • Tìm User theo email                           │
│    → Không tồn tại? throw USER_NOT_FOUND         │
│                                                   │
│  • Gọi generateNewVerificationCode()             │
│    → Kiểm tra có mã còn hạn?                     │
│       ✓ Có: Trả về mã cũ                         │
│       ✗ Không: Xóa mã cũ, tạo mã mới             │
│                                                   │
│    → Tạo mã mới:                                 │
│       - UUID random string                       │
│       - Type: PASSWORD_RESET                     │
│       - Expires: 2 phút (có thể tăng)            │
│       - Lưu vào DB                               │
│                                                   │
│  • Gọi sendVerificationEmail()                   │
│    → Tạo link: {siteUrl}/reset-password?code=... │
│    → Gửi email qua JavaMailSender                │
└──────┬───────────────────────────────────────────┘
       │ 3. Response
       ▼
┌──────────────────────────────────────────────────┐
│   Response: "Password reset email has been sent" │
└──────────────────────────────────────────────────┘
```

**Email được gửi:**
```html
Subject: Password Reset Request

You requested to reset your password.
Click below to continue:

[RESET PASSWORD] ← Link: http://localhost:3000/reset-password?code=uuid-code

This link will expire in 2 minutes.
If you did not request this, please ignore this email.

Thank you,
Ipower IELTS.
```

---

### **Bước 2: User click link trong email**

```
┌──────────────┐
│     Email    │
└──────┬───────┘
       │ 1. User click link: 
       │    {siteUrl}/reset-password?code={uuid-code}
       ▼
┌──────────────────────────────────────────────────┐
│         Frontend (Reset Password Page)            │
│                                                   │
│  • Parse code từ URL parameter                   │
│  • (Optional) Gọi GET /auth/verify-reset-code    │
│    để kiểm tra code có hợp lệ                    │
│                                                   │
│  • Nếu hợp lệ: Hiển thị form nhập password       │
│    [ New Password    : __________ ]              │
│    [ Confirm Password: __________ ]              │
│    [      Submit      ]                          │
│                                                   │
│  • Nếu không hợp lệ: Hiển thị lỗi                │
└──────────────────────────────────────────────────┘
```

**Optional - Verify code trước (Recommended):**
```
GET /auth/verify-reset-code?code={uuid-code}

Response (Success):
{
  "code": 1000,
  "message": "Valid reset code",
  "data": {
    "email": "user@example.com"  ← Có thể hiển thị email này cho user biết
  }
}

Response (Error - Hết hạn):
{
  "code": 11005,
  "message": "Expired verification code"
}
```

---

### **Bước 3: User nhập mật khẩu mới**

```
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │ 1. User nhập password mới và click "Reset"
       │ POST /auth/reset-password
       │ {
       │   "code": "uuid-code",
       │   "newPassword": "newpass123",
       │   "confirmPassword": "newpass123"
       │ }
       ▼
┌──────────────────────────────────────────────────┐
│              AuthController                       │
│  @PostMapping("/reset-password")                 │
└──────┬───────────────────────────────────────────┘
       │ 2. Gọi userService.resetPassword()
       ▼
┌──────────────────────────────────────────────────┐
│              UserService                          │
│  resetPassword(code, newPassword, confirmPassword)│
│                                                   │
│  ✓ STEP 1: Kiểm tra password khớp                │
│    if (newPassword != confirmPassword)           │
│       throw PASSWORD_NOT_MATCH                   │
│                                                   │
│  ✓ STEP 2: Tìm VerificationCode theo code        │
│    verificationCode = findByCode(code)           │
│    → Không tồn tại? throw INVALID_CODE           │
│                                                   │
│  ✓ STEP 3: Kiểm tra hết hạn                      │
│    if (expiresAt < now())                        │
│       • Xóa code khỏi DB                         │
│       • throw EXPIRED_VERIFICATION_CODE          │
│                                                   │
│  ✓ STEP 4: Kiểm tra type                         │
│    if (type != PASSWORD_RESET)                   │
│       throw INVALID_CODE                         │
│                                                   │
│  ✓ STEP 5: Cập nhật password                     │
│    user = verificationCode.getUser()             │
│    user.setPasswordHash(                         │
│       passwordEncoder.encode(newPassword)        │
│    )                                             │
│    userRepository.save(user)                     │
│                                                   │
│  ✓ STEP 6: Xóa VerificationCode                  │
│    verificationCodeRepository.delete(code)       │
│                                                   │
│  ✓ STEP 7: Đăng xuất khỏi tất cả thiết bị        │
│    (Xóa tất cả RefreshToken của user)            │
│    → Bảo mật: User phải đăng nhập lại            │
│                                                   │
└──────┬───────────────────────────────────────────┘
       │ 3. Response
       ▼
┌──────────────────────────────────────────────────┐
│   Response: "Password has been reset successfully│
│   Please login with your new password."          │
└──────┬───────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────┐
│         Frontend                                  │
│  • Hiển thị thông báo thành công                 │
│  • Redirect user đến trang login                 │
│  • User đăng nhập với password mới                │
└──────────────────────────────────────────────────┘
```

---

## 🔒 BẢO MẬT

### 1. **Thời gian hết hạn của mã**
- **Hiện tại:** 2 phút (có thể tăng lên 15-30 phút)
- **Cấu hình tại:** `UserService.generateNewVerificationCode()` - dòng 123
  ```java
  verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(2));
  ```

### 2. **Mỗi user chỉ có 1 mã PASSWORD_RESET hợp lệ**
- Khi tạo mã mới, mã cũ (nếu có) sẽ bị xóa
- Ngăn chặn spam request reset password

### 3. **Mã chỉ sử dụng được 1 lần**
- Sau khi reset password thành công, mã sẽ bị xóa ngay lập tức
- Không thể tái sử dụng mã đã dùng

### 4. **Đăng xuất khỏi tất cả thiết bị**
- Sau khi đổi password, tất cả RefreshToken sẽ bị xóa
- User phải đăng nhập lại trên tất cả thiết bị
- Mục đích: Bảo vệ tài khoản nếu bị hack

### 5. **Password được hash**
- Password mới được hash bằng BCrypt trước khi lưu
- Không lưu plain text password

### 6. **Kiểm tra type của mã**
- Mã PASSWORD_RESET không thể dùng cho mục đích khác
- Ngăn chặn lạm dụng mã

---

## 📊 BẢNG TRẠNG THÁI CODE

| Thời điểm | Trạng thái Code | Hành động |
|-----------|----------------|-----------|
| User request forgot password | Tạo mới hoặc trả về code cũ (nếu còn hạn) | Gửi email |
| User chưa click link | Code còn hạn trong DB | Đợi user click |
| User click link sau > 2 phút | Code hết hạn | Báo lỗi `EXPIRED_VERIFICATION_CODE` |
| User click link trong 2 phút | Code hợp lệ | Cho phép nhập password mới |
| User submit password mới | Code bị xóa | Reset password thành công |
| User cố gắng dùng lại code | Code không tồn tại | Báo lỗi `INVALID_CODE` |

---

## 🎯 ERROR CODES

| Code | Message | Giải thích |
|------|---------|-----------|
| 1002 | User not found | Email không tồn tại trong hệ thống |
| 12006 | Invalid verification code | Mã xác minh không hợp lệ hoặc đã được sử dụng |
| 11005 | Expired verification code | Mã xác minh đã hết hạn (> 2 phút) |
| 12007 | Password and confirm password do not match | Mật khẩu mới và xác nhận mật khẩu không khớp |
| 12003 | Password must be at least 6 characters | Mật khẩu quá ngắn (< 6 ký tự) |
| 1007 | Failed to send verification email | Lỗi khi gửi email (SMTP error) |

---

## 🧪 TEST CASES

### **Test Case 1: Quên mật khẩu thành công**
```
1. POST /auth/forgot-password
   Body: { "email": "existing@example.com" }
   Expected: 200 OK, "Password reset email has been sent"

2. Check email → Click link (trong 2 phút)

3. POST /auth/reset-password
   Body: {
     "code": "valid-code",
     "newPassword": "newpass123",
     "confirmPassword": "newpass123"
   }
   Expected: 200 OK, "Password has been reset successfully"

4. POST /auth/login
   Body: {
     "identifier": "existing@example.com",
     "password": "newpass123"
   }
   Expected: 200 OK, Login thành công
```

### **Test Case 2: Email không tồn tại**
```
POST /auth/forgot-password
Body: { "email": "notexist@example.com" }
Expected: 404 NOT_FOUND, "User not found"
```

### **Test Case 3: Code hết hạn**
```
1. POST /auth/forgot-password
   Body: { "email": "user@example.com" }
   
2. Đợi > 2 phút

3. POST /auth/reset-password
   Body: { "code": "expired-code", ... }
   Expected: 410 GONE, "Expired verification code"
```

### **Test Case 4: Password không khớp**
```
POST /auth/reset-password
Body: {
  "code": "valid-code",
  "newPassword": "newpass123",
  "confirmPassword": "different123"
}
Expected: 400 BAD_REQUEST, "Password and confirm password do not match"
```

### **Test Case 5: Dùng code 2 lần**
```
1. POST /auth/reset-password (lần 1)
   Expected: 200 OK

2. POST /auth/reset-password (lần 2 với cùng code)
   Expected: 400 BAD_REQUEST, "Invalid verification code"
```

---

## 📝 CONFIGURATION

### **Tăng thời gian hết hạn mã (recommended: 15-30 phút)**

**File:** `UserService.java` - Line 123

**Hiện tại:**
```java
verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(2));
```

**Đề xuất:**
```java
verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(15));
// Hoặc
verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(30));
```

### **Cấu hình SMTP Email (nếu chưa có)**

**File:** `application.properties`

```properties
# SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Site URL (Frontend URL)
APP_SITE_URL=http://localhost:3000
```

**Lưu ý:** Với Gmail, bạn cần tạo "App Password" thay vì dùng password thường.

---

## 🚀 FRONTEND INTEGRATION

### **1. Trang Forgot Password (`/forgot-password`)**
```javascript
const handleForgotPassword = async (email) => {
  try {
    const response = await fetch('http://localhost:8080/auth/forgot-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    
    const data = await response.json();
    
    if (data.code === 1000) {
      alert('Email đã được gửi! Vui lòng kiểm tra hộp thư.');
    } else {
      alert(data.message);
    }
  } catch (error) {
    console.error('Error:', error);
  }
};
```

### **2. Trang Reset Password (`/reset-password?code=xxx`)**
```javascript
const handleResetPassword = async (code, newPassword, confirmPassword) => {
  try {
    // Optional: Verify code trước
    const verifyResponse = await fetch(
      `http://localhost:8080/auth/verify-reset-code?code=${code}`
    );
    const verifyData = await verifyResponse.json();
    
    if (verifyData.code !== 1000) {
      alert('Mã xác minh không hợp lệ hoặc đã hết hạn!');
      return;
    }
    
    // Reset password
    const response = await fetch('http://localhost:8080/auth/reset-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, newPassword, confirmPassword })
    });
    
    const data = await response.json();
    
    if (data.code === 1000) {
      alert('Đặt lại mật khẩu thành công!');
      // Redirect to login
      window.location.href = '/login';
    } else {
      alert(data.message);
    }
  } catch (error) {
    console.error('Error:', error);
  }
};
```

---

## 💡 GỢI Ý CẢI TIẾN

### 1. **Rate Limiting**
Thêm giới hạn số lần request forgot password để tránh spam:
```java
// Giới hạn: 3 lần / 1 giờ cho mỗi email
@RateLimiter(key = "#email", limit = 3, duration = 3600)
public void requestPasswordReset(String email, String siteUrl) { ... }
```

### 2. **Logging**
Thêm log để tracking:
```java
log.info("Password reset requested for email: {}", email);
log.info("Password reset successful for user: {}", user.getUserId());
```

### 3. **Notification**
Gửi email thông báo khi password được thay đổi:
```java
// Sau khi reset password thành công
sendPasswordChangedNotification(user.getEmail());
```

### 4. **Two-Factor Authentication (2FA)**
Thêm OTP qua SMS trước khi cho phép reset password

### 5. **Security Questions**
Yêu cầu user trả lời câu hỏi bảo mật trước khi gửi email reset

---

## 📞 SUPPORT

Nếu có vấn đề, kiểm tra:

1. ✅ Đã cấu hình SMTP đúng chưa?
2. ✅ `APP_SITE_URL` có trỏ đúng frontend chưa?
3. ✅ Database có bảng `verification_code` chưa?
4. ✅ Enum `VerificationCodeEnum.PASSWORD_RESET` đã tồn tại chưa?
5. ✅ Email gửi đi có vào spam không?

---

**🎉 Chúc bạn triển khai thành công!**
