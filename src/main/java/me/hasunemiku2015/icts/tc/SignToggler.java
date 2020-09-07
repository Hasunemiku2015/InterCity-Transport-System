package me.hasunemiku2015.icts.tc;

import com.bergerkiller.bukkit.tc.signactions.SignAction;

public class SignToggler {
    // Register TC-ActionSigns
    private static final InterLink its = new InterLink();
    private static final InterLinkReceive itsr = new InterLinkReceive();

    public static void init() {
        SignAction.register(its);
        SignAction.register(itsr);
    }

    public static void deinit() {
        SignAction.unregister(its);
        SignAction.unregister(itsr);
    }
}
