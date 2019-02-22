package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：报警颜色及类型对应的封装
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/19 15:22
 * @version V1.0
 */
public class AlarmTypeBean implements Serializable {

    //警灯颜色
    private String TypeColor;
    //报警类型
    private String TypeName;

    public AlarmTypeBean(String typeColor, String typeName) {
        TypeColor = typeColor;
        TypeName = typeName;
    }

    public String getTypeColor() {
        return TypeColor;
    }

    public void setTypeColor(String typeColor) {
        TypeColor = typeColor;
    }

    public String getTypeName() {
        return TypeName;
    }

    public void setTypeName(String typeName) {
        TypeName = typeName;
    }

    @Override
    public String toString() {
        return "AlarmTypeBean{" +
                "TypeColor='" + TypeColor + '\'' +
                ", TypeName='" + TypeName + '\'' +
                '}';
    }
}
