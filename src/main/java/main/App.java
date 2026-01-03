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

// entry point of the app, i read inputs and write outputs here
public final class App {
    private App() {
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER = new ObjectMapper().writer()
            .withDefaultPrettyPrinter();

    // main flow: reset system, load users, run commands and save results
    public static void run(final String inputPath, final String outputPath) {
        List<ObjectNode> outputs = new ArrayList<>();
        BugTrackerSystem system = BugTrackerSystem.getInstance();
        system.reset();
        system.setOutputs(outputs);

        ObjectMapper mapper = new ObjectMapper();

        try {
            File usersFile = new File(INPUT_USERS_FIELD);
            if (usersFile.exists()) {
                List<Map<String, Object>> usersData = mapper.readValue(usersFile,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> params : usersData) {
                    system.addUser(main.utils.UserFactory.createUser(params));
                }
            }

            File inputsFile = new File(inputPath);
            if (inputsFile.exists()) {
                List<Map<String, Object>> commandsData = mapper.readValue(inputsFile,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> commandData : commandsData) {
                    String command = (String) commandData.get("command");
                    String username = (String) commandData.get("username");
                    String timestamp = (String) commandData.get("timestamp");

                    main.command.CommandRunner.execute(command, username, timestamp, commandData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }
}
