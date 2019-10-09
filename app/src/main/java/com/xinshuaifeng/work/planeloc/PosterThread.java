package com.xinshuaifeng.work.planeloc;

import android.location.Location;
import android.util.Log;

public class PosterThread implements Runnable {
    private AirNav airNav = new AirNav();
    private LocationData loc;
    private String host;

    @Override
    public void run() {
        long threadId = Thread.currentThread().getId();
        Log.i("custom", "start thread");
//        airNav.connect(host);

        boolean flag = true;
        while (flag) {
            String result = airNav.submitData(this.loc.getPlaneNo(),
                    loc.getAltitude(),
                    loc.getLatitude(),
                    loc.getLongitude(),
                    loc.getPressureAltitude(),
                    loc.getSpeed());
            Log.i("custom", "Sended...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public LocationData getLoc() {
        return loc;
    }

    public void setLoc(LocationData loc) {
        this.loc = loc;
    }

    public void setHost(String host) {
        this.host = host;
        airNav.connect(host);
    }
}
