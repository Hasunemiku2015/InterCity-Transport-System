package com.hasunemiku2015.icts.nms;

import com.hasunemiku2015.refraction.annotations.Abstracted;
import com.hasunemiku2015.refraction.annotations.BaseClass;

@BaseClass(name = "net.minecraft.server.${VERSION}.PlayerConnection")
public interface PlayerConnection {
    void sendPacket(@Abstracted PacketPlayOutMount packet);
}
