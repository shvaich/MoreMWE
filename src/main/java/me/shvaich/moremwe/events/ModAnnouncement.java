package me.shvaich.moremwe.events;

import me.shvaich.moremwe.commands.CommandMoreMWE;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ModAnnouncement {

    private int ticksDelay = 35;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.thePlayer != null && e.phase == TickEvent.Phase.START) {
            if (ticksDelay <= 0) {
                MinecraftForge.EVENT_BUS.unregister(this);
                CommandMoreMWE.printModHelpMessage();
            }
            ticksDelay--;
        }
    }
}
