package com.busra.connecting.config.security;

import com.busra.connecting.config.security.firebase.FirebaseParser;
import com.busra.connecting.model.ObjectId;
import com.busra.connecting.model.User;
import com.busra.connecting.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class CusAuthConv implements ServerAuthenticationConverter {

//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserService userDetailsService;
    private final FirebaseParser firebaseParser;
    private final ObjectMapper objectMapper;
    private final List<String> kil = Arrays.asList("yahoo", "gmail", "hotmail");
    private String authToken, username, email, ipAddress;
    private boolean isApplied;

    public CusAuthConv(@Qualifier("userService") UserService userDetailsService, FirebaseParser firebaseParser, ObjectMapper objectMapper) {
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        Assert.notNull(firebaseParser, "userDetailsService cannot be null");

        this.userDetailsService = userDetailsService;
        this.firebaseParser = firebaseParser;
        this.objectMapper = objectMapper;
    }

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.param}")
    private String tokenParam;

    @Value("${jwt.prefix}")
    private String bearerPrefix;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) throws BadCredentialsException {
        return Mono.justOrEmpty(exchange).map(ServerWebExchange::getRequest)
                .flatMap(httpRequest -> {
                    this.email = httpRequest.getQueryParams().get("email") != null ? httpRequest.getQueryParams().get("email").get(0) : "";
                    this.username = httpRequest.getQueryParams().get("name") != null ? httpRequest.getQueryParams().get("name").get(0) : "";
                    this.ipAddress = Objects.requireNonNull(httpRequest.getRemoteAddress()).getAddress().getHostAddress();
                    this.isApplied = httpRequest.getPath().value().equals("/api/rest/news/save");
                    return Mono.justOrEmpty(httpRequest.getHeaders().getFirst(tokenHeader));
                })
                .filter(header -> header.trim().startsWith(bearerPrefix))
                .map(header -> header.substring(bearerPrefix.length() + 1))
                .flatMap(token -> {
                    this.authToken = token;
                    return firebaseParser.parseToken(token, true);
                })
                .doOnError(err -> System.out.println("FirebaseAuthException occurred : " + err.getMessage()+ "  user id : " + getUid()+ "  user id token : "+ authToken))
                .onErrorResume(FirebaseAuthException.class, err -> {
                  //  userDetailsService.save(User.of().withId(new ObjectId(getUid())).build());
                    // eyJhbGciOiJSUzI1NiIsImtpZCI6IjYzODBlZjEyZjk1ZjkxNmNhZDdhNGNlMzg4ZDJjMmMzYzIzMDJmZGUiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQWxpIiwicGljdHVyZSI6IiIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9jZW50cmVuZXdzLWRmYzYwIiwiYXVkIjoiY2VudHJlbmV3cy1kZmM2MCIsImF1dGhfdGltZSI6MTY5MjM2MjkzOSwidXNlcl9pZCI6IjlLUTRZMmZPUjBONzBQOFAyemtjVnF6YWxmSDIiLCJzdWIiOiI5S1E0WTJmT1IwTjcwUDhQMnprY1ZxemFsZkgyIiwiaWF0IjoxNjkyNjE3MDUwLCJleHAiOjE2OTI2MjA2NTAsImVtYWlsIjoiZXJndWxfbWVzdXRAeWFob28uY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbImVyZ3VsX21lc3V0QHlhaG9vLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.ZXFDQSgEQF9jaBHzk2anHtN30-TBhZZaxVjx1RfriDHN63tP-SD22T90j4yQeBXY_0J2L6l3dYcr9NN-Xbjp11wcZi_qVQPiHmSNDreEJuZUXCPS1Gv4cZpu1lpV0Nzz4Bm1UY3dwkMVRGT2KjmfHF2JV6-lFthSi5hN9FIcFAPdezyR6gEDhP20N5_DbfKOTOsPv66i45p3dkjOHGbAGrbH5lQGVuJ9t8RaJynM9zRSpYCadQnWmYN6-RljoUExrOSjlpoWXxqpf18oCnGNeeEkiDAzrx8TApfcVUlAs6v34UR86m85-CAMrcFJnQQabOEffRXXPLhCflnNVt9ypw
                    return Mono.empty();
                })
                .map(fTokens -> getUserObject(User.of(), fTokens, email, ipAddress, username))
                .flatMap(user -> this.getUserMono(user, this.isApplied, ipAddress))
                .map(user -> new JwtAuthenticationToken(this.authToken, user, this.authToken, user.getAuthorities()));
    }

    private String getUid() {
        String[] chunks = authToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        try {
            String uid = (new ObjectId(objectMapper.readTree(payload).get("user_id").asText().substring(0, 12).getBytes())).toHexString();
         //   FirebaseToken token = objectMapper.readValue(payload, FirebaseToken.class);
            return uid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<User> getUserMono(User user, boolean isApply, String ipAddress) {
        return this.userDetailsService.findById(user.getId().toHexString())
                .flatMap(user1 -> {
                    if (user1.getEmail() == null) {
                        return userDetailsService.save(user);
                    } else if (isApply && !user.getIpAddress().contains(ipAddress)) {
                        List<String> list = user1.getIpAddress();
                        list.add(ipAddress);
                        return userDetailsService.save(User.from(user).withIpAddress(list).build());
                    }
                    if (!user1.isEnabled())
                        return Mono.empty();
                    else
                        return Mono.just(user1);
                });
    }

    private User getUserObject(User.Builder builder, FirebaseToken token, String email, String ipAddress, String username) {
        return builder.withId(new ObjectId(token.getUid().substring(0, 12).getBytes()))
                .withEmail(token.getEmail() != null ? token.getEmail() : email)
                .withContentsCount(0L)
                .withDate(new Date())
                .withFirstname(token.getName())
                .withLastname(token.getName())
                .withRoles(Collections.singletonList("ROLE_ADMIN"))
                .withFollowers(Collections.emptyList())
                .withUsers(Collections.emptyList())
                .withTags(Collections.emptyList())
                .withMediaParts(Collections.emptyList())
                .withPassword(token.getUid())
                .withUsername(getUserName(token.getEmail() != null ? token.getEmail() : email))
                .withSummary("Lütfen Kısa Özet Giriniz")
                .withEnabled(true)
                .withIban("")
                .withIpAddress(Collections.singletonList(ipAddress))
                .withBlocked(Collections.emptyList())
                .withImage("/assets/profile-img.jpeg")
                .withOffers(Collections.emptyList())
                .build();
    }

    private String getUserName(String emailx) {
        if (!emailx.equals("")) {
            String[] list = emailx.split("@");
            int il = kil.indexOf(list[1].split("\\.")[0]);
            return list[0] + (il != -1 ? il : 4);
        }
        return "";
    }
}
