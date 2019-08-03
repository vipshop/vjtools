package com.vip.vjtools.vjkit.net;

import okhttp3.*;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>@author: lost
 * <p>@date: 2018/10/05 下午11:54
 */
public class OkHttpUtil {

    /**
     * 默认连接超时30秒
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30;

    /**
     * 默认读超时60秒
     */
    public static final int DEFAULT_READ_TIMEOUT = 60;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 默认client
     */
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
        .build();

    private OkHttpUtil() {

    }

    /**
     * get请求
     *
     * @param url 请求url
     * @return 响应对象
     * @throws IOException io异常
     */
    public static HttpResult doGet(String url) throws IOException {
        return doGet(client, url);
    }

    /**
     * get请求，默认连接超时
     *
     * @param url                 请求url
     * @param readTimeoutInMillis 读超时时间，单位微秒
     * @return 响应对象，需要关闭
     * @throws IOException io异常
     */
    public static HttpResult doGet(String url, int readTimeoutInMillis) throws IOException {
        Validate.notEmpty(url, "url can not be null.");
        Validate.validState(readTimeoutInMillis >= 0, "timeoutInMillis is illegal.");

        OkHttpClient newClient = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
            .build();

        return doGet(newClient, url);
    }

    /**
     * post表单
     *
     * @param url    请求地址
     * @param params 表单数据
     * @return 响应对象
     * @throws IOException io异常
     */
    public static HttpResult doPost(String url, Map<String, String> params) throws IOException {
        if (params == null){
            params = new HashMap<>();
        }
        return doPost(client, url, params);
    }

    /**
     * post表单
     *
     * @param url                 请求地址
     * @param params              表单数据
     * @param readTimeoutInMillis 读超时时间，单位微秒
     * @return 响应对象
     * @throws IOException io异常
     */
    public static HttpResult doPost(String url, Map<String, String> params, int readTimeoutInMillis)
        throws IOException {
        Validate.notEmpty(params, "form body can not be null.");
        Validate.validState(readTimeoutInMillis >= 0, "timeoutInMillis is illegal.");

        OkHttpClient newClient = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
            .build();

        return doPost(newClient, url, params);
    }

    /**
     * post json
     *
     * @param url  请求地址
     * @param json json
     * @return 返回
     * @throws IOException io异常
     */
    public static HttpResult doPostJson(String url, String json) throws IOException {
        return doPostJson(client, url, json);
    }

    /**
     * post json
     *
     * @param url                 请求地址
     * @param json                json
     * @param readTimeoutInMillis 读超时，单位微秒
     * @return 响应对象
     * @throws IOException io异常
     */
    public static HttpResult doPostJson(String url, String json, int readTimeoutInMillis) throws IOException {
        OkHttpClient newClient = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
            .build();

        return doPostJson(newClient, url, json);
    }

    private static HttpResult doGet(OkHttpClient httpClient, String url) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .build();

        Response response = httpClient.newCall(request).execute();
        return new HttpResult(response.code(), response.body().string());
    }

    private static HttpResult doPost(OkHttpClient httpClient, String url, Map<String, String> params)
        throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        FormBody body = builder.build();

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        Response response = httpClient.newCall(request).execute();
        return new HttpResult(response.code(), response.body().string());
    }

    private static HttpResult doPostJson(OkHttpClient httpClient, String url, String json) throws IOException {
        json = json == null ? "{}" : json;
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        Response response = httpClient.newCall(request).execute();
        return new HttpResult(response.code(), response.body().string());
    }
}
