package gatlingDemoStoreRestApis.pageObjectModel;

import java.util.Map;

public class Headers {
    public static Map<CharSequence, String> authorizationHeader = Map.ofEntries(
            Map.entry("authorization", "Bearer #{jwt}")
    );

}
