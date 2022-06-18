package com.hasunemiku2015.icts.nms;

import com.hasunemiku2015.refraction.annotations.BaseClass;
import com.hasunemiku2015.refraction.annotations.Field;

@BaseClass(name = "net.minecraft.server.${VERSION}.EntityPlayer")
public interface EntityPlayer {
    @Field(name = "playerConnection")
    PlayerConnection getPlayerConnection();
}
