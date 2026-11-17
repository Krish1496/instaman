package com.focusguard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView st;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences p = getSharedPreferences("p", 0);
        int bg = Color.rgb(15, 15, 18);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(56, 120, 56, 80);
        l.setBackgroundColor(bg);

        TextView t = new TextView(this);
        t.setText("Focus Guard");
        t.setTextSize(30);
        t.setTextColor(Color.WHITE);
        l.addView(t);

        st = new TextView(this);
        st.setTextSize(15);
        st.setPadding(0, 16, 0, 40);
        l.addView(st);

        add(l, p, "reels", "Block Instagram Reels");
        add(l, p, "explore", "Block Instagram Explore");
        add(l, p, "feed", "Block Instagram Home feed (posts)");
        add(l, p, "shorts", "Block YouTube Shorts");

        Button bt = new Button(this);
        bt.setText("Open Accessibility Settings");
        bt.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 64;
        l.addView(bt, lp);

        TextView n = new TextView(this);
        n.setText("Turn on Focus Guard in Accessibility (Installed/Downloaded apps).\n\nIf it is greyed out: Settings → Apps → Focus Guard → ⋮ → Allow restricted settings.");
        n.setTextColor(Color.GRAY);
        n.setTextSize(13);
        n.setPadding(0, 40, 0, 0);
        l.addView(n);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(bg);
        sv.addView(l);
        setContentView(sv);
    }

    @Override protected void onResume() {
        super.onResume();
        String en = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean on = en != null && en.contains(getPackageName() + "/");
        st.setText(on ? "● Active" : "○ Not active");
        st.setTextColor(on ? Color.rgb(80, 220, 120) : Color.rgb(255, 160, 60));
    }

    void add(LinearLayout l, SharedPreferences p, String k, String label) {
        Switch s = new Switch(this);
        s.setText(label);
        s.setTextColor(Color.WHITE);
        s.setTextSize(17);
        s.setPadding(0, 28, 0, 28);
        s.setChecked(p.getBoolean(k, true));
        s.setOnCheckedChangeListener((v, c) -> p.edit().putBoolean(k, c).apply());
        l.addView(s);
    }
}
