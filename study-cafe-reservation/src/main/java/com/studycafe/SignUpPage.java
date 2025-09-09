package com.studycafe;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Font;

public class SignUpPage {
    /**
     * 회원가입 페이지를 표시하는 메서드
     */
    public void showSignUpPage() {
        // 프레임 생성
        JFrame frame = new JFrame("회원가입");
        frame.setSize(500, 800); // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null); // 레이아웃 비활성화

        // 전화번호 레이블 및 입력 필드
        JLabel phoneLabel = new JLabel("전화번호:");
        phoneLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18));
        phoneLabel.setBounds(100, 100, 100, 30); // 좌표 및 크기 조정
        JTextField phoneField = new JTextField();
        phoneField.setBounds(200, 100, 200, 30);

        // 이름 레이블 및 입력 필드
        JLabel nameLabel = new JLabel("이름:");
        nameLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18));
        nameLabel.setBounds(100, 150, 100, 30);
        JTextField nameField = new JTextField();
        nameField.setBounds(200, 150, 200, 30);

        // 비밀번호 레이블 및 입력 필드
        JLabel passwordLabel = new JLabel("비밀번호:");
        passwordLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18));
        passwordLabel.setBounds(100, 200, 100, 30);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(200, 200, 200, 30);

        // 인증번호 레이블 및 입력 필드
        JLabel verificationLabel = new JLabel("인증번호:");
        verificationLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 18));
        verificationLabel.setBounds(100, 250, 100, 30);
        JTextField verificationField = new JTextField();
        verificationField.setBounds(200, 250, 200, 30);

        // 인증번호 발송 버튼
        JButton sendCodeButton = new JButton("인증번호 발송");
        sendCodeButton.setBounds(150, 300, 200, 40);
        sendCodeButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        // 회원가입 버튼
        JButton signUpButton = new JButton("회원가입");
        signUpButton.setBounds(100, 400, 150, 40);
        signUpButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        // 뒤로 가기 버튼
        JButton backButton = new JButton("뒤로 가기");
        backButton.setBounds(250, 400, 150, 40);
        backButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));

        // 프레임에 모든 컴포넌트 추가
        frame.getContentPane().add(phoneLabel);
        frame.getContentPane().add(phoneField);
        frame.getContentPane().add(nameLabel);
        frame.getContentPane().add(nameField);
        frame.getContentPane().add(passwordLabel);
        frame.getContentPane().add(passwordField);
        frame.getContentPane().add(verificationLabel);
        frame.getContentPane().add(verificationField);
        frame.getContentPane().add(sendCodeButton);
        frame.getContentPane().add(signUpButton);
        frame.getContentPane().add(backButton);

        frame.setVisible(true);

        // 인증번호 발송 버튼 동작
        sendCodeButton.addActionListener(e -> {
            String phoneNumber = phoneField.getText();

            // 전화번호 유효성 검사
            if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
                JOptionPane.showMessageDialog(frame, "올바른 전화번호를 입력하세요.");
                return;
            }

            SmsHelper.generateVerificationCode();
            SmsHelper.sendVerificationCode(phoneNumber);

            JOptionPane.showMessageDialog(frame, "인증번호가 발송되었습니다.");
        });

        // 회원가입 버튼 동작
        signUpButton.addActionListener(e -> {
            String phoneNumber = phoneField.getText();
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());
            String inputCode = verificationField.getText();

            // 필수 입력값 확인
            if (phoneNumber.isEmpty() || name.isEmpty() || password.isEmpty() || inputCode.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "모든 필드를 채워주세요.");
                return;
            }

            // 인증번호 확인 (DatabaseHelper에서 처리)
            if (!DatabaseHelper.isValidVerificationCode(phoneNumber, inputCode)) {
                JOptionPane.showMessageDialog(frame, "인증번호가 일치하지 않습니다.");
                return;
            }

            // 회원가입 처리
            if (DatabaseHelper.registerUser(phoneNumber, name, password)) {
                JOptionPane.showMessageDialog(frame, "회원가입이 완료되었습니다.");
                MainPage mainPage = new MainPage();
                mainPage.showMainPage();
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "회원가입 실패: 이미 존재하는 전화번호입니다.");
            }
        });


        // 뒤로 가기 버튼 동작
        backButton.addActionListener(e -> {
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
            frame.dispose();
        });
    }
}
