package com.vip.vjtools.vjkit.net;

import java.io.Serializable;

/**
 * 封装httpClient响应结果
 */
public class HttpResult implements Serializable {

    private static final long serialVersionUID = 2168152194164783950L;

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private String content;

    public HttpResult() {
    }

    public HttpResult(int code) {
        this.code = code;
    }

    public HttpResult(String content) {
        this.content = content;
    }

    public HttpResult(int code, String content) {
        this.code = code;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "HttpResult [code=" + code + ", content=" + content + "]";
    }

}
