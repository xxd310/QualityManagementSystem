package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/27.
 */
public class ActualAcceptanceSituation implements Serializable {
    private String id;
    private String record_id;
    private String submit;
    private String score;
    private String description;
    private String url;
    private String create_time;
    private String local_url;
    private String accept_id;

    public ActualAcceptanceSituation() {
    }

    public ActualAcceptanceSituation(String id, String record_id, String submit, String score, String description, String url, String create_time, String local_url, String accept_id) {
        this.id = id;
        this.record_id = record_id;
        this.submit = submit;
        this.score = score;
        this.description = description;
        this.url = url;
        this.create_time = create_time;
        this.local_url = local_url;
        this.accept_id = accept_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    public String getAccept_id() {
        return accept_id;
    }

    public void setAccept_id(String accept_id) {
        this.accept_id = accept_id;
    }

    @Override
    public String toString() {
        return "ActualAcceptanceSituation{" +
                "id='" + id + '\'' +
                ", record_id='" + record_id + '\'' +
                ", submit='" + submit + '\'' +
                ", score='" + score + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", create_time='" + create_time + '\'' +
                ", local_url='" + local_url + '\'' +
                ", accept_id='" + accept_id + '\'' +
                '}';
    }
}
