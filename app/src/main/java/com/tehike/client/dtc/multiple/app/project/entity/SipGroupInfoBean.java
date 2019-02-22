package com.tehike.client.dtc.multiple.app.project.entity;

/**
 * 描述：webapi数据sip分组数据封装
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/25 11:34
 * @version V1.0
 */

public class SipGroupInfoBean {

    //sip组Id
    int id;
    //sip组内成员数量
    String member_count;
    //组名称
    String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMember_count() {
        return member_count;
    }

    public void setMember_count(String member_count) {
        this.member_count = member_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public SipGroupInfoBean() {
    }

    @Override
    public String toString() {
        return "SipGroupInfoBean{" +
                "id=" + id +
                ", member_count='" + member_count + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
