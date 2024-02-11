package dev.techsphere.sftpconfigurer.config;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class OnPropertyArrayCondition extends SpringBootCondition {

    protected static final String[] REQUIRED_SERVER_KEY = {
        "name",
        "host",
        "user"
    };

    protected static final String[] REQUIRED_INBOUND_KEY = {
        "remoteDir",
        "localDir",
        "schedule"
    };

    protected static final String[] REQUIRED_OUTBOUND_KEY = {
            "remoteDir",
            "localDir",
            "schedule"
    };


    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        List<String> missingProperties = new ArrayList<>();

        Map<String, Map<String, Object>> serverProps = Binder.get(context.getEnvironment())
                                                        .bind("sftp.server", Map.class)
                                                        .get();

        if(serverProps.size() == 0) {
            missingProperties.addAll(Arrays.asList(REQUIRED_SERVER_KEY));
            return determineOutcome(missingProperties);
        }

        for (Entry<String, Map<String, Object>> entry: serverProps.entrySet()) {
            String index = entry.getKey();
            Map<String, Object> serverDetails = entry.getValue();

            missingProperties.addAll(Arrays.stream(REQUIRED_SERVER_KEY).filter(key -> !serverDetails.containsKey(key))
                    .map(key -> "sftp.server[" + index + "]."+ key).toList());

            if(!serverDetails.containsKey("privateKeyPath") && !serverDetails.containsKey("password")) {
                missingProperties.add("sftp.server[" + index + "].privateKeyPath");
                missingProperties.add("sftp.server[" + index + "].privateKeyPassphrase");
                missingProperties.add("sftp.server[" + index + "].password");
            }

            if (serverDetails.containsKey("inbound")) {
                Map<String, Map<String, String>> inboundDetails = (Map<String, Map<String, String>>) serverDetails.get("inbound");

                for(Entry<String, Map<String, String>> inboundEntry: inboundDetails.entrySet()) {
                    String inIndex = inboundEntry.getKey();
                    Map<String, String> inProps = inboundEntry.getValue();

                    missingProperties.addAll(Arrays.stream(REQUIRED_INBOUND_KEY).filter(key -> !inProps.containsKey(key))
                            .map(key -> "sftp.server["+ index + "]inbound["+ inIndex + "]." + key)
                            .toList());
                }
            }

            if (serverDetails.containsKey("outbound")) {
                Map<String, Map<String, String>> outboundDetails = (Map<String, Map<String, String>>) serverDetails.get("outbound");

                for(Entry<String, Map<String, String>> outboundEntry: outboundDetails.entrySet()) {
                    String inIndex = outboundEntry.getKey();
                    Map<String, String> inProps = outboundEntry.getValue();

                    missingProperties.addAll(Arrays.stream(REQUIRED_OUTBOUND_KEY).filter(key -> !inProps.containsKey(key))
                            .map(key -> "sftp.server["+ index + "]outbound["+ inIndex + "]." + key)
                            .toList());
                }
            }


        }

        return determineOutcome(missingProperties);
    }

    private ConditionOutcome determineOutcome(List<String> missingProperties) {
        if (missingProperties.isEmpty())
            return ConditionOutcome.match(ConditionMessage.forCondition(OnPropertyArrayCondition.class.getCanonicalName(), "Server details")
                    .found("property", "properties")
                    .items(Arrays.asList(REQUIRED_SERVER_KEY, REQUIRED_INBOUND_KEY, REQUIRED_OUTBOUND_KEY)));

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(OnPropertyArrayCondition.class.getCanonicalName(), "Server Properties")
                .didNotFind("property", "properties")
                .items(missingProperties));
    }
}
