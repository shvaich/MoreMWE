package me.shvaich.moremwe.commands.mwcommands;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.MWMapLocationHandler;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class MWCommandMyMapLocation extends AbstractMWCommand {

    public static final MWCommandMyMapLocation STATUS = new MWCommandMyMapLocation("whereami", MWMapLocationHandler.McPlayerMessage.STATUS);
    public static final MWCommandMyMapLocation HELP = new MWCommandMyMapLocation("help", MWMapLocationHandler.McPlayerMessage.HELP) {
        @Override
        public List<String> getCommandAliases() {
            return Collections.singletonList("h");
        }
    };

    private final String name;
    private final MWMapLocationHandler.McPlayerMessage messageType;

    private MWCommandMyMapLocation(String name, MWMapLocationHandler.McPlayerMessage messageType) {
        this.name = name;
        this.messageType = messageType;
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public void onCommand(String[] args) {
        ChatUtil.ChatChannel chatChannel = MoreMWEConfig.mcPlayerBaseLocationMessageChatChannel;
        if (args.length > 0) {
            final String subcommand = args[0];
            if (isHelpSubcommand(subcommand)) {
                printHelp();
                return;
            }
            if (args.length > 1 && "setchat".equalsIgnoreCase(subcommand)) {
                final ChatUtil.ChatChannel c = getChatChannelFromArgument(args[1]);
                if (c != null) {
                    if (c != MoreMWEConfig.mcPlayerBaseLocationMessageChatChannel) {
                        MoreMWEConfig.mcPlayerBaseLocationMessageChatChannel = c;
                        MoreMWEConfig.instance().save();
                    }
                    printCurrentChatChannel();
                }
                else {
                    ChatUtil.addErrorMessage("Invalid chat channel!");
                }
                return;
            }
            if ("chat".equalsIgnoreCase(subcommand)) {
                printCurrentChatChannel();
                return;
            }

            final ChatUtil.ChatChannel c = getChatChannelFromArgument(subcommand);
            if (c != null) chatChannel = c;
        }
        MWMapLocationHandler.instance().sendMcPlayerMessage(Minecraft.getMinecraft(), chatChannel, messageType);
    }

    @Override
    public List<String> onTabComplete(String[] args) {
        if (args.length <= 1) {
            final List<String> l = getChatChannelAutocomplete();
            l.add("chat");
            l.add("setchat");
            l.add("help");
            return getListOfStringsMatchingLastWord(args, l);
        }
        if (args.length == 2 && "setchat".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, getChatChannelAutocomplete());
        }
        return Collections.emptyList();
    }

    @Override
    protected void printHelp(String slashCommand) {
        printHelpBlock(StringUtil.capitalize(name) + " MW-Command", new String[][]{
                { slashCommand, "Sends your location message in your selected chat channel" },
                { slashCommand + " <default|all|party|guild>", "Sends your location message in the specified chat channel" },
                { slashCommand + " setchat <default|all|party|guild>", "Sets your selected chat channel to the specified channel" },
                { slashCommand + " chat", "Shows your selected chat channel" }
        });
    }

    private static void printCurrentChatChannel() {
        ChatUtil.addChatMessage(ChatUtil.getModTag() + "Your personal location messages will be sent to " + MoreMWEConfig.mcPlayerBaseLocationMessageChatChannel.getDisplayText() + EnumChatFormatting.RESET + " chat");
    }
}
