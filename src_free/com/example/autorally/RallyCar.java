package com.example.autorally;

import java.util.Date;

public class RallyCar{
    private int _id;
    //private int rally_id;
    private Date date_time;
    private int car_number;
    private int car_rating;
    private double loc_longitude;
    private double loc_latitude;
    private String interview_note;
    private int interview_rating;
    private byte[] pic;
    private RallyEvent event;
    private String links;

    public RallyCar(int _id, Date date_time, int car_number,
            int car_rating, double loc_longitude, double loc_latitude,
            byte[] pic, int interview_rating, String interview_note, RallyEvent event) {
        super();
        this._id = _id;
        this.date_time = date_time;
        this.car_number = car_number;
        this.car_rating = car_rating;
        this.loc_longitude = loc_longitude;
        this.loc_latitude = loc_latitude;
        this.pic = pic;
        this.interview_rating = interview_rating;
        this.interview_note =  (interview_note != null) ? interview_note : "";
        this.event = event;
    }
    
    public int getId() {
        return _id;
    }

    public RallyCar(){
        this.car_rating = 3;
    }
    
    public Date getDate() {
        return date_time;
    }

    public void setDate(Date date_time) {
        this.date_time = date_time;
    }
    
    public void setDateLong(long date_time) {
        this.date_time = new Date(date_time);
    }

    public int getCarNumber() {
        return car_number;
    }

    public void setCarNumber(int car_number) {
        this.car_number = car_number;
    }

    public int getCarRating() {
        return car_rating;
    }

    public void setCarRating(int car_rating) {
        this.car_rating = car_rating;
    }

    public double getLocLat() {
        return loc_latitude;
    }

    public void setLocLat(double loc_latitude) {
        this.loc_latitude = loc_latitude;
    }
    public double getLocLong() {
        return loc_longitude;
    }

    public void setLocLong(double loc_longitude) {
        this.loc_longitude = loc_longitude;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

    public String getNote() {
        return interview_note;
    }

    public void setNote(String note) {
        this.interview_note = note;
    }
    
    public int getIntRating() {
        return interview_rating;
    }

    public void setIntRating(int interview_rating) {
        this.interview_rating = interview_rating;
    }
    
    public RallyEvent getEvent() {
        return event;
    }

    public void setEvent(RallyEvent event) {
        this.event = event;
    }

    public void setId(int newRowId) {
        this._id = newRowId;
    }

    public String getLinksStr() {
        return links;
    }

    public void setLinksStr(String links) {
        this.links = links;
    }
}

