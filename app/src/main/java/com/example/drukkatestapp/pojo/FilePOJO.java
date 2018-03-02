package com.example.drukkatestapp.pojo;

/**
 * Created by ferenckovacsx on 2018-03-01.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FilePOJO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("filename")
    @Expose
    private String filename;
    @SerializedName("size")
    @Expose
    private Integer size;
    @SerializedName("created")
    @Expose
    private Integer created;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public FilePOJO(String uuid, String filename, Integer size, Integer created) {
        this.uuid = uuid;
        this.filename = filename;
        this.size = size;
        this.created = created;
    }

    public FilePOJO() {
    }
}
