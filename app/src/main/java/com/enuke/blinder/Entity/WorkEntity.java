package com.enuke.blinder.Entity;

/**
 * Created by nitesh on 1/9/15.
 */
public class WorkEntity {

    private String workid;
    private String userid;
    private String workdescription;
    private String workemployer;
    private String worklocation;

    public String getWorkid() {
        return workid;
    }

    public void setWorkid(String workid) {
        this.workid = workid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getWorkdescription() {
        return workdescription;
    }

    public void setWorkdescription(String workdescription) {
        this.workdescription = workdescription;
    }

    public String getWorkemployer() {
        return workemployer;
    }

    public void setWorkemployer(String workemployer) {
        this.workemployer = workemployer;
    }

    public String getWorklocation() {
        return worklocation;
    }

    public void setWorklocation(String worklocation) {
        this.worklocation = worklocation;
    }
}
