package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/19.
 */
public class Project implements Serializable{
    private String id;
    private String name;
    private String url;
    private String description;
    private String local_url;

    public Project() {
    }

    public Project(String id, String name, String url, String description, String local_url) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.local_url = local_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }
}
