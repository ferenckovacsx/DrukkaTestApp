package com.example.drukkatestapp.pojo;

/**
 * Created by ferenckovacsx on 2018-03-01.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponsePOJO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("emai")
    @Expose
    private String emai;
    @SerializedName("created")
    @Expose
    private Integer created;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmai() {
        return emai;
    }

    public void setEmai(String emai) {
        this.emai = emai;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }


}
