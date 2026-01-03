package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.Developer;
import main.model.user.Manager;
import main.model.user.User;
import main.model.enums.Role;
import main.model.enums.Status;
import main.system.BugTrackerSystem;
import java.time.LocalDate;
import java.util.*;

// searching for tickets or devs with filters
public class SearchCommands {

    // executing the search, figuring out the type if missing
    @SuppressWarnings("unchecked")
    public static void handleSearch(BugTrackerSystem system, User user, Map<String, Object> params,
            ObjectNode result) {
        Map<String, Object> p = params;
        if (params.containsKey("params") && params.get("params") instanceof Map) {
            p = (Map<String, Object>) params.get("params");
        } else if (params.containsKey("filters") && params.get("filters") instanceof Map) {
            p = (Map<String, Object>) params.get("filters");
        }

        String searchType = (String) p.get("searchType");
        if (searchType == null) {
            // guessing the search type based on the keys provided
            boolean hasDevKeys = p.containsKey("seniority") || p.containsKey("performanceScoreAbove")
                    || p.containsKey("performanceScoreBelow");
            searchType = hasDevKeys ? "DEVELOPER" : "TICKET";
        }

        result.put("searchType", searchType);
        ArrayNode resultsArr = result.putArray("results");

        if ("DEVELOPER".equals(searchType)) {
            if (user.getRole() != Role.MANAGER) {
                return;
            }

            Manager mgr = (Manager) user;
            List<Developer> devCandidates = new ArrayList<>();
            for (String sub : mgr.getSubordinates()) {
                User u = system.getUser(sub);
                if (u instanceof Developer)
                    devCandidates.add((Developer) u);
            }

            Iterator<Developer> it = devCandidates.iterator();
            while (it.hasNext()) {
                Developer d = it.next();
                if (p.containsKey("seniority") && !d.getSeniority().toString().equals(p.get("seniority"))) {
                    it.remove();
                    continue;
                }
                if (p.containsKey("expertiseArea") && !d.getExpertiseArea().toString().equals(p.get("expertiseArea"))) {
                    it.remove();
                    continue;
                }
                if (p.containsKey("performanceScoreAbove")) {
                    double score = 0.0;
                    if (p.get("performanceScoreAbove") instanceof Number)
                        score = ((Number) p.get("performanceScoreAbove")).doubleValue();
                    if (d.getPerformanceScore() <= score) {
                        it.remove();
                        continue;
                    }
                }
                if (p.containsKey("performanceScoreBelow")) {
                    double score = 0.0;
                    if (p.get("performanceScoreBelow") instanceof Number)
                        score = ((Number) p.get("performanceScoreBelow")).doubleValue();
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
                n.put("hireDate", d.getHireDate());
            }

        } else {
            List<Ticket> candidates = new ArrayList<>();
            if (user.getRole() == Role.MANAGER) {
                candidates.addAll(system.getAllTickets());
            } else {
                for (Milestone m : system.getMilestones().values()) {
                    if (m.getAssignedDevs().contains(user.getUsername())) {
                        for (int tid : m.getTickets()) {
                            Ticket t = system.getTicket(tid);
                            if (t != null && t.getStatus() == Status.OPEN) {
                                if (!candidates.contains(t))
                                    candidates.add(t);
                            }
                        }
                    }
                }
            }

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

                if (p.containsKey("availableForAssignment") && Boolean.TRUE.equals(p.get("availableForAssignment"))) {
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

            candidates.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));

            for (Ticket t : candidates) {
                ObjectNode n = resultsArr.addObject();
                n.put("id", t.getId());
                n.put("type", t.getType().toString());
                n.put("title", t.getTitle());
                n.put("businessPriority", t.getPriority().toString());
                n.put("status", t.getStatus().toString());
                n.put("createdAt", t.getCreatedAt().toString());
                n.put("solvedAt", t.getSolvedAt() != null ? t.getSolvedAt().toString() : "");
                n.put("reportedBy", t.getReportedBy());

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
                    ArrayNode mw = n.putArray("matchingWords");
                    for (String m : matches)
                        mw.add(m);
                }
            }
        }
    }
}
