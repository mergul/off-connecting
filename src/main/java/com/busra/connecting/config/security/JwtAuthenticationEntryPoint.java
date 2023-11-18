package com.busra.connecting.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.Charset;

@Component("entryPoint")
@Qualifier("entryPoint")
@ComponentScan(basePackageClasses = {com.busra.connecting.config.security.JwtAuthenticationEntryPoint.class})
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = 1L;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange serverWebExchange, AuthenticationException e) {
        return handleAndRespond(serverWebExchange, 1002, HttpStatus.UNAUTHORIZED);
    }
    public Mono<Void> handleAndRespond(ServerWebExchange exchange, int errorCode, HttpStatus httpStatus) {
        var response = exchange.getResponse();
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatusCode(httpStatus);

        String errorJson = null;
        try {
            errorJson = objectMapper.writeValueAsString(new ErrorResponse(errorCode, httpStatus.getReasonPhrase(), httpStatus));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var dataBufferFactory = response.bufferFactory();
        var buffer = dataBufferFactory.wrap(errorJson.getBytes(Charset.defaultCharset()));

        return response.writeWith(Mono.just(buffer)).doOnError((e)-> { DataBufferUtils.release(buffer); });
    }
}
