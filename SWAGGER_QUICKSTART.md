# 🚀 QUICK START GUIDE - API DOCUMENTATION

## Truy cập Swagger UI trong 3 bước

### Bước 1: Khởi động ứng dụng

```bash
mvn spring-boot:run
```

### Bước 2: Mở trình duyệt

Truy cập: **http://localhost:8080/swagger-ui.html**

### Bước 3: Bắt đầu test API!

---

## 🔥 Test API nhanh (không cần đăng nhập)

### 1. Xem danh sách khóa học

- Tìm: `GET /courses/activecourses`
- Click **"Try it out"**
- Click **"Execute"**
- ✅ Xem kết quả!

### 2. Xem chi tiết khóa học

- Tìm: `GET /courses/{id}`
- Click **"Try it out"**
- Nhập `id = 1`
- Click **"Execute"**
- ✅ Xem chi tiết!

---

## 🔐 Test API có Authentication

### Bước 1: Đăng ký tài khoản

1. Tìm endpoint: `POST /auth/signup`
2. Click **"Try it out"**
3. Nhập JSON:

```json
{
  "name": "Test User",
  "email": "test@example.com",
  "phoneNumber": "0987654321",
  "password": "123456",
  "address": "TP.HCM",
  "gender": "Nam",
  "ngaySinh": "1990-01-01",
  "job": "Developer"
}
```

4. Click **"Execute"**
5. ✅ Check email để xác thực!

### Bước 2: Xác thực email

- Click vào link trong email
- Hoặc gọi: `GET /auth/verify?code={code-from-email}&type=EMAIL_VERIFICATION`

### Bước 3: Đăng nhập

1. Tìm: `POST /auth/login`
2. Nhập:

```json
{
  "identifier": "0987654321",
  "password": "123456"
}
```

3. Click **"Execute"**
4. **Copy** `accessToken` từ response

### Bước 4: Thêm token vào Swagger

1. Click nút **🔓 Authorize** (góc trên bên phải)
2. Paste token vào ô "Value"
3. Click **"Authorize"**
4. Click **"Close"**

### Bước 5: Test API bất kỳ!

Giờ tất cả API đều có thể test được! 🎉

---

## 📝 Test chức năng Quên Mật Khẩu

### 1. Request reset password

```json
POST /auth/forgot-password
{
  "email": "test@example.com"
}
```

### 2. Check email và copy code

### 3. Reset password

```json
POST /auth/reset-password
{
  "code": "code-from-email",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

### 4. Login với password mới

```json
POST /auth/login
{
  "identifier": "test@example.com",
  "password": "newpassword123"
}
```

---

## 🎯 Các API quan trọng cần test

### Authentication
- ✅ POST /auth/signup
- ✅ POST /auth/login
- ✅ POST /auth/logout
- ✅ POST /auth/forgot-password
- ✅ POST /auth/reset-password

### Courses
- ✅ GET /courses/activecourses
- ✅ GET /courses/{id}
- ✅ POST /courses (Admin)
- ✅ PUT /courses/{id} (Admin)

### Users
- ✅ GET /users/me
- ✅ PUT /users/{id}

### Payment
- ✅ POST /payment/create-payment

---

## 💡 Tips

### Lỗi thường gặp:

1. **401 Unauthorized**: Chưa thêm token → Click "Authorize"
2. **403 Forbidden**: Không có quyền → Cần role ADMIN
3. **404 Not Found**: ID không tồn tại → Kiểm tra lại ID

### Shortcuts trong Swagger:

- `Ctrl + F`: Tìm kiếm endpoint
- Click vào tag để thu gọn/mở rộng
- Sử dụng "Models" bên dưới để xem schema

---

## 📚 Tài liệu chi tiết

Xem thêm:
- **API_DOCUMENTATION.md** - Tài liệu API đầy đủ
- **FORGOT_PASSWORD_GUIDE.md** - Hướng dẫn quên mật khẩu chi tiết

---

## 🎉 Done!

Bây giờ bạn đã sẵn sàng để:
- ✅ Xem tất cả API endpoints
- ✅ Test API trực tiếp
- ✅ Xem request/response examples
- ✅ Hiểu được cấu trúc dữ liệu
- ✅ Debug API dễ dàng

**Happy Testing! 🚀**
