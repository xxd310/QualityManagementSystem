package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/27.
 */
public class AcceptanceStandard implements Serializable {
    private String id;
    private String project_id;
    private String area_id;
    private String classify_id;
    private String task_id;
    private String accept_name;
    private String standard;
    private String url;
    private String local_url;

    public AcceptanceStandard() {
    }

    public AcceptanceStandard(String id, String project_id, String area_id, String classify_id, String task_id, String accept_name, String standard, String url, String local_url) {
        this.id = id;
        this.project_id = project_id;
        this.area_id = area_id;
        this.classify_id = classify_id;
        this.task_id = task_id;
        this.accept_name = accept_name;
        this.standard = standard;
        this.url = url;
        this.local_url = local_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public String getClassify_id() {
        return classify_id;
    }

    public void setClassify_id(String classify_id) {
        this.classify_id = classify_id;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getAccept_name() {
        return accept_name;
    }

    public void setAccept_name(String accept_name) {
        this.accept_name = accept_name;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    @Override
    public String toString() {
        return "AcceptanceStandard{" +
                "id='" + id + '\'' +
                ", project_id='" + project_id + '\'' +
                ", area_id='" + area_id + '\'' +
                ", classify_id='" + classify_id + '\'' +
                ", task_id='" + task_id + '\'' +
                ", accept_name='" + accept_name + '\'' +
                ", standard='" + standard + '\'' +
                ", url='" + url + '\'' +
                ", local_url='" + local_url + '\'' +
                '}';
    }
}
