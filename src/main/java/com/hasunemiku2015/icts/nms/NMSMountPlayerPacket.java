package com.hasunemiku2015.icts.nms;

import com.hasunemiku2015.icts.nms.refraction.generated.CraftPlayerImplementation;
import com.hasunemiku2015.icts.nms.refraction.generated.PacketPlayOutMountImplementation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSMountPlayerPacket {
    private final Player player;
    private final PlayerConnection connection;

    public NMSMountPlayerPacket(Player player) {
        this.player = player;
        this.connection = CraftPlayerImplementation.create(player).getHandle().getPlayerConnection();
    }

    public void sendPacket(Entity entity) {
        PacketPlayOutMount packet = PacketPlayOutMountImplementation.create(null).newInstance();
        packet.a(entity.getEntityId());
        packet.b(new int[] {player.getEntityId()});
        connection.sendPacket(packet);
    }
}