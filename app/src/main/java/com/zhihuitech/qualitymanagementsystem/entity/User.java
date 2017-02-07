package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/30.
 */
public class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private String tel;
    private String group_id;
    private String project_id;
    private String remark;

    public User() {
    }

    public User(String id, String username, String password, String tel, String group_id, String project_id, String remark) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.tel = tel;
        this.group_id = group_id;
        this.project_id = project_id;
        this.remark = remark;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
