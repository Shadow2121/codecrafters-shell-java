import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.File;

public class TypeCommand implements Command {
    private final CommandRegistry registry;

    // Dependency Injection: We ask for the Registry in the constructor
    public TypeCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() { return "type"; }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        PrintWriter writer = new PrintWriter(out);

        // args[0] is "type", args[1] is the target (e.g. "echo" or "ls")
        if (args.length < 2) return;

        String target = args[1];

        // 1. Check if it is a Builtin (using the injected Registry)
        if (registry.isBuiltin(target)) {
            writer.println(target + " is a shell builtin");
        }
        // 2. Check if it is an External Command (using System PATH)
        else {
            String path = getPath(target);
            if (path != null) {
                writer.println(target + " is " + path);
            } else {
                writer.println(target + ": not found");
            }
        }
        writer.flush();
    }

    // Reuse your path logic here (or move it to a shared Utility class)
    private String getPath(String command) {
        String pathVariable = System.getenv("PATH");
        if (pathVariable == null) return null;
        for (String folder : pathVariable.split(System.getProperty("path.separator"))) {
            File file = new File(new File(folder), command);
            if (file.exists() && file.canExecute()) return file.getAbsolutePath();
        }
        return null;
    }
}