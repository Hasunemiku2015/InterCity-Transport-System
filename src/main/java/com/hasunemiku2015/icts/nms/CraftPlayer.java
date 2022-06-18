package com.hasunemiku2015.icts.nms;

import com.hasunemiku2015.refraction.annotations.BaseClass;

@BaseClass(name = "org.bukkit.craftbukkit.%s.entity.CraftPlayer")
public interface CraftPlayer {
    EntityPlayer getHandle();
}
