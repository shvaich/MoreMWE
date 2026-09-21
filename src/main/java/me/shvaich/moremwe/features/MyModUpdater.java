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
            final String linkMsg = YELLOW + "Click here to view the changelog & download page";
            final String bar = GRAY + ChatUtil.bar();
            final IChatComponent msg = new ChatComponentText(bar + "\n" + ChatUtil.centerLine(DARK_RED.toString() + BOLD + MoreMWE.NAME + GOLD + " v" + updateInfo.version + GREEN + " is available!"))
                    .appendText("\n" + ChatUtil.getSpaceToCenter(linkMsg))
                    .appendSibling(ChatUtil.getURLMessage(linkMsg, "https://github.com/shvaich/MoreMWE/releases"));

            if (this.automaticUpdate) {
                msg.appendText("\n\n");
                if (this.isFeatherClient) {
                    msg.appendText(RED + "✘ The automatic updater is disabled on Feather");
                }
                else if (this.downloadSuccess) {
                    msg.appendText(GREEN + "✔ Update has been downloaded and will be installed automatically when closing the game");
                }
                else {
                    msg.appendText(RED + "✘ Failed to download and install update");
                }
            }
            msg.appendText("\n" + bar);
            ChatUtil.addChatMessage(msg);
        }
    }
}
