import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;

public class CatCommand implements Command {
    @Override
    public String getName() { return "cat"; }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        PrintWriter writer = new PrintWriter(out);

        // Loop through all files provided (e.g., "cat file1.txt file2.txt")
        for (int i = 1; i < args.length; i++) {
            String fileName = args[i];
            File file = resolveFile(ctx.getCurrentDirectory(), fileName);

            if (file.exists() && file.isFile()) {
                // Flush the writer before copying raw bytes to ensure order
                writer.flush();
                try {
                    // Optimized copy: File -> OutputStream
                    Files.copy(file.toPath(), out);
                    out.flush();
                } catch (IOException e) {
                    writer.println("cat: " + fileName + ": Read error");
                }
            } else {
                writer.println("cat: " + fileName + ": No such file or directory");
            }
        }
        writer.flush();
    }

    // Helper to handle relative paths for files
    private File resolveFile(File currentDir, String fileName) {
        if (fileName.startsWith("/")) return new File(fileName);
        return new File(currentDir, fileName);
    }
}