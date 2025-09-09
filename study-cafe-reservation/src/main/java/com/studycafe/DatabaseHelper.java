package com.studycafe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;



public class DatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/study_cafe_db";
    static final String DB_USER = "root";
    private static final String DB_PASSWORD = "****";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC 드라이버 로드 실패", e);
        }
    }

    // 사용자 인증 (비밀번호로 인증)
    public static boolean verifyUser(String phoneNumber, String password) {
        String query = "SELECT password FROM users WHERE phone_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password); // 비밀번호 비교
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 좌석 목록 가져오기
    public static List<Seat> getSeats() {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT * FROM seats";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int seatId = rs.getInt("seat_id");
                int seatNumber = rs.getInt("seat_number");
                boolean isOccupied = rs.getBoolean("is_occupied");
                seats.add(new Seat(seatId, seatNumber, isOccupied));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }
    //좌석 사용중인지 확인
    public static Integer getUserReservedSeat(String phoneNumber) {
        String query = "SELECT seat_id FROM reservations WHERE phone_number = ? AND end_time > NOW()";
        Integer seatId = -1;  // 기본값은 -1로 설정하여 예약된 좌석이 없으면 처리할 수 있도록 함.

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, phoneNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    seatId = rs.getInt("seat_id");  // 예약된 좌석의 seat_id를 가져옴
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seatId;  // 예약된 좌석이 없으면 -1 반환
    }

 // 퇴실 처리 (잔여 시간 업데이트 및 좌석 상태 변경)
    public static boolean exitSeat(int seatId, String phoneNumber) {
        // 사용자가 예약한 좌석 정보를 가져옴
        Reservation activeReservation = getActiveReservationForSeat(seatId);

        if (activeReservation == null) {
            JOptionPane.showMessageDialog(null, "활성화된 예약이 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return false; // 실패 반환
        }

        long currentTimeMillis = System.currentTimeMillis();
        long endTimeMillis = activeReservation.getEndTime().getTime();
        long remainingTimeMillis = endTimeMillis - currentTimeMillis;

        if (remainingTimeMillis < 0) {
            // 남은 시간이 없는 경우
            JOptionPane.showMessageDialog(null, "남은 시간이 없습니다. 추가 결제를 진행해주세요.", "퇴실", JOptionPane.WARNING_MESSAGE);
            return false; // 실패 반환
        }

        // 사용 시간 계산 (단위: 시간)
        long usageTimeMillis = activeReservation.getEndTime().getTime() - activeReservation.getStartTime().getTime();
        double usageTimeInHours = usageTimeMillis / (1000.0 * 60 * 60);

        // 1시간 미만인 경우 남은 시간 저장 X
        if (remainingTimeMillis < 60 * 60 * 1000) {
            JOptionPane.showMessageDialog(null, "1시간 미만의 잔여 시간은 저장되지 않습니다. 퇴실 처리만 진행됩니다.", "퇴실", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 1시간 이상
            int remainingTimeInMinutes = (int) (remainingTimeMillis / (1000 * 60)); // 분 단위 변환
            updateRemainingTime(phoneNumber, remainingTimeInMinutes);
            String message = String.format("남은 시간 %d분이 저장되었습니다.", remainingTimeInMinutes);
            JOptionPane.showMessageDialog(null, message, "퇴실", JOptionPane.INFORMATION_MESSAGE);
        }

        // 좌석 상태 업데이트 (사용 완료 처리)
        updateSeatStatus(seatId, false);

        // 사용자의 seat_id 초기화
        Integer reservedSeatId = getUserReservedSeat(phoneNumber); // 현재 사용자가 예약한 좌석을 가져옴
        if (reservedSeatId != null) {
            updateUserSeatId(phoneNumber, reservedSeatId); // 좌석 ID를 -1로 업데이트
        }

        // **추가: 통계 업데이트**
        updateMonthlyStatistics(seatId, usageTimeInHours);

        return true; // 성공 반환
    }
//월별 통계
    private static void updateMonthlyStatistics(int seatId, double usageTimeInHours) {
        String updateQuery = "INSERT INTO monthly_statistics (seat_id, month_year, total_usage_time, total_reservations) " +
                             "VALUES (?, ?, ?, 1) " +
                             "ON DUPLICATE KEY UPDATE " +
                             "total_usage_time = total_usage_time + VALUES(total_usage_time), " +
                             "total_reservations = total_reservations + 1";

        // 현재 월 (yyyy-MM-01 형식)
        java.sql.Date currentMonth = java.sql.Date.valueOf(
            java.time.LocalDate.now().withDayOfMonth(1).toString()
        );

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, seatId);
            stmt.setDate(2, currentMonth);
            stmt.setDouble(3, usageTimeInHours);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "월별 통계 업데이트 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }


 
    public static void updateUserSeatId(String phoneNumber, int seatId) {
        // 좌석 상태를 초기화하는 쿼리
        String query = "UPDATE reservations SET seat_id = NULL, is_completed = 1 WHERE phone_number = ? AND seat_id = ?";

        System.out.println("Updating user seat ID: phoneNumber = " + phoneNumber + ", seatId = " + seatId);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // phone_number와 seat_id 매핑
            stmt.setString(1, phoneNumber);
            stmt.setInt(2, seatId);

            // 실행
            int rowsAffected = stmt.executeUpdate();

            // 업데이트가 정상적으로 이루어지지 않은 경우
            if (rowsAffected == 0) {
                JOptionPane.showMessageDialog(null,
                        "사용자의 좌석 ID 업데이트에 실패했습니다. 확인 후 다시 시도하세요.",
                        "업데이트 실패",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "좌석 ID가 초기화되었습니다.",
                        "업데이트 성공",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            // 오류 로그 및 사용자 알림
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "좌석 ID 업데이트 중 오류가 발생했습니다. 관리자에게 문의하세요.",
                    "데이터베이스 오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }










 // 잔여 시간 저장
    public static int updateRemainingTime(String phoneNumber, int remainingTime) {
        String query = "UPDATE users SET remaining_time = ? WHERE phone_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, remainingTime);
            stmt.setString(2, phoneNumber);
            
            // 영향을 미친 행의 수를 반환
            return stmt.executeUpdate(); // 업데이트된 행의 개수를 반환
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // 오류 발생 시 -1 반환
        }
    }


    // 특정 좌석의 활성 예약 가져오기
    public static Reservation getActiveReservationForSeat(int seatId) {
        Reservation reservation = null;
        String query = "SELECT * FROM reservations WHERE seat_id = ? AND is_completed = FALSE ORDER BY start_time DESC LIMIT 1";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, seatId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp startTime = rs.getTimestamp("start_time");
                Timestamp endTime = rs.getTimestamp("end_time");
                reservation = new Reservation(
                    rs.getInt("reservation_id"),
                    rs.getString("phone_number"),
                    rs.getInt("seat_id"),
                    startTime,
                    endTime,
                    rs.getBoolean("is_completed")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return reservation;
    }

    // 좌석 예약
    public static boolean reserveSeat(int seatId, String phoneNumber, Timestamp startTime, Timestamp endTime, int selectedMinutes) {
        String reservationQuery = "INSERT INTO reservations (phone_number, seat_id, start_time, end_time, is_completed) VALUES (?, ?, ?, ?, ?)";
        String updateSeatQuery = "UPDATE seats SET is_occupied = TRUE WHERE seat_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 예약 정보 삽입
            try (PreparedStatement stmt = conn.prepareStatement(reservationQuery)) {
                stmt.setString(1, phoneNumber);
                stmt.setInt(2, seatId);
                stmt.setTimestamp(3, startTime);
                stmt.setTimestamp(4, endTime);
                stmt.setBoolean(5, false); // 기본 상태 : 예약은 아직 완료되지 않음
                stmt.executeUpdate();
            }

            // 좌석 상태 업데이트
            try (PreparedStatement stmt = conn.prepareStatement(updateSeatQuery)) {
                stmt.setInt(1, seatId);
                stmt.executeUpdate();
            }

            // 잔여 시간이 아닌 경우 결제 정보 저장
            if (selectedMinutes > 0) {
                boolean paymentSuccess = recordPayment(phoneNumber, selectedMinutes, startTime); // 선택한 분만 결제 정보로 저장
                if (!paymentSuccess) {
                    System.out.println("결제 정보 저장 실패");
                    return false;  // 결제 정보 저장 실패 시 예약을 취소할 수 있음
                }
            }
         // 출입 기록 DB에 저장
            DatabaseHelper.logEntry(phoneNumber);
            return true; // 예약 성공
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 예약 실패
        }
    }
//결제 정보 저장
    private static boolean recordPayment(String phoneNumber, int minutes, Timestamp paymentDate) {
        String paymentQuery = "INSERT INTO payments (phone_number, reservation_id, amount, payment_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(paymentQuery)) {

            // 예약된 ID를 조회해서 넣기
            int reservationId = getLatestReservationId(phoneNumber, minutes); // 예약된 마지막 ID를 조회하는 함수 호출

            stmt.setString(1, phoneNumber);
            stmt.setInt(2, reservationId);  // 예약 ID를 사용
            stmt.setInt(3, minutes);        // 결제된 시간만큼 저장
            stmt.setTimestamp(4, paymentDate);
            stmt.executeUpdate();

            return true; // 결제 정보 저장 성공
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 결제 정보 저장 실패
        }
    }

    // 최신 예약 ID 조회
    private static int getLatestReservationId(String phoneNumber, int minutes) {
        String query = "SELECT reservation_id FROM reservations WHERE phone_number = ? ORDER BY reservation_id DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("reservation_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 최신 예약 ID가 없으면 -1 반환
    }




    // 좌석 예약 상태 업데이트
    public static void updateSeatStatus(int seatId, boolean isOccupied) {
        String query = "UPDATE seats SET is_occupied = ? WHERE seat_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isOccupied);
            stmt.setInt(2, seatId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 /*/
  * / 데이터베이스 URL 반환
    public static String getDbUrl() {
        return DB_URL;
    }

    // 데이터베이스 사용자 이름 반환
    public static String getDbUser() {
        return DB_USER;
    }

    // 데이터베이스 비밀번호 반환
    public static String getDbPassword() {
        return DB_PASSWORD;
        
       
    }
    
    
    */
    /*
     * 
     *  =======================================================메인 기능=========================================================
     *  
     *  
     *  
     *  
     *  
    */
    // 전화번호를 기반으로 사용자가 예약한 좌석의 seat_id를 반환하는 메소드
    public static int getSeatIdFromUser(String phoneNumber) {
        String query = "SELECT seat_id FROM reservations WHERE phone_number = ? AND is_completed = FALSE LIMIT 1";
        int seatId = -1; // 기본값 설정 (유효하지 않으면 -1)

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, phoneNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    seatId = rs.getInt("seat_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seatId;
    }
 // 전화번호 형식 검증 메소드 (010******** 형식)
 // 전화번호 형식 검증
    private static boolean isValidPhoneNumber(String phoneNumber) {
        // 전화번호가 "010"으로 시작하고, 나머지가 9자리 숫자인지 확인
        String regex = "^010\\d{8}$";
        return phoneNumber.matches(regex);
    }

    // 사용자 등록 메소드
    public static boolean registerUser(String phoneNumber, String name, String password) {
        // 전화번호 형식 검증
        if (!isValidPhoneNumber(phoneNumber)) {
            System.out.println("잘못된 전화번호 형식입니다. (010******** 형식이어야 함)");
            return false;
        }

        // 전화번호가 이미 존재하는지 확인
        if (isUserExist(phoneNumber)) {
            // 전화번호가 이미 존재하면 사용자 정보를 업데이트
            return updateUser(phoneNumber, name, password);
        } else {
            // 전화번호가 없으면 새로운 사용자 등록
            return insertNewUser(phoneNumber, name, password);
        }
    }

    // 새로운 사용자 삽입
    private static boolean insertNewUser(String phoneNumber, String name, String password) {
        String query = "INSERT INTO users (phone_number, password, name) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phoneNumber);
            stmt.setString(2, password); // 실제 환경에서는 비밀번호 암호화가 필요
            stmt.setString(3, name);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("사용자가 성공적으로 등록되었습니다.");
                return true;
            } else {
                System.out.println("사용자 등록에 실패했습니다.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 기존 사용자 업데이트
    private static boolean updateUser(String phoneNumber, String name, String password) {
        String query = "UPDATE users SET name = ?, password = ? WHERE phone_number = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, password); // 실제 환경에서는 비밀번호 암호화가 필요
            stmt.setString(3, phoneNumber);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("사용자 정보가 성공적으로 업데이트되었습니다.");
                return true;
            } else {
                System.out.println("사용자 정보 업데이트에 실패했습니다.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
// remaining_time을 조회하는 메서드
    public static int checkRemainingTime(String phoneNumber) {
        String query = "SELECT remaining_time FROM users WHERE phone_number = ?";
        int remainingTime = -1; // 기본값을 -1로 설정하여 전화번호가 존재하지 않으면 이를 구분할 수 있도록 함.

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // 전화번호를 파라미터로 설정
            stmt.setString(1, phoneNumber);

            // 쿼리 실행 및 결과 처리
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    remainingTime = rs.getInt("remaining_time");
                } else {
                    System.out.println("사용자를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return remainingTime;
    }
   //남은 시간 저장
    public static void saveRemainingTime(String phoneNumber) {
        String sql = "UPDATE users SET remaining_time = ? WHERE phone_number = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement psmt = conn.prepareStatement(sql)) {

            // 예시로 1시간을 저장 (다른 로직에 맞게 조정)
            psmt.setInt(1, 60); // 60분으로 설정
            psmt.setString(2, phoneNumber);

            int rowsUpdated = psmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("남은 시간이 성공적으로 저장되었습니다.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //=========================================================
   //관리자 메뉴 연결 기능
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
 // 출입 기록을 로그로 저장하는 메소드
    public static void logEntry(String phoneNumber) {
        String query = "INSERT INTO door_access_logs (phone_number, access_time) VALUES (?, CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // 전화번호를 PreparedStatement에 설정
            stmt.setString(1, phoneNumber);

            // SQL 실행
            stmt.executeUpdate();

            System.out.println("출입 기록이 성공적으로 저장되었습니다.");

        } catch (SQLException e) {
            System.err.println("출입 기록 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //=========================================================
   //sms 관련 기능
 // DB에 회원가입 메세지 저장을 위한 번호 등록 우선처리
    public static boolean insertUser(String phoneNumber) {
        // 먼저 사용자 존재 여부 확인
        if (isUserExist(phoneNumber)) {
            System.out.println("이미 존재하는 전화번호입니다.");
            return true; // 이미 존재하면 삽입하지 않음, 메시지 전송만 진행
        }
        
        String query = "INSERT INTO users (phone_number, name, password) VALUES (?, 'default_name', 'default_password')";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phoneNumber);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // 삽입이 성공한 경우 true 반환
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // 삽입 실패 시 false 반환
        }
    }

    private static boolean isUserExist(String phoneNumber) {
        String query = "SELECT COUNT(*) FROM users WHERE phone_number = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;  // 이미 존재하는 전화번호
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // 존재하지 않는 전화번호
    }

 // sms_notifications 테이블에 SMS 전송 정보 저장
    public static void insertSmsNotification(String phoneNumber, String message, boolean isSuccess)
 {
        String sql = "INSERT INTO sms_notifications (phone_number, message, send_time, status) VALUES (?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // 예시: DB 연결 후 쿼리 실행 (DB 연결 코드 추가)
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);
            stmt.setString(2, message);
            stmt.setTimestamp(3, now);
            stmt.setBoolean(4, false);  // 기본적으로 실패 상태로 설정

            stmt.executeUpdate();
            System.out.println("SMS 정보 저장 완료");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // 인증번호와 만료 시간을 DB에 저장하는 메서드
    public static void insertVerificationCode(String phoneNumber, String verificationCode) throws SQLException {
        String query = "INSERT INTO verification_codes (phone_number, verification_code, expiration_time) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phoneNumber);
            stmt.setString(2, verificationCode);  // 생성된 인증번호
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000));  // 5분 후 만료

            stmt.executeUpdate();
        }
    }
 // 인증번호 확인 메서드
    public static boolean isValidVerificationCode(String phoneNumber, String inputCode) {
        String query = "SELECT verification_code FROM verification_codes WHERE phone_number = ? AND expiration_time > NOW() ORDER BY expiration_time DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedCode = rs.getString("verification_code").trim();
                return storedCode.equals(inputCode.trim());  // 공백 제거 후 비교
            } else {
                return false;  // 인증번호가 없거나 만료된 경우
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
   //만료 시간이 5-6분 남은 예약 조회
    public static List<Reservation> getExpiringReservations(int minutesBeforeExpiryStart, int minutesBeforeExpiryEnd) {
        List<Reservation> expiringReservations = new ArrayList<>();
        String query = "SELECT reservation_id, phone_number, seat_id, start_time, end_time, is_completed " +
                       "FROM reservations " +
                       "WHERE is_completed = FALSE " +
                       "AND TIMESTAMPDIFF(MINUTE, NOW(), end_time) BETWEEN ? AND ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, minutesBeforeExpiryStart); // 5분
            pstmt.setInt(2, minutesBeforeExpiryEnd);   // 6분

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation(
                        rs.getInt("reservation_id"),
                        rs.getString("phone_number"),
                        rs.getInt("seat_id"),
                        rs.getTimestamp("start_time"),
                        rs.getTimestamp("end_time"),
                        rs.getBoolean("is_completed")
                    );
                    expiringReservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            System.err.println("만료된 예약 조회 중 오류 발생: " + e.getMessage());
        }
        return expiringReservations;
    }


}
