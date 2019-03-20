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
     */
    public static byte[] toByteArray(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    // 以下 是整型数 和 网络字节序的  byte[] 数组之间的转换
    public static byte[] longToBytes(long n) {
        byte[] b = new byte[8];
        b[7] = (byte) (n & 0xff);
        b[6] = (byte) (n >> 8 & 0xff);
        b[5] = (byte) (n >> 16 & 0xff);
        b[4] = (byte) (n >> 24 & 0xff);
        b[3] = (byte) (n >> 32 & 0xff);
        b[2] = (byte) (n >> 40 & 0xff);
        b[1] = (byte) (n >> 48 & 0xff);
        b[0] = (byte) (n >> 56 & 0xff);
        return b;
    }


    //double 转byte
    public static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((intBits >> 8 * i) & 0xff);
        }
        return byteRet;
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
