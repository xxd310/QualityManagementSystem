package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/19.
 */
public class Task implements Serializable{
    private String id;
    private String name;
    private String project_id;
    private String area_id;
    private String classify_id;
    private String url;
    private String description;
    private String local_url;
    private String finish;

    public Task() {
    }

    public Task(String id, String name, String project_id, String area_id, String classify_id, String url, String description, String local_url, String finish) {
        this.id = id;
        this.name = name;
        this.project_id = project_id;
        this.area_id = area_id;
        this.classify_id = classify_id;
        this.url = url;
        this.description = description;
        this.local_url = local_url;
        this.finish = finish;
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

    public String getClassify_id() {
        return classify_id;
    }

    public void setClassify_id(String classify_id) {
        this.classify_id = classify_id;
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

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }
}
