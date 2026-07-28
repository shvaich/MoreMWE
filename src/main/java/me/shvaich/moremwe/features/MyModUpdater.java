package me.shvaich.moremwe.features;

import fr.alexdoru.mwe.api.ModUpdater;
import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.util.ChatComponentText;
import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraft.util.IChatComponent;

import java.io.File;

public final class MyModUpdater extends ModUpdater {
    public MyModUpdater(File modJarFile) {
        super(modJarFile, MoreMWE.NAME, MoreMWE.VERSION, MoreMWEConfig.automaticUpdate);
    }

    @Override
    protected String getApiEndpoint() {
        return "https://api.github.com/repos/shvaich/MoreMWE/releases/latest";
    }

    @Override
    protected void printChatNotification() {
        if (this.updateInfo != null) {
            final String bar = GRAY + ChatUtil.bar();
            final IChatComponent msg = new ChatComponentText(bar + "\n" + ChatUtil.centerLine(DARK_RED.toString() + BOLD + MoreMWE.NAME + GOLD + " v" + updateInfo.version + GREEN + " is available!"))
                    .appendSibling(ChatUtil.getURLMessage(ChatUtil.centerLine(YELLOW + "Click here to view the changelog & download page"), "https://github.com/shvaich/MoreMWE/releases"));

            if (this.automaticUpdate) {
                msg.appendSibling(new ChatComponentText(""));
                if (this.isFeatherClient) {
                    msg.appendSibling(new ChatComponentText(RED + "✘ The automatic updater is disabled on Feather"));
                } else if (this.downloadSuccess) {
                    msg.appendSibling(new ChatComponentText(GREEN + "✔ Update has been downloaded and will be installed automatically when closing the game"));
                }
            }
            msg.appendSibling(new ChatComponentText("\n" + bar));
            ChatUtil.addChatMessage(msg);
        }
    }
}
