/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.coherence.httpsession;


import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.session.Session;
import io.micronaut.session.annotation.SessionValue;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest(propertySources = {"classpath:micronaut-http-session-test.yaml", "classpath:micronaut-http-session-test-config.xml"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CoherenceHttpSessionBindingTest {
    @Inject
    ApplicationContext context;

    @Test
    void shouldBindSessionUsingHeaderProcessing() {
        EmbeddedServer embeddedServer = context.getBean(EmbeddedServer.class).start();
        try {
            HttpClient client = context.createBean(HttpClient.class, embeddedServer.getURL());

            Flowable<HttpResponse<String>> flowable = Flowable.fromPublisher(client.exchange(HttpRequest.GET("/sessiontest/simple"), String.class));
            HttpResponse<String> response = flowable.blockingFirst();

            assertEquals("not in session", response.getBody().get());
            assertNotNull(response.header(HttpHeaders.AUTHORIZATION_INFO));

            String sessionId = response.header(HttpHeaders.AUTHORIZATION_INFO);
            flowable = Flowable.fromPublisher(client.exchange(
                    HttpRequest.GET("/sessiontest/simple").header(HttpHeaders.AUTHORIZATION_INFO, sessionId),
                    String.class));
            response = flowable.blockingFirst();

            assertEquals("value in session", response.getBody().get());
            assertNotNull(response.header(HttpHeaders.AUTHORIZATION_INFO));


            flowable = Flowable.fromPublisher(client.exchange(
                    HttpRequest.GET("/sessiontest/value").header(HttpHeaders.AUTHORIZATION_INFO, sessionId),
                    String.class));
            response = flowable.blockingFirst();
            assertEquals("value in session", response.getBody().get());
            assertNotNull(response.header(HttpHeaders.AUTHORIZATION_INFO));


            flowable = Flowable.fromPublisher(client.exchange(
                    HttpRequest.GET("/sessiontest/optional").header(HttpHeaders.AUTHORIZATION_INFO, sessionId),
                    String.class));
            response = flowable.blockingFirst();
            assertEquals("value in session", response.getBody().get());
            assertNotNull(response.header(HttpHeaders.AUTHORIZATION_INFO));
        } finally {
            embeddedServer.stop();
        }
    }

    @Controller("/sessiontest")
    static class SessionController {
        @Get("/simple")
        public String simple(Session session) {
            return session.get("myValue", String.class).orElseGet(() -> {
                session.put("myValue", "value in session");
                return "not in session";
            });
        }

        @Get("/value")
        String value(@SessionValue Optional<String> myValue) {
            return myValue.orElse("no value in session");
        }

        @Get("/optional")
        String optional(Optional<Session> session) {
            if (session.isPresent()) {
                Session s = session.get();
                return s.get("myValue", String.class).orElseGet(() -> {
                    s.put("myValue", "value in session");
                    return "not in session";
                });
            } else {
                return "no session";
            }
        }
    }
}
