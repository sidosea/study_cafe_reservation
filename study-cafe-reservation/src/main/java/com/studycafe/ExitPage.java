package com.studycafe;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExitPage {
    // 기존에 정의한 showExitPage() 메서드 내용
    public void showExitPage() {
        JFrame frame = new JFrame("퇴실 페이지");
        frame.setSize(500, 800); // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // 전화번호 레이블 및 입력 필드 생성
        JLabel phoneLabel = new JLabel("전화번호:");
        phoneLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18)); // 폰트 크기 설정
        phoneLabel.setBounds(63, 58, 100, 30); // 위치와 크기 설정
        JTextField phoneField = new JTextField();
        phoneField.setBounds(183, 58, 200, 30); // 위치와 크기 설정
        panel.add(phoneLabel);
        panel.add(phoneField);

        // 비밀번호 레이블 및 입력 필드 생성
        JLabel passwordLabel = new JLabel("비밀번호:");
        passwordLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18)); // 폰트 크기 설정
        passwordLabel.setBounds(63, 108, 100, 30); // 위치와 크기 설정
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(183, 108, 200, 30); // 위치와 크기 설정
        panel.add(passwordLabel);
        panel.add(passwordField);

        // 퇴실 버튼
        JButton exitButton = new JButton("퇴실하기");
        exitButton.setBounds(100, 300, 140, 50); // 위치 및 크기 수정
        exitButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16)); // 폰트 설정
        panel.add(exitButton);

        // 취소 버튼
        JButton cancelButton = new JButton("돌아가기");
        cancelButton.setBounds(260, 300, 140, 50); // 위치 및 크기 수정
        cancelButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16)); // 폰트 설정
        panel.add(cancelButton);

        // 퇴실 버튼 클릭 시 동작
        exitButton.addActionListener(e -> {
            String phoneNumber = phoneField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (phoneNumber.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "전화번호와 비밀번호를 모두 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DatabaseHelper.verifyUser(phoneNumber, password)) {
                Integer seatId = DatabaseHelper.getUserReservedSeat(phoneNumber);

                if (seatId == null) {
                    JOptionPane.showMessageDialog(null, "예약된 좌석이 없습니다. 퇴실할 좌석이 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 퇴실 처리
                boolean success = DatabaseHelper.exitSeat(seatId, phoneNumber);

                if (success) {
                    JOptionPane.showMessageDialog(null, "퇴실 처리가 완료되었습니다. 메인 화면으로 이동합니다.", "퇴실 완료", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose(); // 현재 창 닫기
                    new MainPage().showMainPage(); // 메인 화면 열기
                }
            } else {
                JOptionPane.showMessageDialog(frame, "전화번호 또는 비밀번호가 잘못되었습니다. 다시 확인해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });



        // 취소 버튼 클릭 시 동작
        cancelButton.addActionListener(e -> {
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
            frame.dispose(); // 돌아가기 버튼 클릭 시 창 닫기
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
        
     // 현재 시간 표시를 위한 레이블 추가
        JLabel timeLabel = new JLabel();
        timeLabel.setBounds(0, 10, 200, 30); // 시간 레이블 위치 및 크기 설정
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 20));  // 글씨 크기 설정
        panel.add(timeLabel);

        // 시간 업데이트를 위한 타이머 설정
        Timer timer = new Timer(1000, e -> {
            // 현재 시간을 가져와서 포맷팅
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String currentTime = sdf.format(new Date());
            timeLabel.setText(currentTime);
        });
        timer.start();  // 타이머 시작

    }
}
