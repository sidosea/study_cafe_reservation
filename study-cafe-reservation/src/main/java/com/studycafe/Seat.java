package com.studycafe;

public class Seat {
    private int seatId;
    private int seatNumber;
    private boolean isOccupied;

    public Seat(int seatId, int seatNumber, boolean isOccupied) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.isOccupied = isOccupied;
    }

    public int getSeatId() {
        return seatId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public boolean isOccupied() {
        return isOccupied;
    }
}
