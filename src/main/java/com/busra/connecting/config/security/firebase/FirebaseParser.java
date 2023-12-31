package com.busra.connecting.config.security.firebase;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Component
public class FirebaseParser {
    public Mono<FirebaseToken> parseToken(String idToken, boolean checkRevoked) {
        if (idToken.isEmpty()) {
            throw new IllegalArgumentException("FirebaseTokenBlank");
        }
        //try {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
        //	Mono<FirebaseToken> decodedToken = Mono
        //			.fromFuture(toCompletableFuture(firebaseAuth.verifyIdTokenAsync(idToken, checkRevoked)));
        // Mono<FirebaseToken> decodedToken =
        return ApiFutureUtil.toMono(
                firebaseAuth.verifyIdTokenAsync(idToken, checkRevoked));
        // return decodedToken;
        //} catch (Exception e) {
        //	throw new FirebaseTokenInvalidException(e.getMessage());
        //}
    }
    public Mono<Void> revokeRefresh(String uid) {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
        return ApiFutureUtil.toMono(firebaseAuth.revokeRefreshTokensAsync(uid));
    }

    public CompletableFuture<FirebaseToken> toCompletableFuture(ApiFuture<FirebaseToken> listenableFuture) {
        final CompletableFuture<FirebaseToken> completableFuture = new CompletableFuture<FirebaseToken>();
        ApiFutures.addCallback(listenableFuture, new ApiFutureCallback<FirebaseToken>() {
            @Override
            public void onSuccess(FirebaseToken result) {
                completableFuture.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                // completableFuture.complete(null);
                CompletableFuture rew = completableFuture.handle((firebaseToken, throwable) -> {
                    String code = ((FirebaseAuthException) throwable).getErrorCode().toString();
                    throw new FirebaseTokenInvalidException(code);
                });
                rew.join();
                //completableFuture.completeExceptionally(t);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }
}
