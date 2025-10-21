-- ==========================
-- Khóa 1: IELTS Writing (makhoahoc=1)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao,hinhanh, mota, dauvao, daura)
VALUES (N'Khóa IELTS Writing', 50, 8500000.00, 25, 'https://example.com/video/ielts-writing.mp4', 1, '2025-10-17 00:00:00', 'admin','khoahoc1.png' ,N'Khóa học tập trung vào kỹ năng viết IELTS, giúp học viên đạt band 7+ qua thực hành và feedback.', N'IELTS 5.0+', N'Đạt band 7.0+ Writing, kỹ năng viết luận chuyên sâu.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Nắm vững cấu trúc bài Writing Task 1 & 2, bao gồm cách giới thiệu đề bài, mô tả biểu đồ, phát triển ý chính và kết luận hợp lý, giúp học viên tự tin phân tích và trình bày thông tin.', 1),
                                                  (N'Phát triển vốn từ vựng học thuật và ngữ pháp nâng cao, bao gồm các collocation, linking words, và cấu trúc câu phức, nhằm nâng cao điểm Writing và tránh lỗi ngữ pháp cơ bản.', 1),
                                                  (N'Thực hành viết luận theo đề thi IELTS thật, nhận phản hồi chi tiết từ giảng viên, cải thiện kỹ năng lập luận, sắp xếp ý tưởng logic và nâng cao khả năng trình bày ý kiến rõ ràng.', 1);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (1, N'Module 1: Giới thiệu Writing Task 1', 3),
                                                         (1, N'Module 2: Giới thiệu Writing Task 2', 3),
                                                         (1, N'Module 3: Cấu trúc câu & liên từ', 3),
                                                         (1, N'Module 4: Phát triển ý tưởng', 3),
                                                         (1, N'Module 5: Từ vựng học thuật', 3),
                                                         (1, N'Module 6: Thực hành đề mẫu', 3),
                                                         (1, N'Module 7: Feedback bài viết', 3),
                                                         (1, N'Module 8: Chiến lược làm bài', 4);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Giới thiệu các dạng biểu đồ Task 1', 1),
(N'Cách mô tả số liệu chi tiết', 1),
(N'Bài tập mẫu Task 1', 1),
-- Module 2
(N'Cấu trúc đoạn văn Task 2', 2),
(N'Phát triển luận điểm', 2),
(N'Bài tập mẫu Task 2', 2),
-- Module 3
(N'Các loại câu phức trong Writing', 3),
(N'Sử dụng linking words hiệu quả', 3),
(N'Luyện tập câu đơn & câu ghép', 3),
-- Module 4
(N'Brainstorm ý tưởng cho đề bài', 4),
(N'Xây dựng dàn ý logic', 4),
(N'Phát triển luận điểm chi tiết', 4),
-- Module 5
(N'Từ vựng học thuật cơ bản', 5),
(N'Collocations và phrases', 5),
(N'Sử dụng từ thay thế để tránh lặp từ', 5),
-- Module 6
(N'Thực hành Writing Task 1', 6),
(N'Thực hành Writing Task 2', 6),
(N'Nhận xét bài viết mẫu', 6),
-- Module 7
(N'Feedback chi tiết bài viết Task 1', 7),
(N'Feedback chi tiết bài viết Task 2', 7),
(N'Điểm mạnh và điểm cần cải thiện', 7),
-- Module 8
(N'Chiến lược quản lý thời gian', 8),
(N'Chiến lược làm bài hiệu quả', 8),
(N'Mẹo đạt điểm cao Writing', 8);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
                                                              (1, 'IELTS_Task1_Guide.pdf', 'https://example.com/docs/ielts-task1.pdf', N'Hướng dẫn chi tiết Writing Task 1', 'https://example.com/images/task1.jpg'),
                                                              (2, 'IELTS_Task2_Guide.pdf', 'https://example.com/docs/ielts-task2.pdf', N'Hướng dẫn chi tiết Writing Task 2', 'https://example.com/images/task2.jpg');
-- ==========================
-- Khóa 2: IELTS Speaking (makhoahoc=2)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao, hinhanh,mota, dauvao, daura)
VALUES (N'Khóa IELTS Speaking', 40, 8000000.00, 20, 'https://example.com/video/ielts-speaking.mp4', 1, '2025-10-17 00:00:00', 'admin', 'khoahoc2.png',N'Khóa học rèn luyện kỹ năng nói IELTS, tập trung vào fluency và pronunciation.', N'IELTS 4.5+', N'Đạt band 6.5+ Speaking, tự tin trong phỏng vấn.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Củng cố khả năng trả lời các câu hỏi Part 1, 2, 3, với chiến lược phát triển ý tưởng, từ vựng phong phú và phát âm chuẩn.', 2),
                                                  (N'Rèn luyện phản xạ giao tiếp tự nhiên, tự tin nói về các chủ đề xã hội, học tập, sở thích và kinh nghiệm cá nhân.', 2),
                                                  (N'Thực hành mô phỏng bài thi Speaking theo chuẩn IELTS, nhận feedback từ giảng viên và nâng cao điểm số thực tế.', 2);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (2, N'Module 1: Giới thiệu Part 1', 2),
                                                         (2, N'Module 2: Giới thiệu Part 2', 2),
                                                         (2, N'Module 3: Giới thiệu Part 3', 2),
                                                         (2, N'Module 4: Phát âm và ngữ điệu', 3),
                                                         (2, N'Module 5: Từ vựng chủ đề hàng ngày', 3),
                                                         (2, N'Module 6: Thực hành cue card', 2),
                                                         (2, N'Module 7: Feedback và sửa lỗi', 3),
                                                         (2, N'Module 8: Chiến lược thi Speaking', 3);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Câu hỏi giới thiệu bản thân', 9),
(N'Trả lời câu hỏi đơn giản', 9),
(N'Bài tập Part 1 mẫu', 9),
-- Module 2
(N'Cấu trúc cue card', 10),
(N'Phát triển ý cho chủ đề', 10),
(N'Bài tập Part 2 mẫu', 10),
-- Module 3
(N'Thảo luận chủ đề trừu tượng', 11),
(N'Sử dụng ví dụ minh họa', 11),
(N'Bài tập Part 3 mẫu', 11),
-- Module 4
(N'Âm vị tiếng Anh cơ bản', 12),
(N'Ngữ điệu và nhịp điệu', 12),
(N'Luyện phát âm từ vựng', 12),
-- Module 5
(N'Từ vựng về gia đình và bạn bè', 13),
(N'Từ vựng về công việc và học tập', 13),
(N'Từ vựng về sở thích', 13),
-- Module 6
(N'Thực hành cue card 1', 14),
(N'Thực hành cue card 2', 14),
(N'Thực hành cue card 3', 14),
-- Module 7
(N'Feedback chi tiết Part 1', 15),
(N'Feedback chi tiết Part 2', 15),
(N'Feedback chi tiết Part 3', 15),
-- Module 8
(N'Quản lý thời gian Speaking', 16),
(N'Mẹo trả lời tự nhiên', 16),
(N'Chiến lược đạt band cao', 16);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
                                                              (9, 'Speaking_Part1_Guide.pdf', 'https://example.com/docs/speaking-part1.pdf', N'Hướng dẫn Part 1 Speaking', 'https://example.com/images/part1.jpg'),
                                                              (10, 'Speaking_Part2_Guide.pdf', 'https://example.com/docs/speaking-part2.pdf', N'Hướng dẫn Part 2 Speaking', 'https://example.com/images/part2.jpg'),
                                                              (11, 'Speaking_Part3_Guide.pdf', 'https://example.com/docs/speaking-part3.pdf', N'Hướng dẫn Part 3 Speaking', 'https://example.com/images/part3.jpg');
-- ==========================
-- Khóa 3: IELTS Listening (makhoahoc=3)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao,hinhanh, mota, dauvao, daura)
VALUES (N'Khóa IELTS Listening', 45, 8200000.00, 22, 'https://example.com/video/ielts-listening.mp4', 1, '2025-10-17 00:00:00', 'admin','khoahoc3.png',N'Khóa học cải thiện kỹ năng nghe IELTS qua audio thực tế và chiến lược.', N'IELTS 5.0+', N'Đạt band 7.0+ Listening, nắm bắt thông tin chính xác.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Nâng cao kỹ năng nghe hiểu các đoạn hội thoại và bài giảng, tập trung vào việc nắm bắt thông tin chính xác và chi tiết.', 3),
                                                  (N'Luyện tập các dạng câu hỏi Listening như multiple choice, matching, note completion để cải thiện tốc độ và độ chính xác.', 3),
                                                  (N'Thực hành nghe audio IELTS thật, phân tích lỗi sai và áp dụng chiến lược nghe hiệu quả để đạt band cao.', 3);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (3, N'Module 1: Giới thiệu Listening Section 1', 3),
                                                         (3, N'Module 2: Giới thiệu Listening Section 2', 3),
                                                         (3, N'Module 3: Giới thiệu Listening Section 3', 2),
                                                         (3, N'Module 4: Giới thiệu Listening Section 4', 3),
                                                         (3, N'Module 5: Kỹ năng nghe từ khóa', 3),
                                                         (3, N'Module 6: Thực hành audio mẫu', 3),
                                                         (3, N'Module 7: Phân tích lỗi sai', 2),
                                                         (3, N'Module 8: Chiến lược thi Listening', 3);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Hội thoại hàng ngày cơ bản', 17),
(N'Dạng câu hỏi completion', 17),
(N'Bài tập Section 1 mẫu', 17),
-- Module 2
(N'Bài nói đơn lẻ về chủ đề', 18),
(N'Dạng câu hỏi matching', 18),
(N'Bài tập Section 2 mẫu', 18),
-- Module 3
(N'Hội thoại học thuật nhóm', 19),
(N'Dạng câu hỏi multiple choice', 19),
(N'Bài tập Section 3 mẫu', 19),
-- Module 4
(N'Bài giảng học thuật', 20),
(N'Dạng câu hỏi note completion', 20),
(N'Bài tập Section 4 mẫu', 20),
-- Module 5
(N'Xác định từ khóa trong audio', 21),
(N'Luyện nghe tốc độ nhanh', 21),
(N'Bài tập từ khóa', 21),
-- Module 6
(N'Thực hành full test 1', 22),
(N'Thực hành full test 2', 22),
(N'Thực hành full test 3', 22),
-- Module 7
(N'Phân tích lỗi nghe sai', 23),
(N'Cải thiện kỹ năng nghe', 23),
(N'Bài tập sửa lỗi', 23),
-- Module 8
(N'Quản lý thời gian Listening', 24),
(N'Mẹo dự đoán câu trả lời', 24),
(N'Chiến lược đạt band 8+', 24);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
                                                              (17, 'Listening_Section1_Guide.pdf', 'https://example.com/docs/listening-section1.pdf', N'Hướng dẫn Section 1 Listening', 'https://example.com/images/section1.jpg'),
                                                              (20, 'Listening_Section4_Guide.pdf', 'https://example.com/docs/listening-section4.pdf', N'Hướng dẫn Section 4 Listening', 'https://example.com/images/section4.jpg');
-- ==========================
-- Khóa 4: IELTS Reading (makhoahoc=4)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao,hinhanh, mota, dauvao, daura)
VALUES (N'Khóa IELTS Reading', 50, 8500000.00, 25, 'https://example.com/video/ielts-reading.mp4', 1, '2025-10-17 00:00:00', 'admin', 'khoahoc4.png',N'Khóa học nâng cao kỹ năng đọc IELTS với văn bản học thuật.', N'IELTS 5.5+', N'Đạt band 7.5+ Reading, tốc độ đọc nhanh.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Phát triển kỹ năng đọc hiểu nhanh các văn bản học thuật, nắm bắt ý chính và chi tiết để trả lời câu hỏi chính xác.', 4),
                                                  (N'Luyện tập các dạng câu hỏi Reading như true/false, matching headings, summary completion để nâng cao tốc độ.', 4),
                                                  (N'Thực hành đọc bài IELTS thật, phân tích từ vựng và cấu trúc câu để đạt điểm cao trong phần Reading.', 4);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (4, N'Module 1: Giới thiệu Passage 1', 3),
                                                         (4, N'Module 2: Giới thiệu Passage 2', 3),
                                                         (4, N'Module 3: Giới thiệu Passage 3', 3),
                                                         (4, N'Module 4: Kỹ năng skimming & scanning', 3),
                                                         (4, N'Module 5: Từ vựng đồng nghĩa', 3),
                                                         (4, N'Module 6: Thực hành full passage', 3),
                                                         (4, N'Module 7: Phân tích câu hỏi', 3),
                                                         (4, N'Module 8: Chiến lược thi Reading', 4);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Văn bản chủ đề hàng ngày', 25),
(N'Dạng true/false/not given', 25),
(N'Bài tập Passage 1 mẫu', 25),
-- Module 2
(N'Văn bản học thuật trung cấp', 26),
(N'Dạng matching headings', 26),
(N'Bài tập Passage 2 mẫu', 26),
-- Module 3
(N'Văn bản học thuật nâng cao', 27),
(N'Dạng summary completion', 27),
(N'Bài tập Passage 3 mẫu', 27),
-- Module 4
(N'Kỹ năng đọc lướt ý chính', 28),
(N'Kỹ năng tìm chi tiết nhanh', 28),
(N'Bài tập skimming', 28),
-- Module 5
(N'Từ vựng đồng nghĩa cơ bản', 29),
(N'Sử dụng paraphrase', 29),
(N'Bài tập từ đồng nghĩa', 29),
-- Module 6
(N'Thực hành full test 1', 30),
(N'Thực hành full test 2', 30),
(N'Thực hành full test 3', 30),
-- Module 7
(N'Phân tích loại câu hỏi', 31),
(N'Xử lý câu hỏi khó', 31),
(N'Bài tập phân tích', 31),
-- Module 8
(N'Quản lý thời gian Reading', 32),
(N'Mẹo đọc hiệu quả', 32),
(N'Chiến lược đạt band 7+', 32);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
                                                              (25, 'Reading_Passage1_Guide.pdf', 'https://example.com/docs/reading-passage1.pdf', N'Hướng dẫn Passage 1 Reading', 'https://example.com/images/passage1.jpg'),
                                                              (27, 'Reading_Passage3_Guide.pdf', 'https://example.com/docs/reading-passage3.pdf', N'Hướng dẫn Passage 3 Reading', 'https://example.com/images/passage3.jpg'),
                                                              (28, 'Skimming_Scanning_Guide.pdf', 'https://example.com/docs/skimming-scanning.pdf', N'Hướng dẫn Skimming & Scanning', 'https://example.com/images/skimming.jpg');
-- ==========================
-- Khóa 5: IELTS Vocabulary (makhoahoc=5)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao,hinhanh, mota, dauvao, daura)
VALUES (N'Khóa IELTS Vocabulary', 35, 7500000.00, 18, 'https://example.com/video/ielts-vocabulary.mp4', 1, '2025-10-17 00:00:00', 'admin','khoahoc5.png', N'Khóa học mở rộng từ vựng cho IELTS, tập trung vào chủ đề phổ biến.', N'IELTS 4.0+', N'Từ vựng phong phú, áp dụng linh hoạt trong thi.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Xây dựng vốn từ vựng học thuật phong phú cho tất cả kỹ năng IELTS, tập trung vào các chủ đề phổ biến như môi trường, giáo dục.', 5),
                                                  (N'Học cách sử dụng collocations, idioms và phrasal verbs để nâng cao điểm số trong Writing và Speaking.', 5),
                                                  (N'Thực hành áp dụng từ vựng mới qua bài tập và quiz để ghi nhớ lâu dài và sử dụng linh hoạt.', 5);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (5, N'Module 1: Từ vựng chủ đề Environment', 2),
                                                         (5, N'Module 2: Từ vựng chủ đề Education', 2),
                                                         (5, N'Module 3: Từ vựng chủ đề Health', 2),
                                                         (5, N'Module 4: Collocations và idioms', 2),
                                                         (5, N'Module 5: Phrasal verbs', 2),
                                                         (5, N'Module 6: Thực hành từ vựng Writing', 3),
                                                         (5, N'Module 7: Thực hành từ vựng Speaking', 2),
                                                         (5, N'Module 8: Quiz và review', 3);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Từ vựng về ô nhiễm', 33),
(N'Từ vựng về biến đổi khí hậu', 33),
(N'Bài tập Environment', 33),
-- Module 2
(N'Từ vựng về hệ thống giáo dục', 34),
(N'Từ vựng về học tập', 34),
(N'Bài tập Education', 34),
-- Module 3
(N'Từ vựng về sức khỏe thể chất', 35),
(N'Từ vựng về sức khỏe tinh thần', 35),
(N'Bài tập Health', 35),
-- Module 4
(N'Collocations phổ biến', 36),
(N'Idioms hàng ngày', 36),
(N'Bài tập collocations', 36),
-- Module 5
(N'Phrasal verbs cơ bản', 37),
(N'Phrasal verbs nâng cao', 37),
(N'Bài tập phrasal verbs', 37),
-- Module 6
(N'Từ vựng cho Task 1', 38),
(N'Từ vựng cho Task 2', 38),
(N'Bài tập Writing', 38),
-- Module 7
(N'Từ vựng cho Part 1', 39),
(N'Từ vựng cho Part 2', 39),
(N'Bài tập Speaking', 39),
-- Module 8
(N'Quiz từ vựng tổng hợp', 40),
(N'Review lỗi sai', 40),
(N'Bài tập ôn tập', 40);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
    (33, 'Vocabulary_Environment.pdf', 'https://example.com/docs/vocab-environment.pdf', N'Từ vựng chủ đề Environment', 'https://example.com/images/environment.jpg');
-- ==========================
-- Khóa 6: IELTS Grammar (makhoahoc=6)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao,hinhanh, mota, dauvao, daura)
VALUES (N'Khóa IELTS Grammar', 40, 7800000.00, 20, 'https://example.com/video/ielts-grammar.mp4', 1, '2025-10-17 00:00:00', 'admin', 'khoahoc6.png',N'Khóa học củng cố ngữ pháp cho IELTS, tránh lỗi phổ biến.', N'IELTS 4.5+', N'Ngữ pháp nâng cao, đạt band 7.0+ ở Writing và Speaking.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Nắm vững ngữ pháp nâng cao như thì phức tạp, câu điều kiện, và cấu trúc câu phức để tránh lỗi trong Writing và Speaking.', 6),
                                                  (N'Luyện tập áp dụng ngữ pháp vào các bài thi IELTS thực tế để nâng cao độ chính xác và fluency.', 6),
                                                  (N'Phân tích lỗi ngữ pháp phổ biến và thực hành sửa chữa để đạt band cao hơn trong tất cả kỹ năng.', 6);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (6, N'Module 1: Thì cơ bản và nâng cao', 2),
                                                         (6, N'Module 2: Câu điều kiện', 2),
                                                         (6, N'Module 3: Câu bị động', 3),
                                                         (6, N'Module 4: Cấu trúc câu phức', 2),
                                                         (6, N'Module 5: Linking words', 3),
                                                         (6, N'Module 6: Thực hành Writing', 2),
                                                         (6, N'Module 7: Thực hành Speaking', 3),
                                                         (6, N'Module 8: Review ngữ pháp', 3);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Thì hiện tại và quá khứ', 41),
(N'Thì tương lai và hoàn thành', 41),
(N'Bài tập thì', 41),
-- Module 2
(N'Câu điều kiện loại 1,2,3', 42),
(N'Mixed conditionals', 42),
(N'Bài tập conditionals', 42),
-- Module 3
(N'Cấu trúc bị động cơ bản', 43),
(N'Bị động với modal verbs', 43),
(N'Bài tập passive', 43),
-- Module 4
(N'Relative clauses', 44),
(N'Adverbial clauses', 44),
(N'Bài tập câu phức', 44),
-- Module 5
(N'Linking words for contrast', 45),
(N'Linking words for addition', 45),
(N'Bài tập linking', 45),
-- Module 6
(N'Ngữ pháp trong Task 1', 46),
(N'Ngữ pháp trong Task 2', 46),
(N'Bài tập Writing', 46),
-- Module 7
(N'Ngữ pháp trong Part 1', 47),
(N'Ngữ pháp trong Part 2', 47),
(N'Bài tập Speaking', 47),
-- Module 8
(N'Review lỗi phổ biến', 48),
(N'Quiz ngữ pháp tổng hợp', 48),
(N'Bài tập ôn tập', 48);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
                                                              (41, 'Grammar_Tenses_Guide.pdf', 'https://example.com/docs/grammar-tenses.pdf', N'Hướng dẫn thì ngữ pháp', 'https://example.com/images/tenses.jpg'),
                                                              (42, 'Conditionals_Guide.pdf', 'https://example.com/docs/conditionals.pdf', N'Hướng dẫn câu điều kiện', 'https://example.com/images/conditionals.jpg');
-- ==========================
-- Khóa 7: IELTS Full Preparation (makhoahoc=7)
-- ==========================
INSERT INTO khoahoc (tenkhoahoc, sogiohoc, hocphi, sobuoihoc, video, trangthai, ngaytao, nguoitao, hinhanh,mota, dauvao, daura)
VALUES (N'Khóa IELTS Full Preparation', 60, 9500000.00, 30, 'https://example.com/video/ielts-full-prep.mp4', 1, '2025-10-17 00:00:00', 'admin','khoahoc7.png', N'Khóa học chuẩn bị toàn diện cho kỳ thi IELTS, luyện 4 kỹ năng.', N'IELTS 5.5+', N'Đạt band 7.0+ tổng thể, sẵn sàng cho kỳ thi thực tế.');
INSERT INTO muctieukh (tenmuctieu, makhoahoc) VALUES
                                                  (N'Chuẩn bị toàn diện cho kỳ thi IELTS với luyện tập tất cả 4 kỹ năng, tập trung vào điểm yếu cá nhân hóa.', 7),
                                                  (N'Thực hành full test IELTS thật, phân tích kết quả và áp dụng chiến lược để đạt band mục tiêu.', 7),
                                                  (N'Xây dựng sự tự tin và kỹ năng quản lý thời gian cho ngày thi thực tế.', 7);
INSERT INTO module (makhoahoc, tenmodule, thoiluong) VALUES
                                                         (7, N'Module 1: Ôn Listening cơ bản', 4),
                                                         (7, N'Module 2: Ôn Reading cơ bản', 4),
                                                         (7, N'Module 3: Ôn Writing cơ bản', 4),
                                                         (7, N'Module 4: Ôn Speaking cơ bản', 3),
                                                         (7, N'Module 5: Full test practice 1', 4),
                                                         (7, N'Module 6: Full test practice 2', 4),
                                                         (7, N'Module 7: Phân tích test', 3),
                                                         (7, N'Module 8: Chiến lược tổng hợp', 4);
INSERT INTO noidung (tennoidung, mamodule) VALUES
-- Module 1
(N'Ôn Section 1-2 Listening', 49),
(N'Ôn Section 3-4 Listening', 49),
(N'Bài tập Listening', 49),
-- Module 2
(N'Ôn Passage 1-2 Reading', 50),
(N'Ôn Passage 3 Reading', 50),
(N'Bài tập Reading', 50),
-- Module 3
(N'Ôn Task 1 Writing', 51),
(N'Ôn Task 2 Writing', 51),
(N'Bài tập Writing', 51),
-- Module 4
(N'Ôn Part 1-2 Speaking', 52),
(N'Ôn Part 3 Speaking', 52),
(N'Bài tập Speaking', 52),
-- Module 5
(N'Full test 1 Listening', 53),
(N'Full test 1 Reading', 53),
(N'Full test 1 Writing & Speaking', 53),
-- Module 6
(N'Full test 2 Listening', 54),
(N'Full test 2 Reading', 54),
(N'Full test 2 Writing & Speaking', 54),
-- Module 7
(N'Phân tích điểm mạnh', 55),
(N'Phân tích điểm yếu', 55),
(N'Cải thiện chiến lược', 55),
-- Module 8
(N'Quản lý thời gian thi', 56),
(N'Mẹo cho ngày thi', 56),
(N'Ôn tập cuối cùng', 56);
INSERT INTO tailieu (mamodule, tenfile, link, mota, hinh) VALUES
                                                              (49, 'FullPrep_Listening.pdf', 'https://example.com/docs/fullprep-listening.pdf', N'Ôn Listening full prep', 'https://example.com/images/full-listening.jpg'),
                                                              (50, 'FullPrep_Reading.pdf', 'https://example.com/docs/fullprep-reading.pdf', N'Ôn Reading full prep', 'https://example.com/images/full-reading.jpg'),
                                                              (53, 'FullTest1_Guide.pdf', 'https://example.com/docs/fulltest1.pdf', N'Hướng dẫn full test 1', 'https://example.com/images/fulltest1.jpg');