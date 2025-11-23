------------------------------------------------------------
-- 1. HOCVIEN
------------------------------------------------------------
INSERT INTO hocvien (manguoidung, hoten, ngaysinh, gioitinh, diachi, nghenghiep, trinhdo, anhdaidien)
VALUES
    (5, N'Nguyễn Ngọc Ánh', '2000-05-12', N'Nữ', N'113 Võ Thị Sáu TPHCM', N'Sinh viên', N'Đại học', N'hocvien1.png'),
    (6, N'Trần Minh Tuấn', '1999-11-20', N'Nam', N'96 Đồng Khởi TP.HCM', N'Nhân viên IT', N'Cao đẳng', N'hocvien2.png'),
    (7, N'Phạm Hoa Mai', '2001-02-02', N'Nữ', N'149 Lê Trọng Tấn Tây Thanh TPHCM', N'Sinh viên', N'Đại học', N'hocvien3.png');


------------------------------------------------------------
-- 2. LOAIKHUYENMAI
------------------------------------------------------------
INSERT INTO loaikhuyenmai (ten, mota)
VALUES
    (N'Giảm % cho khóa học', N'Giảm cho các khóa học riêng lẻ'),
    (N'Giảm theo combo', N'Giảm cho các khóa học khi mua theo combo'),
    (N'Giảm cho học viên cũ', N'Giảm cho học viên cũ đã đăng kí học tại trung tâm');


------------------------------------------------------------
-- 3. KHUYENMAI  (đã có cột trangthai + mota)
------------------------------------------------------------
INSERT INTO khuyenmai (ten, phantramgiam, ngaybatdau, ngayketthuc, loaikhuyenmai, trangthai, mota)
VALUES
    (N'Giảm 10% khóa học các khóa TOEIC 450', 10, '2025-11-01', '2025-12-31', 1, 1, NULL),
    (N'Giảm 10% khi mua combo 2 khóa IELTS Speaking&Writing và Ielts Listening&Reading', 10, '2025-11-01', '2025-12-31', 2, 1, NULL),
    (N'Giảm 10% cho học viên cũ khi đăng kí học', 10, '2025-11-01', '2025-12-31', 3, 1, NULL);


------------------------------------------------------------
-- 4. PHUONGTHUCTHANHTOAN
------------------------------------------------------------
INSERT INTO phuongthucthanhtoan (ten)
VALUES
    (N'Tiền mặt'),
    (N'VNPay');


------------------------------------------------------------
-- 5. HOADON (đã có cột trangthai)
------------------------------------------------------------
INSERT INTO hoadon (mahocvien, tongtien, ngaytao, phuongthuc_id, trangthai)
VALUES
    (1, 2500000, '2025-02-20 10:30:00', 1, 1),
    (2, 3000000, '2025-02-20 11:10:00', 1, 1),
    (3, 1500000, '2025-02-21 09:00:00', 2, 1);


------------------------------------------------------------
-- 6. CHITIETHOADON
-- Giả sử khóa học 101, 102, 103 tồn tại; bạn chỉnh lại theo DB thật
------------------------------------------------------------
INSERT INTO chitiethoadon (hoadon_id, malophoc, giaban)
VALUES
    (1, 5, 2500000),
    (1, 4, 2500000),
    (2, 5, 3000000),
    (3, 4, 1500000);


------------------------------------------------------------
-- 7. CHITIETKHUYENMAI (đã bỏ giamsotien)
-- Chỉ ánh xạ khuyến mãi → khóa học. Bạn chỉnh ID khóa học theo DB thật.
------------------------------------------------------------
INSERT INTO chitietkhuyenmai (khuyenmai_id, khoahoc_id)
VALUES
    (1, 4),
    (1, 5),
    (2, 3),
    (3, 2);


------------------------------------------------------------
-- 8. DIEMDANH
-- Giả sử buổi học 1,2,3 tồn tại
------------------------------------------------------------
INSERT INTO diemdanh (mahocvien, mabuoihoc, vang, ghichu)
VALUES
    (1, 1, 0, N'Có mặt'),
    (2, 1, 1, N'Vắng có phép'),
    (3, 2, 0, N'Có mặt');
