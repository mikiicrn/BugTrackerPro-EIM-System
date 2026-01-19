package main.model.ticket;

import main.model.enums.ExpertiseArea;
import main.model.enums.Frequency;
import main.model.enums.Priority;
import main.model.enums.Severity;
import main.model.enums.Status;
import main.model.enums.TicketType;

/**
 * Represents a defect in the system, extending the generic Ticket entity
 *
 * This class encapsulates specific attributes required for defect tracking,
 * such as
 * expected vs. actual behavior, which are critical for reproduction and
 * debugging
 * It also incorporates quantitative metrics like Frequency and Severity
 * to facilitate the calculation of algorithmic scores for risk and impact
 *
 * Design Choice:
 * Implemented as a final class to ensure the integrity of the defect model,
 * preventing
 * accidental sub-classing that could violate the specific contracts defined for
 * Bug tracking
 */
public final class Bug extends Ticket {
    private String expectedBehavior;
    private String actualBehavior;
    private Frequency frequency;
    private Severity severity;
    private String environment;
    private Integer errorCode;

    /**
     * Value derived from the maximum possible Frequency value multiplied by the
     * maximum Severity value
     * Used to normalize the risk score to a 0-100 scale
     */
    private static final double RISK_MAX = 12.0;

    /**
     * Empirical constant representing the theoretical maximum impact score
     * Derived from the summation of max Priority, Severity, Frequency weights and a
     * standard maximum open duration
     * Used for normalization in impact reports
     */
    private static final double IMPACT_MAX = 93.55673;

    /**
     * Normalization constant for stability calculations, specific to the Bug ticket
     * type
     */
    private static final double STABILITY_MAX = 65.8962;

    /**
     * Normalization constant for efficiency calculations
     */
    private static final double EFFICIENCY_MAX = 70.0;

    /**
     * Scaling factor used to amplify the raw efficiency score before normalization
     */
    private static final double EFFICIENCY_SCALE = 10.0;
    private static final double MAX_PERCENTAGE = 100.0;

    /**
     * Constructs a new Bug ticket with comprehensive defect details
     *
     * @param id               Unique identifier for the ticket
     * @param title            Short summary of the defect
     * @param priority         Business priority level
     * @param status           Current workflow status
     * @param expertiseArea    Area of the system affected
     * @param description      Detailed description of the issue
     * @param reportedBy       User who identified the defect
     * @param expectedBehavior Description of the intended system behavior (for
     *                         verification)
     * @param actualBehavior   Description of the observed incorrect behavior (for
     *                         reproduction)
     * @param frequency        How often the defect manifests (critical for risk
     *                         assessment)
     * @param severity         The technical impact of the defect (critical for risk
     *                         assessment)
     * @param environment      System context (e.g., OS, Browser) where the bug was
     *                         observed
     * @param errorCode        Specific error code logged, if applicable
     */
    public Bug(final int id, final String title, final Priority priority, final Status status,
            final ExpertiseArea expertiseArea, final String description, final String reportedBy,
            final String expectedBehavior, final String actualBehavior, final Frequency frequency,
            final Severity severity, final String environment, final Integer errorCode) {
        super(id, TicketType.BUG, title, priority, status, expertiseArea, description,
                reportedBy);
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.frequency = frequency;
        this.severity = severity;
        this.environment = environment;
        this.errorCode = errorCode;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public String getActualBehavior() {
        return actualBehavior;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getEnvironment() {
        return environment;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * Calculates the quantitative risk score for this bug
     *
     * Algorithm:
     * Uses a standard Risk Matrix approach: Risk = Frequency * Severity
     *
     * The system then normalizes this raw product against RISK_MAX to produce a
     * percentage
     * value [0, 100]. This allows the system to uniformly compare risks, regardless
     * of
     * the underlying enum scale changes, provided RISK_MAX is updated accordingly
     *
     * Defensive Programming:
     * Null checks are employed for frequency and severity to ensure resilience
     * against incomplete ticket data, defaulting to 0 to represent negligible risk
     * in such cases
     *
     * @return The normalized risk percentage
     */
    @Override
    public double calculateRisk() {
        double freq;
        if (frequency != null) {
            freq = frequency.getValue();
        } else {
            freq = 0;
        }

        double sev;
        if (severity != null) {
            sev = severity.getValue();
        } else {
            sev = 0;
        }

        double raw = freq * sev;
        return Math.min(MAX_PERCENTAGE, (raw * MAX_PERCENTAGE) / RISK_MAX);
    }

    /**
     * Computes the efficiency score for resolving this bug
     *
     * Logic:
     * The algorithm rewards the resolution of high-frequency, high-severity bugs in
     * a short timeframe
     *
     * Formula: (Frequency + Severity) * SCALE / DaysToResolve
     *
     * This implies that efficiency is directly proportional to the "difficulty"
     * (severity + frequency)
     * and inversely proportional to the time taken
     *
     * Edge Case Handling:
     * If daysToResolve is <= 0 (representing immediate same-day fixes), the system
     * normalizes it to 1
     * to prevent ArithmeticException (division by zero) and to incorrectly penalize
     * instant fixes
     *
     * @param daysToResolve The time taken to close the ticket
     * @return The normalized efficiency score [0, 100]
     */
    @Override
    public double calculateEfficiency(final long daysToResolve) {
        long effectiveDays = daysToResolve;
        if (effectiveDays <= 0) {
            effectiveDays = 1;
        }
        double freq;
        if (frequency != null) {
            freq = frequency.getValue();
        } else {
            freq = 0;
        }

        double sev;
        if (severity != null) {
            sev = severity.getValue();
        } else {
            sev = 0;
        }

        double raw = (freq + sev) * EFFICIENCY_SCALE / effectiveDays;
        return Math.min(MAX_PERCENTAGE, (raw * MAX_PERCENTAGE) / EFFICIENCY_MAX);
    }

    /**
     * Calculates the raw impact score of determining the urgency and cost of this
     * bug
     *
     * Algorithm:
     * Impact = ((Priority + Severity) * Frequency) + DaysOpen
     *
     * Reasoning:
     * This composite score weights the intrinsic "badness" of the bug (P+S)
     * magnified by how often it happens (F)
     * Critically, this algorithm adds daysOpen to this product. This ensures that
     * even a
     * moderate bug becomes
     * high-impact if it is ignored for a long period, reflecting the "technical
     * debt" accumulation
     *
     * @param daysOpen The number of days the ticket has remained open
     * @return The raw impact score (unbounded)
     */
    @Override
    public double calculateRawImpact(final long daysOpen) {
        double p;
        if (getPriority() != null) {
            p = getPriority().getValue();
        } else {
            p = 0;
        }

        double sev;
        if (severity != null) {
            sev = severity.getValue();
        } else {
            sev = 0;
        }

        double freq;
        if (frequency != null) {
            freq = frequency.getValue();
        } else {
            freq = 0;
        }

        return ((p + sev) * freq) + daysOpen;
    }

    @Override
    public double getImpactMax() {
        return IMPACT_MAX;
    }

    @Override
    public double getStabilityMax() {
        return STABILITY_MAX;
    }
}
