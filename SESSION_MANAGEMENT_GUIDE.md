# Session Management - Hủy và Thêm Buổi Học

## Tổng Quan

Tính năng quản lý buổi học cho phép:
1. **Hủy buổi học**: Đánh dấu buổi học với trạng thái "Đã hủy"
2. **Thêm buổi học**: Thêm buổi học mới vào lớp, nhưng chỉ được phép thêm số buổi bằng hoặc ít hơn số buổi đã hủy

## Quy Tắc Nghiệp Vụ

### Hủy Buổi Học
- Bất kỳ buổi học nào chưa bị hủy đều có thể bị hủy
- Buổi học đã bị hủy không thể hủy lại lần nữa
- Trạng thái sẽ được đổi thành "Đã hủy"

### Thêm Buổi Học
- **Điều kiện**: Chỉ được phép thêm buổi học mới khi đã có ít nhất 1 buổi học bị hủy
- **Giới hạn**: Số buổi học được thêm vào không được vượt quá số buổi học đã bị hủy
- **Công thức kiểm tra**:
  ```
  Số buổi ban đầu = (Số giờ học của khóa học * 60) / Số phút mỗi buổi học
  Số buổi đã thêm = Tổng số buổi hiện tại - Số buổi ban đầu
  Điều kiện: Số buổi đã thêm < Số buổi đã hủy
  ```

### Ví Dụ
- Khóa học có 30 giờ học, mỗi buổi 90 phút → 20 buổi học ban đầu
- Hủy 2 buổi học → Được phép thêm tối đa 2 buổi học mới
- Nếu đã thêm 1 buổi → Còn được phép thêm 1 buổi nữa
- Nếu đã thêm 2 buổi → Không được phép thêm nữa (trừ khi hủy thêm buổi khác)

## API Endpoints

### 1. Hủy Buổi Học

**Endpoint**: `DELETE /courseclasses/sessions/{sessionId}/cancel`

**Request**:
```http
DELETE /courseclasses/sessions/123/cancel
```

**Response** (200 OK):
```json
{
  "message": "Hủy buổi học thành công",
  "data": {
    "sessionId": 123,
    "date": "2024-01-15",
    "note": "Giảng viên bận",
    "status": "Đã hủy"
  }
}
```

**Error Response** (400 Bad Request):
```json
{
  "code": 1006,
  "message": "Invalid request"
}
```
*Lỗi này xảy ra khi buổi học đã bị hủy trước đó*

### 2. Thêm Buổi Học

**Endpoint**: `POST /courseclasses/{classId}/sessions`

**Request**:
```http
POST /courseclasses/1/sessions
Content-Type: application/json

{
  "sessionDate": "2024-02-20",
  "note": "Buổi học bù"
}
```

**Response** (200 OK):
```json
{
  "message": "Thêm buổi học thành công",
  "data": {
    "sessionId": 124,
    "date": "2024-02-20",
    "note": "Buổi học bù",
    "status": "Chưa học"
  }
}
```

**Error Responses**:

1. **Không có buổi nào bị hủy** (400 Bad Request):
```json
{
  "code": 1006,
  "message": "Invalid request"
}
```

2. **Đã thêm đủ số buổi được phép** (400 Bad Request):
```json
{
  "code": 1006,
  "message": "Invalid request"
}
```

## Luồng Xử Lý

### Cancel Session Flow
```
1. Client gửi DELETE request với sessionId
2. Service kiểm tra session có tồn tại không
3. Kiểm tra session đã bị hủy chưa
4. Cập nhật status = "Đã hủy"
5. Trả về thông tin session đã cập nhật
```

### Add Session Flow
```
1. Client gửi POST request với classId và session data
2. Service kiểm tra lớp học có tồn tại không
3. Lấy tất cả buổi học của lớp
4. Đếm số buổi đã hủy (canceledCount)
5. Tính số buổi ban đầu từ (giờ học * 60 / phút mỗi buổi)
6. Tính số buổi đã thêm = tổng số buổi - số buổi ban đầu
7. Kiểm tra: addedSessions < canceledCount
8. Nếu OK: Tạo session mới với status "Chưa học"
9. Trả về thông tin session mới
```

## Cấu Trúc Dữ Liệu

### Session Entity
```java
@Entity
@Table(name = "buoihoc")
public class Session {
    private Integer sessionId;
    private LocalDate sessionDate;
    private String status; // "Chưa học", "Đã học", "Đã hủy"
    private String note;
    private CourseClass courseClass;
}
```

### SessionCreateRequest DTO
```java
public class SessionCreateRequest {
    private LocalDate sessionDate;
    private String note;
}
```

## Database Queries

### Count Canceled Sessions
```java
@Query("SELECT COUNT(s) FROM Session s WHERE s.courseClass.classId = :classId AND s.status = 'Đã hủy'")
long countCanceledSessionsByClassId(@Param("classId") Integer classId);
```

### Find All Sessions by Class
```java
List<Session> findByCourseClass_ClassIdOrderBySessionDate(Integer classId);
```

## Testing

### Test Case 1: Hủy buổi học thành công
```bash
# Hủy buổi học có ID = 10
curl -X DELETE http://localhost:8080/courseclasses/sessions/10/cancel
```

### Test Case 2: Thêm buổi học thành công
```bash
# Sau khi đã hủy ít nhất 1 buổi
curl -X POST http://localhost:8080/courseclasses/1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "sessionDate": "2024-02-20",
    "note": "Buổi bù"
  }'
```

### Test Case 3: Thêm buổi khi chưa hủy buổi nào (should fail)
```bash
# Nếu chưa có buổi nào bị hủy → Lỗi 400
curl -X POST http://localhost:8080/courseclasses/1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "sessionDate": "2024-02-20",
    "note": "Buổi bù"
  }'
```

### Test Case 4: Thêm quá nhiều buổi (should fail)
```bash
# Ví dụ: Đã hủy 1 buổi, đã thêm 1 buổi
# Thử thêm buổi thứ 2 → Lỗi 400
curl -X POST http://localhost:8080/courseclasses/1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "sessionDate": "2024-02-21",
    "note": "Buổi bù 2"
  }'
```

## Implementation Details

### Files Modified
1. **SessionRepository.java**: Added `countCanceledSessionsByClassId` query
2. **CourseClassService.java**: Added `cancelSession` and `addSession` methods
3. **CourseClassController.java**: Added DELETE and POST endpoints
4. **SessionCreateRequest.java**: Created new DTO for session creation

### Key Methods

#### cancelSession()
```java
@Transactional
public ClassDetailResponse.SessionInfoDetail cancelSession(Integer sessionId)
```
- Tìm session theo ID
- Kiểm tra đã bị hủy chưa
- Cập nhật status = "Đã hủy"
- Trả về thông tin session

#### addSession()
```java
@Transactional
public ClassDetailResponse.SessionInfoDetail addSession(
    Integer classId, 
    SessionCreateRequest request)
```
- Tìm lớp học theo classId
- Lấy tất cả sessions của lớp
- Đếm số buổi đã hủy
- Tính số buổi ban đầu từ course.studyHours
- Validate: addedSessions < canceledCount
- Tạo session mới với status "Chưa học"
- Trả về thông tin session mới

## Business Rules Summary

| Hành Động | Điều Kiện | Kết Quả |
|-----------|-----------|---------|
| Hủy buổi học | Buổi học chưa bị hủy | Status = "Đã hủy" |
| Hủy buổi đã hủy | Buổi học đã bị hủy | Lỗi 400 |
| Thêm buổi học | Có ít nhất 1 buổi đã hủy | Tạo session mới |
| Thêm buổi học | Không có buổi nào bị hủy | Lỗi 400 |
| Thêm buổi học | Đã thêm đủ số buổi | Lỗi 400 |

## Notes

- Buổi học mới được tạo luôn có status = "Chưa học"
- SessionScheduler sẽ tự động cập nhật status thành "Đã học" sau 23:55 ngày học
- Không giới hạn số lần hủy buổi học (nhưng mỗi buổi chỉ hủy 1 lần)
- Số buổi ban đầu được tính động từ Course.studyHours và CourseClass.minutesPerSession
