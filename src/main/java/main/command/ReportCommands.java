package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.ticket.Ticket;
import main.model.user.Developer;
import main.model.user.Manager;
import main.model.user.User;
import main.model.enums.Role;
import main.model.enums.Priority;
import main.model.enums.TicketType;
import main.model.enums.Status;
import main.system.BugTrackerSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// creating all the reports for managers
public final class ReportCommands {

    private static final double MAJOR_RISK_THRESHOLD = 50.0;
    private static final double MODERATE_RISK_THRESHOLD = 15.0;
    private static final double STATS_DIVISOR = 3.0;
    private static final double JUNIOR_BONUS = 5.0;
    private static final double JUNIOR_WEIGHT = 0.5;
    private static final double MID_BONUS = 15.0;
    private static final double MID_CLOSED_WEIGHT = 0.5;
    private static final double MID_PRIO_WEIGHT = 0.7;
    private static final double MID_TIME_WEIGHT = 0.3;
    private static final double SENIOR_BONUS = 30.0;
    private static final double SENIOR_CLOSED_WEIGHT = 0.5;
    private static final double SENIOR_PRIO_WEIGHT = 1.0;
    private static final double SENIOR_TIME_WEIGHT = 0.5;
    private static final double MAX_PERCENTAGE = 100.0;
    private static final double FACTOR = 100.0;

    private ReportCommands() {
    }

    /**
     * Generates the performance report for developers
     *
     * @param system The bug tracker system
     * @param user   The user generating the report (Manager)
     * @param params The parameters for the report (unused)
     * @param result The result object to populate
     */
    public static void handleGeneratePerformanceReport(final BugTrackerSystem system,
            final User user,
            final Map<String, Object> params,
            final ObjectNode result) {
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers.");
        }
        Manager m = (Manager) user;
        ArrayNode rep = result.putArray("report");
        List<String> subs = new ArrayList<>(m.getSubordinates());
        Collections.sort(subs);
        for (String s : subs) {
            User u = system.getUser(s);
            if (u.getRole() == Role.DEVELOPER) {
                Developer d = (Developer) u;
                ObjectNode r = rep.addObject();
                r.put("username", d.getUsername());

                List<Ticket> closed = new ArrayList<>();

                String cmdTimestamp = (String) params.get("timestamp");
                if (cmdTimestamp == null) {
                    cmdTimestamp = system.getCurrentDate().toString();
                }

                java.time.LocalDate cmdDate = java.time.LocalDate.parse(cmdTimestamp);
                java.time.YearMonth currentMonth = java.time.YearMonth.from(cmdDate);
                java.time.YearMonth previousMonth = currentMonth.minusMonths(1);

                for (Ticket t : system.getAllTickets()) {
                    if (d.getUsername().equals(t.getAssignedTo())
                            && t.getStatus() == Status.CLOSED) {
                        java.time.LocalDate solvedAt = t.getSolvedAt();
                        if (solvedAt != null) {
                            java.time.YearMonth solvedMonth = java.time.YearMonth.from(solvedAt);
                            if (solvedMonth.equals(previousMonth)) {
                                closed.add(t);
                            }
                        }
                    }
                }

                r.put("closedTickets", closed.size());

                double totalDays = 0;
                int bugCount = 0;
                int featureCount = 0;
                int uiCount = 0;
                int highPrioCount = 0;

                for (Ticket t : closed) {
                    if (t.getPriority() == Priority.HIGH
                            || t.getPriority() == Priority.CRITICAL) {
                        highPrioCount++;
                    }
                    if (t.getType() == TicketType.BUG) {
                        bugCount++;
                    } else if (t.getType() == TicketType.FEATURE_REQUEST) {
                        featureCount++;
                    } else if (t.getType() == TicketType.UI_FEEDBACK) {
                        uiCount++;
                    }

                    java.time.LocalDate resolutionDate = t.getSolvedAt();
                    if (resolutionDate == null) {
                        continue;
                    }

                    long daysRes = java.time.temporal.ChronoUnit.DAYS
                            .between(t.getAssignedAt(), resolutionDate) + 1;
                    if (daysRes < 1) {
                        daysRes = 1;
                    }

                    totalDays += daysRes;
                }

                double avgRes;
                if (closed.isEmpty()) {
                    avgRes = 0.0;
                } else {
                    avgRes = truncate(totalDays / closed.size());
                }
                r.put("averageResolutionTime", avgRes);

                double perfScore = 0.0;
                if (!closed.isEmpty()) {
                    if (d.getSeniority() == main.model.enums.Seniority.JUNIOR) {
                        double meanCount = (bugCount + featureCount + uiCount) / STATS_DIVISOR;
                        double variance = (Math.pow(bugCount - meanCount, 2)
                                + Math.pow(featureCount - meanCount, 2)
                                + Math.pow(uiCount - meanCount, 2)) / STATS_DIVISOR;
                        double stdDev = Math.sqrt(variance);
                        double diversity;
                        if (meanCount == 0) {
                            diversity = 0.0;
                        } else {
                            diversity = stdDev / meanCount;
                        }

                        perfScore = Math.max(0, JUNIOR_WEIGHT * closed.size() - diversity)
                                + JUNIOR_BONUS;
                    } else if (d.getSeniority() == main.model.enums.Seniority.MID) {
                        perfScore = Math.max(0, MID_CLOSED_WEIGHT * closed.size()
                                + MID_PRIO_WEIGHT * highPrioCount
                                - MID_TIME_WEIGHT * avgRes) + MID_BONUS;
                    } else if (d.getSeniority() == main.model.enums.Seniority.SENIOR) {
                        perfScore = Math.max(0, SENIOR_CLOSED_WEIGHT * closed.size()
                                + SENIOR_PRIO_WEIGHT * highPrioCount
                                - SENIOR_TIME_WEIGHT * avgRes) + SENIOR_BONUS;
                    }
                }

                perfScore = truncate(perfScore);
                r.put("performanceScore", perfScore);
                r.put("seniority", d.getSeniority().toString());
            }
        }
    }

    /**
     * Generates a ticket risk report for managers
     *
     * @param system The bug tracker system
     * @param user   The user generating the report (must be MANAGER)
     * @param result The result object to populate
     */
    public static void handleGenerateTicketRiskReport(final BugTrackerSystem system,
            final User user,
            final ObjectNode result) {
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers.");
        }

        ObjectNode report = result.putObject("report");
        List<Ticket> allTickets = system.getAllTickets();
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket t : allTickets) {
            main.model.enums.Status s = t.getStatus();
            if (s == main.model.enums.Status.OPEN || s == null) {
                tickets.add(t);
            }
        }

        report.put("totalTickets", tickets.size());

        ObjectNode byType = report.putObject("ticketsByType");
        Map<TicketType, Integer> typeCounts = new HashMap<>();
        for (TicketType t : TicketType.values()) {
            typeCounts.put(t, 0);
        }
        for (Ticket t : tickets) {
            typeCounts.put(t.getType(), typeCounts.get(t.getType()) + 1);
        }
        for (TicketType t : TicketType.values()) {
            byType.put(t.toString(), typeCounts.get(t));
        }

        ObjectNode byPriority = report.putObject("ticketsByPriority");
        Map<Priority, Integer> prioCounts = new HashMap<>();
        for (Priority p : Priority.values()) {
            prioCounts.put(p, 0);
        }
        for (Ticket p : tickets) {
            prioCounts.put(p.getPriority(), prioCounts.get(p.getPriority()) + 1);
        }
        for (Priority p : Priority.values()) {
            byPriority.put(p.toString(), prioCounts.get(p));
        }

        ObjectNode riskMap = report.putObject("riskByType");

        for (TicketType t : TicketType.values()) {
            List<Ticket> typeTickets = new ArrayList<>();
            for (Ticket tick : tickets) {
                if (tick.getType() == t) {
                    typeTickets.add(tick);
                }
            }

            List<Double> scores = new ArrayList<>();
            for (Ticket tick : typeTickets) {
                scores.add(tick.calculateRisk());
            }

            double totalScore = calculateAverage(scores);

            String level = "MINOR";
            if (totalScore > MAJOR_RISK_THRESHOLD) {
                level = "MAJOR";
            } else if (totalScore >= MODERATE_RISK_THRESHOLD) {
                level = "MODERATE";
            }

            riskMap.put(t.toString(), level);
        }
    }

    /**
     * Generates a resolution efficiency report for managers
     *
     * @param system The bug tracker system
     * @param user   The user generating the report (must be MANAGER)
     * @param result The result object to populate
     */
    public static void handleGenerateResolutionEfficiencyReport(final BugTrackerSystem system,
            final User user,
            final ObjectNode result) {
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers.");
        }

        ObjectNode report = result.putObject("report");
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket t : system.getAllTickets()) {
            if (t.getStatus() != main.model.enums.Status.OPEN) {
                tickets.add(t);
            }
        }

        report.put("totalTickets", tickets.size());

        ObjectNode byType = report.putObject("ticketsByType");
        Map<TicketType, Integer> typeCounts = new HashMap<>();
        for (TicketType t : TicketType.values()) {
            typeCounts.put(t, 0);
        }
        for (Ticket t : tickets) {
            typeCounts.put(t.getType(), typeCounts.get(t.getType()) + 1);
        }
        for (TicketType t : TicketType.values()) {
            byType.put(t.toString(), typeCounts.get(t));
        }

        ObjectNode byPriority = report.putObject("ticketsByPriority");
        Map<Priority, Integer> prioCounts = new HashMap<>();
        for (Priority p : Priority.values()) {
            prioCounts.put(p, 0);
        }
        for (Ticket t : tickets) {
            prioCounts.put(t.getPriority(), prioCounts.get(t.getPriority()) + 1);
        }
        for (Priority p : Priority.values()) {
            byPriority.put(p.toString(), prioCounts.get(p));
        }

        ObjectNode efficiencyNode = report.putObject("efficiencyByType");

        for (TicketType t : TicketType.values()) {
            List<Ticket> typeTickets = new ArrayList<>();
            for (Ticket tick : tickets) {
                if (tick.getType() == t) {
                    typeTickets.add(tick);
                }
            }
            List<Double> scores = new ArrayList<>();
            for (Ticket tick : typeTickets) {
                if (tick.getStatus() == Status.CLOSED
                        || tick.getStatus() == main.model.enums.Status.RESOLVED) {
                    java.time.LocalDate assignedAt = tick.getAssignedAt();
                    java.time.LocalDate solvedAt = tick.getSolvedAt();
                    if (assignedAt == null || solvedAt == null) {
                        continue;
                    }
                    long daysToResolve = java.time.temporal.ChronoUnit.DAYS
                            .between(assignedAt, solvedAt) + 1;
                    if (daysToResolve <= 0) {
                        daysToResolve = 1;
                    }

                    double eff = tick.calculateEfficiency(daysToResolve);
                    scores.add(eff);
                }
            }

            double finalEff = calculateAverage(scores);
            efficiencyNode.put(t.toString(), finalEff);
        }
    }

    /**
     * Generates a customer impact report for managers
     *
     * @param system The bug tracker system
     * @param user   The user generating the report (must be MANAGER)
     * @param result The result object to populate
     */
    public static void handleGenerateCustomerImpactReport(final BugTrackerSystem system,
            final User user,
            final ObjectNode result) {
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers.");
        }

        ObjectNode report = result.putObject("report");
        List<Ticket> allTickets = system.getAllTickets();
        List<Ticket> tickets = new ArrayList<>();

        for (Ticket t : allTickets) {
            if (t.getPriority() != Priority.LOW && t.getStatus() != Status.CLOSED) {
                tickets.add(t);
            }
        }

        report.put("totalTickets", tickets.size());

        ObjectNode byType = report.putObject("ticketsByType");
        Map<TicketType, Integer> typeCounts = new HashMap<>();
        for (TicketType t : TicketType.values()) {
            typeCounts.put(t, 0);
        }

        for (Ticket t : tickets) {
            typeCounts.put(t.getType(), typeCounts.get(t.getType()) + 1);
        }
        for (TicketType t : TicketType.values()) {
            byType.put(t.toString(), typeCounts.get(t));
        }

        ObjectNode byPriority = report.putObject("ticketsByPriority");
        Map<Priority, Integer> prioCounts = new HashMap<>();
        for (Priority p : Priority.values()) {
            prioCounts.put(p, 0);
        }

        for (Ticket t : tickets) {
            prioCounts.put(t.getPriority(), prioCounts.get(t.getPriority()) + 1);
        }
        for (Priority p : Priority.values()) {
            byPriority.put(p.toString(), prioCounts.get(p));
        }

        ObjectNode impactNode = report.putObject("customerImpactByType");

        Map<TicketType, Double> totalScores = new HashMap<>();
        Map<TicketType, Integer> counts = new HashMap<>();
        for (TicketType tt : TicketType.values()) {
            totalScores.put(tt, 0.0);
            counts.put(tt, 0);
        }

        for (Ticket t : tickets) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(t.getCreatedAt(),
                    system.getCurrentDate());
            double raw = t.calculateRawImpact(days);
            double normalizedScore = calculateImpactFinal(raw, t.getImpactMax());

            totalScores.put(t.getType(), totalScores.get(t.getType()) + normalizedScore);
            counts.put(t.getType(), counts.get(t.getType()) + 1);
        }

        double finalBugScore;
        if (counts.get(TicketType.BUG) > 0) {
            finalBugScore = truncate(totalScores.get(TicketType.BUG) / counts.get(TicketType.BUG));
        } else {
            finalBugScore = 0.0;
        }

        double finalFeatureScore;
        if (counts.get(TicketType.FEATURE_REQUEST) > 0) {
            finalFeatureScore = truncate(totalScores.get(TicketType.FEATURE_REQUEST)
                    / counts.get(TicketType.FEATURE_REQUEST));
        } else {
            finalFeatureScore = 0.0;
        }

        double finalUiScore;
        if (counts.get(TicketType.UI_FEEDBACK) > 0) {
            finalUiScore = truncate(totalScores.get(TicketType.UI_FEEDBACK)
                    / counts.get(TicketType.UI_FEEDBACK));
        } else {
            finalUiScore = 0.0;
        }

        impactNode.put("BUG", finalBugScore);
        impactNode.put("FEATURE_REQUEST", finalFeatureScore);
        impactNode.put("UI_FEEDBACK", finalUiScore);
    }

    /**
     * Calculates the final impact score normalized to a range
     *
     * @param baseScore The raw calculated score
     * @param maxValue  The maximum possible value for normalization
     * @return The normalized score
     */
    public static double calculateImpactFinal(final double baseScore, final double maxValue) {
        return Math.min(MAX_PERCENTAGE, (baseScore * MAX_PERCENTAGE) / maxValue);
    }

    /**
     * Calculates the average impact from a list of scores
     *
     * @param scores The list of scores
     * @return The average score
     */
    public static double calculateAverageImpact(final List<Double> scores) {
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double truncate(final double value) {
        return Math.round(value * FACTOR) / FACTOR;
    }

    private static double calculateAverage(final List<Double> scores) {
        if (scores.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Double s : scores) {
            sum += s;
        }
        return truncate(sum / scores.size());
    }

    /**
     * Generates an app stability report for managers
     *
     * @param system The bug tracker system
     * @param user   The user generating the report (must be MANAGER)
     * @param result The result object to populate
     */
    public static void handleAppStabilityReport(final BugTrackerSystem system, final User user,
            final ObjectNode result) {
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers.");
        }

        ObjectNode report = result.putObject("report");
        List<Ticket> activeTickets = new ArrayList<>();

        for (Ticket t : system.getAllTickets()) {
            if (t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS) {
                activeTickets.add(t);
            }
        }

        report.put("totalOpenTickets", activeTickets.size());

        ObjectNode byType = report.putObject("openTicketsByType");
        Map<TicketType, Integer> typeCounts = new HashMap<>();
        for (TicketType t : TicketType.values()) {
            typeCounts.put(t, 0);
        }
        for (Ticket t : activeTickets) {
            typeCounts.put(t.getType(), typeCounts.get(t.getType()) + 1);
        }
        for (TicketType t : TicketType.values()) {
            byType.put(t.toString(), typeCounts.get(t));
        }

        ObjectNode byPriority = report.putObject("openTicketsByPriority");
        Map<Priority, Integer> prioCounts = new HashMap<>();
        for (Priority p : Priority.values()) {
            prioCounts.put(p, 0);
        }
        for (Ticket t : activeTickets) {
            prioCounts.put(t.getPriority(), prioCounts.get(t.getPriority()) + 1);
        }
        for (Priority p : Priority.values()) {
            byPriority.put(p.toString(), prioCounts.get(p));
        }

        ObjectNode riskMap = report.putObject("riskByType");
        boolean hasSignificantRisk = false;
        boolean allRiskNegligible = true;

        for (TicketType t : TicketType.values()) {
            List<Ticket> typeTickets = new ArrayList<>();
            for (Ticket tick : activeTickets) {
                if (tick.getType() == t) {
                    typeTickets.add(tick);
                }
            }
            List<Double> scores = new ArrayList<>();
            for (Ticket tick : typeTickets) {
                scores.add(tick.calculateRisk());
            }
            double totalScore = calculateAverage(scores);
            String level = "NEGLIGIBLE";
            if (totalScore > MAJOR_RISK_THRESHOLD) {
                level = "SIGNIFICANT";
                hasSignificantRisk = true;
                allRiskNegligible = false;
            } else if (totalScore >= MODERATE_RISK_THRESHOLD) {
                level = "MODERATE";
                allRiskNegligible = false;
            }
            riskMap.put(t.toString(), level);
        }

        ObjectNode impactMap = report.putObject("impactByType");
        boolean allImpactLow = true;

        for (TicketType t : TicketType.values()) {
            List<Ticket> typeTickets = new ArrayList<>();
            for (Ticket tick : activeTickets) {
                if (tick.getType() == t) {
                    typeTickets.add(tick);
                }
            }

            double sumScore = 0;
            for (Ticket tick : typeTickets) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(tick.getCreatedAt(),
                        system.getCurrentDate());
                double raw = tick.calculateRawImpact(days);
                sumScore += (raw * MAX_PERCENTAGE) / tick.getStabilityMax();
            }

            double avgImpact;
            if (typeTickets.isEmpty()) {
                avgImpact = 0.0;
            } else {
                avgImpact = truncate(sumScore / typeTickets.size());
            }
            impactMap.put(t.toString(), avgImpact);
            if (avgImpact >= MAJOR_RISK_THRESHOLD) {
                allImpactLow = false;
            }
        }

        String stability;
        if (activeTickets.isEmpty()) {
            stability = "STABLE";
        } else if (allRiskNegligible && allImpactLow) {
            stability = "STABLE";
        } else if (hasSignificantRisk) {
            stability = "UNSTABLE";
        } else {
            stability = "PARTIALLY_STABLE";
        }

        report.put("appStability", stability);

        if ("STABLE".equals(stability)) {
            system.setActive(false);
        }
    }

}
