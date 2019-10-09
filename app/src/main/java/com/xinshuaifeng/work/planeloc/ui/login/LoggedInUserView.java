package com.xinshuaifeng.work.planeloc.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String planeNo;
    private String tailNo;
    private String host;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String planeNo, String tailNo, String host) {
        this.planeNo = planeNo;
        this.tailNo = tailNo;
        this.host = host;
    }

    String getPlaneNo() {
        return this.planeNo;
    }

    String getTailNo() {
        return this.tailNo;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
