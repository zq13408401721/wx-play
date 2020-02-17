package com.example.artapp;

import android.os.Message;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MyUtils {

    private static String[] chars = new String[]{"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};


    /**
     * 获取调起微信支付的签名
     * @param key
     * @param appid  微信的appid
     * @param body 微信支付内容
     * @param mch_id  微信支付分配的商户号
     * @return
     */
    public static String getSign(String key,String appid,String body,String mch_id){
        //nonce_str  随机字符串，不长于32位
        String nonce_str = getRandomString(8);
        String string = "";
        StringBuilder builder = new StringBuilder();
        builder.append("appid=");
        builder.append(appid);
        builder.append("&body=");
        builder.append(body);
        builder.append("&mch_id=");
        builder.append(mch_id);
        builder.append("&nonce_str=");
        builder.append(nonce_str);
        builder.append("&key=");
        builder.append(key);
        string = MyMD5.MD5Encode(builder.toString()).toUpperCase();
        return string;
    }

    /**
     * 获取随机字符
     * @param num
     * @return
     */
    public static String getRandomString(int num){
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<num; i++){
            int index = random.nextInt(chars.length);
            sb.append(chars[index]);
        }
        return sb.toString();
    }

    /**
     * 获取sha256的加密签名
     * @param str
     * @return
     */
    public static String getHmacSHA256(String key,String str){
        Mac sha256_mac;
        String encodeStr = "";
        try{
            sha256_mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
            sha256_mac.init(secret_key);
            byte[] array = sha256_mac.doFinal(str.getBytes("UTF-8"));
            String result = byte2Hex(array);
            encodeStr = result.toUpperCase();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * byte转为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes){
        StringBuffer buffer = new StringBuffer();
        String temp = null;
        for(int i=0; i<bytes.length; i++){
            temp = Integer.toHexString(bytes[i]&0xFF);
            //1得到一位的进行补0操作
            if(temp.length() == 1){
                buffer.append("0");
            }
            buffer.append(temp);
        }
        return buffer.toString();
    }

}
