package main.utils;

import main.model.enums.*;
import main.model.ticket.*;
import java.util.Map;

public class TicketFactory {

    // Another Factory Pattern here
    // This one helps me create Bug, FeatureRequest, or UIFeedback tickets
    // Keeps the creation logic internally here so the rest of the app doesn't worry
    // about it

    public static Ticket createTicket(int id, Map<String, Object> params) {
        String typeStr = (String) params.get("type");
        if (typeStr == null)
            throw new IllegalArgumentException("Ticket type is missing");

        TicketType type;
        try {
            type = TicketType.valueOf(typeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ticket type: " + typeStr);
        }

        String title = (String) params.get("title");

        Object priorityObj = params.get("businessPriority"); // Check key name Match input
        // NOTE: Input JSON creates "businessPriority".
        if (priorityObj == null)
            throw new IllegalArgumentException("Priority missing (businessPriority)");

        Priority priority;
        try {
            priority = Priority.valueOf((String) priorityObj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid priority: " + priorityObj);
        }

        Status status = Status.OPEN; // Default status
        if (params.containsKey("status")) {
            status = Status.valueOf((String) params.get("status"));
        }

        ExpertiseArea expertiseArea = null;
        if (params.containsKey("expertiseArea")) {
            expertiseArea = ExpertiseArea.valueOf((String) params.get("expertiseArea"));
        }

        String description = (String) params.get("description");
        String reportedBy = (String) params.get("reportedBy");

        // Checking the ticket type and creating the specific object needed
        // Simple and clean
        switch (type) {
            case BUG:
                String expectedBehavior = (String) params.get("expectedBehavior");
                String actualBehavior = (String) params.get("actualBehavior");

                if (params.get("frequency") == null)
                    throw new IllegalArgumentException("Frequency missing");
                Frequency frequency = Frequency.valueOf((String) params.get("frequency"));

                if (params.get("severity") == null)
                    throw new IllegalArgumentException("Severity missing");
                Severity severity = Severity.valueOf((String) params.get("severity"));

                String environment = (String) params.get("environment");
                Integer errorCode = null;
                if (params.get("errorCode") != null) {
                    errorCode = (Integer) params.get("errorCode");
                }
                return new Bug(id, title, priority, status, expertiseArea, description, reportedBy,
                        expectedBehavior, actualBehavior, frequency, severity, environment, errorCode);
            case FEATURE_REQUEST:
                if (params.get("businessValue") == null)
                    throw new IllegalArgumentException("BusinessValue missing");
                BusinessValue fBusinessValue = BusinessValue.valueOf((String) params.get("businessValue"));

                if (params.get("customerDemand") == null)
                    throw new IllegalArgumentException("CustomerDemand missing");
                CustomerDemand customerDemand = CustomerDemand.valueOf((String) params.get("customerDemand"));

                return new FeatureRequest(id, title, priority, status, expertiseArea, description, reportedBy,
                        fBusinessValue, customerDemand);
            case UI_FEEDBACK:
                String uiElementId = (String) params.get("uiElementId");
                if (params.get("businessValue") == null)
                    throw new IllegalArgumentException("BusinessValue missing for UI_FEEDBACK");
                BusinessValue uBusinessValue = BusinessValue.valueOf((String) params.get("businessValue"));

                int usabilityScore = (Integer) params.get("usabilityScore");
                String screenshotUrl = (String) params.get("screenshotUrl");
                String suggestedFix = (String) params.get("suggestedFix");
                return new UIFeedback(id, title, priority, status, expertiseArea, description, reportedBy,
                        uiElementId, uBusinessValue, usabilityScore, screenshotUrl, suggestedFix);
            default:
                throw new IllegalArgumentException("Unknown ticket type: " + type);
        }
    }
}
