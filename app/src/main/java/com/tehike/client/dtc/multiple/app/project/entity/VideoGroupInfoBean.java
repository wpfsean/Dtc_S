package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：视频分组对应的封闭实体类(Webapi)
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/10 10:13
 * @version V1.0
 */
public class VideoGroupInfoBean implements Serializable{

    private String id;
    private int member_count;
    private String name;

    @Override
    public String toString() {
        return "VideoGroupInfoBean{" +
                "id='" + id + '\'' +
                ", member_count=" + member_count +
                ", name='" + name + '\'' +
                '}';
    }

    public VideoGroupInfoBean(String id, int member_count, String name) {
        this.id = id;
        this.member_count = member_count;
        this.name = name;
    }

    public VideoGroupInfoBean() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMember_count() {
        return member_count;
    }

    public void setMember_count(int member_count) {
        this.member_count = member_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
