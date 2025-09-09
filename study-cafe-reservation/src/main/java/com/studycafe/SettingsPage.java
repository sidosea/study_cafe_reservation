package com.studycafe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SettingsPage extends JFrame {
    private JComboBox<String> logTypeComboBox;
    private JTextField searchField;
    private JButton searchButton;
    private JTable logTable;
    private DefaultTableModel tableModel;

    public SettingsPage() {
        setTitle("관리자 메뉴 - 로그 조회 및 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널: 로그 유형 선택 및 검색 기능
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel logTypeLabel = new JLabel("로그 유형:");
        logTypeComboBox = new JComboBox<>(new String[]{
            "결제 로그", "출입 로그", "SMS 알림 로그", "월별 통계 로그"
        });
        searchField = new JTextField(20);
        searchButton = new JButton("검색");

        topPanel.add(logTypeLabel);
        topPanel.add(logTypeComboBox);
        topPanel.add(searchField);
        topPanel.add(searchButton);

        // 테이블: 로그 데이터를 표시
        tableModel = new DefaultTableModel(new String[]{
            "컬럼1", "컬럼2", "컬럼3", "컬럼4"
        }, 0);
        logTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(logTable);

        // 하단 패널: 상태 표시
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("로그를 선택하고 검색하세요.");

        bottomPanel.add(statusLabel);

        // 뒤로 가기 버튼 추가
        JButton backButton = new JButton("돌아가기");
        backButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 16));
        bottomPanel.add(backButton);

        backButton.addActionListener(e -> {
            // MainPage로 돌아가기
            MainPage mainPage = new MainPage();
            mainPage.showMainPage();
            dispose(); // 현재 프레임 닫기
        });

        // 이벤트 처리
        searchButton.addActionListener(e -> performSearch());
        logTypeComboBox.addActionListener(e -> updateTableColumns());

        // 메인 프레임에 컴포넌트 추가
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    private void updateTableColumns() {
        String selectedLogType = (String) logTypeComboBox.getSelectedItem();
        switch (selectedLogType) {
            case "결제 로그":
                tableModel.setColumnIdentifiers(new String[]{
                    "결제 ID", "전화번호", "예약 ID", "결제 일시"
                });
                break;
            case "출입 로그":
                tableModel.setColumnIdentifiers(new String[]{
                    "출입 ID", "전화번호", "출입 시각"
                });
                break;
            case "SMS 알림 로그":
                tableModel.setColumnIdentifiers(new String[]{
                    "SMS ID", "전화번호", "메시지", "전송 시간", "전송 상태"
                });
                break;
            case "월별 통계 로그":
                tableModel.setColumnIdentifiers(new String[]{
                    "통계 ID", "좌석 ID", "월-연도", "사용 시간", "예약 건수", "활동 시간대"
                });
                break;
        }
        tableModel.setRowCount(0); // 기존 데이터 초기화
        performSearch(); // 선택된 로그 유형에 맞게 데이터 다시 로드
    }
    
    // 이벤트 처리 메서드, 여기에서만 쓰는 기능, databaseheleper로 옮기지 않음 -> 더 복잡해짐
    private void performSearch() {
        String selectedLogType = (String) logTypeComboBox.getSelectedItem();
        String searchText = searchField.getText().trim(); // 검색어

        String query = "";
        String[] columns = {};

        switch (selectedLogType) {
            case "결제 로그":
                query = "SELECT payment_id, phone_number, reservation_id, payment_date " +
                        "FROM payments WHERE phone_number LIKE ?";
                columns = new String[]{"결제 ID", "전화번호", "예약 ID", "결제 일시"};
                break;

            case "출입 로그":
                query = "SELECT access_id, phone_number, access_time " +
                        "FROM door_access_logs WHERE phone_number LIKE ?";
                columns = new String[]{"출입 ID", "전화번호", "출입 시각"};
                break;

            case "SMS 알림 로그":
                query = "SELECT sms_id, phone_number, message, send_time, status " +
                        "FROM sms_notifications WHERE phone_number LIKE ?";
                columns = new String[]{"SMS ID", "전화번호", "메시지", "전송 시간", "전송 상태"};
                break;

            case "월별 통계 로그":
                query = "SELECT stat_id, seat_id, month_year, total_usage_time, total_reservations " +
                        "FROM monthly_statistics WHERE month_year LIKE ?";
                columns = new String[]{"통계 ID", "좌석 ID", "월-연도", "사용 시간", "예약 건수"};
                break;

            default:
                JOptionPane.showMessageDialog(this, "알 수 없는 로그 유형입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
        }

        // 테이블 초기화
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchText + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] rowData = new Object[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    rowData[i] = rs.getObject(i + 1); // ResultSet의 컬럼 인덱스는 1부터 시작
                }
                tableModel.addRow(rowData);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "검색 결과가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }


}
