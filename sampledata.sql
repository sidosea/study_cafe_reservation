-- 사용자 정보 샘플 데이터 삽입
INSERT INTO users (phone_number, password, name, remaining_time) VALUES
('01012345678', 'password123', '홍길동', 120),
('01023456789', 'password456', '김철수', 150),
('01045678901', 'password012', '박민수', 60),
('01056789012', 'password345', '최수정', 0),
('1', '1', '엄', 70),
('2', '2', '엄', 30),
('01067890123', 'password678', '정지훈', 200);


-- 좌석 정보 샘플 데이터 삽입 (20개 좌석)
INSERT INTO seats (seat_number, is_occupied) VALUES
    (1, FALSE), (2, FALSE), (3, FALSE), (4, FALSE), (5, FALSE),
    (6, FALSE), (7, FALSE), (8, FALSE), (9, FALSE), (10, FALSE),
    (11, FALSE), (12, FALSE), (13, FALSE), (14, FALSE), (15, FALSE),
    (16, FALSE), (17, FALSE), (18, FALSE), (19, FALSE), (20, FALSE);

-- 예약 정보 샘플 데이터 삽입
INSERT INTO reservations (phone_number, seat_id, start_time, end_time, is_completed) VALUES
('01012345678', 1, '2024-11-28 09:00:00', '2024-11-28 12:00:00', TRUE),
('01023456789', 2, '2024-11-28 10:00:00', '2024-11-28 13:00:00', TRUE),
('01056789012', 5, '2024-11-29 10:00:00', '2024-11-29 12:00:00', TRUE),
('01067890123', 6, '2024-11-29 11:00:00', '2024-11-29 13:00:00', TRUE);

-- 결제 정보 샘플 데이터 삽입
INSERT INTO payments (phone_number, reservation_id, amount, payment_date) VALUES
('01012345678', 1, 3, '2024-11-28 08:30:00'),
('01023456789', 2, 3, '2024-11-28 09:30:00'),
('01056789012', 3, 2, '2024-11-29 09:30:00'),
('01067890123', 4, 2, '2024-11-29 10:30:00');

-- SMS 알림 정보 샘플 데이터 삽입
INSERT INTO sms_notifications (phone_number, message, send_time, status) VALUES
('01067890123', '샘플데이터', '2024-11-29 10:45:00', TRUE);

-- 출입 로그 정보 샘플 데이터 삽입
INSERT INTO door_access_logs (phone_number, access_time) VALUES
('01012345678', '2024-11-28 09:05:00'),
('01023456789', '2024-11-28 10:05:00'),
('01056789012', '2024-11-29 10:05:00'),
('01067890123', '2024-11-29 11:05:00');

-- 월별 통계 정보 샘플 데이터 삽입
-- 10월 데이터
INSERT INTO monthly_statistics (seat_id, month_year, total_usage_time, total_reservations) VALUES
(1, '2023-10-01', 15.5, 8),
(2, '2023-10-01', 20.0, 10),
(3, '2023-10-01', 12.0, 6),
(4, '2023-10-01', 18.5, 9),
(5, '2023-10-01', 25.0, 12);

-- 11월 데이터
INSERT INTO monthly_statistics (seat_id, month_year, total_usage_time, total_reservations) VALUES
(1, '2023-11-01', 14.0, 7),
(2, '2023-11-01', 19.0, 11),
(3, '2023-11-01', 13.5, 8),
(4, '2023-11-01', 21.0, 10),
(5, '2023-11-01', 28.0, 14);

-- 12월 데이터
INSERT INTO monthly_statistics (seat_id, month_year, total_usage_time, total_reservations) VALUES
(1, '2023-12-01', 17.0, 9),
(2, '2023-12-01', 22.0, 12),
(3, '2023-12-01', 16.0, 8),
(4, '2023-12-01', 19.5, 10),
(5, '2023-12-01', 30.0, 15);



INSERT INTO users (phone_number, password, name, remaining_time) VALUES
('01047020231', '123', '김현석', 6)
ON DUPLICATE KEY UPDATE password = VALUES(password), name = VALUES(name), remaining_time = VALUES(remaining_time);

select * from users ;
select * from reservations;