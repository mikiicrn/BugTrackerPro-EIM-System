# BugTrackerPro - Enterprise Issue Management System

## 1. Abstract
BugTrackerPro is a robust, terminal-based issue tracking system designed to
simulate complex lifecycle management within a software development team. It
moves beyond simple operations by implementing dynamic policy enforcement,
temporal simulation, and automated workflow transitions via a decoupled
Object-Oriented architecture.

## 2. Implementation Logic & Architectural Decisions

The system was architected with a focus on **extensibility** and **data
integrity**, adhering strictly to SOLID principles to prevent "spaghetti code".

### I. Design Patterns & Behavioral Logic
* **Singleton Pattern (`BugTrackerSystem`):** Acts as the central "Single Source
    of Truth." It manages global consistency for the temporal simulation,
    ensuring that time-dependent events (like deadline checks) are synchronized
    across all modules.

* **State Pattern (Applied Twice):**
    1.  **System Level:** Controls the macro-phases (`Assessment` vs. `Dev`).
        It enforces high-level rules, such as blocking new ticket creation
        once the development phase begins.
    2.  **Ticket Lifecycle:** Replaces fragile `switch-case` logic with
        polymorphic behavior. Each ticket state (`Open`, `In_Progress`,
        `Validated`) knows its own valid transitions, preventing illegal flows
        (eg: jumping from `Open` straight to `Validated`)

* **Strategy Pattern (The Validator Engine):** Used for `SeniorityPolicy` and
    `ExpertisePolicy`. This allows the system to validate `canHandle(ticket)`
    dynamically. By encapsulating rules in strategy classes, the system respects
    the **Open/Closed Principle**—we can add new validation rules without
    modifying the core User or Ticket classes.

### II. Data Structures & Algorithmic Rationale
Efficiency and scalability were key drivers in data structure selection:

* **HashMap<String, User> (Lookup Optimization):** Selected to guarantee **O(1)
    average time complexity** for user authentication and assignment validation.
    Since these operations occur most frequently, hashing provides a significant
    performance advantage over linear search.

* **ArrayList<Ticket> (Iteration Efficiency):** Chosen for ticket storage. While
    random ID access is O(N), the primary usage pattern involves sequential
    iteration for batch reporting and daily deadline monitoring. In this
    context, the **CPU cache locality** benefits of contiguous memory arrays
    outweigh the overhead of linked structures.

### III. Algorithms & Automated Logic
* **Metric Normalization (Unified Analytics):** Implemented a normalization
    algorithm to project divergent metrics (Efficiency, Risk) onto a uniform
    **0-100 scale**.
    *Formula:* `(RawValue * 100) / MaxScale`.
    This ensures comparable analytics across different ticket types,
    facilitating a unified dashboard for the generated reports.

* **Proactive Priority Escalation (Recursive Check):** A temporal monitoring
    algorithm scans milestones daily.
    *Logic:* If `CurrentDate + 1 == DueDate` and status is not terminal, the
    system triggers a recursive escalation, bumping tickets to `CRITICAL`.
    Crucially, it triggers a **Re-validation Routine** to automatically
    de-assign staff who no longer meet the seniority requirements for the
    new priority level.

## 3. Key Functionalities
The application exposes a CLI interface supporting the following core flows:

* **Lifecycle Control:** `createTicket` -> `assignTicket` -> `closeTicket`.
* **Phase Management:** `setSystemState` (Locks/Unlocks creation features).
* **Analytics Engine:** `generatedevreport`, `generatestabilityreport`.
* **User Administration:** `createUser`, `removeUser`.

## 4. Assumptions & Constraints
* The system assumes a single-threaded environment for the simulation loop.
* Usernames are unique identifiers (enforced by the HashMap keys).
* Input data is expected to be well-formed as per the assignment spec.

---
**Signed by:** Mihaela Ciuranu, 323CA