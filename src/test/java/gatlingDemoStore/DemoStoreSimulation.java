package gatlingDemoStore;

import gatlingDemoStore.helpers.SimulationDefaults;
import gatlingDemoStore.pageObject.UserJourneys;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.rampUsers;


public class DemoStoreSimulation extends Simulation {
    @Override
    public void before() {
        System.out.printf("Running test with %d users%n", SimulationDefaults.USER_COUNT);
        System.out.printf("Ramping users over  %d seconds%n", SimulationDefaults.RAMP_DURATION.getSeconds());
        System.out.printf("Total test duration: %d%n", SimulationDefaults.TEST_DURATION.getSeconds());
    }

    {
        setUp(
                UserJourneys.Scenarios.defaultScenario.injectOpen(
                        rampUsers(SimulationDefaults.USER_COUNT).during(SimulationDefaults.RAMP_DURATION)
                ).protocols(SimulationDefaults.HTTP_PROTOCOL),
                UserJourneys.Scenarios.highPurchase.injectOpen(
                        rampUsers(SimulationDefaults.USER_COUNT).during(SimulationDefaults.RAMP_DURATION)

                ).protocols(SimulationDefaults.HTTP_PROTOCOL)

        );
    }

}
