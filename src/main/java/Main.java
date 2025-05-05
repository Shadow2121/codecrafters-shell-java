import java.util.Scanner;

public class Main {
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
        } else {
            System.out.println(input + " command not found");
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
