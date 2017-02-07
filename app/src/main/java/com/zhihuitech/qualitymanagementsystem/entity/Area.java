package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/19.
 */
public class Area implements Serializable {
    private String id;
    private String name;
    private String project_id;
    private String description;

    public Area() {
    }

    public Area(String id, String name, String project_id, String description) {
        this.id = id;
        this.name = name;
        this.project_id = project_id;
        this.description = description;
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

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
