package com.melexis.cashttpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Sample application to test basic functionality.
 */
public class Sample {
    public static void main(String[] args) throws IOException {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Username:");
        String username = rdr.readLine().trim();
        System.out.print("Password:");
        String password = rdr.readLine().trim();

        CasHttpClient client = new CasHttpClient("https://cas.melexis.com:8443/cas/login", 20);
        client.setCasPassWord(password);
        client.setCasUserName(username);

        System.out.println(client.get("http://cvs.tess.elex.be"));
    }
}
