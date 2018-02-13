package com.Emergency.Driver.Models;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class Patient {

    private Locate locate;
    private String PatientStatus;

    public Patient() {
        super();
    }

    public Locate getLocate() {
        return locate;
    }

    public String getPatientStatus() {
        return PatientStatus;
    }

    public void setLocate(Locate locate) {
        this.locate = locate;
    }

    public void setPatientStatus(String patientStatus) {
        PatientStatus = patientStatus;
    }
}


