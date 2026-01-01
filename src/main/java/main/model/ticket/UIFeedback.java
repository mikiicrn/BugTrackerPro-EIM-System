package main.model.ticket;

import main.model.enums.BusinessValue;
import main.model.enums.ExpertiseArea;
import main.model.enums.Priority;
import main.model.enums.Status;
import main.model.enums.TicketType;

public class UIFeedback extends Ticket {
    // Criticism or praise for the UI
    // Tracks specific element ID and usability score
    private String uiElementId;
    private BusinessValue businessValue;
    private int usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;

    public UIFeedback(int id, String title, Priority priority, Status status, ExpertiseArea expertiseArea,
            String description, String reportedBy, String uiElementId, BusinessValue businessValue,
            int usabilityScore, String screenshotUrl, String suggestedFix) {
        super(id, TicketType.UI_FEEDBACK, title, priority, status, expertiseArea, description, reportedBy);
        this.uiElementId = uiElementId;
        this.businessValue = businessValue;
        this.usabilityScore = usabilityScore;
        this.screenshotUrl = screenshotUrl;
        this.suggestedFix = suggestedFix;
    }

    public String getUiElementId() {
        return uiElementId;
    }

    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    public int getUsabilityScore() {
        return usabilityScore;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }
}
