package com.hasunemiku2015.icts.tc;

import com.bergerkiller.bukkit.tc.signactions.SignAction;

@SuppressWarnings("SpellCheckingInspection")
public class SignToggler {
    // Register TC-ActionSigns
    private static final ICLink its = new ICLink();
    private static final ICReceive itsr = new ICReceive();

    public static void init() {
        SignAction.register(its);
        SignAction.register(itsr);
    }

    public static void deinit() {
        SignAction.unregister(its);
        SignAction.unregister(itsr);
    }
}
