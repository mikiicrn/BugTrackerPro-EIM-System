package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.system.BugTrackerSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * main.App represents the main application logic that processes input commands,
 * generates outputs, and writes them to a file
 */
public class App {
    private App() {
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath  path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */
    public static void run(final String inputPath, final String outputPath) {
        // feel free to change this if needed
        // however keep 'outputs' variable name to be used for writing
        List<ObjectNode> outputs = new ArrayList<>();
        BugTrackerSystem system = BugTrackerSystem.getInstance();
        system.reset();
        system.setOutputs(outputs);

        ObjectMapper mapper = new ObjectMapper();

        try {
            // Load Users
            File usersFile = new File(INPUT_USERS_FIELD);
            if (usersFile.exists()) {
                List<Map<String, Object>> usersData = mapper.readValue(usersFile,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> params : usersData) {
                    system.addUser(main.utils.UserFactory.createUser(params));
                }
            }

            // Load Commands
            File inputsFile = new File(inputPath);
            if (inputsFile.exists()) {
                List<Map<String, Object>> commandsData = mapper.readValue(inputsFile,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> commandData : commandsData) {
                    String command = (String) commandData.get("command");
                    String username = (String) commandData.get("username");
                    String timestamp = (String) commandData.get("timestamp");
                    Map<String, Object> params = null;
                    if (commandData.containsKey("params")) {
                        params = (Map<String, Object>) commandData.get("params");
                    }

                    // Helper for params vs explicit args?
                    // Some commands have top-level args e.g. createMilestone has name/dueDate/lists
                    // at top level, not in "params"?
                    // Let's check in_02_test_milestone.json
                    // "command": "createMilestone", "name": "...", "dueDate": "..."
                    // Ah, structure varies!
                    // reportTicket has "params": {...}
                    // createMilestone has flat structure?

                    // I should pass the WHOLE commandData map as params if "params" is missing,
                    // or merge them?
                    // Let's check `in_01`: `reportTicket` has `params`.
                    // Let's check `in_02`.

                    Map<String, Object> effectiveParams = params;
                    if (effectiveParams == null) {
                        effectiveParams = commandData;
                    }
                    // Ideally pass commandData entirely to specialized handler or the
                    // "effectiveParams"
                    // Since `CommandRunner` expects specific keys.
                    // The `reportTicket` params are NESTED.
                    // The `createMilestone` params are FLAT (except "tickets", "assignedDevs").

                    // Use `effectiveParams` logic: if "params" exists, use it. If not, use
                    // `commandData`.
                    // BUT: `reportTicket` uses `params` for `Ticket` fields.
                    // `createMilestone` uses top level.

                    // In `CommandRunner` I should handle this.
                    // Pass `commandData` AND `params` (if any). Or just `commandData` and let
                    // Runner extract?
                    // Simpler: pass `effectiveParams` calculated here.
                    // If `params` exists, pass that. If not, pass `commandData`.
                    // WAIT. `createMilestone` needs `name`, `dueDate` which are top level.
                    // `reportTicket` needs `title`, `type` which are in `params`.
                    // So passing `params` only works for `reportTicket`.
                    // passing `commandData` works for `createMilestone`.

                    // I will pass `commandData` as `args` to `execute`. `CommandRunner` will
                    // extract what it needs.
                    // Wait, `reportTicket` specifically separates user/timestamp from ticket data.
                    // If I pass `commandData` to `TicketFactory`, it works IF `TicketFactory` keys
                    // match.
                    // `TicketFactory` expects `type`, `title`.
                    // `in_01`: `type` is inside `params`.
                    // So `CommandRunner` needs logic:
                    // if command == reportTicket, use "params" child.
                    // else use root.

                    main.command.CommandRunner.execute(command, username, timestamp, commandData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // DO NOT CHANGE THIS SECTION IN ANY WAY
        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }
}
