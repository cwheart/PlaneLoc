package com.xinshuaifeng.work.planeloc;

public class AirNav {

    public native String submitData(String planeNo,
                                    float altitude,
                                    float latitude,
                                    float longitude,
                                    float pressure,
                                    float speed);

    public native void connect(String host);

    static {
        System.loadLibrary("airnav-lib");
    }

}
