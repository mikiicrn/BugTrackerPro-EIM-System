package main.model.ticket;

import main.model.enums.BusinessValue;
import main.model.enums.CustomerDemand;
import main.model.enums.ExpertiseArea;
import main.model.enums.Priority;
import main.model.enums.Status;
import main.model.enums.TicketType;

/**
 * Someone wants a cool new feature. Important to know the business value and
 * how much customers
 * want it
 */
public final class FeatureRequest extends Ticket {
    private BusinessValue businessValue;
    private CustomerDemand customerDemand;

    /**
     * Constructs a new FeatureRequest ticket
     *
     * @param id             The ticket ID
     * @param title          The ticket title
     * @param priority       The ticket priority
     * @param status         The ticket status
     * @param expertiseArea  The required expertise area
     * @param description    The description of the feature
     * @param reportedBy     The username of the reporter
     * @param businessValue  The estimated business value
     * @param customerDemand The level of customer demand
     */
    public FeatureRequest(final int id, final String title, final Priority priority,
            final Status status, final ExpertiseArea expertiseArea,
            final String description, final String reportedBy,
            final BusinessValue businessValue,
            final CustomerDemand customerDemand) {
        super(id, TicketType.FEATURE_REQUEST, title, priority, status, expertiseArea, description,
                reportedBy);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
    }

    /**
     * Gets the business value of this feature request
     *
     * @return The business value
     */
    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    /**
     * Gets the customer demand for this feature request
     *
     * @return The customer demand
     */
    public CustomerDemand getCustomerDemand() {
        return customerDemand;
    }

    private static final double RISK_MAX = 20.0;
    private static final double IMPACT_MAX = 146.66666666666666;
    private static final double EFFICIENCY_MAX = 20.0;
    private static final double MAX_PERCENTAGE = 100.0;

    @Override
    public double calculateRisk() {
        double bv;
        if (businessValue != null) {
            bv = businessValue.getValue();
        } else {
            bv = 0;
        }

        double demand;
        if (customerDemand != null) {
            demand = customerDemand.getValue();
        } else {
            demand = 0;
        }

        double raw = bv + demand;
        return Math.min(MAX_PERCENTAGE, (raw * MAX_PERCENTAGE) / RISK_MAX);
    }

    @Override
    public double calculateEfficiency(final long daysToResolve) {
        long effectiveDays = daysToResolve;
        if (effectiveDays <= 0) {
            effectiveDays = 1;
        }
        double bv;
        if (businessValue != null) {
            bv = businessValue.getValue();
        } else {
            bv = 0;
        }

        double demand;
        if (customerDemand != null) {
            demand = customerDemand.getValue();
        } else {
            demand = 0;
        }

        double raw = (bv + demand) / (double) effectiveDays;
        return Math.min(MAX_PERCENTAGE, (raw * MAX_PERCENTAGE) / EFFICIENCY_MAX);
    }

    @Override
    public double calculateRawImpact(final long daysOpen) {
        double bv;
        if (businessValue != null) {
            bv = businessValue.getValue();
        } else {
            bv = 0;
        }

        double p;
        if (getPriority() != null) {
            p = getPriority().getValue();
        } else {
            p = 0;
        }

        double d;
        if (customerDemand != null) {
            d = customerDemand.getValue();
        } else {
            d = 0;
        }

        return bv * (p + d);
    }

    @Override
    public double getImpactMax() {
        return IMPACT_MAX;
    }

    @Override
    public double getStabilityMax() {
        return IMPACT_MAX;
    }
}
