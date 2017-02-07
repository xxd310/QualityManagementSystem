package com.zhihuitech.qualitymanagementsystem.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/18.
 */
public class PathUrl implements Serializable {
    private String path;
    private String url;
    private String local_url;

    public PathUrl(String path, String url, String local_url) {
        this.path = path;
        this.url = url;
        this.local_url = local_url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return "PathUrl{" +
                "path='" + path + '\'' +
                ", url='" + url + '\'' +
                ", local_url='" + local_url + '\'' +
                '}';
    }
}
