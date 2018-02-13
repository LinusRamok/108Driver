package com.Emergency.Driver.Models;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class ambStatus {
    private String status;
    private int busytime;
    private String allottedPatient;


    public ambStatus() {
        super();
    }

    public ambStatus(String status) {
        this.status=status;
    }
    public ambStatus(String status,int busytime,String allottedPatient) {
        this.status=status;
        this.busytime=busytime;
        this.allottedPatient=allottedPatient;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBusytime() {
        return busytime;
    }

    public void setAllottedPatient(String allottedPatient) {
        this.allottedPatient = allottedPatient;
    }

    public void setBusytime(int busytime) {
        this.busytime = busytime;
    }

    public String getAllottedPatient() {
        return allottedPatient;
    }


}
