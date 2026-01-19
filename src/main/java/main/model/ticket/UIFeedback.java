package main.model.ticket;

import main.model.enums.BusinessValue;
import main.model.enums.ExpertiseArea;
import main.model.enums.Priority;
import main.model.enums.Status;
import main.model.enums.TicketType;

/**
 * Criticism or praise for the UI. Tracks specific element ID and usability
 * score
 */
public final class UIFeedback extends Ticket {
    private String uiElementId;
    private BusinessValue businessValue;
    private int usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;

    /**
     * Constructs a new UIFeedback ticket
     *
     * @param id             The ticket ID
     * @param title          The ticket title
     * @param priority       The ticket priority
     * @param status         The ticket status
     * @param expertiseArea  The required expertise area
     * @param description    The description
     * @param reportedBy     The username of the reporter
     * @param uiElementId    The ID of the UI element
     * @param businessValue  The business value
     * @param usabilityScore The usability score
     * @param screenshotUrl  The URL of the screenshot
     * @param suggestedFix   The suggested fix
     */
    public UIFeedback(final int id, final String title, final Priority priority,
            final Status status, final ExpertiseArea expertiseArea,
            final String description, final String reportedBy, final String uiElementId,
            final BusinessValue businessValue, final int usabilityScore,
            final String screenshotUrl, final String suggestedFix) {
        super(id, TicketType.UI_FEEDBACK, title, priority, status, expertiseArea, description,
                reportedBy);
        this.uiElementId = uiElementId;
        this.businessValue = businessValue;
        this.usabilityScore = usabilityScore;
        this.screenshotUrl = screenshotUrl;
        this.suggestedFix = suggestedFix;
    }

    /**
     * Gets the UI element ID
     *
     * @return The UI element ID
     */
    public String getUiElementId() {
        return uiElementId;
    }

    /**
     * Gets the business value
     *
     * @return The business value
     */
    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    /**
     * Gets the usability score
     *
     * @return The usability score
     */
    public int getUsabilityScore() {
        return usabilityScore;
    }

    /**
     * Gets the screenshot URL
     *
     * @return The screenshot URL
     */
    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    /**
     * Gets the suggested fix
     *
     * @return The suggested fix
     */
    public String getSuggestedFix() {
        return suggestedFix;
    }

    private static final double RISK_MAX = 100.0;
    private static final double IMPACT_MAX = 100.0;
    private static final double EFFICIENCY_MAX = 20.0;
    private static final int USABILITY_MAX = 11;
    private static final double MAX_PERCENTAGE = 100.0;

    @Override
    public double calculateRisk() {
        double bv;
        if (businessValue != null) {
            bv = businessValue.getValue();
        } else {
            bv = 0;
        }
        double raw = (USABILITY_MAX - usabilityScore) * bv;
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

        double raw = (usabilityScore + bv) / (double) effectiveDays;
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
        return usabilityScore * bv;
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
