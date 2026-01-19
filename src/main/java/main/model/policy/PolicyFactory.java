package main.model.policy;

import main.model.enums.Seniority;
import java.util.EnumMap;
import java.util.Map;

/**
 * Factory to retrive SeniorityPolicy based on Seniority enum
 */
public final class PolicyFactory {
    private static final Map<Seniority, SeniorityPolicy> POLICY_MAP = new EnumMap<>(Seniority.class);

    static {
        POLICY_MAP.put(Seniority.JUNIOR, new JuniorPolicy());
        POLICY_MAP.put(Seniority.MID, new MidPolicy());
        POLICY_MAP.put(Seniority.SENIOR, new SeniorPolicy());
    }

    private PolicyFactory() {
    }

    /**
     * Gets the policy for a given seniority
     *
     * @param seniority The seniority enum
     * @return The corresponding SeniorityPolicy
     */
    public static SeniorityPolicy getPolicy(final Seniority seniority) {
        return POLICY_MAP.get(seniority);
    }
}
