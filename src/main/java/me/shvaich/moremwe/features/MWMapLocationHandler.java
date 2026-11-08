package me.shvaich.moremwe.features;

import com.google.common.base.Predicates;
import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWMap;
import fr.alexdoru.mwe.api.enums.MWTeam;
import fr.alexdoru.mwe.api.events.MapEvent;
import fr.alexdoru.mwe.api.events.MegaWallsGameEvent;
import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.PlayerUtil;
import me.shvaich.moremwe.utils.StringUtil;
import me.shvaich.moremwe.utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public final class MWMapLocationHandler {

    private static final String COOLDOWN_MSG_START = "Location messages are on a cooldown for ";
    private static final long MSG_WAIT_TIME = 120_000; // 2m

    private static MWMapLocationHandler instance;

    private MWMap currentMap;
    private final TimerUtil messageTimer = new TimerUtil(10_000L);
    private final Queue<Long> messagesTimes = new ArrayDeque<>();
    private final String[] previousMessages = new String[3];
    private int oldestMessagePos;


    public enum McPlayerMessage {
        STATUS("I'm at", "right now"), // I'm at [PLACE] (? right now)
        HELP("Help me at", "please"); // Help me at [PLACE] (? please)

        private final String message, transformationAddon;
        private final boolean isPrefix = true;

        McPlayerMessage(String message, String transformationAddon) {
            this.message = message;
            this.transformationAddon = transformationAddon;
        }
    }

    public enum PingMessage {
        // Block: (? Look) [PLACE]
        // Entity: (? Spotted) [TEAM] Player at [PLACE]
        STATUS("", "look", "spotted", true, EnumChatFormatting.AQUA),

        // Block: (? Maybe) Go to [PLACE]
        // Entity: (? Maybe) Go to [PLACE]
        GO_TO("Go to", "maybe", true, EnumChatFormatting.GREEN),

        // Block: Push [PLACE] (? now)
        // Entity: Push [TEAM] Player at (? now)
        PUSH("Push", "now", false, EnumChatFormatting.GOLD),

        // Block: (? Spotted) Enemy at [PLACE]
        // Entity: (? Spotted) [? TEAM] Enemy at [PLACE]
        ENEMY("Enemy at", "spotted", true, EnumChatFormatting.RED);

        private final String message, blockTransformationAddon, entityTransformationAddon;
        private final boolean isPrefix;
        private final String displayText;

        PingMessage(String message, String blockTransformationAddon, String entityTransformationAddon, boolean isPrefix, EnumChatFormatting color) {
            this.message = message;
            this.blockTransformationAddon = blockTransformationAddon;
            this.entityTransformationAddon = entityTransformationAddon;
            this.isPrefix = isPrefix;
            this.displayText = color + name().replace('_', ' ');
        }

        PingMessage(String message, String transformationAddon, boolean isPrefix, EnumChatFormatting color) {
            this(message, transformationAddon, transformationAddon, isPrefix, color);
        }

        public String getDisplayText() { return displayText; }
    }

    private MWMapLocationHandler(){}

    public static MWMapLocationHandler instance() {
        MWMapLocationHandler i = instance;
        if (i == null) instance = i = new MWMapLocationHandler();
        return i;
    }

    public boolean isActive() { return MWEApi.Scoreboard.getScoreboardParser().isInMwGame() && currentMap != null; }

    public void sendPingMessage(Minecraft mc, ChatUtil.ChatChannel chatChannel, PingMessage type) {
        if (!canSendMessage(mc)) return;
        final MovingObjectPosition pos = getObjectInView(mc);
        if (pos != null) {
            if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                final BlockPos blockPos = pos.getBlockPos();
                if (blockPos != null) {
                    sendMessage(blockPos.getX(), blockPos.getZ(), chatChannel, type.message, type.blockTransformationAddon, type.isPrefix);
                    return;
                }
            }
            else if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                final Entity entity = pos.entityHit;
                if (entity != null) {
                    String msg = type.message;
                    if (type != PingMessage.GO_TO) {
                        final MWTeam playerTeam = (entity instanceof EntityPlayer) ? PlayerUtil.getPlayerTeam((EntityPlayer) entity) : null;
                        if (type == PingMessage.ENEMY) {
                            if (playerTeam != null && playerTeam != PlayerUtil.getPlayerTeam(Minecraft.getMinecraft().thePlayer)) {
                                msg = playerTeam.getName() + ' ' + msg;
                            }
                        }
                        else {
                            String playerMsg = "Player at";
                            if (playerTeam != null) playerMsg = playerTeam.getName() + ' ' + playerMsg;
                            msg = msg.isEmpty() ? playerMsg : (msg + ' ' + playerMsg);
                        }
                    }
                    sendMessage(entity.posX, entity.posZ, chatChannel, msg, type.entityTransformationAddon, type.isPrefix);
                    return;
                }
            }
        }
        ChatUtil.addErrorMessage("No target in sight!");
    }

    public void sendMcPlayerMessage(Minecraft mc, ChatUtil.ChatChannel chatChannel, McPlayerMessage type) {
        if (canSendMessage(mc)) {
            sendMessage(mc.thePlayer.posX, mc.thePlayer.posZ, chatChannel, type.message, type.transformationAddon, type.isPrefix);
        }
    }

    private void sendMessage(double x, double z, ChatUtil.ChatChannel chatChannel, String message, String transformation, boolean isPrefix) {
        final String location = getLocation(x, z);
        if (StringUtil.isEmpty(location)) {
            ChatUtil.addErrorMessage("Failed to send location message ; unknown location");
            return;
        }
        String sentMessage = (message.isEmpty() ? location : (message + ' ' + location));
        if (chatChannel != ChatUtil.ChatChannel.PARTY) {
            final String key = sentMessage;
            int sameOccurrences = 0;
            for (final String msg : previousMessages) if (key.equals(msg)) sameOccurrences++;

            final String expired = previousMessages[oldestMessagePos];
            if (expired != null) {
                for (int i = 0; i < previousMessages.length; i++) if (expired.equals(previousMessages[i])) previousMessages[i] = null;
            }
            previousMessages[oldestMessagePos] = key.equals(expired) ? null : key;
            this.oldestMessagePos = (oldestMessagePos + 1) % previousMessages.length;

            if (sameOccurrences > 1) {
                sentMessage = isPrefix ? (transformation + ' ' + sentMessage) : (sentMessage + ' ' + transformation);
            }
            if (sameOccurrences == 1 || sameOccurrences == 3) sentMessage += " (2x)";
        }
        Minecraft.getMinecraft().thePlayer.sendChatMessage(chatChannel + sentMessage);
    }


    private boolean canSendMessage(Minecraft mc) {
        if (mc.thePlayer == null) {
            MoreMWE.LOGGER.error("Failed to send location message");
            return false;
        }

        if (!MWEApi.Scoreboard.getScoreboardParser().isInMwGame()) {
            ChatUtil.addErrorMessage("Location messages only works during Mega Walls matches");
            return false;
        }

        if (currentMap == null) {
            ChatUtil.addErrorMessage("Cannot send message ; Map not loaded");
            return false;
        }

        if (!messageTimer.update()) {
            ChatUtil.addErrorMessage(COOLDOWN_MSG_START + messageTimer.getTimeLeftText());
            return false;
        }

        final long now = System.currentTimeMillis();
        while (!messagesTimes.isEmpty() && (now - messagesTimes.peek()) >= MSG_WAIT_TIME) {
            messagesTimes.poll();
        }
        if (messagesTimes.size() >= 5) {
            final String timeText = TimerUtil.getTimeText(MSG_WAIT_TIME - (now - messagesTimes.peek()));
            ChatUtil.addErrorMessage(COOLDOWN_MSG_START + timeText);
            return false;
        }
        messagesTimes.offer(now);
        return true;
    }

    private String getLocation(double x, double z) {
        if (x < currentMap.eastLimit && x > currentMap.westLimit && z < currentMap.southLimit && z > currentMap.northLimit) {
            final String mid = "MIDDLE";
            if (x < currentMap.innerEastLimit && x > currentMap.innerWestLimit && z < currentMap.innerSouthLimit && z > currentMap.innerNorthLimit) {
                return mid;
            }

            final boolean isAB = x + z > currentMap.eastLimit + currentMap.northLimit;
            final boolean isCD = x - z > currentMap.westLimit - currentMap.northLimit;
            final MWTeam frontBase = isAB && isCD ? currentMap.eastBase : !isAB && isCD ? currentMap.northBase : !isAB ? currentMap.westBase : currentMap.southBase;
            return mid + " (Front " + frontBase.getName() + ")";
        }
        return getBaseLocation(x, z);
    }

    private String getBaseLocation(double x, double z) {
        final double middleWidth = currentMap.eastLimit - currentMap.westLimit;
        final double middleHeight = currentMap.southLimit - currentMap.northLimit;
        MWTeam base = null;
        double relativeSpawnDistance = 0, relativeEntranceDistance = 0;
        if (z < currentMap.northLimit && x > currentMap.westLimit && x < currentMap.eastLimit) {
            base = currentMap.northBase;
            relativeSpawnDistance = (currentMap.eastLimit - x) / middleWidth;
            relativeEntranceDistance = (currentMap.northLimit - z) / middleHeight;
        }
        else if (x > currentMap.eastLimit && z > currentMap.northLimit && z < currentMap.southLimit) {
            base = currentMap.eastBase;
            relativeSpawnDistance = (currentMap.southLimit - z) / middleHeight;
            relativeEntranceDistance = (x - currentMap.eastLimit) / middleWidth;
        }
        else if (z > currentMap.southLimit && x > currentMap.westLimit && x < currentMap.eastLimit) {
            base = currentMap.southBase;
            relativeSpawnDistance = (x - currentMap.westLimit) / middleWidth;
            relativeEntranceDistance = (z - currentMap.southLimit) / middleHeight;
        }
        else if (x < currentMap.westLimit && z > currentMap.northLimit && z < currentMap.southLimit) {
            base = currentMap.westBase;
            relativeSpawnDistance = (z - currentMap.northLimit) / middleHeight;
            relativeEntranceDistance = (currentMap.westLimit - x) / middleWidth;
        }
        if (base == null) return null;
        if (!MoreMWEConfig.detailedBaseLocationMessage) return base.getName();

        final double third = 1 / 3.0d;
        final boolean isInSpawnSide = relativeSpawnDistance < third;
        String str;

        if (relativeEntranceDistance < third) {
            str = "PRE - " + (isInSpawnSide ? "SPAWN-SIDE" : (relativeSpawnDistance < (third + third) ? "CENTER" : "FAR-SIDE"));
        }
        else if (relativeEntranceDistance < (third + third)) {
            str = isInSpawnSide ? "SPAWN" : (relativeSpawnDistance < (third + third) ? "FRONT-WITHER" : "MAIN - FAR-SIDE");
        }
        else {
            str = isInSpawnSide ? "REAR - SPAWN-SIDE" : (relativeSpawnDistance < (third + third) ? "WITHER" : "REAR - FAR-SIDE");
        }

        /* Add Neighbor:
            final MWTeam neighbor;
            if (base == currentMap.northBase) neighbor = isInSpawnSide ? currentMap.eastBase : currentMap.westBase;
            else if (base == east) neighbor = isInSpawnSide ? currentMap.southBase : currentMap.northBase;
            else if (base == south) neighbor = isInSpawnSide ? currentMap.westBase : currentMap.eastBase;
            else neighbor = isInSpawnSide ? currentMap.northBase : currentMap.southBase;

            str += " N|" + neighbor.getName();
        */

        return base.getName() + " (" + str + ")";
    }

    @SubscribeEvent
    public void onMapEvent(MapEvent e) {
        this.currentMap = MWMap.fromName(e.mapName);
    }

    @SubscribeEvent
    public void onMWGameEvent(MegaWallsGameEvent e) {
        if (e.type == MegaWallsGameEvent.Type.GAME_START || e.type == MegaWallsGameEvent.Type.GAME_END) {
            Arrays.fill(previousMessages, null);
            this.oldestMessagePos = 0;
        }
    }

    private static MovingObjectPosition getObjectInView(Minecraft mc) {
        final float partialTicks = 1.0F;
        final Entity viewEntity = mc.thePlayer;
        MovingObjectPosition pos = mc.objectMouseOver;
        if (mc.theWorld != null && (pos == null || pos.typeOfHit == MovingObjectPosition.MovingObjectType.MISS)) {
            final int distance = Math.max(1, mc.gameSettings.renderDistanceChunks) * 16;

            // ray trace to a block
            pos = viewEntity.rayTrace(distance, partialTicks);
            if (pos != null && pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) pos = null;

            final Vec3 eyes = viewEntity.getPositionEyes(partialTicks);
            final double limit = pos != null ? eyes.distanceTo(pos.hitVec) : distance;

            final Vec3 look = viewEntity.getLook(partialTicks);
            final Vec3 end = eyes.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);

            final List<Entity> candidates = mc.theWorld.getEntitiesInAABBexcluding(viewEntity,
                    viewEntity.getEntityBoundingBox()
                            .addCoord(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance)
                            .expand(1.0F, 1.0F, 1.0F),
                    Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith)
            );

            Entity closest = null;
            Vec3 closestHit = null;
            double closestDist = limit;

            for (final Entity e : candidates) {
                final float border = e.getCollisionBorderSize();
                final AxisAlignedBB box = e.getEntityBoundingBox().expand(border, border, border);
                final MovingObjectPosition intercept = box.calculateIntercept(eyes, end);

                if (box.isVecInside(eyes)) {
                    if (closestDist >= 0.0D) {
                        closest = e;
                        closestHit = intercept == null ? eyes : intercept.hitVec;
                        closestDist = 0.0D;
                    }
                }
                else if (intercept != null) {
                    final double d = eyes.distanceTo(intercept.hitVec);
                    if (d < closestDist || closestDist == 0.0D) {
                        if (e == viewEntity.ridingEntity && !viewEntity.canRiderInteract()) {
                            if (closestDist == 0.0D) {
                                closest = e;
                                closestHit = intercept.hitVec;
                            }
                        }
                        else {
                            closest = e;
                            closestHit = intercept.hitVec;
                            closestDist = d;
                        }
                    }
                }
            }

            if (closest != null && (closestDist < limit || pos == null)) {
                pos = new MovingObjectPosition(closest, closestHit);
            }
        }
        return pos;
    }
}