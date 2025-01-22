package com.example.realtimechat.network.utils;

import com.example.realtimechat.Constants;
import com.volcengine.model.ipaas.response.Host;

import org.apache.http.client.methods.RequestBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.Request;

public class Sign {

    private static final String REGION = "cn-north-1";
    private static final String SERVICE = "rtc";
    private static final String ACCESS_KEY = "AKLTNjJmMDBlODQ2ZGZmNGM1OWI0MTY0MjIzMjEwMDNmYmE";
    private static final String SECRET_KEY = "WkdaaE1HTmlZemd3WTJOaU5ETXpNemxsWVRJM1pUVmlZalExWW1RM1pUQQ==";
    private static final String SCHEMA = "https";
    private static final String END_POINT = "rtc.volcengineapi.com";
    private static final String PATH = "/";

    private static final BitSet URLENCODER = new BitSet(256);

    private static final String CONST_ENCODE = "0123456789ABCDEF";
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    static {
        int i;
        for (i = 97; i <= 122; ++i) {
            URLENCODER.set(i);
        }

        for (i = 65; i <= 90; ++i) {
            URLENCODER.set(i);
        }

        for (i = 48; i <= 57; ++i) {
            URLENCODER.set(i);
        }
        URLENCODER.set('-');
        URLENCODER.set('_');
        URLENCODER.set('.');
        URLENCODER.set('~');
    }

    public static Request signRequest(String method, byte[] body, String action, String version, Request request) throws Exception {

        if (body == null) {
            body = new byte[0];
        }

        String xContentSha256 = hashSHA256(body);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date();
        String xDate = sdf.format(date);
        String shortXDate = xDate.substring(0, 8);
        String contentType = "application/json";

        String signHeader = "host;x-content-sha256;x-date";


        SortedMap<String, String> realQueryList = new TreeMap<>();
        realQueryList.put("Action", action);
        realQueryList.put("Version", version);
        StringBuilder querySB = new StringBuilder();
        for (String key : realQueryList.keySet()) {
            querySB.append(signStringEncoder(key)).append("=").append(signStringEncoder(realQueryList.get(key))).append("&");
        }
        querySB.deleteCharAt(querySB.length() - 1);

        String canonicalStringBuilder = method + "\n" + PATH + "\n" + querySB + "\n" +
                "host:" + END_POINT + "\n" +
                "x-content-sha256:" + xContentSha256 + "\n" +
                "x-date:" + xDate + "\n" +
                "\n" +
                signHeader + "\n" +
                xContentSha256;

        System.out.println(canonicalStringBuilder);

        String hashcanonicalString = hashSHA256(canonicalStringBuilder.getBytes());
        String credentialScope = shortXDate + "/" + REGION + "/" + SERVICE + "/request";
        String signString = "HMAC-SHA256" + "\n" + xDate + "\n" + credentialScope + "\n" + hashcanonicalString;

        byte[] signKey = genSigningSecretKeyV4(Constants.SECRET_KEY, shortXDate, REGION, SERVICE);
        String signature = byteArrayToHex(hmacSHA256(signKey, signString));

        String authorization = "HMAC-SHA256" +
                " Credential=" + Constants.ACCESS_KEY + "/" + credentialScope +
                ", SignedHeaders=" + signHeader +
                ", Signature=" + signature;

        System.out.println(authorization);

        return request
                .newBuilder()
                .header("X-Date", xDate)
                .header("X-Content-Sha256", xContentSha256)
                .header("Authorization", authorization)
                .build();
    }

    private static String signStringEncoder(String source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source.length());
        ByteBuffer bb = UTF_8.encode(source);
        while (bb.hasRemaining()) {
            int b = bb.get() & 255;
            if (URLENCODER.get(b)) {
                buf.append((char) b);
            } else if (b == 32) {
                buf.append("%20");
            } else {
                buf.append("%");
                char hex1 = CONST_ENCODE.charAt(b >> 4);
                char hex2 = CONST_ENCODE.charAt(b & 15);
                buf.append(hex1);
                buf.append(hex2);
            }
        }

        return buf.toString();
    }

    public static String hashSHA256(byte[] content) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            return byteArrayToHex(md.digest(content));
        } catch (Exception e) {
            throw new Exception(
                    "Unable to compute hash while signing request: "
                            + e.getMessage(), e);
        }
    }

    public static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hmacSHA256(byte[] key, String content) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(content.getBytes());
        } catch (Exception e) {
            throw new Exception(
                    "Unable to calculate a request signature: "
                            + e.getMessage(), e);
        }
    }

    private static byte[] genSigningSecretKeyV4(String secretKey, String date, String region, String service) throws Exception {
        byte[] kDate = hmacSHA256((secretKey).getBytes(), date);
        byte[] kRegion = hmacSHA256(kDate, region);
        byte[] kService = hmacSHA256(kRegion, service);
        return hmacSHA256(kService, "request");
    }
}