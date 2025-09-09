package com.studycafe;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TimePaymentPage {

    private int selectedMinutes = 0; // 선택된 시간 (분 단위로 저장)

    public void showTimePaymentPage(int seatId, String phoneNumber) {
        // 프레임 생성
        JFrame frame = new JFrame("시간 결제");
        frame.setSize(500, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 패널 생성
        JPanel panel = new JPanel(null); // 레이아웃 없이 절대 위치 사용

        // 제목 레이블
        JLabel label = new JLabel("사용 시간을 선택해주세요:");
        label.setBounds(20, 20, 460, 40);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("나눔바른고딕", Font.BOLD, 16));
        panel.add(label);

        // 버튼 생성 및 배치
        String[] timeOptions = {
            "1시간 (2,000원)", "2시간 (3,000원)", "3시간 (4,000원)", 
            "4시간 (5,000원)", "6시간 (7,000원)", "10시간 (8,000원)", 
            "12시간 (10,000원)"
        };
        int[] minutesArray = {60, 120, 180, 240, 360, 600, 720}; // 각 버튼의 분 매핑
        int x = 20, y = 80, buttonWidth = 440, buttonHeight = 50;

        for (int i = 0; i < timeOptions.length; i++) {
            JButton timeButton = new JButton(timeOptions[i]);
            timeButton.setBounds(x, y, buttonWidth, buttonHeight);
            int minutes = minutesArray[i]; // 버튼에 연결된 분

            timeButton.addActionListener(e -> {
                selectedMinutes = minutes; // 선택된 분 업데이트
                JOptionPane.showMessageDialog(frame, minutes + "분이 선택되었습니다.");
            });

            panel.add(timeButton);
            y += buttonHeight + 10; // 버튼 아래 간격
        }

        // 잔여시간 사용 버튼
        JButton useRemainingTimeButton = new JButton("잔여시간 사용");
        useRemainingTimeButton.setBounds(x, y, buttonWidth, buttonHeight);
        useRemainingTimeButton.addActionListener(e -> {
            int remainingTime = DatabaseHelper.checkRemainingTime(phoneNumber); // 남은 시간 조회 (분 단위로 처리)
            if (remainingTime > 0) {
                int minutesToAdd = 0;
                int result = JOptionPane.showConfirmDialog(frame,
                    "현재 잔여시간은 " + remainingTime + "분입니다. 추가 시간을 선택하시겠습니까?", 
                    "잔여시간 사용", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    // 추가 시간을 선택하도록 유도
                    String[] timeOptionsForAdd = {"60분", "120분", "180분", "240분", "360분", "600분", "720분"};
                    String selectedOption = (String) JOptionPane.showInputDialog(frame, 
                        "추가할 시간을 선택하세요:", "추가 시간 선택", JOptionPane.PLAIN_MESSAGE,
                        null, timeOptionsForAdd, timeOptionsForAdd[0]);

                    if (selectedOption != null) {
                        minutesToAdd = Integer.parseInt(selectedOption.replace("분", ""));
                        if (remainingTime >= minutesToAdd) {
                            selectedMinutes = minutesToAdd+remainingTime; // 선택한 시간을 분 단위로 저장
                            JOptionPane.showMessageDialog(frame, selectedMinutes + "분이 선택되었습니다.");
                            reserveSeat(seatId, phoneNumber, selectedMinutes, frame); // 분 단위로 예약 처리
                        } else {
                            JOptionPane.showMessageDialog(frame, "잔여 시간이 부족합니다.", "경고", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {
                    // "아니오"를 선택하면 잔여시간을 선택하지 않고 바로 결제 진행
                    selectedMinutes = remainingTime; // 남은 시간을 바로 사용
                    JOptionPane.showMessageDialog(frame, remainingTime + "분이 선택되었습니다.");
                    reserveSeat(seatId, phoneNumber, selectedMinutes, frame); // 분 단위로 예약 처리
                }
            } else {
                JOptionPane.showMessageDialog(frame, "잔여 시간이 없습니다.", "알림", JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(useRemainingTimeButton);
        y += buttonHeight + 30; // 버튼 아래 간격

        // 결제하기 버튼
        JButton payButton = new JButton("결제하기");
        payButton.setBounds(x, y, buttonWidth, buttonHeight);
        payButton.setFont(new Font("나눔바른고딕", Font.BOLD, 18));

        payButton.addActionListener(e -> {
            if (selectedMinutes > 0) {  // selectedMinutes로 사용 시간 확인
                reserveSeat(seatId, phoneNumber, selectedMinutes, frame); // 분 단위로 예약
            } else {
                JOptionPane.showMessageDialog(frame, "사용 시간을 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(payButton);

        // 패널과 프레임 연결
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void reserveSeat(int seatId, String phoneNumber, int minutes, JFrame frame) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(minutes); // 분 단위로 시간 계산

        // 예약 처리
        boolean success = DatabaseHelper.reserveSeat(seatId, phoneNumber, Timestamp.valueOf(startTime), Timestamp.valueOf(endTime),selectedMinutes);
        if (success) {
            // 결제 완료 후 잔여 시간 갱신
            int timeUpdated = DatabaseHelper.updateRemainingTime(phoneNumber, minutes);
            if (timeUpdated > 0) {
                JOptionPane.showMessageDialog(frame, "결제가 완료되었습니다. 좌석이 예약되었습니다.");
                // 예약된 좌석과 시간을 로그로 출력
                System.out.println(phoneNumber + " 사용자가 " + seatId + "번 좌석을 " + startTime + "부터 " + endTime + "까지 사용 시작했습니다.");
            } else {
                JOptionPane.showMessageDialog(frame, "결제 완료 후 잔여 시간 업데이트 실패.", "경고", JOptionPane.WARNING_MESSAGE);
            }

            frame.dispose();
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
        } else {
            JOptionPane.showMessageDialog(frame, "결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
}
