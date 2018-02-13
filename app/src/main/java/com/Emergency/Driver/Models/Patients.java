package com.Emergency.Driver.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kamlesh on 13-02-2018.
 */

public class Patients {
    Map<String,Patient> map =new HashMap<>();


    public Patients() {
        super();
    }

    public Map<String, Patient> getMap() {
        return map;
    }

    public void setMap(Map<String, Patient> map) {
        this.map = map;
    }
}
