package com.tehike.client.dtc.multiple.app.project.update;

import java.io.Serializable;


/**
 * 描述：自动更新类封装
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/31 13:52
 * @version V1.0
 */
public class UpDateInfo implements Serializable {

    private int version;

    private String description;

    public UpDateInfo() {
    }

    public int getVersion() {

        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;


    @Override
    public String toString() {
        return "UpDateInfo{" +
                "version=" + version +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
