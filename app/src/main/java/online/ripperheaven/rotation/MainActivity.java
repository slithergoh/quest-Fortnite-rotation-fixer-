package online.ripperheaven.rotation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rikka.shizuku.Shizuku;
import rikka.shizuku.Shizuku.UserServiceArgs;

public class MainActivity extends Activity {
    private static final String FORTNITE_PACKAGE = "com.epicgames.fortnite";
    private static final int BG = Color.rgb(13, 13, 16);
    private static final int PANEL = Color.rgb(27, 27, 33);
    private static final int RED = Color.rgb(210, 35, 54);
    private static final int REQUEST_SHIZUKU = 4102;

    private TextView gameStatus;
    private TextView shizukuStatus;
    private TextView resultStatus;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private ICommandService commandService;
    private final UserServiceArgs serviceArgs = new UserServiceArgs(new ComponentName("online.ripperheaven.rotation", CommandService.class.getName()))
            .daemon(false).processNameSuffix("fortnitecmd").version(1);
    private final android.content.ServiceConnection serviceConnection = new android.content.ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, android.os.IBinder service) {
            commandService = ICommandService.Stub.asInterface(service);
            runOnUiThread(() -> {
                resultStatus.setText("Shell service connected. Fortnite-only controls are ready.");
                refreshStatus();
            });
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            commandService = null;
            runOnUiThread(() -> refreshStatus());
        }
    };

    private final Shizuku.OnRequestPermissionResultListener permissionListener =
            (requestCode, grantResult) -> runOnUiThread(() -> {
                if (requestCode == REQUEST_SHIZUKU) {
                    refreshStatus();
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        bindCommandService();
                        Toast.makeText(this, "Shell access granted. Connecting controls…", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Shizuku permission was not granted.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        Shizuku.addRequestPermissionResultListener(permissionListener);
        buildUi();
        refreshStatus();
    }

    @Override protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        try { Shizuku.unbindUserService(serviceArgs, serviceConnection, true); } catch (Exception ignored) { }
        worker.shutdownNow();
        super.onDestroy();
    }

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(22), dp(18), dp(22), dp(18));
        l.setBackgroundColor(BG);
        return l;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));
        b.setMinHeight(dp(54));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = column();
        scroll.addView(page);
        setContentView(scroll);

        page.addView(text("RIPPER ROTATION", 25, Color.WHITE, true));
        TextView sub = text("QUEST 3  •  FORTNITE PER-APP RESIZE", 12, 0xffaaaaaf, true);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(5);
        subLp.bottomMargin = dp(18);
        page.addView(sub, subLp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(PANEL);
        page.addView(card);

        card.addView(text("FORTNITE", 13, 0xffbbbbc2, true));
        gameStatus = text("Checking installation…", 16, Color.WHITE, true);
        LinearLayout.LayoutParams gs = new LinearLayout.LayoutParams(-1, -2);
        gs.topMargin = dp(7);
        card.addView(gameStatus, gs);

        TextView shTitle = text("SHIZUKU SHELL ACCESS", 13, 0xffbbbbc2, true);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(-1, -2);
        st.topMargin = dp(20);
        page.addView(shTitle, st);
        shizukuStatus = text("Checking Shizuku…", 15, 0xffffbd69, true);
        LinearLayout.LayoutParams ss = new LinearLayout.LayoutParams(-1, -2);
        ss.topMargin = dp(7);
        page.addView(shizukuStatus, ss);

        Button permission = button("CONNECT / GRANT SHIZUKU ACCESS", this::connectShizuku);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
        pp.topMargin = dp(12);
        page.addView(permission, pp);

        TextView note = text(
                "This applies Android's FORCE_RESIZE_APP compatibility override to Fortnite's package only. " +
                "It does not change the Quest's global resolution. It may allow Fortnite to resize, but does not guarantee a 4:3 iPad layout.",
                13, 0xffc4c4ca, false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = dp(16);
        page.addView(note, np);

        Button apply = button("ENABLE FORTNITE RESIZE MODE", () -> runFortniteOverride(true));
        apply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(RED));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2);
        ap.topMargin = dp(18);
        page.addView(apply, ap);

        Button restore = button("RESTORE FORTNITE DEFAULT", () -> runFortniteOverride(false));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.topMargin = dp(8);
        page.addView(restore, rp);

        Button launch = button("LAUNCH FORTNITE", this::launchFortnite);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        page.addView(launch, lp);

        resultStatus = text("Ready. Connect Shizuku to apply the per-app setting.", 13, 0xffc4c4ca, false);
        LinearLayout.LayoutParams rs = new LinearLayout.LayoutParams(-1, -2);
        rs.topMargin = dp(14);
        page.addView(resultStatus, rs);

        Button shizukuHelp = button("OPEN SHIZUKU DOWNLOAD", () ->
                openUrl("https://shizuku.rikka.app/download/"));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(16);
        page.addView(shizukuHelp, hp);

        Button settings = button("OPEN FORTNITE APP SETTINGS", () -> {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + FORTNITE_PACKAGE));
            try { startActivity(i); }
            catch (Exception e) { Toast.makeText(this, "Fortnite settings are unavailable.", Toast.LENGTH_SHORT).show(); }
        });
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(8);
        page.addView(settings, fp);
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "Could not open link.", Toast.LENGTH_SHORT).show(); }
    }

    private boolean isFortniteInstalled() {
        try {
            getPackageManager().getPackageInfo(FORTNITE_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void refreshStatus() {
        if (gameStatus != null) {
            boolean installed = isFortniteInstalled();
            gameStatus.setText(installed ? "INSTALLED  •  " + FORTNITE_PACKAGE : "NOT FOUND  •  Check Fortnite package ID");
            gameStatus.setTextColor(installed ? 0xff72df9a : 0xffffbd69);
        }
        if (shizukuStatus != null) {
            if (!Shizuku.pingBinder()) {
                shizukuStatus.setText("NOT RUNNING  •  Start Shizuku service first");
                shizukuStatus.setTextColor(0xffffbd69);
            } else if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                shizukuStatus.setText("RUNNING  •  Permission required");
                shizukuStatus.setTextColor(0xffffbd69);
            } else if (commandService == null) {
                shizukuStatus.setText("PERMISSION GRANTED  •  Connecting command service…");
                shizukuStatus.setTextColor(0xffffbd69);
            } else {
                shizukuStatus.setText("CONNECTED  •  Shell permission granted");
                shizukuStatus.setTextColor(0xff72df9a);
            }
        }
    }

    private void connectShizuku() {
        if (!Shizuku.pingBinder()) {
            resultStatus.setText("Shizuku is not running. Install it, start its service using Wireless debugging or ADB, then return here.");
            openUrl("https://shizuku.rikka.app/guide/setup/");
            return;
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            bindCommandService();
            refreshStatus();
            resultStatus.setText("Shizuku permission is already granted. Connecting…");
            return;
        }
        Shizuku.requestPermission(REQUEST_SHIZUKU);
    }

    private void bindCommandService() {
        try {
            Shizuku.bindUserService(serviceArgs, serviceConnection);
            resultStatus.setText("Connecting the shell command service…");
        } catch (Exception e) {
            resultStatus.setText("Could not connect Shizuku service: " + e.getMessage());
        }
    }

    private void runFortniteOverride(boolean enable) {
        if (!isFortniteInstalled()) {
            resultStatus.setText("Fortnite package was not found: " + FORTNITE_PACKAGE);
            return;
        }
        if (!Shizuku.pingBinder()) {
            resultStatus.setText("Start Shizuku first, then connect it here.");
            return;
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            connectShizuku();
            resultStatus.setText("Grant Shizuku permission, then tap the resize button again.");
            return;
        }
        if (commandService == null) {
            bindCommandService();
            resultStatus.setText("Connecting the Shizuku command service. Tap again when connected.");
            return;
        }

        resultStatus.setText(enable ? "Applying Fortnite-only resize override…" : "Restoring Fortnite default…");
        worker.execute(() -> {
            String command = "am compat " + (enable ? "enable" : "disable")
                    + " FORCE_RESIZE_APP " + FORTNITE_PACKAGE;
            try {
                String output = commandService.execute(command);
                runOnUiThread(() -> {
                    resultStatus.setText(output);
                    refreshStatus();
                });
            } catch (Exception e) {
                runOnUiThread(() -> resultStatus.setText("Command failed: " + e.getMessage()));
            }
        });
    }

    private void launchFortnite() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(FORTNITE_PACKAGE);
            if (launch == null) {
                Toast.makeText(this, "Fortnite launch activity not found. Check the installed package name.", Toast.LENGTH_LONG).show();
                refreshStatus();
                return;
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "Could not launch Fortnite: " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }
}
