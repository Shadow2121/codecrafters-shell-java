import java.io.OutputStream;

public interface Command {
    // We pass the ShellContext (state) and the OutputStreams (where to write)
    void execute(String[] args, ShellContext ctx, OutputStream out, OutputStream err) throws Exception;

    String getName();
}