package com.studycafe;

// https://github.com/coolsms/coolsms-java-examples 참고

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import java.util.Random;
import java.util.List;

public class SmsHelper {
    private static final String API_KEY = "****";
    private static final String API_SECRET = "****";
    private static final DefaultMessageService messageService;

    static {
        // API 키와 비밀 키 설정
        messageService = NurigoApp.INSTANCE.initialize(API_KEY, API_SECRET, "https://api.coolsms.co.kr");
    }

    // 인증번호 생성 메서드
    public static String generateVerificationCode() {
        Random rand = new Random();
        StringBuilder verificationCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            verificationCode.append(rand.nextInt(10)); // 0~9 사이의 숫자 추가
        }
        return verificationCode.toString();
    }

    // SMS 전송 메서드
    public static void sendSms(String phoneNumber, String messageText) {
        Message message = new Message();
        message.setFrom("01047020231"); // 발신 번호 설정
        message.setTo(phoneNumber);    // 수신 번호 설정
        message.setText(messageText);  // 메시지 내용 설정

        boolean isSuccess = false; // 전송 성공 여부

        try {
            // 메시지 발송
            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
            isSuccess = true;
            System.out.println("SMS 발송 성공: " + response);
        } catch (Exception e) {
            System.err.println("SMS 발송 실패: " + e.getMessage());
        } finally {
            // 전송 정보 DB 저장
            DatabaseHelper.insertSmsNotification(phoneNumber, messageText, isSuccess);
        }
    }

    // 인증번호 발송 메서드
    public static void sendVerificationCode(String phoneNumber) {
        // 1. 먼저 phoneNumber를 users 테이블에 등록
        try {
            boolean isInserted = DatabaseHelper.insertUser(phoneNumber);  // 사용자 등록 메서드 호출
            if (!isInserted) {
                System.err.println("사용자 등록 실패");
                return;
            }
        } catch (Exception e) {
            System.err.println("사용자 등록 중 오류 발생: " + e.getMessage());
            return;
        }

        // 2. 인증번호 생성
        String verificationCode = generateVerificationCode();
        String message = String.format("[서원스터디카페] 인증번호 %s", verificationCode);

        // 3. SMS 발송
        sendSms(phoneNumber, message);

        // 4. 인증번호와 SMS 발송 정보를 DB에 저장
        try {
            DatabaseHelper.insertVerificationCode(phoneNumber, verificationCode);
            System.out.println("인증번호 DB 저장 완료");
        } catch (Exception e) {
            System.err.println("DB 저장 실패: " + e.getMessage());
        }
    }

    // 사용 시간 만료 5분 전 알림 메서드
    public static void sendExpiryReminder() {
        try {
            // 5-6분 남은 사용자만 조회
            List<Reservation> reservations = DatabaseHelper.getExpiringReservations(5, 6);

            for (Reservation reservation : reservations) {
                String phoneNumber = reservation.getPhoneNumber();
                int seatId = reservation.getSeatId();
                String message = String.format("[서원스터디카페] %d번 좌석이 5분 후 만료됩니다", seatId);

                // SMS 전송
                sendSms(phoneNumber, message);
                System.out.println(seatId + "번 좌석 5분 전 알림 전송 완료: " + phoneNumber);
            }
        } catch (Exception e) {
            System.err.println("5분 전 알림 전송 중 오류 발생: " + e.getMessage());
        }
    }
}
