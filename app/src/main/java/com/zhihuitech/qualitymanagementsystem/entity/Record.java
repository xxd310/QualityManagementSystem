package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/28.
 */
public class Record implements Serializable {
    private String id;
    private String project_id;
    private String area_id;
    private String classify_id;
    private String task_id;
    private String create_time;
    private String format_time;
    private String directoryPath;
    private boolean selected;
    private String submit;
    private String username;

    public Record() {
    }

    public Record(String id, String project_id, String area_id, String classify_id, String task_id, String create_time, String format_time, String directoryPath, boolean selected, String submit, String username) {
        this.id = id;
        this.project_id = project_id;
        this.area_id = area_id;
        this.classify_id = classify_id;
        this.task_id = task_id;
        this.create_time = create_time;
        this.format_time = format_time;
        this.directoryPath = directoryPath;
        this.selected = selected;
        this.submit = submit;
        this.username = username;
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

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getFormat_time() {
        return format_time;
    }

    public void setFormat_time(String format_time) {
        this.format_time = format_time;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
