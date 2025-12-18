package models;

import java.util.Date;

public class Reservation {
private int reservationId;
private int memberId;
private int copyId;
private Date reservedAt;
private String status;

public Reservation() {}

    // Getters & Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getCopyId() { return copyId; }
    public void setCopyId(int copyId) { this.copyId = copyId; }

    public Date getReservedAt() { return reservedAt; }
    public void setReservedAt(Date reservedAt) { this.reservedAt = reservedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}