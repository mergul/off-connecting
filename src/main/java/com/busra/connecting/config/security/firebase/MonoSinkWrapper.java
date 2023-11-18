package com.busra.connecting.config.security.firebase;

import reactor.core.publisher.MonoSink;

/**
 * Wrapper around {@link MonoSink}. Since project-reactor is not a required dependency in this Axon version, we need
 * wrappers for backwards compatibility. As soon as dependency is no longer optional, this wrapper should be removed.
 *
 * @param <T> The value type
 * @author Milan Savic
 * @since 3.3
 */
public class MonoSinkWrapper<T> {

    private final MonoSink<T> monoSink;

    /**
     * Initializes this wrapper with delegate sink.
     *
     * @param monoSink Delegate sink
     */
    MonoSinkWrapper(MonoSink<T> monoSink) {
        this.monoSink = monoSink;
    }

    /**
     * Wrapper around {@link MonoSink#success(Object)}.
     *
     * @param value to be passed to the delegate sink
     */
    public void success(T value) {
        monoSink.success(value);
    }

    /**
     * Wrapper around {@link MonoSink#error(Throwable)}.
     *
     * @param t to be passed to the delegate sink
     */
    public void error(Throwable t) {
        monoSink.error(t);
    }
}
