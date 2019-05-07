package com.example.foodyrestaurant;

import java.util.ArrayList;

public class ReservationDBRestaurant {

    private String reservationID;
    private String bikerID;
    private ArrayList<OrderItem> dishesOrdered;
    private boolean accepted;
    private String resNote;
    private String numberPhone;
    private String nameUser;
    private String orderTime;
    private String orderTimeBiker;

    public ReservationDBRestaurant() {
    }

    public ReservationDBRestaurant(String reservationID, String bikerID, ArrayList<OrderItem> dishesOrdered, boolean accepted, String resNote, String numberPhone, String nameUser, String orderTime, String orderTimeBiker) {
        this.reservationID = reservationID;
        this.bikerID = bikerID;
        this.dishesOrdered = dishesOrdered;
        this.accepted = accepted;
        this.resNote = resNote;
        this.numberPhone = numberPhone;
        this.nameUser = nameUser;
        this.orderTime = orderTime;
        this.orderTimeBiker = orderTimeBiker;
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }


    public String getReservationID() {
        return reservationID;
    }

    public void setReservationID(String reservationID) {
        this.reservationID = reservationID;
    }

    public String getBikerID() {
        return bikerID;
    }

    public void setBikerID(String bikerID) {
        this.bikerID = bikerID;
    }

    public ArrayList<OrderItem> getDishesOrdered() {
        return dishesOrdered;
    }

    public void setDishesOrdered(ArrayList<OrderItem> dishesOrdered) {
        this.dishesOrdered = dishesOrdered;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getResNote() {
        return resNote;
    }

    public void setResNote(String resNote) {
        this.resNote = resNote;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderTimeBiker() {
        return orderTimeBiker;
    }

    public void setOrderTimeBiker(String orderTimeBiker) {
        this.orderTimeBiker = orderTimeBiker;
    }
}
