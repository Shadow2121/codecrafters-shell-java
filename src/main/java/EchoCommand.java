import java.io.OutputStream;
import java.io.PrintWriter;

public class EchoCommand implements Command {
    @Override
    public String getName() { return "echo"; }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        // Use PrintWriter for easy text writing
        PrintWriter writer = new PrintWriter(out);

        // args[0] is "echo", so start at 1
        for (int i = 1; i < args.length; i++) {
            writer.print(args[i] + (i < args.length - 1 ? " " : ""));
        }
        writer.println();
        writer.flush(); // IMPORTANT: Always flush the stream!
    }
}