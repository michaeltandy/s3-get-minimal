/**
 * Copyright (C) 2014 Michael Tandy. This file is MIT licensed. Copy and paste, 
 * modify all you like, it's all fine with me!
 */
package uk.me.mjt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class S3RequestSignerTest {
    private String ACCESS_KEY_ID = null;
    private String SECRET_ACCESS_KEY = null;
    
    /**
     * Keep our test credentials outside the project folder, so we don't commit
     * them by accident. Stored in aws.properties in the user's home folder, or
     * change that below if you like. File format is like this:
<pre>
accessKey=AKIA...
secretKey=jGoF...
</pre>
     * You'll also want to check you test against a bucket you have access to,
     * either by creating your own or requesting access to my test bucket.
     * @throws IOException 
     */
    @Before
    public void loadCredentialsFromFile() throws IOException {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(System.getProperty("user.home")+"/aws.properties"));
            ACCESS_KEY_ID = prop.getProperty("accessKey");
            SECRET_ACCESS_KEY = prop.getProperty("secretKey");
        } catch (Exception e) {
            ACCESS_KEY_ID = null;
            SECRET_ACCESS_KEY = null;
        }
    }
    
    @Test
    public void testGetFromS3() throws IOException {
        assertNotNull("Put some keys in ~/aws.properties for this test to work",ACCESS_KEY_ID);
        assertNotNull("Put some keys in ~/aws.properties for this test to work",SECRET_ACCESS_KEY);
        
        String result = S3RequestSigner.getStringFromS3(
                "https://s3-eu-west-1.amazonaws.com",
                "s3-get-test-mjt-me-uk",
                "/outcome.txt",
                ACCESS_KEY_ID, 
                SECRET_ACCESS_KEY);
        
        assertEquals("This was a triumph.\nIt was a huge success.",result);
    }
    
    @Test(expected=IOException.class)
    public void testGetFromS3FailsBadAuth() throws IOException {
        String result = S3RequestSigner.getStringFromS3(
                "https://s3-eu-west-1.amazonaws.com",
                "s3-get-test-mjt-me-uk",
                "/outcome.txt",
                "asdfasdfasdf", 
                "qwerqwerqwer");
    }
    
    @Test(expected=IOException.class)
    public void testGetFromS3FailsBadAuth2() throws IOException {
        assertNotNull("Put some keys in ~/aws.properties for this test to work",ACCESS_KEY_ID);
        assertNotNull("Put some keys in ~/aws.properties for this test to work",SECRET_ACCESS_KEY);
        
        String result = S3RequestSigner.getStringFromS3(
                "https://s3-eu-west-1.amazonaws.com",
                "s3-second-get-test-mjt-me-uk",
                "/file-that-should-not-be-public.txt",
                ACCESS_KEY_ID, 
                SECRET_ACCESS_KEY);
    }
    
    @Test(expected=IOException.class)
    public void testGetFromS3FailsNotFound() throws IOException {
        assertNotNull("Put some keys in ~/aws.properties for this test to work",ACCESS_KEY_ID);
        assertNotNull("Put some keys in ~/aws.properties for this test to work",SECRET_ACCESS_KEY);
        
        String result = S3RequestSigner.getStringFromS3(
                "https://s3-eu-west-1.amazonaws.com",
                "s3-get-test-mjt-me-uk",
                "/notfound.txt",
                ACCESS_KEY_ID, 
                SECRET_ACCESS_KEY);
    }
    
    @Test
    public void testCalculateAuthHeader() {
        // Test data from http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html#RESTAuthenticationExamples
        String authorization = S3RequestSigner.calculateGetAuthHeader(
                "Tue, 27 Mar 2007 19:36:42 +0000",
                "/johnsmith/photos/puppy.jpg",
                "AKIAIOSFODNN7EXAMPLE",
                "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        assertEquals("AWS AKIAIOSFODNN7EXAMPLE:bWq2s1WEIj+Ydj0vQ697zp+IXMU=",authorization);
    }
    
    @Test
    public void testFormatDateTime() {
        // Test data calculated using http://www.unixtimestamp.com
        Date d = new Date();
        d.setTime(1393447469L*1000);
        String expectedDate = "Wed, 26 Feb 2014 20:44:29 +0000";
        String signature = S3RequestSigner.formatDate(d);
        
        assertEquals(expectedDate,signature);
    }
    
    @Test
    public void testSignStringWithKey() {
        // Test data directly from http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html#RESTAuthenticationExamples
        String toSign = "GET\n" +
                        "\n" +
                        "\n" +
                        "Tue, 27 Mar 2007 19:36:42 +0000\n" +
                        "/johnsmith/photos/puppy.jpg";
        String secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
        String signature = S3RequestSigner.signStringWithKey(toSign,secretAccessKey);
        
        assertEquals("bWq2s1WEIj+Ydj0vQ697zp+IXMU=",signature);
    }
    
}
