
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Main {
    public static char[] SPECIAL_CHARS = {'"', '\\'};

    // 1. Initialize the Registry (The Factory)
    private static final CommandRegistry registry = new CommandRegistry();
    // 2. Initialize the State (The Context)
    private static final ShellContext context = new ShellContext();

    public static void main(String[] args) throws Exception {
        // Create a simple completer with fixed options
        Completer completer = new StringsCompleter("help", "exit", "list", "version");

        // Create a line reader with the completer
        Terminal terminal = TerminalBuilder.builder().build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();

//        Scanner scanner = new Scanner(System.in);

        while (!context.shouldExit()) {
            String line = reader.readLine("$ ");

            // Handle Ctrl+D (EOF) gracefully
//            if (!scanner.hasNextLine()) break;


            if (line.trim().isEmpty()) continue;

            handleInput(line);
        }
    }

    private static void handleInput(String input) {
        // --- 1. HANDLE REDIRECTION PARSING HERE (ONCE) ---
        String commandPart = input;
        String fileName = null;
        boolean append = false;
        boolean redirectError = false;

        // Simple parsing logic (adapting your existing logic)
        if (input.contains(">>")) {
            String[] parts = input.split(">>", 2);
            commandPart = parts[0].trim();
            fileName = parts[1].trim();
            append = true;
            if(parts[0].endsWith("1")) { commandPart = commandPart.substring(0, commandPart.length()-1).trim(); }
            if(parts[0].endsWith("2")) { redirectError = true; commandPart = commandPart.substring(0, commandPart.length()-1).trim(); }
        } else if (input.contains(">")) {
            String[] parts = input.split(">", 2);
            commandPart = parts[0].trim();
            fileName = parts[1].trim();
            if(parts[0].endsWith("1")) { commandPart = commandPart.substring(0, commandPart.length()-1).trim(); }
            if(parts[0].endsWith("2")) { redirectError = true; commandPart = commandPart.substring(0, commandPart.length()-1).trim(); }
        }

        // --- 2. SETUP STREAMS ---
        OutputStream outStream = System.out;
        OutputStream errStream = System.err;
        OutputStream fileStream = null;

        try {
            if (fileName != null) {
                Path path = Paths.get(fileName);
                // Open the file with correct options
                fileStream = Files.newOutputStream(path,
                        StandardOpenOption.CREATE,
                        append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING);

                if (redirectError) errStream = fileStream;
                else outStream = fileStream;
            }

            // --- 3. PARSE ARGUMENTS ---
            // Keep your extractArgs logic, just move it to a helper method
            ArrayList<String> argList = extractArgs(commandPart);
            String[] cmdArgs = argList.toArray(new String[0]);

            if (cmdArgs.length == 0) return;

            // --- 4. EXECUTE ---
            Command cmd = registry.getCommand(cmdArgs[0]);
            if (cmd != null) {
                cmd.execute(cmdArgs, context, outStream, errStream);
            } else {
                // Handle external commands (ProcessBuilder)
                new ExternalCommand(cmdArgs[0]).execute(cmdArgs, context, outStream, errStream);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            // Only close the file stream, NEVER close System.out
            if (fileStream != null) {
                try { fileStream.close(); } catch (IOException e) {}
            }
        }
    }


    private static ArrayList<String> extractArgs(String input) {
        ArrayList<String> args = new ArrayList<>();
        boolean isSingleQuotes = false;
        boolean isDoubleQuotes = false;
        boolean isBackSlash = false;
        StringBuilder curr = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if(ch == '\\' && !isSingleQuotes && !isBackSlash) {
                isBackSlash = true;
            } else if(isBackSlash) {
                if(isDoubleQuotes && Arrays.binarySearch(SPECIAL_CHARS, ch) < 0) {
                    curr.append("\\");
                }
                isBackSlash = false;
                curr.append(ch);
            }else if(ch == '"' && !isSingleQuotes) {
                isDoubleQuotes = !isDoubleQuotes;
            } else if (ch == '\'' && !isDoubleQuotes) {
                isSingleQuotes = !isSingleQuotes;
            } else if (ch == ' ' && !isSingleQuotes && !isDoubleQuotes) {
                if (!curr.isEmpty()) args.add(curr.toString());
                curr = new StringBuilder();
            } else {
                curr.append(ch);
            }
        }

        if (!curr.isEmpty()) {
            args.add(curr.toString());
        }

        return args;
    }


}
