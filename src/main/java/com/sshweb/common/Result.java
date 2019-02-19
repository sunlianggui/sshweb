package com.sshweb.common;

/**
 * Created by Administrator on 2019/1/23.
 */
public class Result {

    private String id;
    private String status;
    private String statusText;
    private String data;

    public Result(String id, String status, String statusText, String data) {
        this.id = id;
        this.status = status;
        this.statusText = statusText;
        this.data = data;
    }

    public Result(String id, String status, String statusText) {
        this.id = id;
        this.status = status;
        this.statusText = statusText;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
