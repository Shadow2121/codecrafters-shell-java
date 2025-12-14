import java.io.OutputStream;
import java.io.PrintWriter;

public class ExitCommand implements Command {
    @Override
    public String getName() { return "exit"; }

    @Override
    public void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) {
        ctx.requestExit();
    }
}
