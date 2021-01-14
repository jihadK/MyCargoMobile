package com.example.mycargo.util;

import android.location.Location;

public class SendLocationToAllActivity {
    private Location location;

    public SendLocationToAllActivity(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
