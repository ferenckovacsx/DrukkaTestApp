package com.example.drukkatestapp.pojo;

/**
 * Created by ferenckovacsx on 2018-03-02.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeleteFilePOJO {

    public DeleteFilePOJO(String uuid) {
        this.uuid = uuid;
    }

    @SerializedName("uuid")
    @Expose
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
