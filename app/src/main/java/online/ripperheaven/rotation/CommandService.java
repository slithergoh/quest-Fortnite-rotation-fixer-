package online.ripperheaven.rotation;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Keep
public class CommandService extends ICommandService.Stub {
    public CommandService() { }

    @Keep
    public CommandService(Context context) { }

    @Override
    public String execute(String command) {
        // Only permit the two reversible compatibility commands for Fortnite.
        String enable = "am compat enable FORCE_RESIZE_APP com.epicgames.fortnite";
        String disable = "am compat disable FORCE_RESIZE_APP com.epicgames.fortnite";
        if (!enable.equals(command) && !disable.equals(command)) {
            throw new SecurityException("Command not allowed");
        }

        Process process = null;
        try {
            process = new ProcessBuilder("sh", "-c", command).redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() < 4000) output.append(line).append("\n");
            }
            int code = process.waitFor();
            if (code == 0) {
                return (output.length() == 0 ? "Android accepted the command." : output.toString().trim())
                        + "\nRestart Fortnite to test the resize change. This does not force 4:3.";
            }
            return "Android rejected the command (exit " + code + "): " + output.toString().trim();
        } catch (Exception e) {
            Log.e("RipperRotation", "Fortnite compatibility command failed", e);
            return "Command failed: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        } finally {
            if (process != null) process.destroy();
        }
    }

    public void destroy() {
        System.exit(0);
    }
}
