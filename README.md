# QLTTNgoaiNgu Backend

Hệ thống quản lý trung tâm ngoại ngữ - Backend API

## Yêu cầu

- **Java:** JDK 21
- **Database:** SQL Server 2019+
- **Maven:** Được tích hợp qua Maven Wrapper

---

## Cấu hình quan trọng

### 1. Cấu hình Database

Mở file `src/main/resources/application.properties` và điều chỉnh:

```properties
# Thay đổi port 57013 thành port SQL Server của bạn
spring.datasource.url=jdbc:sqlserver://localhost:57013;databaseName=QLTTNgoaiNgu;encrypt=true;trustServerCertificate=true

# Thay đổi username nếu cần
spring.datasource.username=sa

# Password được truyền qua environment variable hoặc thay đổi trực tiếp
spring.datasource.password=${DATASOURCE_PASSWORD:your_password}
```

> ⚠️ **LƯU Ý:** Dòng `spring.datasource.password` sử dụng biến môi trường `DATASOURCE_PASSWORD`. Nếu không set biến này, giá trị mặc định `your_password` sẽ được dùng. **Bạn cần thay `your_password` thành mật khẩu thực của SQL Server.**

### 2. Cấu hình JWT Secret

```properties
# JWT Secret key - phải đủ dài (>= 256 bits cho HS256)
app.jwtSecret=${APP_JWT_SECRET:QLTTNgoaiNgu2024SecureJWTKeyForHS256AlgorithmMinimum256BitsLong!@#$}
```

> ⚠️ **LỖI THƯỜNG GẶP:** Khi merge code, JWT Secret có thể bị thay đổi hoặc quá ngắn gây lỗi `JWT64` hoặc `SignatureException`. Đảm bảo key có đủ độ dài.

### 3. Cấu hình VNPay (Sandbox)

```properties
# VNPay test environment - Thay bằng credentials thật khi deploy production
vnpay.tmn-code=8FHCECWU
vnpay.hash-secret=Y5CH2TNSOR0VLOJ9I2QPHNHYF1ZKS0M6
vnpay.return-url=${VNPAY_RETURN_URL:https://your-domain.com/orders/payment/vnpay-return}
```

---

## Chạy ứng dụng

### Cách chạy tiêu chuẩn (KHÔNG chạy Liquibase)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"; $env:DATASOURCE_PASSWORD = "123"; $env:LIQUIBASE_ENABLED = "false"; .\mvnw.cmd spring-boot:run
```

### Giải thích các biến môi trường:

| Biến | Mô tả | Giá trị mẫu |
| `LIQUIBASE_ENABLED` | Bật/tắt Liquibase migration | `false` (khuyến nghị tắt) |

> 💡 **Tại sao tắt Liquibase?** Nếu database đã có schema (từ file BACPAC hoặc script), Liquibase sẽ báo lỗi conflict. Tắt để tránh lỗi.

---

## Database Setup

### Khuyến nghị: Sử dụng file BACPAC

Để có dữ liệu mẫu đầy đủ, sử dụng file BACPAC được cung cấp:

1. Mở **SQL Server Management Studio (SSMS)**
2. Right-click **Databases** → **Import Data-tier Application...**
3. Chọn file `QLTTNgoaiNgu.bacpac`
4. Hoàn thành import

> File BACPAC chứa schema và dữ liệu mới nhất, đã được verify.

---

## Tài khoản Test

Sau khi import database, sử dụng các tài khoản sau để test:

| Vai trò | Email | Mật khẩu |
|---------|-------|----------|
| **Admin** | `admin@ipucenter.edu.vn` | `12345678` |
| **Teacher** | `nguyenvana.teacher@ipucenter.edu.vn` | `12345678` |
| **Student** | `hv.nguyenvana@gmail.com` | `12345678` |

---
