package org.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@MicronautTest
@Testcontainers
class RedissonCacheIssueTest {

    private static final int REDIS_PORT = 6379;
    private static ApplicationContext context;


    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis/redis-stack:7.4.0-v0")
            .withExposedPorts(6379)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

//    @Inject
//    TestCache testCache;


    @BeforeAll
    public static void setUp() {
        context = ApplicationContext.run(PropertySource.of(
                "test", Map.of("redis.host", redis.getContainerIpAddress(), "redis.port",
                        redis.getMappedPort(REDIS_PORT),
                        "redisson.singleServerConfig.address", "redis://" + redis.getHost() + ":" + redis.getMappedPort(REDIS_PORT))
        ));
        redis.start();
    }

    @AfterAll
    public static void tearDown() {
        redis.stop();
    }


    @Test
    void testSyncCache() {
        //ok, everything works as expected
        TestCache testCache = context.createBean(TestCache.class);
        for (int i = 0; i < 3; i++) {
            Assertions.assertEquals("a1", testCache.getMyValue("a"));
        }
    }

    @Test
    void testAsyncCache() {
        TestCache testCache = context.createBean(TestCache.class);
        //not ok
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                //first time, result without caching, type String as expected
                Assertions.assertEquals("a1", testCache.getMyValueSync("a").block());
            } else {
                //next time, result from cache, and it has become Optional
                Assertions.assertEquals(Optional.of("a1"), testCache.getMyValueSync("a").block());
            }
        }
    }

}
