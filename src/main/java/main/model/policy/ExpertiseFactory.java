package main.model.policy;

import main.model.enums.ExpertiseArea;
import java.util.EnumMap;
import java.util.Map;

/**
 * Factory to retrieve ExpertisePolicy based on ExpertiseArea enum
 */
public final class ExpertiseFactory {
    private static final Map<ExpertiseArea, ExpertisePolicy> POLICY_MAP = new EnumMap<>(ExpertiseArea.class);

    static {
        POLICY_MAP.put(ExpertiseArea.FRONTEND, new FrontendPolicy());
        POLICY_MAP.put(ExpertiseArea.BACKEND, new BackendPolicy());
        POLICY_MAP.put(ExpertiseArea.FULLSTACK, new FullstackPolicy());
        POLICY_MAP.put(ExpertiseArea.DEVOPS, new DevOpsPolicy());
        POLICY_MAP.put(ExpertiseArea.DESIGN, new DesignPolicy());
        POLICY_MAP.put(ExpertiseArea.DB, new DbPolicy());
    }

    private ExpertiseFactory() {
    }

    /**
     * Gets the policy for a given expertise area
     *
     * @param expertise The expertise area enum
     * @return The corresponding ExpertisePolicy
     */
    public static ExpertisePolicy getPolicy(final ExpertiseArea expertise) {
        return POLICY_MAP.get(expertise);
    }
}
