package main.model.user;

import main.model.enums.ExpertiseArea;
import main.model.enums.Role;
import main.model.enums.Seniority;
import main.model.ticket.Ticket;
import main.model.policy.PolicyFactory;
import main.model.policy.ExpertiseFactory;

/**
 * The workhorses of the project. They have skills (Expertise) and experience
 * (Seniority).
 * They fix bugs and implement features
 */
public class Developer extends User {
    private String hireDate;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;
    private double performanceScore = 0.0;

    /**
     * Constructs a new Developer
     *
     * @param username      The username
     * @param email         The email
     * @param hireDate      The hire date string
     * @param expertiseArea The developer's expertise area
     * @param seniority     The developer's seniority level
     */
    public Developer(final String username, final String email, final String hireDate,
            final ExpertiseArea expertiseArea, final Seniority seniority) {
        super(username, email, Role.DEVELOPER);
        this.hireDate = hireDate;
        this.expertiseArea = expertiseArea;
        this.seniority = seniority;
    }

    /**
     * Checks if this developer can handle the given ticket based on seniority and
     * expertise
     *
     * @param ticket The ticket to check
     * @return True if the developer can handle it, false otherwise
     */
    public boolean canHandle(final Ticket ticket) {
        // Check Ticket Type and Priority based on Seniority
        boolean seniorityCheck = false;
        if (PolicyFactory.getPolicy(seniority) != null) {
            seniorityCheck = PolicyFactory.getPolicy(seniority).canHandle(ticket);
        }

        if (!seniorityCheck) {
            return false;
        }

        ExpertiseArea ticketArea = ticket.getExpertiseArea();
        if (ticketArea == null) {
            return true;
        }

        if (ExpertiseFactory.getPolicy(expertiseArea) != null) {
            return ExpertiseFactory.getPolicy(expertiseArea).covers(ticketArea);
        }
        return false;
    }

    /**
     * Gets the hire date
     *
     * @return The hire date
     */
    public String getHireDate() {
        return hireDate;
    }

    /**
     * Gets the expertise area
     *
     * @return The expertise area
     */
    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    /**
     * Gets the seniority level
     *
     * @return The seniority
     */
    public Seniority getSeniority() {
        return seniority;
    }

    /**
     * Gets the performance score
     *
     * @return The performance score
     */
    public double getPerformanceScore() {
        return performanceScore;
    }

    /**
     * Sets the performance score
     *
     * @param performanceScore The new score
     */
    public void setPerformanceScore(final double performanceScore) {
        this.performanceScore = performanceScore;
    }
}
