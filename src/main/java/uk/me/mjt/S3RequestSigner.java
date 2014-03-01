/**
 * Copyright (C) 2014 Michael Tandy. This file is MIT licensed. Copy and paste, 
 * modify all you like, it's all fine with me!
 */
package uk.me.mjt;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class S3RequestSigner {
    
    /**
     * Get a file from S3, fully reading it into a string.
     * @throws IOException if the file is not found, or the key is wrong.
     */
    public static String getStringFromS3(String endpoint, String bucket, String object, String accessKeyId, String secretAccessKey) throws IOException {
        InputStream is = null;
        
        try {
            is = getInputStreamFromS3(endpoint, bucket, object, accessKeyId, secretAccessKey);
            // See http://stackoverflow.com/a/13632114/1367431
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {}
        }
    }
    
    /**
     * Get a file from S3, returning it as an InputStream.
     * @throws IOException if the file is not found, or the key is wrong.
     */
    public static InputStream getInputStreamFromS3(String endpoint, String bucket, String object, String accessKeyId, String secretAccessKey) throws IOException {
        if (endpoint==null || bucket == null || object == null || accessKeyId == null || secretAccessKey == null)
            throw new IllegalArgumentException("All parameters are required to be non-null.");
        if (!(endpoint.startsWith("http://")||endpoint.startsWith("https://")))
            throw new IllegalArgumentException("endpoint must start http:// or https://");
        if (bucket.contains("/"))
            throw new IllegalArgumentException("bucket may not contain forwardslashes.");
        
        String reqUrl = endpoint + (endpoint.endsWith("/")?"":"/") + bucket + (object.startsWith("/")?"":"/") + object;
        String date = formatDate(new Date());
        
        InputStream is = null;
        HttpURLConnection conn = null;
        
        try {
            URL u = new URL(reqUrl);
            conn = (HttpURLConnection)u.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Date", date);
            conn.setRequestProperty("Authorization",
                    calculateGetAuthHeader(date,"/"+bucket+object,
                            accessKeyId,secretAccessKey));
            
            is = conn.getInputStream();
            return is;
        } catch (IOException e) {
            if (is != null) try {
                is.close();
            } catch (IOException e2) {}
            if (conn != null) try {
                conn.disconnect();
            } catch (Exception e2) {}
            throw e;
        }
    }
    
    public static String formatDate(Date d) {
        // Tue, 27 Mar 2007 19:36:42 +0000
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        return sdf.format(d);
    }
    
    public static String calculateGetAuthHeader(String date, String canonicalizedResource, String accessKeyId, String secretAccessKey) {
        String toSign = "GET\n\n\n" + date + "\n" + canonicalizedResource;
        String signed = signStringWithKey(toSign,secretAccessKey);
        return "AWS " + accessKeyId + ":" + signed;
    }
    
    public static String signStringWithKey(String toSign,String secretAccessKey) {
        try {
            
            SecretKeySpec signingKey = new SecretKeySpec(secretAccessKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            return DatatypeConverter.printBase64Binary(mac.doFinal(toSign.getBytes("UTF-8")));
            
        } catch (InvalidKeyException e) {
            throw new RuntimeException("InvalidKeyException - this should never happen???",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException for UTF-8 - this should never happen.",e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for HmacSHA1 - this should never happen.",e);
        }
    }
    
}
