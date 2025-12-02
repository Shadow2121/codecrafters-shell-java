import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static String[] validCommands = {"type", "exit", "echo"};
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage

        Scanner scanner = new Scanner(System.in);
        while(true) {

            System.out.print("$ ");
            String input = scanner.nextLine();

            if(input.equalsIgnoreCase("exit")) {
                break;
            }

            processCommand(input);
        }
    }

    private static void processCommand(String input) {
        String[] words = input.split("\\s+");
        if(words[0].equalsIgnoreCase("echo")) {
            printEcho(words);
        } else if (words[0].equalsIgnoreCase("type")) {
            checkType(words[1].toLowerCase());
        } else if (words[0].equalsIgnoreCase("pwd")) {
            printWorkingDir();
        }
        else {
            // System.out.println(input + ": command not found");
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(words); // For Windows

                // Redirect standard output to the Java program's console
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // Read the output of the external program
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                System.out.println(words[0] + ": not found");
            }
        }
    }

    private static void printWorkingDir() {
        String currentDirectory = System.getProperty("user.dir");
        System.out.println(currentDirectory);
    }

    private static void checkType(String word) {
        if( Arrays.asList(validCommands).contains(word)) {
            System.out.println(word + " is a shell builtin");
        } else {
            String path = getAbsolutePathIfValidExecutable(word);
            if(path != null) {
                System.out.println(word + " is " + path);
            } else {
                System.out.println(word + ": not found");
            }
        }
    }

    private static String getAbsolutePathIfValidExecutable(String command) {
        String pathVariable = System.getenv("PATH");

        if (pathVariable != null) {
            // Get the platform-specific path separator
            String pathSeparator = System.getProperty("path.separator");

            // Split the PATH string into individual folder paths
            String[] pathFolders = pathVariable.split(pathSeparator);

            for (String folder : pathFolders) {
                File folderFile = new File(folder);
                File commandFile = new File(folderFile, command);

                if(commandFile.exists() && commandFile.canExecute()) {
                    return commandFile.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static void printEcho(String[] words) {
        if (words.length == 1) {
            System.out.println();
            return;
        }

        System.out.println(String.join(" ", words).substring(words[0].length()).trim());
    }
}
