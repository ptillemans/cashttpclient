package com.melexis.cashttpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sample application to test the client with multiple threads.
 */
public class SampleMultiThread {

    public static final int NR_REQUESTS = 250;
    private static String username;
    private static String password;
    private static int numThreads;

    private static String[] urls = {"http://cvs.tess.elex.be/cgi-bin/cvsweb/~checkout~/design/90316/planning/MLX90316_APQP.xls?rev=1.14",
            "http://cvs.sofia.elex.be/cgi-bin/cvsweb/~checkout~/design/10111/planning/APQP.XLS?rev=1.11",
            "http://cvs.sensors.elex.be/cgi-bin/cvsweb/~checkout~/design/12123/planning/12123_Planning.mpp?rev=1.7",
            "http://cvs.erfurt.elex.be/cgi-bin/cvsweb/~checkout~/design/33300/planning/MircoPlan_33300AA.mpp?rev=1.30"};

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

        Random random = new Random();

        final String[] results = new String[NR_REQUESTS];

        for(int i = 0; i < NR_REQUESTS; i++) {
            final int cnt = i;
            final String url = urls[random.nextInt(urls.length)];
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        client.head(url);
                        long runtime = System.currentTimeMillis() - start;
                        results[cnt] = cnt + ", \"" + url + "\", " + runtime;
                        System.out.println(cnt + "(" + url + ") : " + runtime);


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

        for (int i = 0; i < NR_REQUESTS; i++) {
            if (results[i] != null) {
                System.out.println(results[i]);
            }
        }
    }
}
