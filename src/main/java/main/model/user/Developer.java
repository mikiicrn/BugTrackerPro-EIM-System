package main.model.user;

import main.model.enums.ExpertiseArea;
import main.model.enums.Role;
import main.model.enums.Seniority;
import main.model.ticket.Ticket;
import main.model.enums.TicketType;
import main.model.enums.Priority;

import java.util.List;

public class Developer extends User {
    // The workhorses of the project
    // They have skills (Expertise) and experience (Seniority)
    // They fix bugs and implement features
    private String hireDate;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;

    public Developer(String username, String email, String hireDate, ExpertiseArea expertiseArea, Seniority seniority) {
        super(username, email, Role.DEVELOPER);
        this.hireDate = hireDate;
        this.expertiseArea = expertiseArea;
        this.seniority = seniority;
    }

    public boolean canHandle(Ticket ticket) {
        // Check Ticket Type and Priority based on Seniority
        boolean seniorityCheck = false;
        switch (seniority) {
            case JUNIOR:
                if ((ticket.getPriority() == Priority.LOW || ticket.getPriority() == Priority.MEDIUM) &&
                        (ticket.getType() == TicketType.BUG || ticket.getType() == TicketType.UI_FEEDBACK)) {
                    seniorityCheck = true;
                }
                break;
            case MID:
                if (ticket.getPriority() != Priority.CRITICAL) {
                    seniorityCheck = true;
                }
                // Mid cannot handle CRITICAL, but can handle all types? Prompt says:
                // MID: BUG, UI_FEEDBACK, FEATURE_REQUEST. So all types.
                break;
            case SENIOR:
                seniorityCheck = true;
                break;
        }

        if (!seniorityCheck)
            return false;

        // Check Expertise
        // Specializare Zone accesibile
        // FRONTEND -> FRONTEND, DESIGN
        // BACKEND -> BACKEND, DB
        // FULLSTACK -> FRONTEND, BACKEND, DEVOPS, DESIGN, DB (All?)
        // DEVOPS -> DEVOPS
        // DESIGN -> DESIGN, FRONTEND
        // DB -> DB

        ExpertiseArea ticketArea = ticket.getExpertiseArea();
        if (ticketArea == null)
            return true; // Should not happen if ticket valid

        switch (expertiseArea) {
            case FRONTEND:
                return ticketArea == ExpertiseArea.FRONTEND || ticketArea == ExpertiseArea.DESIGN;
            case BACKEND:
                return ticketArea == ExpertiseArea.BACKEND || ticketArea == ExpertiseArea.DB;
            case FULLSTACK:
                // FULLSTACK -> FRONTEND, BACKEND, DEVOPS, DESIGN, DB.
                // Assuming "DEVOPS, DESIGN, DB" are also covered.
                // Wait, FULLSTACK description: FRONTEND, BACKEND, DEVOPS, DESIGN, DB. Yes.
                return true;
            case DEVOPS:
                return ticketArea == ExpertiseArea.DEVOPS;
            case DESIGN:
                return ticketArea == ExpertiseArea.DESIGN || ticketArea == ExpertiseArea.FRONTEND;
            case DB:
                return ticketArea == ExpertiseArea.DB;
            default:
                return false;
        }
    }

    public String getHireDate() {
        return hireDate;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    private double performanceScore = 0.0;

    public double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }
}
