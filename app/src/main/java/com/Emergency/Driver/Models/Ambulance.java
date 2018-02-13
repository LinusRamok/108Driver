package com.Emergency.Driver.Models;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class Ambulance {

    private Locate locate;
    private ambStatus AmbStatus;


    public Ambulance() {
        super();
    }

    public Ambulance(Locate locate) {

        this.locate=locate;
    }

    public Locate getLocate() {
        return locate;
    }



    public ambStatus getAmbstatus() {
        return AmbStatus;
    }

    public void setAmbstatus(ambStatus ambstatus) {
        AmbStatus = ambstatus;
    }
}
