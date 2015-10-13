package com.qing.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by zwq on 2015/04/15 11:28.<br/><br/>
 * 操作字符工具类
 */
public class StringUtils {

    private static SimpleDateFormat sdf;
    private static String mTemplate = "yyyy-MM-dd HH:mm:ss";

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

    /**
     * 获取md5值
     * @param content
     * @return
     */
    public static String getMD5(String content) {
        if(content==null) return null;
        byte[] md5 = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());
            md5 = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(26);
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
    public static String getMD5_V2(String content) {
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

    public static String getRandom(int length){
        if (length < 3) {
            length = 3;
        }
        /*
        %n$ms：代表输出的是字符串，n代表是第几个参数，设置m的值可以在输出之前放置空格
        %n$md：代表输出的是整数，n代表是第几个参数，设置m的值可以在输出之前放置空格，也可以设为0m,在输出之前放置m个0
        %n$mf：代表输出的是浮点数，n代表是第几个参数，设置m的值可以控制小数位数，如m=2.2时，输出格式为00.00
        String.format("%1$06d", 25)--->000025(6:长度，不足用0补齐)
        */
        return String.format("%1$0" + length + "d", getRandomUnderMax((int) Math.pow(10, length)));
    }

    /**
     * 获取不大于maxNum的随机数
     * @param maxNum
     * @return
     */
    public static int getRandomUnderMax(int maxNum){
        if (maxNum <= 0){
            maxNum = 10;
        }
        Random random = new Random();
        return random.nextInt(maxNum);
    }

    /**
     * 获取6位的随机数
     * @return
     */
    public static String getRandom(){
        return getRandom(6);
    }
}
