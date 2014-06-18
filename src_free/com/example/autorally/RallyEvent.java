package com.example.autorally;

public class RallyEvent{
    private int _id;
    private String event;
    private int team;
    private String location_name;
    private int cars;
    
    public RallyEvent(){}
    public RallyEvent (String event, int team, String loc_str, int cars){
        this.event = event;
        this.team = team;
        this.cars = cars;
        this.location_name = loc_str;
    }
    public RallyEvent (int id, String event, int team, String loc_str, int _cars){
        this(event, team, loc_str, _cars);
        this._id = id;
    }
    
    public String getEvent() {
        return (event != null) ? event : "";
    }
    public void setEvent(String event) {
        this.event = event;
    }
    public int getTeam() {
        return team;
    }
    public void setTeam(int team) {
        this.team = team;
    }
    public String getLocationName() {
        return (location_name != null) ? location_name : "";
    }
    public void setLocationName(String loc_str) {
        this.location_name = loc_str;
    }
    public void setId(int newRowId) {
        this._id = newRowId;
    }
    public int getId() {
        return _id;
    }

    public void setCars(int cars_col) {
        this.cars = cars_col;
    }
    public int getCars() {
        return cars;
    }
}
