package com.qing.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtils {

    /**
     * 将"-"、" "、"/"替换成"_"
     * @param content
     * @return
     */
    public static String replaceX(String content) {
        if (!isNullOrEmpty(content)) {
            content = content.trim().replaceAll("-", "_").replaceAll(" ", "_").replaceAll("/", "_");
            return content.toLowerCase();
        } else {
            return null;
        }
    }

    /**
     * 是否为空 或 空字符串
     * @param text
     * @return
     */
    public static boolean isNullOrEmpty(String text){
        if(text==null || text.trim().equals("")){
            return true;
        }
        return false;
    }

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 获取md5值
     * @param content
     * @return
     */
    public static String md5(String content) {
        if(content==null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());
            byte messageDigest[] = digest.digest();
            return toHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static SimpleDateFormat sdf;
    private static String mTemplate = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取指定格式时间
     * @param template
     * @param date
     * @return
     */
    public static String getDateTime(String template,Date date){
        if(isNullOrEmpty(template)){
            template = mTemplate;
        }
        if(date==null){
            date = new Date();
        }
//        if(sdf==null){
            sdf = new SimpleDateFormat(template, Locale.CHINA);
//        }
        return sdf.format(date);
    }

    public static String getDateTime(String template){
        return getDateTime(template, new Date());
    }

    /**
     * 获取MM-dd HH:mm时间
     * @return
     */
    public static String getDateTime(){
        return getDateTime("MM-dd HH:mm");
    }
}
