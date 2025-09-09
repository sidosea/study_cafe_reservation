package com.studycafe;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SeatSelectionPage {

    private String phoneNumber;

    public SeatSelectionPage(String phoneNumber) {
        this.phoneNumber = phoneNumber; // 로그인된 사용자 정보 전달
    }

    public void showSeatSelectionPage() {
        JFrame frame = new JFrame("좌석 선택");
        frame.setSize(500, 800); // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // 좌석 데이터 가져오기 (20개로 제한)
        List<Seat> seats = DatabaseHelper.getSeats();
        int maxSeats = Math.min(seats.size(), 20);  // 최대 20개 좌석만 가져옴

        int x = 20, y = 100, width = 60, height = 60;  // 버튼 크기 더 작게 설정
        int gap = 20; // 버튼 간격

        // 선택된 좌석 ID 저장 변수
        final int[] selectedSeatId = { -1 }; // 초기값은 -1로 설정

        // 좌석 버튼 배치
        for (int i = 0; i < maxSeats; i++) {
            Seat seat = seats.get(i);
            JButton seatButton = new JButton(String.valueOf(seat.getSeatNumber()));

            seatButton.setBounds(x, y, width, height);

            // 좌석 상태에 따라 색상 설정
            if (seat.isOccupied()) {
                seatButton.setBackground(Color.RED);
                seatButton.setEnabled(false); // 이미 점유된 좌석은 선택 불가
            } else {
                seatButton.setBackground(Color.GREEN);
            }

            // 좌석 ID를 버튼에 저장
            seatButton.putClientProperty("seatId", seat.getSeatId());

            // 버튼 클릭 이벤트 설정
            seatButton.addActionListener(e -> {
                int seatId = (int) seatButton.getClientProperty("seatId");

                // 선택된 좌석 ID 업데이트
                selectedSeatId[0] = seatId;
                JOptionPane.showMessageDialog(frame, "좌석 " + seatId + " 선택됨.");
            });

            panel.add(seatButton);

            // 좌석 버튼 위치 업데이트 (버튼 간격 조정)
            x += width + gap;
            if (x + width > frame.getWidth() - gap) {  // 화면 너비를 넘으면 줄 바꿈
                x = 20;
                y += height + gap;
            }
        }

        // 확인 버튼
        JButton confirmButton = new JButton("예약하기");
        confirmButton.setBounds(20, y + height + gap, frame.getWidth() - 40, 50);
        confirmButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 20));

        // 확인 버튼 클릭 이벤트
        confirmButton.addActionListener(e -> {
            if (selectedSeatId[0] == -1) {
                JOptionPane.showMessageDialog(frame, "좌석을 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            } else {
                if (phoneNumber != null) { // 로그인한 사용자일 경우
                    frame.dispose(); // 현재 창 닫기
                    TimePaymentPage timePaymentPage = new TimePaymentPage();
                    timePaymentPage.showTimePaymentPage(selectedSeatId[0], phoneNumber); // 선택한 좌석과 전화번호 전달
                } else { // 비로그인 상태
                    JOptionPane.showMessageDialog(frame, "로그인 후 좌석을 선택해주세요.");
                }
            }
        });
        panel.add(confirmButton);

        // 뒤로 가기 버튼
        JButton backButton = new JButton("메인 페이지로");
        backButton.setBounds(20, y + height + gap + 60, frame.getWidth() - 40, 50);
        backButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 20));

        backButton.addActionListener(e -> {
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
            frame.dispose();
        });
        panel.add(backButton);

        frame.add(panel);
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
