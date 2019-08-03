package com.vip.vjtools.vjkit.net;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OkHttp 提供更简洁的api,超时及读超时都有默认值默认的连接池无疑更加优于apache httpclient 建议不要使用apache httpclient
 * <p>@author: lost
 * <p>@date: 2018/10/06 上午1:29
 */
public class OkHttpUtilTest {

    List<String> urlList = new ArrayList();

    @Before
    public void init() {
        for (int i = 1; i < 30; i++) {
            String url = String.format("http://www.infoq.com/cn/articles/%s", i);
            urlList.add(url);
        }
    }

    @Test
    public void doGet() throws Exception {
        String url = "http://www.baidu.com";
        HttpResult result = OkHttpUtil.doGet(url);
        //System.out.println(JsonMapper.INSTANCE.toJson(result));
        assertThat(result.getCode()).isEqualTo(200);
        System.out.println(result.getContent());
        assertThat(result.getContent()).isNotBlank();

    }

    @Test
    public void doPost() throws IOException {
        String payUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HttpResult result = OkHttpUtil.doPost(payUrl, null);
        System.out.println(result);
        assertThat(result.getCode()).isEqualTo(200);
    }

    /**
     * OKHttp 有连接池吗？为每个单用户创建一个，这点优于apache httpclient this(5, 5, TimeUnit.MINUTES);高并发下建议，使用一个连接池 简单测试与apache httpclient
     * 连接池性能相当 > apache httpclient没有使用连接池
     *
     * @see okhttp3.ConnectionPool#ConnectionPool()
     */
    @Test
    public void testPool() {
        long start2 = System.currentTimeMillis();
        runTask(urlList);
        long end2 = System.currentTimeMillis();
        System.out.println(" 共享连接池耗时 -> " + (end2 - start2));
    }

    private void runTask(List<String> urlList) {
        int pagecount = urlList.size();
        ExecutorService executors = Executors.newFixedThreadPool(pagecount);

        CountDownLatch countDownLatch = new CountDownLatch(pagecount);
        try {
            for (int i = 0; i < pagecount; i++) {
                //启动线程抓取
                executors.execute(new GetRunnable(urlList.get(i), countDownLatch));

            }
            countDownLatch.await();

            executors.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            System.out.println(
                "线程" + Thread.currentThread().getName() + "," + System.currentTimeMillis() + ", 所有线程已完成，开始进入下一步！");
        }
    }

    static class GetRunnable implements Runnable {

        private CountDownLatch countDownLatch;
        private String url;

        public GetRunnable(String url, CountDownLatch countDownLatch) {

            this.countDownLatch = countDownLatch;
            this.url = url;

        }

        @Override

        public void run() {

            try {

                OkHttpUtil.doGet(url);

            } catch (Exception e) {

                e.printStackTrace();

            } finally {
                countDownLatch.countDown();
            }

        }

    }

}