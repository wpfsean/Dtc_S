package com.tehike.client.dtc.multiple.app.project.utils;

/**
 * Created by Root on 2018/4/19.
 */

public class ByteUtil {

    /**
     * 构造函数
     * （抛出不支持的操作的异常）
     */
    public ByteUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    //字节数组转转hex字符串
    public static String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : inBytArr) {
            strBuilder.append(Byte2Hex(Byte.valueOf(valueOf)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //1字节转2个Hex字符
    public static String Byte2Hex(Byte inByte) {
        return String.format("%02x", new Object[]{inByte}).toUpperCase();
    }



    /**
     * byte转int （注意高低位）
     *
     * @param src
     * @param offset
     * @return
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * Get0的下标(用于解析sipServer,SIP_NAME,sipPass)
     * @param sipServer
     * @return
     */
    public static int getPosiotion(byte[] sipServer){
        int temp = 0;
        for(int i = 0; i< sipServer.length;i++){
            if (sipServer[i] == 0) {
                return i;
            }
        }
        return temp;
    }




    /**
     * int 转志Byte数组
     *
     * @param i
     * @return
     */
    public static byte[] toByteArray(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    public static boolean isChineseChar(char c) {
        try {
            return String.valueOf(c).getBytes("UTF-8").length > 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
