package gatlingDemoStore.helpers;

import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.http.HttpDsl.http;

public interface SimulationDefaults {
    String DOMAIN = "demostore.gatling.io";
    HttpProtocolBuilder HTTP_PROTOCOL = http.baseUrl("https://" + DOMAIN);
    Integer USER_COUNT = Integer.parseInt(System.getProperty("USERS", "5"));
    Duration RAMP_DURATION = Duration.ofSeconds(Long.parseLong(System.getProperty("RAMP_DURATION", "60")));
    Duration TEST_DURATION = Duration.ofSeconds(Long.parseLong(System.getProperty("TEST_DURATION", "60")));
}
