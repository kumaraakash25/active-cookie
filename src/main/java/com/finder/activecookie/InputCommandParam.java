package com.finder.activecookie;

public enum InputCommandParam {

    FILE(0, "-f"),
    DATE(2, "-d");

    private int index;
    private String param;

    InputCommandParam(int index, String param) {
        this.index = index;
        this.param = param;
    }

    public String getParam() {
        return param;
    }

    public int getIndex() {
        return index;
    }
}
