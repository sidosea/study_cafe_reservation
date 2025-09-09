package com.studycafe;

import javax.swing.*;
import java.awt.*;

public class ReEntryPage {

    public void showReEntryPage() {
        JFrame frame = new JFrame("재입장하기");
        frame.setSize(500, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel phoneLabel = new JLabel("전화번호:");
        phoneLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18));
        phoneLabel.setBounds(63, 58, 100, 30);
        JTextField phoneField = new JTextField();
        phoneField.setBounds(183, 58, 200, 30);

        JLabel passwordLabel = new JLabel("비밀번호:");
        passwordLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18));
        passwordLabel.setBounds(63, 108, 100, 30);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(183, 108, 200, 30);

        JButton enterButton = new JButton("입장하기");
        enterButton.setBounds(63, 208, 150, 40);
        enterButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        JButton backButton = new JButton("돌아가기");
        backButton.setBounds(233, 208, 150, 40);
        backButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        frame.getContentPane().add(phoneLabel);
        frame.getContentPane().add(phoneField);
        frame.getContentPane().add(passwordLabel);
        frame.getContentPane().add(passwordField);
        frame.getContentPane().add(enterButton);
        frame.getContentPane().add(backButton);

        frame.setVisible(true);

        enterButton.addActionListener(e -> {
            String phoneNumber = phoneField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (phoneNumber.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "전화번호와 비밀번호를 모두 입력해주세요.");
                return;
            }

            if (DatabaseHelper.verifyUser(phoneNumber, password)) {
                int remainingTime = DatabaseHelper.checkRemainingTime(phoneNumber);
                if (remainingTime > 0) {
                    // 사용자가 예약한 좌석 ID를 DB에서 가져오기
                    Integer seatId = DatabaseHelper.getUserReservedSeat(phoneNumber);
                    if (seatId != -1) {
                        JOptionPane.showMessageDialog(frame, "문이 열렸습니다.");

                        // 출입 기록 DB에 저장
                        DatabaseHelper.logEntry(phoneNumber);

                        MainPage mainPage = new MainPage();
                        mainPage.showMainPage();
                        frame.dispose(); // 현재 프레임 닫기
                    } else {
                        JOptionPane.showMessageDialog(frame, "예약된 좌석이 없습니다.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "잔여 시간이 부족합니다. 충전 후 이용해주세요.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "로그인 실패: 전화번호 또는 비밀번호를 확인하세요.");
            }
        });

        backButton.addActionListener(e -> {
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
            frame.dispose(); // 현재 프레임 닫기
        });
    }
}
