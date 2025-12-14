import java.io.OutputStream;
import java.io.PrintWriter;

public class PwdCommand implements Command {
    @Override
    public String getName() { return "pwd"; }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        // Use PrintWriter for easy text writing
        PrintWriter writer = new PrintWriter(out);
        writer.println(ctx.getCurrentDirectory().getAbsolutePath());
        writer.flush(); // IMPORTANT: Always flush the stream!
    }
}
