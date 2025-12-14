import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class CdCommand implements Command {
    @Override
    public String getName() { return "cd"; }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        if (args.length < 2) return; // 'cd' with no args usually goes home, but we'll skip for now

        String path = args[1];
        File newDir;

        // 1. Handle Home Directory (~)
        if (path.equals("~")) {
            newDir = new File(System.getProperty("user.home"));
        }
        // 2. Handle Absolute Path (Starts with /)
        else if (path.startsWith("/")) {
            newDir = new File(path);
        }
        // 3. Handle Relative Path (., .., or folder name)
        else {
            File current = ctx.getCurrentDirectory();

            // Split path by "/" to handle cases like "../../folder"
            String[] parts = path.split("/");
            for (String part : parts) {
                if (part.equals("..")) {
                    current = current.getParentFile();
                    // Prevent going above root
                    if (current == null) current = new File("/");
                } else if (part.equals(".")) {
                    // Do nothing, stay in current
                } else {
                    current = new File(current, part);
                }
            }
            newDir = current;
        }

        // 4. Validate and Set
        if (newDir.exists() && newDir.isDirectory()) {
            ctx.setCurrentDirectory(newDir);
        } else {
            PrintWriter writer = new PrintWriter(out); // Use 'out' or 'err' depending on spec
            // Per your original code: "cd: path: No such file or directory"
            writer.println("cd: " + path + ": No such file or directory");
            writer.flush();
        }
    }
}