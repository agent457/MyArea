package com.example.myarea;

import java.util.List;

public class POI {
    private int id;
    private String name, description;
    private double Long, Lat;

    public POI(int id, String name, String description, double Long, double Lat){
        this.id = id;
        this.name = name;
        this.description = description;
        this.Long = Long;
        this.Lat = Lat;
    }
}
