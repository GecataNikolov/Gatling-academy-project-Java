package gatlingDemoStore;

import static io.gatling.javaapi.core.CoreDsl.*;

import gatlingDemoStore.helpers.SimulationDefaults;
import gatlingDemoStore.pageObject.UserJourneys;
import io.gatling.javaapi.core.*;


public class DemoStoreSimulation extends Simulation {

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
