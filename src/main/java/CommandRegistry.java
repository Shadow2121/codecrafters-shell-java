import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        register(new EchoCommand());
        register(new ExitCommand()); // Assuming you have this from before
        register(new PwdCommand());
        register(new CdCommand());
//        register(new CatCommand());
        register(new TypeCommand(this)); // Pass registry to Type
    }

    private void register(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    /**
     * FACTORY METHOD
     * If the command is known (e.g., "echo"), return the Java Object.
     * If unknown (e.g., "git"), return an ExternalCommand wrapper.
     */
    public Command getCommand(String name) {
        if (commands.containsKey(name)) {
            return commands.get(name);
        }
        // Fallback: Assume it is an OS executable
        return new ExternalCommand(name);
    }

    /**
     * Helper for the 'type' command to check if a name is registered.
     */
    public boolean isBuiltin(String name) {
        return commands.containsKey(name);
    }
}