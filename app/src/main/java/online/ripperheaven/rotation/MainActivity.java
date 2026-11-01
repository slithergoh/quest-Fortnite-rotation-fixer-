package online.ripperheaven.rotation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String FORTNITE_PACKAGE = "com.epicgames.fortnite";
    private static final int BG = Color.rgb(13, 13, 16);
    private static final int PANEL = Color.rgb(27, 27, 33);
    private static final int RED = Color.rgb(210, 35, 54);
    private TextView status;
    private TextView modeStatus;
    private String selected = "Landscape";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildUi();
        refreshDetection();
    }

    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextSize(size); v.setTextColor(color);
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
        b.setText(label); b.setAllCaps(false);
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

        TextView title = text("RIPPER ROTATION", 25, Color.WHITE, true);
        page.addView(title);
        TextView sub = text("META QUEST 3  •  FORTNITE LAUNCHER", 12, 0xffaaaaaf, true);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(5); subLp.bottomMargin = dp(18);
        page.addView(sub, subLp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundTintList(null); card.setBackgroundColor(PANEL);
        page.addView(card);

        card.addView(text("FORTNITE DETECTION", 13, 0xffbbbbc2, true));
        status = text("Checking installed apps…", 17, Color.WHITE, true);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(-1, -2);
        st.topMargin = dp(8);
        card.addView(status, st);

        TextView heading = text("WINDOW ORIENTATION", 13, 0xffbbbbc2, true);
        LinearLayout.LayoutParams h = new LinearLayout.LayoutParams(-1, -2);
        h.topMargin = dp(22); h.bottomMargin = dp(8);
        page.addView(heading, h);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(button("Landscape", () -> choose("Landscape")), new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(button("Portrait", () -> choose("Portrait")), new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(row1);
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(button("Reverse landscape", () -> choose("Reverse landscape")), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(button("Reset", () -> choose("Landscape")), new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(row2);

        modeStatus = text("Selected: Landscape", 15, 0xffff6878, true);
        LinearLayout.LayoutParams ms = new LinearLayout.LayoutParams(-1, -2);
        ms.topMargin = dp(10); page.addView(modeStatus, ms);

        Button launch = button("LAUNCH FORTNITE", this::launchFortnite);
        launch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(RED));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(22); page.addView(launch, lp);

        TextView note = text(
            "IMPORTANT: Android apps can request orientation only for their own activity. " +
            "This app cannot force a separately running Fortnite activity to rotate on standard Quest permissions. " +
            "The orientation buttons currently control this launcher panel only; they do not change the headset's global orientation or Fortnite's window.",
            13, 0xffc4c4ca, false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = dp(18); page.addView(note, np);

        Button settings = button("OPEN APP SETTINGS", () -> {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + FORTNITE_PACKAGE));
            try { startActivity(i); } catch (Exception e) {
                Toast.makeText(this, "Fortnite settings are unavailable.", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout.LayoutParams setLp = new LinearLayout.LayoutParams(-1, -2);
        setLp.topMargin = dp(14); page.addView(settings, setLp);
    }

    private void choose(String mode) {
        selected = mode;
        modeStatus.setText("Selected: " + selected + "  •  launcher panel only");
        int requested = ActivityInfoOrientation(mode);
        // This intentionally affects only Ripper Rotation's own activity.
        setRequestedOrientation(requested);
    }

    private int ActivityInfoOrientation(String mode) {
        if ("Portrait".equals(mode)) return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if ("Reverse landscape".equals(mode)) return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    private boolean isInstalled() {
        try { getPackageManager().getPackageInfo(FORTNITE_PACKAGE, 0); return true; }
        catch (PackageManager.NameNotFoundException e) { return false; }
    }

    private void refreshDetection() {
        if (isInstalled()) {
            status.setText("INSTALLED  •  Epic Fortnite package detected");
            status.setTextColor(0xff72df9a);
        } else {
            status.setText("NOT DETECTED  •  Package not visible");
            status.setTextColor(0xffffbd69);
        }
    }

    private void launchFortnite() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(FORTNITE_PACKAGE);
            if (launch == null) {
                Toast.makeText(this, "Fortnite launch activity not found. Check the installed package name.", Toast.LENGTH_LONG).show();
                refreshDetection();
                return;
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (SecurityException e) {
            Toast.makeText(this, "Quest blocked the Fortnite launch request.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not launch Fortnite: " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onResume() { super.onResume(); if (status != null) refreshDetection(); }
}
