package com.usian.fallback;

import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ConsumerFallback implements FallbackProvider {
    @Override
    public String getRoute() {
        //降级的服务名，多个服务用*
        return "*";
    }

    /**
     * 当服务无法执行时，该方法返回托底信息
     * @param route
     * @param cause
     * @return
     */
    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        return new ClientHttpResponse() {
            //设置响应头信息
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                MediaType mediaType = new MediaType("application", "json", Charset.forName("utf-8"));
                headers.setContentType(mediaType);
                return headers;
            }

            //设置响应体
            @Override
            public InputStream getBody() throws IOException {
                String content = "该服务暂时不可用，请稍后重试";
                return new ByteArrayInputStream(content.getBytes());
            }

            //ClientHttpResponse的fallback的状态码 返回string
            @Override
            public String getStatusText() throws IOException {
                return this.getStatusCode().getReasonPhrase();
            }

            /**
             *  网关向api服务请求是失败了，但是消费者客户端向网关发起的请求是OK的，
             *  不应该把api的404,500等问题抛给客户端
             *  网关和api服务集群对于客户端来说是黑盒子
             * @return
             * @throws IOException
             */
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            //ClientHttpResponse的fallback的状态码 返回int
            @Override
            public int getRawStatusCode() throws IOException {
                return this.getStatusCode().value();
            }

            @Override
            public void close() {

            }
        };
    }
}
