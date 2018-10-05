package com.vip.vjtools.vjkit.net;

import com.vip.vjtools.vjkit.io.FileUtil;
import org.apache.http.client.fluent.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>@author: lost
 * <p>@date: 2018/10/05 下午4:36
 */
public class HttpClientUtilTest {
    List<String> urlList = new ArrayList();
    @Before
    public void init(){
        for (int i = 1 ; i < 30; i++){
            String url = String.format("http://www.infoq.com/cn/articles/%s",i);
            urlList.add(url);
        }
    }

    @Test
    public void doGet() throws Exception {
        String url = "http://www.baidu.com";
        HttpClientResult result = HttpClientUtil.doGet(url);
        //System.out.println(JsonMapper.INSTANCE.toJson(result));
        assertThat(result.getCode()).isEqualTo(200);
        System.out.println(result.getContent());
        assertThat(result.getContent()).isNotBlank();

    }

    @Test
    public void testPoolGet() throws Exception {
        HttpClientUtil.sharePool(true);

        long start = System.currentTimeMillis();
        runTask(urlList);
        long end = System.currentTimeMillis();
        long temp = (end - start);
        System.out.println("共享连接池耗时 -> " + temp);

    }


    @Test
    public void testNoPool(){
        HttpClientUtil.sharePool(false);
        long start2 = System.currentTimeMillis();
        runTask(urlList);
        long end2 = System.currentTimeMillis();
        System.out.println(" 非共享连接池耗时 -> " + (end2 - start2));
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

                HttpClientUtil.doGet(url);

            } catch (Exception e) {

                e.printStackTrace();

            } finally {
                countDownLatch.countDown();
            }

        }

    }

    /**
     * 使用fluent facade API使用户不必处理连接管理和资源释放。 warn::: 一定要设置超时时间，建议普通请求就使用
     *
     * @link http://hc.apache.org/httpcomponents-client-4.5.x/fluent-hc/xref-test/index.html
     * <p>
     * timeout 不设置的默认值是多少？好坑啊，有风险，如果忘记设就出大问题了
     * @see com.vip.vjtools.vjkit.net.HttpClientUtil
     * @see org.apache.http.client.config.RequestConfig#getConnectTimeout() A timeout value of zero is interpreted as an
     * infinite timeout. A negative value is interpreted as undefined (system default).
     */
    @Test
    public void fluentDoGet() {
        long start = System.currentTimeMillis();
        //不设置超时出大问题，谨慎使用，还是使用自封装的工具类吧
        try {
            String result = Request.Get("http://google.com").connectTimeout(1000)
                .execute().returnContent().asString(Charset.forName("UTF-8"));  //不加这个会乱码
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(String.format(" spend %s", System.currentTimeMillis() - start));

    }

    @Test
    public void fluentDownload() throws IOException {
        File file = FileUtil.createTempFile().toFile();
        try {
            String url = "http://imgsrc.baidu.com/imgad/pic/item/f9dcd100baa1cd11ecf55a03b312c8fcc3ce2d55.jpg";
            Request.Get(url).connectTimeout(6000).execute().saveContent(file);
            System.out.println(file.getAbsolutePath());
        } finally {
            FileUtil.deleteFile(file);
        }

    }

    @Test
    public void doPost() throws Exception {
        String payUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HttpClientResult result = HttpClientUtil.doPost(payUrl);
        //System.out.println(result);
        assertThat(result.getCode()).isEqualTo(200);
    }

}