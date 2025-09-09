package com.studycafe;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainPage {
    // 스케줄러를 클래스 멤버 변수로 선언
    private ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        MainPage mainPage = new MainPage();
        mainPage.startReminderScheduler(); // 스케줄러 시작
        mainPage.showMainPage(); // 메인 페이지 표시
    }

    public void showMainPage() {
        // 프레임 설정
        JFrame frame = new JFrame("Study Cafe Reservation");
        frame.setSize(500, 800); // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setBackground(new Color(192, 192, 192));
        panel.setLayout(null); // 레이아웃 사용하지 않음

        // 이미지 추가
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resources/images/logo_2.png"));
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setBounds(135, 50, 227, 158); // 이미지의 위치와 크기 설정
            panel.add(logoLabel);
        } catch (Exception e) {
            System.out.println("이미지 로딩 실패: " + e.getMessage());
        }

        // 버튼 생성 및 위치/크기 설정
        JButton reEntryButton = new JButton("재입장");
        JButton exitButton = new JButton("퇴실하기");
        JButton loginButton = new JButton("로그인 및 결제");
        JButton signUpButton = new JButton("회원가입");
        JButton seatButton = new JButton("좌석현황");
        JButton settingsButton = new JButton("관리자");

        JButton[] buttons = {reEntryButton, exitButton, loginButton, signUpButton, seatButton};
        int yPosition = 247;
        for (JButton button : buttons) {
            button.setBounds(50, yPosition, 400, 60);
            button.setBackground(Color.WHITE);
            panel.add(button);
            yPosition += 80;
        }

        settingsButton.setBounds(378, 13, 94, 30);
        settingsButton.setBackground(Color.WHITE);
        panel.add(settingsButton);

        // 버튼 이벤트 리스너 추가
        reEntryButton.addActionListener(e -> navigateTo(new ReEntryPage(), frame));
        exitButton.addActionListener(e -> navigateTo(new ExitPage(), frame));
        loginButton.addActionListener(e -> navigateTo(new LoginPage(), frame));
        signUpButton.addActionListener(e -> navigateTo(new SignUpPage(), frame));
        seatButton.addActionListener(e -> navigateTo(new SeatSelectionPage(null), frame));
        settingsButton.addActionListener(e -> openAdminPage(frame));

        // 현재 시간 표시를 위한 레이블 추가
        JLabel timeLabel = new JLabel();
        timeLabel.setBounds(12, 10, 200, 30);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        panel.add(timeLabel);

        // 시간 업데이트를 위한 타이머 설정
        new Timer(1000, e -> updateCurrentTime(timeLabel)).start();

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    // 현재 시간 업데이트
    private void updateCurrentTime(JLabel timeLabel) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(new Date());
        timeLabel.setText(currentTime);
    }

    // 관리자 페이지 열기
    private void openAdminPage(JFrame frame) {
        String adminCode = JOptionPane.showInputDialog(frame, "관리번호를 입력하세요:", "관리자 인증", JOptionPane.PLAIN_MESSAGE);
        if ("1111".equals(adminCode)) {
            new SettingsPage().setVisible(true);
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(frame, "잘못된 관리번호입니다.", "인증 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 페이지 전환
    private void navigateTo(Object page, JFrame frame) {
        if (page instanceof ReEntryPage) ((ReEntryPage) page).showReEntryPage();
        else if (page instanceof ExitPage) ((ExitPage) page).showExitPage();
        else if (page instanceof LoginPage) ((LoginPage) page).showLoginPage();
        else if (page instanceof SignUpPage) ((SignUpPage) page).showSignUpPage();
        else if (page instanceof SeatSelectionPage) ((SeatSelectionPage) page).showSeatSelectionPage();
        frame.dispose();
    }

    // 사용 만료 5분 전 알림 스케줄러를 시작하는 메서드
    public void startReminderScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[스케줄러] 만료 알림 작업 실행 중...");
            SmsHelper.sendExpiryReminder();
        }, 0, 1, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[스케줄러] 종료");
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }));
    }
}
