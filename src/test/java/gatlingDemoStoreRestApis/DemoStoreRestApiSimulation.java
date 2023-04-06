package gatlingDemoStoreRestApis;

import gatlingDemoStoreRestApis.pageObjectModel.Scenario;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static gatlingDemoStoreRestApis.SimulationVariables.*;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;


public class DemoStoreRestApiSimulation extends Simulation {
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://demostore.gatling.io")
            .header("Cache-Control", "no-cache")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    @Override
    public void before() {
        System.out.printf("Test duration is set to: %d\n", TEST_DURATION.getSeconds());
        System.out.printf("Ramping with %d users over %s seconds\n", USER_COUNT, RAMP_DURATION.getSeconds());
    }

    //Sequential execution of scenarios
    {
        setUp(
                Scenario.defaultScn.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION))
                        .protocols(httpProtocol)
                        .andThen(Scenario.noAdminScn.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION))
                                .protocols(httpProtocol)));
    }

}
