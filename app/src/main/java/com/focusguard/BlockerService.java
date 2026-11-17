package com.focusguard;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class BlockerService extends AccessibilityService {
    static final String IG = "com.instagram.android", YT = "com.google.android.youtube";
    SharedPreferences p;
    final Handler h = new Handler(Looper.getMainLooper());
    boolean pending;
    long hold;

    @Override public void onServiceConnected() { p = getSharedPreferences("p", 0); }
    @Override public void onInterrupt() {}

    @Override public void onAccessibilityEvent(AccessibilityEvent e) {
        if (pending || p == null) return;
        pending = true;
        h.postDelayed(() -> { pending = false; check(); }, 250);
    }

    void check() {
        if (System.currentTimeMillis() < hold) return;
        AccessibilityNodeInfo r = getRootInActiveWindow();
        if (r == null || r.getPackageName() == null) return;
        String s = r.getPackageName().toString();
        if (s.equals(IG)) ig(r); else if (s.equals(YT)) yt(r);
    }

    void ig(AccessibilityNodeInfo r) {
        boolean feed = p.getBoolean("feed", true);
        if (p.getBoolean("reels", true)
                && (has(r, IG, "clips_viewer_view_pager") || sel(r, IG, "clips_tab", "Reels"))) { go(r, feed); return; }
        if (p.getBoolean("explore", true) && sel(r, IG, "search_tab", "Search")) { go(r, feed); return; }
        if (feed && sel(r, IG, "feed_tab", "Home")) go(r, true);
    }

    void yt(AccessibilityNodeInfo r) {
        if (!p.getBoolean("shorts", true)) return;
        if (has(r, YT, "reel_recycler") || has(r, YT, "reel_player_page_container")) {
            performGlobalAction(GLOBAL_ACTION_BACK);
            hold = System.currentTimeMillis() + 900;
            return;
        }
        AccessibilityNodeInfo s = byDesc(r, "Shorts", 0);
        if (s != null && s.isSelected()) {
            AccessibilityNodeInfo hm = byDesc(r, "Home", 0);
            if (hm != null) click(hm);
            else { performGlobalAction(GLOBAL_ACTION_BACK); hold = System.currentTimeMillis() + 900; }
        }
    }

    void go(AccessibilityNodeInfo r, boolean feed) {
        AccessibilityNodeInfo t = feed ? tab(r, IG, "profile_tab", "Profile") : tab(r, IG, "feed_tab", "Home");
        if (t != null && !t.isSelected()) click(t);
        else { performGlobalAction(GLOBAL_ACTION_BACK); hold = System.currentTimeMillis() + 900; }
    }

    boolean has(AccessibilityNodeInfo r, String pk, String id) {
        List<AccessibilityNodeInfo> l = r.findAccessibilityNodeInfosByViewId(pk + ":id/" + id);
        return l != null && !l.isEmpty();
    }

    AccessibilityNodeInfo tab(AccessibilityNodeInfo r, String pk, String id, String d) {
        List<AccessibilityNodeInfo> l = r.findAccessibilityNodeInfosByViewId(pk + ":id/" + id);
        return (l != null && !l.isEmpty()) ? l.get(0) : byDesc(r, d, 0);
    }

    boolean sel(AccessibilityNodeInfo r, String pk, String id, String d) {
        AccessibilityNodeInfo n = tab(r, pk, id, d);
        return n != null && n.isSelected();
    }

    AccessibilityNodeInfo byDesc(AccessibilityNodeInfo n, String d, int depth) {
        if (n == null || depth > 14) return null;
        CharSequence c = n.getContentDescription();
        if (c != null && c.toString().toLowerCase().startsWith(d.toLowerCase())) return n;
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo f = byDesc(n.getChild(i), d, depth + 1);
            if (f != null) return f;
        }
        return null;
    }

    void click(AccessibilityNodeInfo n) {
        while (n != null && !n.isClickable()) n = n.getParent();
        if (n != null) n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        else performGlobalAction(GLOBAL_ACTION_BACK);
        hold = System.currentTimeMillis() + 900;
    }
}
