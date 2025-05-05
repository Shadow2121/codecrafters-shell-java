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
            printEcho(words);
        } else if (words[0].equalsIgnoreCase("type")) {
            checkType(words[1].toLowerCase());
        }
        else {
            System.out.println(input + " command not found");
        }
    }

    private static void checkType(String word) {
        if( Arrays.asList(validCommands).contains(word)) {
            System.out.println(word + " is a shell builtin");
        } else {
            System.out.println(word + ": not found");
        }
    }

    private static void printEcho(String[] words) {
        if (words.length == 1) {
            System.out.println();
            return;
        }

        System.out.println(String.join(" ", words).substring(words[0].length()).trim());
    }
}
