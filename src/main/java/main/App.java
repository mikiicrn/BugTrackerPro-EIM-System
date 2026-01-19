package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;
import main.system.BugTrackerSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Application Entry Point and Orchestrator
 *
 * This class serves as the bridge between the external world (JSON files) and
 * the internal domain logic. It is responsible for the full lifecycle of a
 * simulation run:
 * application initialization, data ingestion, command execution, and result
 * serialization
 *
 * Architecture Note:
 * This design separates I/O concerns from business logic. The App class handles
 * how data
 * gets in and out (using the Jackson library), while the BugTrackerSystem and
 * CommandRunner handle what to do with that data
 */
public final class App {
    private App() {
        // Prevent instantiation of utility class
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER = new ObjectMapper().writer()
            .withDefaultPrettyPrinter();

    /**
     * Executes the main simulation flow
     *
     * Workflow:
     * 1. Reset: Ensures a clean state for the Singleton BugTrackerSystem
     * 2. Ingest: Loads the initial database of users from users.json
     * 3. Process: Iterates through the input commands, delegating execution
     * to the CommandRunner. This implements the Command Pattern, where each
     * action is encapsulated and executed polymorphically
     * 4. Serialize: Writes the accumulated JSON output to the specified path
     *
     * @param inputPath  Path to the input JSON file containing commands
     * @param outputPath Path where the result JSON file should be written
     */
    public static void run(final String inputPath, final String outputPath) {
        List<ObjectNode> outputs = new ArrayList<>();
        BugTrackerSystem system = BugTrackerSystem.getInstance();
        system.reset();
        system.setOutputs(outputs);

        ObjectMapper mapper = new ObjectMapper();

        try {
            // Phase 1: Data Store Initialization
            File usersFile = new File(INPUT_USERS_FIELD);
            if (usersFile.exists()) {
                List<Map<String, Object>> usersData = mapper.readValue(usersFile,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> params : usersData) {
                    system.addUser(main.utils.UserFactory.createUser(params));
                }
            }

            // Phase 2: Command Processing Loop
            File inputsFile = new File(inputPath);
            if (inputsFile.exists()) {
                List<Map<String, Object>> commandsData = mapper.readValue(inputsFile,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> commandData : commandsData) {
                    String command = (String) commandData.get("command");
                    String username = (String) commandData.get("username");
                    String timestamp = (String) commandData.get("timestamp");

                    // Delegate to the Command Dispatcher
                    main.command.CommandRunner.execute(command, username, timestamp, commandData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Phase 3: Result Persistence
        try {
            File outputFile = new File(outputPath);
            // Ensure directory structure exists
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }
}
