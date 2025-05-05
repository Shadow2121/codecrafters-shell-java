import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage

        Scanner scanner = new Scanner(System.in);
        while(true) {

            System.out.print("$ ");
            String input = scanner.nextLine();

            if(input.equals("exit 0")) {
                break;
            }

            String[] words = input.split(" ");
            if(words[0].equalsIgnoreCase("echo")) {
                for(int i = 1; i < words.length; i++) {
                    System.out.print(words[i] + " ");
                }
            } else {

                System.out.println(input + ": command not found");
            }


        }

    }
}
