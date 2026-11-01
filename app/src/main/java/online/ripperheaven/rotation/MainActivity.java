package online.ripperheaven.rotation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String FORTNITE_PACKAGE = "com.epicgames.fortnite";
    private static final int BG = Color.rgb(13, 13, 16);
    private static final int PANEL = Color.rgb(27, 27, 33);
    private static final int RED = Color.rgb(210, 35, 54);
    private TextView gameStatus;
    private TextView resultStatus;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildUi();
        refreshStatus();
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
        TextView sub = text("QUEST 3  •  SAFE FORTNITE LAUNCHER", 12, 0xffaaaaaf, true);
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

        TextView explanation = text(
                "NO SHIZUKU  •  NO ROOT  •  NO ADB  •  NO SYSTEM SETTINGS CHANGED\n\n" +
                "This version launches Fortnite and opens its Android app settings. " +
                "Quest does not provide ordinary apps with permission to rotate or force a custom 4:3 aspect ratio on another app. " +
                "Changing this launcher's orientation would not rotate Fortnite, so this app does not pretend that it does.",
                14, 0xffd0d0d5, false);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, -2);
        ep.topMargin = dp(18);
        page.addView(explanation, ep);

        Button launch = button("LAUNCH FORTNITE", this::launchFortnite);
        launch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(RED));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(20);
        page.addView(launch, lp);

        Button settings = button("OPEN FORTNITE APP SETTINGS", this::openFortniteSettings);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(8);
        page.addView(settings, sp);

        resultStatus = text("Ready. This app makes no privileged or global device changes.", 13, 0xffc4c4ca, false);
        LinearLayout.LayoutParams rs = new LinearLayout.LayoutParams(-1, -2);
        rs.topMargin = dp(14);
        page.addView(resultStatus, rs);
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
        if (gameStatus == null) return;
        boolean installed = isFortniteInstalled();
        gameStatus.setText(installed ? "INSTALLED  •  " + FORTNITE_PACKAGE : "NOT FOUND  •  Fortnite package not detected");
        gameStatus.setTextColor(installed ? 0xff72df9a : 0xffffbd69);
    }

    private void launchFortnite() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(FORTNITE_PACKAGE);
            if (launch == null) {
                resultStatus.setText("Fortnite launch activity not found. Confirm the game is installed from the supported source.");
                refreshStatus();
                return;
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception e) {
            resultStatus.setText("Could not launch Fortnite: " + e.getClass().getSimpleName());
        }
    }

    private void openFortniteSettings() {
        try {
            Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + FORTNITE_PACKAGE));
            startActivity(i);
        } catch (Exception e) {
            resultStatus.setText("Fortnite's Android app settings are unavailable on this Horizon OS version.");
        }
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }
}
