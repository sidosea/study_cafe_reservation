CREATE DATABASE study_cafe_db;

USE study_cafe_db;

-- 사용자 정보 테이블
CREATE TABLE users (
    phone_number VARCHAR(15) PRIMARY KEY,  -- 전화번호가 슈퍼키
    password VARCHAR(255) NOT NULL,        -- 비밀번호 (암호화 필요)
    name VARCHAR(50) NOT NULL,             -- 사용자 이름
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 회원가입 날짜
    remaining_time INT DEFAULT 0          -- 남은 시간 (기본값 0)
);


-- 좌석 정보 테이블
CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY, -- 좌석 ID
    seat_number INT NOT NULL,               -- 좌석 번호
    is_occupied BOOLEAN DEFAULT FALSE       -- 좌석 사용 여부
);

-- 예약 정보 테이블
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(15),
    seat_id INT NULL,						-- seat_id 컬럼을 NULL 허용으로 변경
    start_time TIMESTAMP,                   -- 사용 시작 시간
    end_time TIMESTAMP,                     -- 사용 종료 시간
    is_completed BOOLEAN DEFAULT FALSE,     -- 예약 완료 여부
    FOREIGN KEY (phone_number) REFERENCES users(phone_number) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE SET NULL
);




-- 결제 정보 테이블
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(15),
    reservation_id INT,
    amount DECIMAL(10, 2) NOT NULL,           -- 결제 시간
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 결제 일시
    FOREIGN KEY (phone_number) REFERENCES users(phone_number) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE
);


-- SMS 알림 정보 테이블
CREATE TABLE sms_notifications (
    sms_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(15),
    message TEXT,                           -- 전송할 메시지
    send_time TIMESTAMP,                    -- 전송 시간
    status BOOLEAN DEFAULT FALSE,           -- 전송 상태 (성공 여부)
    FOREIGN KEY (phone_number) REFERENCES users(phone_number) ON DELETE CASCADE
);

-- 출입 로그 정보 테이블
CREATE TABLE door_access_logs (
    access_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(15),
    access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 출입 시각
    FOREIGN KEY (phone_number) REFERENCES users(phone_number) ON DELETE CASCADE
);


-- 월별 통계 정보 테이블
CREATE TABLE monthly_statistics (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT,
    month_year DATE,                          -- 월별 데이터를 나타내는 날짜 (yyyy-mm-01 형식)
    total_usage_time DECIMAL(10, 2) DEFAULT 0, -- 해당 월의 총 사용 시간 (단위: 시간)
    total_reservations INT DEFAULT 0,         -- 해당 월의 총 예약 횟수
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE
);

-- 문자 인증 코드 테이블
CREATE TABLE verification_codes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(15) NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (phone_number) REFERENCES users(phone_number) ON DELETE CASCADE
);


SELECT * FROM users;
SELECT * FROM sms_notifications;
