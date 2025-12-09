-- ============================================================================
-- TÀI KHOẢN TEST - DANH SÁCH TÀI KHOẢN THEO VAI TRÒ
-- Database: QLTTNgoaiNgu
-- Ngày: 09/12/2025
-- ============================================================================
-- Mật khẩu mặc định tất cả tài khoản: Password123!
-- ============================================================================

USE [QLTTNgoaiNgu]
GO

-- ============================================================================
-- 1. TÀI KHOẢN ADMIN (3 tài khoản)
-- ============================================================================
SELECT 
    N'ADMIN' AS VaiTro,
    nd.manguoidung AS ID,
    nd.email AS Email,
    nd.sdt AS SoDienThoai,
    'Password123!' AS MatKhau,
    nd.daxacthuc AS DaXacThuc,
    nd.ngaytao AS NgayTao
FROM nguoidung nd
WHERE nd.vaitro = 'ADMIN';
GO

/*
| ID | Email                        | SĐT         | Mật khẩu      |
|----|------------------------------|-------------|---------------|
| 1  | admin@ipucenter.edu.vn       | 0901000001  | Password123!  |
| 2  | manager@ipucenter.edu.vn     | 0901000002  | Password123!  |
| 3  | staff@ipucenter.edu.vn       | 0901000003  | Password123!  |
*/

-- ============================================================================
-- 2. TÀI KHOẢN GIẢNG VIÊN (12 tài khoản)
-- ============================================================================
SELECT 
    N'TEACHER' AS VaiTro,
    gv.magv AS MaGV,
    gv.hoten AS HoTen,
    nd.email AS Email,
    nd.sdt AS SoDienThoai,
    'Password123!' AS MatKhau,
    -- Thông tin lớp đang dạy
    (SELECT COUNT(*) FROM lop WHERE magiangvien = gv.magv AND trangthai = 'InProgress') AS SoLopDangDay,
    (SELECT COUNT(*) FROM bangcap WHERE magv = gv.magv) AS SoBangCap
FROM giangvien gv
JOIN nguoidung nd ON gv.manguoidung = nd.manguoidung
ORDER BY gv.magv;
GO

/*
| MaGV | Họ Tên           | Email                                  | SĐT         | Số lớp đang dạy |
|------|------------------|----------------------------------------|-------------|-----------------|
| 1    | Nguyễn Văn Anh   | nguyenvana.teacher@ipucenter.edu.vn    | 0902000001  | 2               |
| 2    | Trần Thị Bích    | tranthib.teacher@ipucenter.edu.vn      | 0902000002  | 2               |
| 5    | Hoàng Văn Em     | hoangvane.teacher@ipucenter.edu.vn     | 0902000005  | 3               |
| ...  | ...              | ...                                    | ...         | ...             |
*/

-- ============================================================================
-- 3. TÀI KHOẢN HỌC VIÊN (50 tài khoản, hiển thị 10 đại diện)
-- ============================================================================
SELECT 
    N'STUDENT' AS VaiTro,
    hv.mahocvien AS MaHV,
    hv.hoten AS HoTen,
    nd.email AS Email,
    nd.sdt AS SoDienThoai,
    'Password123!' AS MatKhau,
    hv.trinhdo AS TrinhDo,
    -- Thông tin đăng ký
    (SELECT COUNT(*) 
     FROM chitiethoadon cthd 
     JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon 
     WHERE hd.mahocvien = hv.mahocvien AND hd.trangthai = 1) AS SoLopDaDangKy
FROM hocvien hv
JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
ORDER BY hv.mahocvien;
GO

/*
| MaHV | Họ Tên          | Email                      | SĐT         | Trình độ  | Số lớp đã ĐK |
|------|-----------------|----------------------------|-------------|-----------|--------------|
| 1    | Nguyễn Văn An   | hv.nguyenvana@gmail.com    | 0903000001  | Đại học   | 2            |
| 2    | Trần Thị Bình   | hv.tranthib@gmail.com      | 0903000002  | Đại học   | 2            |
| ...  | ...             | ...                        | ...         | ...       | ...          |
*/

-- ============================================================================
-- 4. TỔNG HỢP DỮ LIỆU THEO VAI TRÒ
-- ============================================================================
SELECT 
    VaiTro,
    SoLuong,
    CoDataHienThi
FROM (
    SELECT 'ADMIN' AS VaiTro, 
           COUNT(*) AS SoLuong,
           N'Có (Dashboard thống kê)' AS CoDataHienThi
    FROM nguoidung WHERE vaitro = 'ADMIN'
    UNION ALL
    SELECT 'TEACHER', 
           COUNT(*),
           N'Có (Lớp đang dạy, buổi học)'
    FROM giangvien
    UNION ALL
    SELECT 'STUDENT', 
           COUNT(*),
           N'Có (Lớp đã đăng ký, lịch học)'
    FROM hocvien
) summary;
GO

PRINT N'============================================'
PRINT N'TÀI KHOẢN TEST - TỔNG HỢP'
PRINT N'============================================'
PRINT N'Admin: 3 tài khoản (admin@ipucenter.edu.vn...)'
PRINT N'Teacher: 12 tài khoản (có lớp đang dạy)'
PRINT N'Student: 50 tài khoản (có lớp đã đăng ký)'
PRINT N'============================================'
PRINT N'MẬT KHẨU MẶC ĐỊNH: Password123!'
PRINT N'============================================'
GO
