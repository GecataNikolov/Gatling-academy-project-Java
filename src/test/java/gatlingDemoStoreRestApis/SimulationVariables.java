package gatlingDemoStoreRestApis;

import java.time.Duration;

public interface SimulationVariables {
    int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "5"));
    Duration RAMP_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("RAMP_DURATION", "10")));
    Duration TEST_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("TEST_DURATION", "10")));

}
