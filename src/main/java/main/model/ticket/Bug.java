package main.model.ticket;

import main.model.enums.ExpertiseArea;
import main.model.enums.Frequency;
import main.model.enums.Priority;
import main.model.enums.Severity;
import main.model.enums.Status;
import main.model.enums.TicketType;

public class Bug extends Ticket {
    // A bug! Something is broken
    // We track severity, frequency and what actually happened vs what should have
    // happened
    private String expectedBehavior;
    private String actualBehavior;
    private Frequency frequency;
    private Severity severity;
    private String environment;
    private Integer errorCode;

    public Bug(int id, String title, Priority priority, Status status, ExpertiseArea expertiseArea,
            String description, String reportedBy, String expectedBehavior, String actualBehavior,
            Frequency frequency, Severity severity, String environment, Integer errorCode) {
        super(id, TicketType.BUG, title, priority, status, expertiseArea, description, reportedBy);
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
}
