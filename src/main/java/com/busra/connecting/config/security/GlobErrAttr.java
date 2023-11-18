package com.busra.connecting.config.security;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GlobErrAttr extends DefaultErrorAttributes {

        @Override
        public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                      ErrorAttributeOptions options) {
            Map<String, Object> map = super.getErrorAttributes(
                    request, options);
            map.put("status", HttpStatus.BAD_REQUEST);
            if(map.get("message").toString().substring(0, 30).equals("Firebase ID token has expired.")) {
                map.put("error", "Expired ID Token");
            }
            return map;
        }


}
