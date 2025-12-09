import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        String[] tokens = input.split(">", 2);
        //tokens[0] = tokens[0].substring(0, tokens[0].length() - 1);
        boolean append = false;
        if(tokens.length == 2 && tokens[0].endsWith("2")) {
            if(tokens[1].startsWith(">")) {
                append = true;
                tokens[1] = tokens[1].substring(1);
            }
            if(tokens[0].endsWith("2")) {
                tokens[0] = tokens[0].substring(0, tokens[0].length()-1).trim();
            }
            String fileName = tokens[1].trim();
            Path filePath = Paths.get(fileName);
            try {
                if(!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }

                processCommand(tokens[0].trim(), fileName, 2, append);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(tokens.length == 2) {
            if(tokens[1].startsWith(">")) {
                append = true;
                tokens[1] = tokens[1].substring(1);
            }
            if(tokens[0].endsWith("1")) {
                tokens[0] = tokens[0].substring(0, tokens[0].length()-1).trim();
            }
            String fileName = tokens[1].trim();
            Path filePath = Paths.get(fileName);
            try {
                if(!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }
                processCommand(tokens[0].trim(), fileName, 1, append);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            processCommand(input, null, 0, append);
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

    private static void processCommand(String input, String filePath, int redirectNumber, boolean append) {
        ArrayList<String> args = extractArgs(input);

        if(args.getFirst().equalsIgnoreCase("echo")) {
            printEcho(args, filePath, redirectNumber, append);
        } else if (args.getFirst().equalsIgnoreCase("type")) {
            checkType(args.get(1).toLowerCase(), filePath, redirectNumber, append);
        } else if (args.getFirst().equalsIgnoreCase("pwd")) {
            printWorkingDir(filePath, redirectNumber, append);
        } else if (args.getFirst().equalsIgnoreCase("cd")) {
            changeDir(args.get(1), filePath, redirectNumber, append);
        } else if (args.getFirst().equalsIgnoreCase("cat")) {
            openFileAndPrint(args, filePath, redirectNumber, append);
        }
        else {
            executeAnyCommand(args, filePath, redirectNumber, append);
        }
    }

    private static void executeAnyCommand(ArrayList<String> args, String fileName, int redirectNumber, boolean append) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args); // For Windows

            // Redirect standard output to the Java program's console
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output of the external program
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            if(fileName != null && redirectNumber == 1) {
                Path filePath = Paths.get(fileName);
                try (BufferedWriter writer = Files.newBufferedWriter(
                        filePath,
                        StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                        (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING // Ensures we append, not overwrite
                )) {
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    System.err.println("An error occurred while writing to the file: " + e.getMessage());
                }
            } else {
                while ((line = reader.readLine()) != null) {
                    if(line.endsWith("No such file or directory") && fileName != null && redirectNumber == 2) {
                        Path filePath = Paths.get(fileName);
                        try (BufferedWriter writer = Files.newBufferedWriter(
                                filePath,
                                StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                                (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
                        )) {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            System.err.println("An error occurred while writing to the file: " + e.getMessage());
                        }
                    } else {
                        System.out.println(line);
                    }
                }
            }
        } catch (Exception _) {
            if(fileName != null && redirectNumber == 2) {
                Path filePath = Paths.get(fileName);
                try (BufferedWriter writer = Files.newBufferedWriter(
                        filePath,
                        StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                        (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING // Ensures we append, not overwrite
                )) {
                    writer.write(args.getFirst() + ": not found");
                    writer.newLine();
                } catch (IOException e) {
                    System.err.println("An error occurred while writing to the file: " + e.getMessage());
                }
            } else {
                System.out.println(args.getFirst() + ": not found");
            }
        }
    }

    private static void openFileAndPrint(ArrayList<String> words, String fileName, int redirectNumber, boolean append) {
        for(int i = 1; i < words.size(); i++) {
            File file = getAbsoluteFile(words.get(i));
            if(file != null && file.exists() && file.isFile()) {
                try {
                    // Read all content of the file into a String
                    String fileContent = Files.readString(file.getAbsoluteFile().toPath());

                    if(fileName != null && redirectNumber == 1) {
                        Path filePath = Paths.get(fileName);
                        try (BufferedWriter writer = Files.newBufferedWriter(
                                filePath,
                                StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                                (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
                        )) {
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
                if(fileName != null && redirectNumber == 2) {
                    Path filePath = Paths.get(fileName);
                    try (BufferedWriter writer = Files.newBufferedWriter(
                            filePath,
                            StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                            (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING // Ensures we append, not overwrite
                    )) {
                        writer.write("cat: " + words.get(i) + ": No such file or directory");
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("An error occurred while writing to the file: " + e.getMessage());
                    }
                } else {
                    System.out.println("cat: " + words.get(i) + ": No such file or directory");
                }
            }
        }
    }

    private static void printWorkingDir(String fileName, int redirectNumber, boolean append) {
        String currentDirectory = System.getProperty("user.dir");
        if(fileName != null && redirectNumber == 1) {
            Path filePath = Paths.get(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                    (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
            )) {
                writer.write(currentDirectory);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("An error occurred while writing to the file: " + e.getMessage());
            }
        } else {
            System.out.println(currentDirectory);
        }
    }

    private static void checkType(String word, String fileName, int redirectNumber, boolean append) {
        String output = null;
        String error = null;
        if(word.equalsIgnoreCase("cat")) {
            output = "cat is /bin/cat";
        } else if( Arrays.asList(validCommands).contains(word)) {
            output = word + " is a shell builtin";
        } else {
            String path = getAbsolutePathIfValidExecutable(word);
            if (path != null) {
                output = word + " is " + path;
            } else {
                error = word + ": not found";
            }
        }
        if(fileName != null && output != null && redirectNumber == 1) {
            Path filePath = Paths.get(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                    (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
            )) {
                writer.write(output);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("An error occurred while writing to the file: " + e.getMessage());
            }
        } else if(output != null) {
            System.out.println(output);
        }
        if(error != null ) {
            if(fileName != null && redirectNumber == 2) {
                Path filePath = Paths.get(fileName);
                try (BufferedWriter writer = Files.newBufferedWriter(
                        filePath,
                        StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                        (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
                )) {
                    writer.write(error);
                    writer.newLine();
                } catch (IOException e) {
                    System.err.println("An error occurred while writing to the file: " + e.getMessage());
                }
            } else {
                System.out.println(error);
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

    private static void printEcho(ArrayList<String> args, String fileName, int redirectNumber, boolean append) {
        if(fileName != null && redirectNumber == 1) {
            Path filePath = Paths.get(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                    (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
            )) {
                writer.write(String.join(" ", args).substring(4).trim());
                writer.newLine();
            } catch (IOException e) {
                System.err.println("An error occurred while writing to the file: " + e.getMessage());
            }
        } else {
            System.out.println(String.join(" ", args).substring(4).trim());
        }
    }

    private static void changeDir(String path, String fileName, int redirectNumber, boolean append) {
        if(path.equals("~")) {
            System.setProperty("user.dir", System.getenv("HOME"));
        } else {
            File folder = getAbsoluteFile(path);
            if(folder != null && folder.isDirectory()) {
                System.setProperty("user.dir", folder.getAbsolutePath());
            } else {
                if(fileName != null && redirectNumber == 2) {
                    Path filePath = Paths.get(fileName);
                    try (BufferedWriter writer = Files.newBufferedWriter(
                            filePath,
                            StandardOpenOption.CREATE, // Creates the file if it doesn't exist
                            (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING  // Ensures we append, not overwrite
                    )) {
                        writer.write("cd: " + path + ": No such file or directory");
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("An error occurred while writing to the file: " + e.getMessage());
                    }
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
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
