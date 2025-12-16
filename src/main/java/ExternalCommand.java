import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExternalCommand implements Command {
    private final String commandName;

    public ExternalCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        // 1. Resolve Path (Preserving your logic to check if it exists first)
        // If we don't find it manually, we can try letting ProcessBuilder fail,
        // but your requirements seem to want specific "not found" handling.
//        String executablePath = resolvePath(commandName);

//        if (executablePath == null) {
//            // Write to the ERROR stream provided by Main
//            // If the user did "2> err.txt", this automatically goes there!
//            PrintWriter errWriter = new PrintWriter(err);
//            errWriter.println(commandName + ": not found");
//            errWriter.flush();
//            return;
//        }

        try {
            // 2. Prepare the Process
            // Update the first arg to be the full path (e.g., "ls" -> "/bin/ls")
            String[] processArgs = args.clone();
//            processArgs[0] = executablePath;

            ProcessBuilder pb = new ProcessBuilder(processArgs);

            // Sync the directory state!
            pb.directory(ctx.getCurrentDirectory());

            // 3. Start the Process
            Process process = pb.start();

            // 4. THE PUMPING LOGIC (Multithreaded)
            // We use a small thread pool to handle the streams
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // Thread 1: Read Process STDOUT -> Write to Shell OUT
            executor.submit(() -> copyStream(process.getInputStream(), out));

            // Thread 2: Read Process STDERR -> Write to Shell ERR
            executor.submit(() -> copyStream(process.getErrorStream(), err));

            // 5. Wait for finish
            int exitCode = process.waitFor();

            // Shut down threads cleanly
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);

        } catch (Exception e) {
            // Last resort error handling
            PrintWriter errWriter = new PrintWriter(err);
            errWriter.println("Error executing " + commandName + ": " + e.getMessage());
            errWriter.flush();
        }
    }

    // --- Helper 1: The Stream Copier ---
    // This effectively replaces your "while(reader.readLine())" loop
    private void copyStream(InputStream input, OutputStream output) {
        try {
            // transferTo is very efficient (Java 9+)
            input.transferTo(output);
            output.flush();
        } catch (IOException e) {
            // Broken pipes are common in shells (e.g. user closes terminal), ignore.
        }
    }

    // --- Helper 2: Path Resolution (Refactored from your code) ---
    private String resolvePath(String command) {
        // If the user typed a specific path "./myscript.sh" or "/bin/ls"
        File directFile = new File(command);
        if (command.contains("/") && directFile.canExecute()) {
            return directFile.getAbsolutePath();
        }

        // Otherwise check PATH variables
        String pathVariable = System.getenv("PATH");
        if (pathVariable == null) return null;

        String pathSeparator = System.getProperty("path.separator");
        for (String folder : pathVariable.split(pathSeparator)) {
            File folderFile = new File(folder);
            File commandFile = new File(folderFile, command);
            if (commandFile.exists() && commandFile.canExecute()) {
                return commandFile.getAbsolutePath();
            }
        }
        return null;
    }
}