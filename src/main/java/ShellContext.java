import java.io.File;

public class ShellContext {
    private File currentDirectory;
    private boolean shouldExit = false;

    public ShellContext() {
        this.currentDirectory = new File(System.getProperty("user.dir"));
    }

    public File getCurrentDirectory() { return currentDirectory; }

    public void setCurrentDirectory(File dir) {
        if(dir.exists() && dir.isDirectory()) {
            this.currentDirectory = dir.getAbsoluteFile();
        }
    }

    public void requestExit() { this.shouldExit = true; }
    public boolean shouldExit() { return shouldExit; }
}