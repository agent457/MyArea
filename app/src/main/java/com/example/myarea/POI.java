package com.example.myarea;

public class POI implements Comparable<POI>{
    private final int id;
    private final String name;
    private final String description;
    private final double Long, Lat;

    public POI(int id, String name, String description, double Long, double Lat){
        this.id = id;
        this.name = name;
        this.description = description;
        this.Long = Long;
        this.Lat = Lat;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;

    }

    public double getLong() {
        return this.Long;
    }

    public double getLat() {
        return this.Lat;
    }

    @Override
    public int compareTo(POI o) {
        return o.id - this.id;
    }
}
