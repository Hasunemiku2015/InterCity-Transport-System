package me.hasunemiku2015.icts.TCActions;

import com.bergerkiller.bukkit.tc.signactions.SignAction;

public class SignToggler {
    //TC Signs
    private static final InterLink its = new InterLink();
    private static final InterLinkRecieve itsr = new InterLinkRecieve();

    public static void init(){
        SignAction.register(its);
        SignAction.register(itsr);
    }

    public static void deinit(){
        SignAction.unregister(its);
        SignAction.unregister(itsr);
    }
}