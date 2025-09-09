package com.studycafe;

import java.sql.Timestamp;

public class Reservation {
    private int reservationId; //DB 내용물 접근 제한
    private String phoneNumber;
    private int seatId;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean isCompleted;

    // 생성자, 데이터를 여러곳에서 불러오기 때문에 생성자를 사용해서 처리
    public Reservation(int reservationId, String phoneNumber, int seatId, Timestamp startTime, Timestamp endTime, boolean isCompleted) {
        this.reservationId = reservationId;
        this.phoneNumber = phoneNumber;
        this.seatId = seatId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isCompleted = isCompleted;
    }

    // 게터와 세터
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
