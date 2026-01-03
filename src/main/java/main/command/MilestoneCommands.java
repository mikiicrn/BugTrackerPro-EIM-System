package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.Manager;
import main.model.user.User;
import main.model.enums.Role;
import main.model.enums.Status;
import main.system.BugTrackerSystem;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

// operations related to milestones
public class MilestoneCommands {

    // creating a milestone, i validate the manager and the unique name
    @SuppressWarnings("unchecked")
    public static void handleCreateMilestone(BugTrackerSystem system, User user, Map<String, Object> params,
            LocalDate date) {
        if (!system.getState().canCreateMilestone()) {
            throw new RuntimeException("Cannot create milestone in current state.");
        }
        if (!(user instanceof Manager)) {
            throw new RuntimeException(
                    "The user does not have permission to execute this command: required role MANAGER; user role "
                            + user.getRole() + ".");
        }

        String name = (String) params.get("name");
        String dueDateStr = (String) params.get("dueDate");
        LocalDate dueDate = LocalDate.parse(dueDateStr);

        List<Integer> ticketIds = (List<Integer>) params.get("tickets");
        List<String> assignedDevs = (List<String>) params.get("assignedDevs");
        List<String> blockingFor = (List<String>) params.get("blockingFor");

        Manager manager = (Manager) user;
        for (String devName : assignedDevs) {
            boolean isSub = manager.getSubordinates().stream().anyMatch(s -> s.equals(devName));
            if (!isSub) {
                throw new RuntimeException(
                        "Developer " + devName + " is not a subordinate of " + manager.getUsername());
            }
        }

        for (Integer tid : ticketIds) {
            Milestone existingM = CommandRunner.findMilestoneForTicket(system, tid);
            if (existingM != null) {
                throw new RuntimeException(
                        "Tickets " + tid + " already assigned to milestone " + existingM.getName() + ".");
            }
        }

        Milestone m = new Milestone(name, blockingFor, dueDate, ticketIds, assignedDevs, manager.getUsername(), date);
        system.addMilestone(m);

        for (Integer tid : ticketIds) {
            Ticket t = system.getTicket(tid);
            Map<String, String> data = new HashMap<>();
            data.put("milestone", name);
            data.put("username", manager.getUsername());
            CommandRunner.logHistory(t, "ADDED_TO_MILESTONE", date, data);
        }

        CommandRunner.notifyUsers(system, assignedDevs,
                "New milestone " + name + " has been created with due date " + dueDate + ".");
    }

    public static void handleViewMilestones(BugTrackerSystem system, User user, ObjectNode result) {
        List<Milestone> milestones = new ArrayList<>();
        if (user.getRole() == Role.MANAGER) {
            for (Milestone m : system.getMilestones().values()) {
                if (m.getCreator().equals(user.getUsername()))
                    milestones.add(m);
            }
        } else if (user.getRole() == Role.DEVELOPER) {
            for (Milestone m : system.getMilestones().values()) {
                if (m.getAssignedDevs().contains(user.getUsername()))
                    milestones.add(m);
            }
        }

        milestones.sort(Comparator.comparing(Milestone::getDueDate)
                .thenComparing(Milestone::getName));

        ArrayNode arr = result.putArray("milestones");
        LocalDate now = system.getCurrentDate();

        for (Milestone m : milestones) {
            ObjectNode n = arr.addObject();
            n.put("name", m.getName());

            ArrayNode tArr = n.putArray("tickets");
            ArrayNode openArr = n.putArray("openTickets");
            ArrayNode closedArr = n.putArray("closedTickets");

            int total = m.getTickets().size();
            int closed = 0;

            List<Integer> sortedIds = new ArrayList<>(m.getTickets());
            Collections.sort(sortedIds);

            for (int tid : sortedIds) {
                tArr.add(tid);
                Ticket t = system.getTicket(tid);
                if (t.getStatus() == Status.CLOSED) {
                    closedArr.add(tid);
                    closed++;
                } else {
                    openArr.add(tid);
                }
            }

            boolean allClosed = (total > 0 && closed == total);

            ArrayNode devArr = n.putArray("assignedDevs");
            List<String> devs = new ArrayList<>(m.getAssignedDevs());
            for (String d : devs)
                devArr.add(d);

            n.put("createdBy", m.getCreator());
            n.put("createdAt", m.getCreationDate().toString());
            n.put("dueDate", m.getDueDate().toString());

            ArrayNode blockingForArr = n.putArray("blockingFor");
            List<String> blocking = new ArrayList<>(m.getBlockingFor());
            Collections.sort(blocking);
            for (String b : blocking)
                blockingForArr.add(b);

            n.put("status", allClosed ? "COMPLETED" : "ACTIVE");
            n.put("isBlocked", CommandRunner.isMilestoneBlocked(system, m));

            double pct = total == 0 ? 100.0 : ((double) closed / total);
            BigDecimal bd = new BigDecimal(pct).setScale(2, RoundingMode.HALF_UP);
            n.put("completionPercentage", bd.doubleValue());

            LocalDate refDate = now;
            if (allClosed && total > 0) {
                LocalDate maxSolved = null;
                for (int tid : m.getTickets()) {
                    Ticket t = system.getTicket(tid);
                    if (t.getSolvedAt() != null) {
                        if (maxSolved == null || t.getSolvedAt().isAfter(maxSolved))
                            maxSolved = t.getSolvedAt();
                    }
                }
                if (maxSolved != null)
                    refDate = maxSolved;
            }

            long dDiff = ChronoUnit.DAYS.between(refDate, m.getDueDate()) + 1;
            if (dDiff < 0)
                dDiff = 0;
            n.put("daysUntilDue", dDiff);

            long overdue = 0;
            if (refDate.isAfter(m.getDueDate())) {
                overdue = ChronoUnit.DAYS.between(m.getDueDate(), refDate) + 1;
            }
            n.put("overdueBy", overdue);

            ArrayNode repArr = n.putArray("repartition");
            for (String dName : devs) {
                ObjectNode dNode = repArr.addObject();
                dNode.put("developer", dName);
                ArrayNode dTix = dNode.putArray("assignedTickets");

                List<Integer> devTickets = new ArrayList<>();
                for (int tid : sortedIds) {
                    Ticket t = system.getTicket(tid);
                    if (dName.equals(t.getAssignedTo())) {
                        devTickets.add(tid);
                    }
                }
                for (int dt : devTickets)
                    dTix.add(dt);
            }
        }
    }
}
