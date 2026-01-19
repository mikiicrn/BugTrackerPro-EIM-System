package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import main.model.Milestone;
import main.model.enums.Role;
import main.model.enums.Status;
import main.model.ticket.Ticket;
import main.model.user.Developer;
import main.model.user.Manager;
import main.model.user.User;
import main.system.BugTrackerSystem;

/**
 * Searching for tickets or devs with filters
 */
public final class SearchCommands {

    private SearchCommands() {
    }

    /**
     * Executing the search, figuring out the type if missing
     *
     * @param system The bug tracker system
     * @param user   The user performing the search
     * @param params The search parameters
     * @param result The result node
     */
    @SuppressWarnings("unchecked")
    public static void handleSearch(final BugTrackerSystem system, final User user,
            final Map<String, Object> params, final ObjectNode result) {
        Map<String, Object> p = params;
        if (params.containsKey("params") && params.get("params") != null) {
            try {
                p = (Map<String, Object>) params.get("params");
            } catch (ClassCastException e) {
                // ignore
            }
        } else if (params.containsKey("filters") && params.get("filters") != null) {
            try {
                p = (Map<String, Object>) params.get("filters");
            } catch (ClassCastException e) {
                // ignore
            }
        }

        String searchType = (String) p.get("searchType");
        if (searchType == null) {
            boolean hasDevKeys = p.containsKey("seniority")
                    || p.containsKey("performanceScoreAbove")
                    || p.containsKey("performanceScoreBelow");
            searchType = hasDevKeys ? "DEVELOPER" : "TICKET";
        }

        result.put("searchType", searchType);
        ArrayNode resultsArr = result.putArray("results");

        if ("DEVELOPER".equals(searchType)) {
            handleDeveloperSearch(system, user, p, resultsArr);
        } else {
            handleTicketSearch(system, user, p, resultsArr);
        }
    }

    private static void handleDeveloperSearch(final BugTrackerSystem system, final User user,
            final Map<String, Object> p, final ArrayNode resultsArr) {
        if (user.getRole() != Role.MANAGER) {
            return;
        }

        Manager mgr = (Manager) user;
        List<Developer> devCandidates = new ArrayList<>();
        for (String sub : mgr.getSubordinates()) {
            User u = system.getUser(sub);
            if (u.getRole() == Role.DEVELOPER) {
                devCandidates.add((Developer) u);
            }
        }

        for (Iterator<Developer> it = devCandidates.iterator(); it.hasNext();) {
            Developer d = it.next();
            if (p.containsKey("seniority")
                    && !d.getSeniority().toString().equals(p.get("seniority"))) {
                it.remove();
                continue;
            }
            if (p.containsKey("expertiseArea")
                    && !d.getExpertiseArea().toString().equals(p.get("expertiseArea"))) {
                it.remove();
                continue;
            }
            if (p.containsKey("performanceScoreAbove")) {
                double score = 0.0;
                if (p.get("performanceScoreAbove") != null) {
                    try {
                        score = ((Number) p.get("performanceScoreAbove")).doubleValue();
                    } catch (ClassCastException e) {
                        // ignore
                    }
                }
                if (d.getPerformanceScore() < score) {
                    it.remove();
                    continue;
                }
            }
            if (p.containsKey("performanceScoreBelow")) {
                double score = 0.0;
                if (p.get("performanceScoreBelow") != null) {
                    try {
                        score = ((Number) p.get("performanceScoreBelow")).doubleValue();
                    } catch (ClassCastException e) {
                        // ignore
                    }
                }
                if (d.getPerformanceScore() >= score) {
                    it.remove();
                    continue;
                }
            }
        }

        devCandidates.sort(Comparator.comparing(User::getUsername));

        for (Developer d : devCandidates) {
            ObjectNode n = resultsArr.addObject();
            n.put("username", d.getUsername());
            n.put("expertiseArea", d.getExpertiseArea().toString());
            n.put("seniority", d.getSeniority().toString());
            n.put("performanceScore", d.getPerformanceScore());
            // Safe conversion to string if available, though original code used raw object
            // which might rely on Jackson mapping. Using toString for safety with standard
            // ObjectNode.
            n.put("hireDate",
                    d.getHireDate() != null ? d.getHireDate().toString() : "");
        }
    }

    private static void handleTicketSearch(final BugTrackerSystem system, final User user,
            final Map<String, Object> p, final ArrayNode resultsArr) {
        List<Ticket> candidates = new ArrayList<>();
        if (user.getRole() == Role.MANAGER) {
            candidates.addAll(system.getAllTickets());
        } else {
            for (Milestone m : system.getMilestones().values()) {
                if (m.getAssignedDevs().contains(user.getUsername())) {
                    for (int tid : m.getTickets()) {
                        Ticket t = system.getTicket(tid);
                        if (t != null && t.getStatus() == Status.OPEN) {
                            if (!candidates.contains(t)) {
                                candidates.add(t);
                            }
                        }
                    }
                }
            }
        }

        filterTickets(system, user, p, candidates);

        candidates.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));

        for (Ticket t : candidates) {
            addTicketResult(user, p, resultsArr, t);
        }
    }

    @SuppressWarnings("unchecked")
    private static void filterTickets(final BugTrackerSystem system, final User user,
            final Map<String, Object> p, final List<Ticket> candidates) {
        Iterator<Ticket> it = candidates.iterator();
        while (it.hasNext()) {
            Ticket t = it.next();
            if (p.containsKey("businessPriority")
                    && !t.getPriority().toString().equals(p.get("businessPriority"))) {
                it.remove();
                continue;
            }
            if (p.containsKey("type") && !t.getType().toString().equals(p.get("type"))) {
                it.remove();
                continue;
            }
            if (p.containsKey("createdBefore")) {
                LocalDate d = LocalDate.parse((String) p.get("createdBefore"));
                if (!t.getCreatedAt().isBefore(d)) {
                    it.remove();
                    continue;
                }
            }
            if (p.containsKey("createdAfter")) {
                LocalDate d = LocalDate.parse((String) p.get("createdAfter"));
                if (!t.getCreatedAt().isAfter(d)) {
                    it.remove();
                    continue;
                }
            }

            if (p.containsKey("availableForAssignment")
                    && Boolean.TRUE.equals(p.get("availableForAssignment"))) {
                if (t.getAssignedTo() != null && !t.getAssignedTo().isEmpty()) {
                    it.remove();
                    continue;
                }
                Milestone m = CommandRunner.findMilestoneForTicket(system, t.getId());
                if (m != null && CommandRunner.isMilestoneBlocked(system, m)) {
                    it.remove();
                    continue;
                }

                if (user.getRole() == Role.DEVELOPER) {
                    Developer dev = (Developer) user;
                    if (!dev.canHandle(t)) {
                        it.remove();
                        continue;
                    }
                }
            }

            if (p.containsKey("keywords")) {
                List<String> kws = (List<String>) p.get("keywords");
                boolean matchFound = false;
                String title = t.getTitle().toLowerCase();
                String desc = t.getDescription() != null ? t.getDescription().toLowerCase() : "";

                for (String kw : kws) {
                    String k = kw.toLowerCase();
                    if (title.contains(k) || desc.contains(k)) {
                        matchFound = true;
                    }
                }
                if (!matchFound) {
                    it.remove();
                    continue;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void addTicketResult(final User user, final Map<String, Object> p,
            final ArrayNode resultsArr, final Ticket t) {
        ObjectNode n = resultsArr.addObject();
        n.put("id", t.getId());
        n.put("type", t.getType().toString());
        n.put("title", t.getTitle());
        n.put("businessPriority", t.getPriority().toString());
        n.put("status", t.getStatus().toString());
        n.put("createdAt", t.getCreatedAt().toString());
        n.put("solvedAt", t.getSolvedAt() != null ? t.getSolvedAt().toString() : "");
        n.put("reportedBy", t.getReportedBy());

        if (user.getRole() == Role.MANAGER || p.containsKey("keywords")) {
            ArrayNode mw = n.putArray("matchingWords");
            if (p.containsKey("keywords")) {
                List<String> kws = (List<String>) p.get("keywords");
                List<String> matches = new ArrayList<>();
                String title = t.getTitle().toLowerCase();
                String desc = t.getDescription() != null ? t.getDescription().toLowerCase() : "";
                for (String kw : kws) {
                    String k = kw.toLowerCase();
                    if (title.contains(k) || desc.contains(k)) {
                        matches.add(kw);
                    }
                }
                Collections.sort(matches);
                for (String m : matches) {
                    mw.add(m);
                }
            }
        }
    }
}
