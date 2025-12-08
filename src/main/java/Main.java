import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static String[] validCommands = {"type", "exit", "echo", "pwd", "cat"};
    public static char[] SPECIAL_CHARS = {'"', '\\'};

    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage

        Scanner scanner = new Scanner(System.in);
        while(true) {

            System.out.print("$ ");
            String input = scanner.nextLine();

            if(input.equalsIgnoreCase("exit")) {
                break;
            }

            processInput(input);
        }
    }

    private static void processInput(String input) {
        String[] tokens = input.split(">|1>");
        if(tokens.length == 2) {
            processCommand(tokens[0].trim(), tokens[1].trim());
        } else {
            processCommand(input, null);
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

    private static void processCommand(String input, String filePath) {
        ArrayList<String> args = extractArgs(input);

        if(args.getFirst().equalsIgnoreCase("echo")) {
            printEcho(args, filePath);
        } else if (args.getFirst().equalsIgnoreCase("type")) {
            checkType(args.get(1).toLowerCase(), filePath);
        } else if (args.getFirst().equalsIgnoreCase("pwd")) {
            printWorkingDir(filePath);
        } else if (args.getFirst().equalsIgnoreCase("cd")) {
            changeDir(args.get(1), filePath);
        } else if (args.getFirst().equalsIgnoreCase("cat")) {
            openFileAndPrint(args, filePath);
        }
        else {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(args); // For Windows

                // Redirect standard output to the Java program's console
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // Read the output of the external program
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                if(filePath != null) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (IOException e) {
                        System.err.println("An error occurred while writing to the file: " + e.getMessage());
                    }
                } else {
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

            } catch (Exception e) {
                System.out.println(args.getFirst() + ": not found");
            }
        }
    }

    private static void openFileAndPrint(ArrayList<String> words, String filePath) {
        for(int i = 1; i < words.size(); i++) {
            File file = getAbsoluteFile(words.get(i));
            if(file != null && file.exists() && file.isFile()) {
                try {
                    // Read all content of the file into a String
                    String fileContent = Files.readString(file.getAbsoluteFile().toPath());

                    if(filePath != null) {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                            writer.write(fileContent);
                        } catch (IOException e) {
                            System.err.println("An error occurred while writing to the file: " + e.getMessage());
                        }
                    } else {
                        System.out.print(fileContent);
                    }

                } catch (IOException e) {
                    // Handle potential IOException (e.g., file not found, permission issues)
                    System.err.println("Error reading the file: " + e.getMessage());
                }
            } else {
                System.out.println("cd: " + words.get(i) + ": No such file or directory");
            }
        }
    }

    private static void printWorkingDir(String filePath) {
        String currentDirectory = System.getProperty("user.dir");
        if(filePath != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(currentDirectory);
            } catch (IOException e) {
                System.err.println("An error occurred while writing to the file: " + e.getMessage());
            }
        } else {
            System.out.println(currentDirectory);
        }
    }

    private static void checkType(String word, String filePath) {
        String output = null;
        if(word.equalsIgnoreCase("cat")) {
            output = "cat is /bin/cat";
        } else if( Arrays.asList(validCommands).contains(word)) {
            output = word + " is a shell builtin";
        } else {
            String path = getAbsolutePathIfValidExecutable(word);
            if (path != null) {
                output = word + " is " + path;
            } else {
                System.out.println(word + ": not found");
            }
        }
        if(filePath != null && output != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(output);
            } catch (IOException e) {
                System.err.println("An error occurred while writing to the file: " + e.getMessage());
            }
        } else {
            System.out.println(output);
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

    private static void printEcho(ArrayList<String> args, String filePath) {
        if(filePath != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(String.join(" ", args).substring(4).trim());
            } catch (IOException e) {
                System.err.println("An error occurred while writing to the file: " + e.getMessage());
            }
        } else {
            System.out.println(String.join(" ", args).substring(4).trim());
        }
    }

    private static void changeDir(String path, String filePath) {
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
