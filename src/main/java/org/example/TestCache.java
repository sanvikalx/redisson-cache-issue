package org.example;

import io.micronaut.cache.annotation.Cacheable;
import jakarta.inject.Singleton;
import java.util.concurrent.atomic.AtomicLong;
import reactor.core.publisher.Mono;

@Singleton
public class TestCache {

    private AtomicLong counter = new AtomicLong(0);

    @Cacheable("my-cache")
    public String getMyValue(final String arg) {
        return arg + counter.incrementAndGet();
    }

    @Cacheable("my-cache-async")
    public Mono<String> getMyValueSync(final String arg) {
        return Mono.defer(() -> Mono.just(arg + counter.incrementAndGet()));
    }

    @Cacheable(cacheNames = {"obj-cache-async"}, parameters = {"arg"})
    public Mono<TestObject> getObjectSync(final String arg) {
        final TestObject t = new TestObject();
        t.setText(arg + counter.incrementAndGet());
        return Mono.defer(() -> Mono.just(t));
    }
}
