package com.melexis.cashttpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sample application to test the client with multiple threads.
 */
public class SampleMultiThread {

    private static String username;
    private static String password;
    private static int numThreads;

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Username:");
        username = rdr.readLine().trim();
        System.out.print("Password:");
        password = rdr.readLine().trim();
        System.out.print("Number Threads:");
        String nrThreads = rdr.readLine().trim();
        numThreads = Integer.parseInt(nrThreads);


        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        final CasHttpClient client = new CasHttpClient("https://cas.melexis.com:8443/cas/login", 20);
        client.setPassword(password);
        client.setUsername(username);

        client.get("http://cvs.tess.elex.be");

        for(int i = 0; i < 100; i++) {
            final int cnt = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.get("http://cvs.tess.elex.be");
                        System.out.print(cnt + ",");
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("Request " + cnt + " failed." + e.getMessage());
                    }
                }
            });
        }

        Thread.sleep(20000);

        System.out.println();
        System.out.println("shutdown executor service");
        executorService.shutdown();
        while(executorService.isTerminated()) {
            Thread.sleep(3000);
            System.out.println();
            System.out.println("3 seconds passed");
        }

    }
}
