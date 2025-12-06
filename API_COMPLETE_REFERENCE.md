# 📚 API DOCUMENTATION - COMPLETE REFERENCE
## HỆ THỐNG QUẢN LÝ TRUNG TÂM NGOẠI NGỮ

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 📑 MỤC LỤC

1. [Authentication & Authorization](#1-authentication--authorization-auth)
2. [Course Management](#2-course-management-courses)
3. [Class Management](#3-class-management-courseclasses)
4. [Student Management](#4-student-management-students)
5. [Lecturer Management](#5-lecturer-management-lecturers)
6. [User Management](#6-user-management-users)
7. [Module Management](#7-module-management-modules)
8. [Category Management](#8-category-management-categories)
9. [Payment & VNPay](#9-payment--vnpay-payment)
10. [Order Management](#10-order-management-orders)
11. [Room Management](#11-room-management-rooms)
12. [Skill Management](#12-skill-management-skills)
13. [File Upload](#13-file-upload-files)
14. [Schedule Suggestions](#14-schedule-suggestions-schedules)

---

## 1. AUTHENTICATION & AUTHORIZATION (`/auth`)

### 1.1. Đăng ký tài khoản học viên
```http
POST /auth/signup
Content-Type: application/json
```

**Request Body:**
```json
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

---

### 1.2. Đăng nhập
```http
POST /auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "identifier": "0123456789",
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

---

### 1.3. Làm mới Access Token
```http
POST /auth/refreshtoken
Content-Type: application/json
```

**Request Body:**
```json
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

### 1.4. Đăng xuất
```http
POST /auth/logout
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Logout successful"
}
```

---

### 1.5. Xác thực email
```http
GET /auth/verify?code={uuid-code}&type=EMAIL_VERIFICATION
```

**Response:** HTML page thông báo kết quả xác thực

---

### 1.6. Gửi lại mã xác thực
```http
POST /auth/resend?email={email}&type={EMAIL_VERIFICATION|PASSWORD_RESET}
```

**Response:**
```json
"Verification email sent successfully!"
```

---

### 1.7. Quên mật khẩu - Gửi email reset
```http
POST /auth/forgot-password
Content-Type: application/json
```

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

---

### 1.8. Đặt lại mật khẩu
```http
POST /auth/reset-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "code": "uuid-verification-code",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Password has been reset successfully. Please login with your new password."
}
```

---

### 1.9. Kiểm tra mã reset password
```http
GET /auth/verify-reset-code?code={uuid-code}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Valid reset code",
  "data": {
    "email": "user@example.com"
  }
}
```

---

## 2. COURSE MANAGEMENT (`/courses`)

### 2.1. Lấy tất cả khóa học đang hoạt động (grouped)
```http
GET /courses/activecourses
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "categoryId": 1,
      "categoryName": "IELTS",
      "courses": [
        {
          "courseId": 1,
          "courseName": "IELTS Foundation",
          "level": "Beginner",
          "tuitionFee": 5000000,
          "studyHours": 60,
          "numberOfSessions": 20,
          "image": "ielts-foundation.jpg"
        }
      ]
    }
  ]
}
```

---

### 2.2. Lấy tên các khóa học đang hoạt động
```http
GET /courses/activecourses-name
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "courseId": 1,
      "courseName": "IELTS Foundation"
    },
    {
      "courseId": 2,
      "courseName": "TOEIC Basic"
    }
  ]
}
```

---

### 2.3. Lấy danh sách khóa học (có phân trang)
```http
GET /courses?page=0&size=15
```

**Query Parameters:**
- `page` (int, default=0): Số trang
- `size` (int, default=15): Số items mỗi trang

**Response:**
```json
{
  "code": 1000,
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 4,
    "size": 15,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

---

### 2.4. Lấy chi tiết khóa học
```http
GET /courses/{id}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "courseId": 1,
    "courseName": "IELTS Foundation",
    "description": "Khóa học IELTS nền tảng cho người mới bắt đầu",
    "entryLevel": "Pre-A1",
    "targetLevel": "A2-B1",
    "category": "IELTS",
    "level": "Beginner",
    "tuitionFee": 5000000,
    "promotionPrice": 4500000,
    "studyHours": 60,
    "video": "https://youtube.com/watch?v=course-intro",
    "image": "ielts-foundation.jpg",
    "status": true,
    "objectives": [
      {
        "objectiveId": 1,
        "objectiveDescription": "Đạt IELTS 5.0+"
      },
      {
        "objectiveId": 2,
        "objectiveDescription": "Nắm vững ngữ pháp cơ bản"
      }
    ],
    "modules": [
      {
        "moduleId": 1,
        "moduleName": "Module 1: Listening Skills",
        "duration": 10,
        "contents": [
          {
            "contentId": 1,
            "contentDescription": "Introduction to IELTS Listening"
          },
          {
            "contentId": 2,
            "contentDescription": "Note-taking strategies"
          }
        ],
        "documents": [
          {
            "documentId": 1,
            "documentTitle": "Listening Guide",
            "documentUrl": "docs/listening-guide.pdf"
          },
          {
            "documentId": 2,
            "documentTitle": "Practice Tests",
            "documentUrl": "docs/practice-tests.pdf"
          }
        ]
      },
      {
        "moduleId": 2,
        "moduleName": "Module 2: Speaking Skills",
        "duration": 8,
        "contents": [...],
        "documents": [...]
      }
    ],
    "classInfos": [
      {
        "classId": 1,
        "className": "IELTS-F-01",
        "courseName": "IELTS Foundation",
        "roomName": "Phòng A1",
        "instructorName": "Nguyễn Văn A",
        "schedulePattern": "2-4-6",
        "startTime": "08:00",
        "endTime": "09:30",
        "startDate": "2025-01-15",
        "endDate": "2025-03-15",
        "status": "InProgress"
      }
    ]
  }
}
```

**Mô tả:**
- `promotionPrice`: Giá sau khi áp dụng khuyến mãi khóa học lẻ (Type 1)
- `modules`: Bao gồm nội dung và **tài liệu học tập (documents)** 
- `classInfos`: Danh sách lớp học đang mở cho khóa học này
- `video`: Link video giới thiệu khóa học
- `entryLevel`: Trình độ đầu vào yêu cầu
- `targetLevel`: Trình độ đầu ra mục tiêu

**💡 Combo Promotion:**
Nếu học viên đăng ký nhiều khóa học cùng lúc, sẽ được **giảm giá thêm** (Promotion Type 2 - Combo).
Chi tiết giảm giá combo được tính khi gọi API đăng ký khóa học (`POST /orders`).

---

### 2.5. Tạo khóa học mới
```http
POST /courses
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseName": "IELTS Foundation",
  "description": "Khóa học IELTS nền tảng cho người mới bắt đầu",
  "level": "Beginner",
  "tuitionFee": 5000000,
  "studyHours": 60,
  "numberOfSessions": 20,
  "image": "course-image.jpg",
  "categoryId": 1,
  "objectives": [
    {"objectiveDescription": "Đạt IELTS 5.0+"},
    {"objectiveDescription": "Nắm vững ngữ pháp cơ bản"}
  ],
  "modules": [
    {
      "moduleName": "Module 1: Listening",
      "duration": 10,
      "contents": [
        {"contentDescription": "Introduction to Listening"},
        {"contentDescription": "Practice Tests"}
      ],
      "documents": [
        {"documentTitle": "Listening Guide", "documentUrl": "listening.pdf"}
      ]
    }
  ],
  "skillIds": [1, 2, 3, 4]
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Tạo khóa học thành công"
}
```

---

### 2.6. Cập nhật khóa học
```http
PUT /courses/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:** (Tương tự như tạo khóa học)

**Response:**
```json
{
  "code": 1000,
  "message": "Hoàn tất chỉnh sửa"
}
```

---

### 2.7. Thay đổi trạng thái khóa học
```http
POST /courses/status/{id}
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Hoàn tất chỉnh sửa"
}
```

---

### 2.8. Lấy khóa học đề xuất
```http
GET /courses/recommedcousres/{id}
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "courseId": 2,
      "courseName": "IELTS Intermediate",
      "level": "Intermediate",
      "tuitionFee": 6000000
    }
  ]
}
```

---

### 2.9. Thêm mục tiêu cho khóa học
```http
POST /courses/{courseId}/objectives
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "objectiveDescription": "Đạt IELTS 6.5+"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Objective added successfully",
  "data": {
    "objectiveId": 5,
    "objectiveDescription": "Đạt IELTS 6.5+"
  }
}
```

---

### 2.10. Cập nhật mục tiêu
```http
PUT /courses/objectives/{objectiveId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "objectiveDescription": "Đạt IELTS 7.0+"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Objective updated successfully"
}
```

---

### 2.11. Xóa mục tiêu
```http
DELETE /courses/objectives/{objectiveId}
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Objective deleted successfully"
}
```

---

## 3. CLASS MANAGEMENT (`/courseclasses`)

### 3.1. Lấy chi tiết lớp học
```http
GET /courseclasses/{id}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "classId": 1,
    "className": "IELTS-F-01",
    "course": {
      "courseId": 1,
      "courseName": "IELTS Foundation"
    },
    "room": {
      "roomId": 1,
      "roomName": "Phòng A1"
    },
    "lecturer": {
      "lecturerId": 1,
      "fullName": "Nguyễn Văn A"
    },
    "startDate": "2025-01-15",
    "endDate": "2025-03-15",
    "startTime": "08:00",
    "endTime": "09:30",
    "schedulePattern": "2-4-6",
    "maxCapacity": 20,
    "currentEnrollment": 15,
    "status": true
  }
}
```

---

### 3.2. Lấy tất cả lớp học (có phân trang)
```http
GET /courseclasses?page=0&size=10
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "content": [...],
    "totalElements": 30,
    "totalPages": 3,
    "size": 10,
    "number": 0
  }
}
```

---

### 3.3. Tạo lớp học mới
```http
POST /courseclasses
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "className": "IELTS-F-02",
  "courseId": 1,
  "roomId": 2,
  "lecturerId": 3,
  "startDate": "2025-02-01",
  "startTime": "13:00",
  "durationMinutes": 90,
  "schedulePattern": "2-4-6",
  "maxCapacity": 20
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Course Class has been created",
  "data": {
    "classId": 5,
    "className": "IELTS-F-02",
    "startDate": "2025-02-01",
    "endDate": "2025-03-30"
  }
}
```

---

### 3.4. Cập nhật lớp học
```http
PUT /courseclasses/{classId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:** (Tương tự như tạo lớp học)

**Response:**
```json
{
  "code": 1000,
  "data": {
    "classId": 5,
    "className": "IELTS-F-02-Updated"
  }
}
```

---

### 3.5. Lọc lớp học
```http
GET /courseclasses/filter?lecturerId=1&roomId=2&courseId=3&className=IELTS&page=1&size=10
```

**Query Parameters:**
- `lecturerId` (int, optional): ID giảng viên
- `roomId` (int, optional): ID phòng học
- `courseId` (int, optional): ID khóa học
- `className` (string, optional): Tên lớp (tìm kiếm gần đúng)
- `page` (int, default=1): Số trang
- `size` (int, default=10): Số items mỗi trang

**Response:**
```json
{
  "currentPage": 1,
  "totalPages": 2,
  "totalItems": 15,
  "classes": [
    {
      "classId": 1,
      "className": "IELTS-F-01",
      "courseName": "IELTS Foundation",
      "lecturerName": "Nguyễn Văn A",
      "roomName": "Phòng A1",
      "startDate": "2025-01-15",
      "startTime": "08:00",
      "schedulePattern": "2-4-6"
    }
  ]
}
```

---

### 3.6. Lấy lịch học theo tuần
```http
GET /courseclasses/schedule-by-week?lecturerId=1&date=2025-01-15
```

**Query Parameters:**
- `lecturerId` (int, optional): ID giảng viên
- `roomId` (int, optional): ID phòng học
- `courseId` (int, optional): ID khóa học
- `date` (date, required): Ngày bất kỳ trong tuần (format: YYYY-MM-DD)

**Response:**
```json
{
  "code": 1000,
  "data": {
    "weekStart": "2025-01-13",
    "weekEnd": "2025-01-19",
    "schedule": {
      "MONDAY": [
        {
          "classId": 1,
          "className": "IELTS-F-01",
          "courseName": "IELTS Foundation",
          "startTime": "08:00",
          "endTime": "09:30",
          "roomName": "Phòng A1",
          "lecturerName": "Nguyễn Văn A"
        }
      ],
      "TUESDAY": [],
      "WEDNESDAY": [...],
      "THURSDAY": [...],
      "FRIDAY": [...],
      "SATURDAY": [],
      "SUNDAY": []
    }
  }
}
```

---

### 3.7. Lấy thông tin điểm danh buổi học
```http
GET /courseclasses/sessions/{sessionId}/attendance
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "sessionId": 1,
    "sessionDate": "2025-01-15",
    "sessionNumber": 1,
    "className": "IELTS-F-01",
    "attendanceRecords": [
      {
        "studentId": 1,
        "studentName": "Nguyễn Văn A",
        "status": "PRESENT",
        "note": ""
      }
    ]
  }
}
```

---

## 4. STUDENT MANAGEMENT (`/students`)

### 4.1. Lấy thông tin học viên
```http
GET /students
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "studentId": 1,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "phoneNumber": "0123456789",
    "address": "123 Đường ABC",
    "gender": "Nam",
    "dateOfBirth": "1990-01-01",
    "jobs": "Sinh viên",
    "image": "avatar.jpg"
  }
}
```

---

### 4.2. Cập nhật thông tin học viên
```http
PUT /students
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Nguyễn Văn A Updated",
  "address": "456 Đường XYZ",
  "gender": "Nam",
  "dateOfBirth": "1990-01-01",
  "jobs": "Kỹ sư",
  "image": "new-avatar.jpg"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Cập nhật thành công"
}
```

---

### 4.3. Lấy lịch học theo tuần của học viên
```http
GET /students/schedule-by-week?date=2025-01-15
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "weekStart": "2025-01-13",
    "weekEnd": "2025-01-19",
    "schedule": {
      "MONDAY": [
        {
          "classId": 1,
          "className": "IELTS-F-01",
          "courseName": "IELTS Foundation",
          "startTime": "08:00",
          "endTime": "09:30",
          "roomName": "Phòng A1",
          "lecturerName": "Nguyễn Văn A"
        }
      ]
    }
  }
}
```

---

### 4.4. Lấy danh sách lớp đã đăng ký
```http
GET /students/get-classes-enrolled?page=1&size=10
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "currentPage": 1,
    "totalPages": 2,
    "totalItems": 15,
    "classes": [
      {
        "classId": 1,
        "className": "IELTS-F-01",
        "courseName": "IELTS Foundation",
        "startDate": "2025-01-15",
        "endDate": "2025-03-15",
        "status": "ONGOING"
      }
    ]
  }
}
```

---

## 5. LECTURER MANAGEMENT (`/lecturers`)

### 5.1. Lấy giảng viên khả dụng
```http
POST /lecturers/available
Content-Type: application/json
```

**Request Body:**
```json
{
  "schedulePattern": "2-4-6",
  "startTime": "08:00",
  "durationMinutes": 90,
  "startDate": "2025-01-15"
}
```

**Response:**
```json
[
  {
    "lecturerId": 1,
    "fullName": "Nguyễn Văn A",
    "email": "lecturerA@example.com",
    "phoneNumber": "0987654321"
  },
  {
    "lecturerId": 2,
    "fullName": "Trần Thị B",
    "email": "lecturerB@example.com",
    "phoneNumber": "0987654322"
  }
]
```

---

### 5.2. Lấy danh sách tất cả giảng viên
```http
GET /lecturers/lecturer-name
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "lecturerId": 1,
      "fullName": "Nguyễn Văn A"
    },
    {
      "lecturerId": 2,
      "fullName": "Trần Thị B"
    }
  ]
}
```

---

### 5.3. Lấy thông tin giảng viên
```http
GET /lecturers/{id}
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "lecturerId": 1,
    "fullName": "Nguyễn Văn A",
    "email": "lecturerA@example.com",
    "phoneNumber": "0987654321",
    "specialization": "IELTS Teaching",
    "experience": 5
  }
}
```

---

### 5.4. Lấy thông tin giảng viên hiện tại
```http
GET /lecturers/me
Authorization: Bearer {accessToken}
```

**Response:** (Tương tự 5.3)

---

### 5.5. Điểm danh buổi học
```http
POST /lecturers/sessions/{sessionId}/attendance
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "sessionId": 1,
  "attendanceRecords": [
    {
      "studentId": 1,
      "status": "PRESENT",
      "note": ""
    },
    {
      "studentId": 2,
      "status": "ABSENT",
      "note": "Nghỉ ốm"
    },
    {
      "studentId": 3,
      "status": "LATE",
      "note": "Đến muộn 15 phút"
    }
  ]
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "sessionId": 1,
    "totalStudents": 3,
    "presentCount": 1,
    "absentCount": 1,
    "lateCount": 1
  }
}
```

---

## 6. USER MANAGEMENT (`/users`)

### 6.1. Tạo user mới (Admin only)
```http
POST /users
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "phoneNumber": "0123456789",
  "role": "STUDENT"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "User has been created"
}
```

---

### 6.2. Thêm giảng viên
```http
POST /users/add-lecturer
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "Nguyễn Văn C",
  "email": "lecturerC@example.com",
  "phoneNumber": "0987654323",
  "specialization": "TOEIC Teaching",
  "experience": 3
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "User has been created"
}
```

---

### 6.3. Lấy thông tin học viên hiện tại
```http
GET /users/student-info
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "studentId": 1,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com"
  }
}
```

---

### 6.4. Lấy tên và email
```http
GET /users/name-email
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com"
  }
}
```

---

## 7. MODULE MANAGEMENT (`/modules`)

### 7.1. Lấy modules theo khóa học
```http
GET /modules?courseId=1
```

**Response:**
```json
[
  {
    "moduleId": 1,
    "moduleName": "Module 1: Listening",
    "duration": 10,
    "contents": [
      {
        "contentId": 1,
        "contentDescription": "Introduction to Listening"
      }
    ],
    "documents": [
      {
        "documentId": 1,
        "documentTitle": "Listening Guide",
        "documentUrl": "listening.pdf"
      }
    ]
  }
]
```

---

### 7.2. Thêm module
```http
POST /modules/{courseId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "moduleName": "Module 2: Speaking",
  "duration": 8,
  "contents": [
    {"contentDescription": "Speaking basics"},
    {"contentDescription": "Practice exercises"}
  ],
  "documents": [
    {"documentTitle": "Speaking Guide", "documentUrl": "speaking.pdf"}
  ]
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Successfully added Module"
}
```

---

### 7.3. Cập nhật module
```http
PUT /modules/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "moduleName": "Module 2: Speaking (Updated)",
  "duration": 10
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Module updated successfully"
}
```

---

### 7.4. Xóa module
```http
DELETE /modules/{id}
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Delete course successfully"
}
```

---

## 8. CATEGORY MANAGEMENT (`/categories`)

### 8.1. Tạo danh mục
```http
POST /categories
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "categoryName": "TOEFL",
  "description": "Các khóa học TOEFL"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "categoryId": 3,
    "categoryName": "TOEFL",
    "description": "Các khóa học TOEFL"
  }
}
```

---

### 8.2. Cập nhật danh mục
```http
PUT /categories/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "categoryName": "TOEFL iBT",
  "description": "Các khóa học TOEFL iBT"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "categoryId": 3,
    "categoryName": "TOEFL iBT",
    "description": "Các khóa học TOEFL iBT"
  }
}
```

---

### 8.3. Lấy tất cả danh mục
```http
GET /categories
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "categoryId": 1,
      "categoryName": "IELTS",
      "description": "Các khóa học IELTS"
    },
    {
      "categoryId": 2,
      "categoryName": "TOEIC",
      "description": "Các khóa học TOEIC"
    }
  ]
}
```

---

### 8.4. Lấy chi tiết danh mục
```http
GET /categories/{id}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "categoryId": 1,
    "categoryName": "IELTS",
    "description": "Các khóa học IELTS",
    "courseCount": 5
  }
}
```

---

## 9. PAYMENT & VNPAY (`/payment`)

### 9.1. Tạo thanh toán VNPay
```http
POST /payment/create
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": "5000000",
  "orderInfo": "Thanh toán học phí khóa IELTS Foundation",
  "invoiceId": 123
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "payUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=500000000&vnp_Command=pay&..."
  }
}
```

**Sử dụng:** Redirect user đến `payUrl` để thanh toán

---

### 9.2. VNPay Return URL (Callback)
```http
GET /payment/vnpay-return?vnp_Amount=500000000&vnp_BankCode=NCB&vnp_ResponseCode=00&...
```

**Mô tả:** 
- Endpoint này được VNPay gọi tự động sau khi user thanh toán
- Backend xử lý kết quả và redirect về frontend
- Cập nhật trạng thái hóa đơn nếu thanh toán thành công

**Response:** Redirect đến frontend với query params:

**Thành công:**
```
{FRONTEND_URL}/payment/result?status=success&invoiceId=123&transactionNo=14023873&amount=5000000&responseCode=00
```

**Thất bại:**
```
{FRONTEND_URL}/payment/result?status=failed&invoiceId=123&error=Giao dịch thất bại&responseCode=24
```

---

### VNPay Response Codes

| Code | Meaning |
|------|---------|
| 00 | Giao dịch thành công |
| 07 | Giao dịch bị nghi ngờ gian lận |
| 09 | Thẻ chưa đăng ký InternetBanking |
| 10 | Xác thực thông tin thất bại quá số lần quy định |
| 11 | Đã hết hạn chờ thanh toán |
| 12 | Thẻ bị khóa |
| 13 | Sai mật khẩu xác thực giao dịch (OTP) |
| 24 | Giao dịch bị hủy |
| 51 | Tài khoản không đủ số dư |
| 65 | Vượt quá hạn mức thanh toán trong ngày |
| 75 | Ngân hàng thanh toán đang bảo trì |
| 79 | Nhập sai mật khẩu quá số lần quy định |

---

## 10. ORDER MANAGEMENT (`/orders`)

### 10.1. Đăng ký khóa học (Tạo đơn hàng)
```http
POST /orders
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "studentId": 1,
  "classIds": [1, 2, 3],
  "discountCode": "SUMMER2025",
  "paymentMethod": "VNPAY"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "invoiceId": 123,
    "studentId": 1,
    "studentName": "Nguyễn Văn A",
    "totalOriginalPrice": 15000000,
    "courseDiscountPercent": 10,
    "comboDiscountPercent": 5,
    "returningStudentDiscountPercent": 3,
    "totalDiscountPercent": 18,
    "courseDiscountAmount": 1500000,
    "comboDiscountAmount": 750000,
    "returningStudentDiscountAmount": 450000,
    "totalDiscountAmount": 2700000,
    "totalAmount": 12300000,
    "status": false,
    "createdAt": "2025-01-15T10:30:00",
    "courses": [
      {
        "classId": 1,
        "className": "IELTS-F-01",
        "courseName": "IELTS Foundation",
        "tuitionFee": 5000000,
        "finalAmount": 4100000
      },
      {
        "classId": 2,
        "className": "TOEIC-B-01",
        "courseName": "TOEIC Basic",
        "tuitionFee": 5000000,
        "finalAmount": 4100000
      },
      {
        "classId": 3,
        "className": "CONV-01",
        "courseName": "English Conversation",
        "tuitionFee": 5000000,
        "finalAmount": 4100000
      }
    ]
  }
}
```

**Mô tả chi tiết giảm giá:**

Hệ thống áp dụng **3 loại khuyến mãi** có thể cộng dồn:

1. **Khuyến mãi khóa học lẻ (Type 1):**
   - Giảm giá cho từng khóa học riêng lẻ
   - Ví dụ: Khóa IELTS giảm 10%

2. **Khuyến mãi Combo (Type 2):** ⭐
   - Giảm giá khi đăng ký **nhiều khóa học cùng lúc**
   - Ví dụ: Mua 3 khóa cùng lúc được giảm thêm 5%
   - Chỉ áp dụng khi tất cả các khóa trong combo đều được chọn

3. **Khuyến mãi Học viên cũ (Type 3):**
   - Giảm giá cho học viên đã từng học tại trung tâm
   - Ví dụ: Học viên cũ được giảm thêm 3%

**Công thức tính:**
```
Tổng % giảm = Type1% + Type2% + Type3% (tối đa 100%)
Tiền giảm mỗi khóa = Học phí × (Tổng % / 100)
Thành tiền = Học phí - Tiền giảm
```

**Lưu ý:**
- Mỗi loại khuyến mãi có thể có nhiều chương trình cộng dồn
- Tổng % giảm tối đa là 100% (không thể âm)
- Response trả về chi tiết từng loại giảm giá để hiển thị cho khách hàng

---

## 11. ROOM MANAGEMENT (`/rooms`)

### 11.1. Lấy phòng khả dụng
```http
POST /rooms/available
Content-Type: application/json
```

**Request Body:**
```json
{
  "schedulePattern": "2-4-6",
  "startTime": "08:00",
  "durationMinutes": 90,
  "startDate": "2025-01-15"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "roomId": 1,
      "roomName": "Phòng A1",
      "capacity": 25,
      "status": "Available"
    },
    {
      "roomId": 2,
      "roomName": "Phòng B2",
      "capacity": 30,
      "status": "Available"
    }
  ]
}
```

**Mô tả:**
Entity Room có 4 thuộc tính chính:
- `roomId`: ID phòng (Primary Key)
- `roomName`: Tên phòng
- `capacity`: Sức chứa
- `status`: Trạng thái phòng

---

### 11.2. Lấy danh sách tất cả phòng
```http
GET /rooms/room-name
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "roomId": 1,
      "roomName": "Phòng A1"
    },
    {
      "roomId": 2,
      "roomName": "Phòng B2"
    }
  ]
### 11.3. Lấy chi tiết phòng
```http
GET /rooms/{id}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "roomId": 1,
    "roomName": "Phòng A1",
    "capacity": 25,
    "status": "Available"
  }
}
``` "hasProjector": true,
    "hasAirConditioner": true,
    "location": "Tầng 2"
  }
}
```

---

### 11.4. Tạo phòng mới
```http
POST /rooms
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "roomName": "Phòng C3",
  "capacity": 20
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "roomId": 5,
    "roomName": "Phòng C3",
    "capacity": 20,
    "status": "Available"
  }
}
```

**Mô tả:**
- Chỉ cần 2 trường: `roomName` và `capacity`
- `status` được hệ thống tự động khởi tạo
- `roomId` được tự động generate

---

## 12. SKILL MANAGEMENT (`/skills`)

### 12.1. Lấy tất cả kỹ năng
```http
GET /skills
```

**Response:**
```json
{
  "code": 1000,
  "data": [
    {
      "skillId": 1,
      "skillName": "Listening"
    },
    {
      "skillId": 2,
      "skillName": "Speaking"
    },
    {
      "skillId": 3,
      "skillName": "Reading"
    },
    {
      "skillId": 4,
      "skillName": "Writing"
    }
  ]
}
```

---

## 13. FILE UPLOAD (`/files`)

### 13.1. Upload file
```http
POST /files
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: File to upload

**Response:**
```json
{
  "code": 1000,
  "data": {
    "fileUrl": "550e8400-e29b-41d4-a716-446655440000_document.pdf"
  }
}
```

---

### 13.2. Lấy file
```http
GET /files/{fileName}
```

**Response:** File content (binary)

---

### 13.3. Xóa file
```http
DELETE /files/{fileName}
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "code": 1000,
  "message": "File deleted successfully"
}
```

---

## 14. SCHEDULE SUGGESTIONS (`/schedules`)

### 14.1. Kiểm tra và gợi ý lịch học
```http
POST /schedules/check-and-suggest
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": 1,
  "startDate": "2025-01-15",
  "startTime": "08:00",
  "durationMinutes": 90,
  "schedulePattern": "2-4-6",
  "preferredRoomId": 5,
  "preferredLecturerId": 3
}
```

**Response khi AVAILABLE:**
```json
{
  "status": "AVAILABLE",
  "message": "Lịch học khả dụng! Vui lòng chọn phòng và giảng viên.",
  "initialCheck": {
    "hasAvailableRooms": true,
    "hasAvailableLecturers": true,
    "availableRoomCount": 5,
    "availableLecturerCount": 3
  },
  "availableRooms": [
    {
      "roomId": 1,
      "roomName": "Phòng A1",
      "capacity": 25
    }
  ],
  "availableLecturers": [
    {
      "lecturerId": 1,
      "fullName": "Nguyễn Văn A"
    }
  ]
}
```

**Response khi CONFLICT:**
```json
{
  "status": "CONFLICT",
  "message": "Lịch học bị xung đột. Dưới đây là các gợi ý thay thế:",
  "initialCheck": {
    "hasAvailableRooms": false,
    "hasAvailableLecturers": true,
    "roomConflicts": [
      {
        "type": "ROOM_CONFLICT",
        "description": "Phòng 'A1' bị trùng với lớp 'IELTS-F-01' vào T2, T4, T6 từ 08:00-09:30"
      }
    ]
  },
  "alternatives": [
    {
      "type": "ALTERNATIVE_TIME",
      "reason": "Đổi giờ từ 08:00 sang 13:00",
      "priority": 115,
      "startDate": "2025-01-15",
      "startTime": "13:00",
      "endTime": "14:30",
      "schedulePattern": "2-4-6",
      "availableRooms": [
        {"roomId": 1, "roomName": "Phòng A1"}
      ],
      "availableLecturers": [
        {"lecturerId": 2, "fullName": "Trần Thị B"}
      ]
    },
    {
      "type": "ALTERNATIVE_ROOM",
      "reason": "Đổi phòng sang Phòng B2",
      "priority": 95,
      "startDate": "2025-01-15",
      "startTime": "08:00",
      "endTime": "09:30",
      "schedulePattern": "2-4-6",
      "availableRooms": [
        {"roomId": 2, "roomName": "Phòng B2"}
      ],
      "availableLecturers": [
        {"lecturerId": 3, "fullName": "Lê Văn C"}
      ]
    },
    {
      "type": "ALTERNATIVE_DATE",
      "reason": "Bắt đầu từ ngày 2025-01-22",
      "priority": 80,
      "startDate": "2025-01-22",
      "startTime": "08:00",
      "endTime": "09:30",
      "schedulePattern": "2-4-6",
      "availableRooms": [
        {"roomId": 1, "roomName": "Phòng A1"}
      ],
      "availableLecturers": [
        {"lecturerId": 3, "fullName": "Lê Văn C"}
      ]
    },
    {
      "type": "ALTERNATIVE_PATTERN",
      "reason": "Đổi lịch sang T3, T5, T7",
      "priority": 70,
      "startDate": "2025-01-15",
      "startTime": "08:00",
      "endTime": "09:30",
      "schedulePattern": "3-5-7",
      "availableRooms": [
        {"roomId": 1, "roomName": "Phòng A1"}
      ],
      "availableLecturers": [
        {"lecturerId": 1, "fullName": "Nguyễn Văn A"}
      ]
    }
  ]
}
```

---

## 📊 COMMON RESPONSE STRUCTURE

Tất cả API responses đều có cấu trúc:

```json
{
  "code": 1000,
  "message": "Success message",
  "data": {
    // Response data
  }
}
```

---

## ⚠️ ERROR CODES REFERENCE

### Authentication Errors
| Code | HTTP Status | Message |
|------|-------------|---------|
| 1002 | 404 | User not found |
| 1003 | 403 | User not verified |
| 1005 | 400 | Phone number or email already exists |
| 10000 | 401 | Refresh token not found |
| 10001 | 401 | Refresh token expired |
| 10002 | 401 | Refresh token revoked |
| 11000 | 401 | Invalid authentication credentials |
| 11004 | 401 | Unauthenticated |
| 11005 | 410 | Expired verification code |

### Validation Errors
| Code | HTTP Status | Message |
|------|-------------|---------|
| 12001 | 400 | Invalid email |
| 12002 | 400 | Invalid phone number |
| 12003 | 400 | Password must be at least 6 characters |
| 12006 | 400 | Invalid verification code |
| 12007 | 400 | Password and confirm password do not match |

### Business Logic Errors
| Code | HTTP Status | Message |
|------|-------------|---------|
| 2000 | 404 | Course not found |
| 2005 | 404 | Class not found |
| 8000 | 404 | Payment not found |
| 8001 | 400 | Payment failed |

---

## 🔐 AUTHENTICATION

### Using Bearer Token

Thêm token vào header của mỗi request:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Expiration

- **Access Token:** 24 giờ
- **Refresh Token:** 7 ngày

### Refresh Token Flow

1. Access token hết hạn → API trả về 401
2. Gọi `POST /auth/refreshtoken` với refresh token
3. Nhận access token mới
4. Retry request với token mới

---

## 📋 PAGINATION

Các endpoint có phân trang sử dụng query parameters:

- `page`: Số trang (bắt đầu từ 0)
- `size`: Số items mỗi trang

Response có format:

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## 🔗 USEFUL LINKS

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health

---

## 📞 SUPPORT

Nếu cần hỗ trợ, vui lòng liên hệ:
- Email: support@qlttngoaingu.com
- GitHub: https://github.com/AHQPN/QLTTNgoaiNguBackend

---

**Last Updated:** December 6, 2025  
**Version:** 1.0.0
