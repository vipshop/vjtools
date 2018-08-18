package com.vip.vjtools.vjkit.concurrent.threadpool;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class AbortPolicyWithReportTest {

    @Test
    public void jStackDumpTest() throws InterruptedException {
        AbortPolicyWithReport abortPolicyWithReport = new AbortPolicyWithReport("test");

        try {
            abortPolicyWithReport.rejectedExecution(new Runnable() {
                @Override
                public void run() {
                    System.out.println("hello");
                }
            }, (ThreadPoolExecutor) Executors.newFixedThreadPool(1));
        } catch (RejectedExecutionException rj) {
            // ignore
        }

        Thread.sleep(1000);

    }
}