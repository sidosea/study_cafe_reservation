package com.studycafe;

import javax.swing.*;
import java.awt.*;

public class LoginPage {
    public void showLoginPage() {
        JFrame frame = new JFrame("로그인");
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

        JButton loginButton = new JButton("로그인");
        loginButton.setBounds(63, 208, 150, 40);
        loginButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        JButton backButton = new JButton("뒤로 가기");
        backButton.setBounds(233, 208, 150, 40);
        backButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        frame.getContentPane().add(phoneLabel);
        frame.getContentPane().add(phoneField);
        frame.getContentPane().add(passwordLabel);
        frame.getContentPane().add(passwordField);
        frame.getContentPane().add(loginButton);
        frame.getContentPane().add(backButton);

        frame.setVisible(true);

        loginButton.addActionListener(e -> {
            String phoneNumber = phoneField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (phoneNumber.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "전화번호와 비밀번호를 모두 입력해주세요.");
                return;
            }

            if (DatabaseHelper.verifyUser(phoneNumber, password)) {
                int seatId = DatabaseHelper.getSeatIdFromUser(phoneNumber);
                if (seatId != -1) {
                    // 이미 좌석이 예약되어 있으면 바로 결제 페이지로 이동
                    JOptionPane.showMessageDialog(frame, "이미 예약된 좌석이 있습니다.");
                    TimePaymentPage timePaymentPage = new TimePaymentPage();
                    timePaymentPage.showTimePaymentPage(seatId, phoneNumber);  // 결제 페이지로 이동
                    frame.dispose();  // 로그인 창 닫기
                } else {
                    // 좌석이 예약되지 않았다면 좌석 선택 페이지로 이동
                    JOptionPane.showMessageDialog(frame, "로그인 성공");
                    SeatSelectionPage seatSelectionPage = new SeatSelectionPage(phoneNumber);
                    seatSelectionPage.showSeatSelectionPage();
                    frame.dispose();  // 로그인 창 닫기
                }
            } else {
                JOptionPane.showMessageDialog(frame, "로그인 실패: 전화번호 또는 비밀번호를 확인하세요.");
            }
        });

        backButton.addActionListener(e -> {
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
            frame.dispose();
        });
    }
}
