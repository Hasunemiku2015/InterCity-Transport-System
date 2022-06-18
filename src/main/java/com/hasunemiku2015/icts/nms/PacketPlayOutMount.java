package com.hasunemiku2015.icts.nms;

import com.hasunemiku2015.refraction.annotations.BaseClass;
import com.hasunemiku2015.refraction.annotations.Constructor;
import com.hasunemiku2015.refraction.annotations.Field;

@BaseClass(name = "net.minecraft.server.${VERSION}.PacketPlayOutMount")
public interface PacketPlayOutMount {
    @Field(name = "a")
    void a(int EntityID);

    @Field(name = "b")
    void b(int[] EntityID);

    @Constructor
    PacketPlayOutMount newInstance();
}
