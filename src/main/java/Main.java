import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

            if(input.equalsIgnoreCase("exit 0")) {
                break;
            }

            processCommand(input);
        }
    }

    private static void processCommand(String input) {
        String[] words = input.split("\\s+");
        if(words[0].equalsIgnoreCase("echo")) {
            printEcho(words[1]);
        } else if (words[0].equalsIgnoreCase("type")) {
            checkType(words[1].toLowerCase());
        }
        else {
            System.out.println(input + ": command not found");
        }
    }

    private static void checkType(String word) {
        if( Arrays.asList(validCommands).contains(word)) {
            System.out.println(word + " is a shell builtin");
        } else {
            String pathVariable = System.getenv("PATH");

            if (pathVariable != null) {
                // Get the platform-specific path separator
                String pathSeparator = System.getProperty("path.separator");

                // Split the PATH string into individual folder paths
                String[] pathFolders = pathVariable.split(pathSeparator);

                for (String folder : pathFolders) {
                    File folderFile = new File(folder);
                    File commandFile = new File(folderFile, word);

                    if(commandFile.exists() && commandFile.canExecute()) {
                        System.out.println(word + " is " + folder);
                        return;
                    }
                }
            } else {
                System.out.println("PATH environment variable not found.");
            }
            System.out.println(word + ": not found");
        }
    }

    private static void printEcho(String word) {
        System.out.println(word);
    }
}
