package main.model.ticket;

import main.model.enums.BusinessValue;
import main.model.enums.CustomerDemand;
import main.model.enums.ExpertiseArea;
import main.model.enums.Priority;
import main.model.enums.Status;
import main.model.enums.TicketType;

public class FeatureRequest extends Ticket {
    // Someone wants a cool new feature
    // Important to know the business value and how much customers want it
    private BusinessValue businessValue;
    private CustomerDemand customerDemand;

    public FeatureRequest(int id, String title, Priority priority, Status status, ExpertiseArea expertiseArea,
            String description, String reportedBy, BusinessValue businessValue,
            CustomerDemand customerDemand) {
        super(id, TicketType.FEATURE_REQUEST, title, priority, status, expertiseArea, description, reportedBy);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
    }

    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    public CustomerDemand getCustomerDemand() {
        return customerDemand;
    }
}
