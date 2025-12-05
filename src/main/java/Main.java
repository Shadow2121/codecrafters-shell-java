import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static String[] validCommands = {"type", "exit", "echo", "pwd", "cat"};

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
        ArrayList<String> args = new ArrayList<>();
        boolean isOpen = false;
        String curr = "";

        for (char ch : input.toCharArray()) {
            if (ch == '\'') {
                isOpen = !isOpen;
            } else if (ch == ' ' && !isOpen) {
                if (!curr.isEmpty()) args.add(curr);
                curr = "";
            } else {
                curr += ch;
            }
        }

        if (!curr.isEmpty()) {
            args.add(curr);
        }

        if(words[0].equalsIgnoreCase("echo")) {
            printEcho(args);
        } else if (words[0].equalsIgnoreCase("type")) {
            checkType(words[1].toLowerCase());
        } else if (words[0].equalsIgnoreCase("pwd")) {
            printWorkingDir();
        } else if (words[0].equalsIgnoreCase("cd")) {
            changeDir(words[1]);
        } else if (words[0].equalsIgnoreCase("cat")) {
            openFileAndPrint(args);
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

    private static void openFileAndPrint(ArrayList<String> words) {
        for(int i = 1; i < words.size(); i++) {
            File file = getAbsoluteFile(words.get(i));
            if(file != null && file.exists() && file.isFile()) {
                try {
                    // Read all content of the file into a String
                    String fileContent = Files.readString(file.getAbsoluteFile().toPath());

//                    String singleLineContent = fileContent.replaceAll("[\\n\\r]", "");

                    // Print the content to the console
                    System.out.print(fileContent);

                } catch (IOException e) {
                    // Handle potential IOException (e.g., file not found, permission issues)
                    System.err.println("Error reading the file: " + e.getMessage());
                }
            } else {
                System.out.println("cd: " + words.get(i) + ": No such file or directory");
            }
//            System.out.print(" ");
        }
        System.out.println();
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
            if (path != null) {
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

//    private static void printEcho(String[] words) {
//        if (words.length == 1) {
//            System.out.println();
//            return;
//        }
//
//        System.out.println(String.join(" ", words).substring(words[0].length()).trim());
//    }

    private static void printEcho(ArrayList<String> args) {
        System.out.println(String.join(" ", args).substring(4).trim());

//        boolean isSingleQuote = false;
//        ArrayList<String> words = new ArrayList<>();
//        StringBuilder  word = new StringBuilder();
//        for(char c: line.toCharArray()) {
//            if(isSingleQuote) {
//                if(c == '\'') {
//                    isSingleQuote = false;
//                    words.add(word.toString().trim());
//                    word.setLength(0);
//                } else word.append(c);
//                continue;
//            }
//            if(c == '\'') isSingleQuote = true;
//            else {
//                if (c == ' ') {
//                    if(word.isEmpty()) continue;
//                    words.add(word.toString().trim());
//                    word.setLength(0);
//                } else {
//                    word.append(c);
//                }
//            }
//        }
//        if(!word.isEmpty()) {
//            words.add(word.toString().trim());
//            word.setLength(0);
//        }
//        System.out.println(String.join("", words));
    }

    private static void changeDir(String path) {
        if(path.equals("~")) {
            System.setProperty("user.dir", System.getenv("HOME"));
        } else {
            File folder = getAbsoluteFile(path);
            if(folder != null && folder.isDirectory()) {
                System.setProperty("user.dir", folder.getAbsolutePath());
            } else {
                System.out.println("cd: " + path + ": No such file or directory");
            }
        }
    }

    private static File getAbsoluteFile(String path) {
        if(path.startsWith("/")) {
            File folder = new File(path);
            if(folder.exists()) {
                return folder;
            } else {
                return null;
            }
        } else {
            File folder = new File(System.getProperty("user.dir"));
            while(folder != null && path.startsWith("../")) {
                folder = folder.getParentFile();
                path = path.replaceFirst("../", "");
            }
            if(folder  == null ) {
                System.out.println("we are fucked!!");
            }
            if(path.startsWith("./")) path = path.replaceFirst("./", "");
            assert folder != null;
            String folderPath = folder.getAbsolutePath();
            if(!path.isEmpty()) folderPath += "/" + path;
            File newDir = new File(folderPath);
            if(newDir.exists()) {
                return newDir;
            } else {
                return null;
            }
        }
    }
}
