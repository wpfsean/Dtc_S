package com.tehike.client.dtc.multiple.app.project.utils;

import java.lang.reflect.Method;

/**
 * 描述：$desc$ Base64加密解密（网上找的）
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class CryptoUtil {


    public static String encodeBASE64(String source) {
        Class<?> clazz = null;
        Method encodeMethod = null;
        try {// 优先使用第三方库
            clazz = Class.forName("org.apache.commons.codec.binary.Base64");
            encodeMethod = clazz.getMethod("encodeBase64", byte[].class);
            // 反射方法 静态方法执行无需对象
            return new String((byte[]) encodeMethod.invoke(null, source.getBytes()));
        } catch (ClassNotFoundException e) {
            String vm = System.getProperty("java.vm.name");
            System.out.println(vm);
            try {
                if ("Dalvik".equals(vm)) {// Android
                    clazz = Class.forName("android.util.Base64");
                    // byte[] Base64.encode(byte[] input,int flags)
                    encodeMethod = clazz.getMethod("encode", byte[].class, int.class);
                    return new String((byte[]) encodeMethod.invoke(null, source.getBytes(), 0));
                } else {// JavaSE/JavaEE
                    clazz = Class.forName("sun.misc.BASE64Encoder");
                    encodeMethod = clazz.getMethod("encode", byte[].class);
                    return (String) encodeMethod.invoke(clazz.newInstance(), source.getBytes());
                }
            } catch (ClassNotFoundException e1) {
                return null;
            } catch (Exception e1) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    public static String decodeBASE64(String encodeSource) {
        Class<?> clazz = null;
        Method decodeMethod = null;

        try {// 优先使用第三方库
            clazz = Class.forName("org.apache.commons.codec.binary.Base64");
            decodeMethod = clazz.getMethod("decodeBase64", byte[].class);
            // 反射方法 静态方法执行无需对象
            return new String((byte[]) decodeMethod.invoke(null, encodeSource.getBytes()));
        } catch (ClassNotFoundException e) {
            String vm = System.getProperty("java.vm.name");
            System.out.println(vm);
            try {
                if ("Dalvik".equals(vm)) {// Android
                    clazz = Class.forName("android.util.Base64");
                    // byte[] Base64.decode(byte[] input, int flags)
                    decodeMethod = clazz.getMethod("decode", byte[].class, int.class);
                    return new String((byte[]) decodeMethod.invoke(null, encodeSource.getBytes(), 0));
                } else { // JavaSE/JavaEE
                    clazz = Class.forName("sun.misc.BASE64Decoder");
                    decodeMethod = clazz.getMethod("decodeBuffer", String.class);
                    return new String((byte[]) decodeMethod.invoke(clazz.newInstance(), encodeSource));
                }
            } catch (ClassNotFoundException e1) {
                return null;
            } catch (Exception e1) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
