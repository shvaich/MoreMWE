package me.shvaich.moremwe.commands;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.StringUtil;
import me.shvaich.moremwe.utils.TimerUtil;
import me.shvaich.moremwe.utils.MoreMWEException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;


public class CommandPlay extends MyAbstractCommand {

    private final TimerUtil warpProtectionTimer = new TimerUtil(10_000L);

    private static final Map<String, Mode> modeAliasMap = new HashMap<>();

    private static final String[] NO_ALIASES = new String[0];

    private static final Game[] GAMES = new Game[]{

            new Game("mega_walls", "Mega Walls", new String[]{ "mw", "megawalls" },
                    new Mode("mw_standard", "Standard", "mw", "megawalls", "mega_walls"),
                    new Mode("mw_face_off", "Face Off", "mw_faceoff")
            ),

            new Game("blitz_survival_games", "Blitz Survival Games", new String[]{ "bsg", "blitz" },
                    new Mode("blitz_solo_normal", "Solo", "bsg", "blitz"),
                    new Mode("blitz_teams_normal", "Teams")
            ),

            new Game("sky_wars", "Sky Wars", new String[]{ "sw", "skywars" },
                    new Mode("solo_normal", "Normal (Solo)"),
                    new Mode("teams_normal", "Normal (Doubles)")
            ),

            new Game("duels", "Duels",
                    new Mode("duels_mw_duel", "Mega Walls Duels"),
                    new Mode("duels_blitz_duel", "Blitz Duels"),
                    new Mode("duels_classic_duel", "Classic Duels (1v1)"),
                    new Mode("duels_classic_doubles", "Classic Duels (2v2)"),
                    new Mode("duels_boxing_duel", "Boxing Duels"),
                    new Mode("duels_combo_duel", "Combo Duels"),
                    new Mode("duels_duel_arena", "Duel Arena"),
                    new Mode("duels_potion_duel", "NoDebuff Duels"),
                    new Mode("duels_op_duel", "OP Duels (1v1)"),
                    new Mode("duels_op_doubles", "OP Duels (2v2)"),
                    new Mode("duels_sw_duel", "SkyWars Duels (1v1)"),
                    new Mode("duels_sw_doubles", "SkyWars Duels (2v2)"),
                    new Mode("duels_sumo_duel", "Sumo Duels"),
                    new Mode("bedwars_two_one_duels", "Bed Wars Duels (Normal)"),
                    new Mode("duels_bridge_duel", "The Bridge (1v1)"),
                    new Mode("duels_bridge_doubles", "The Bridge (2v2)"),
                    new Mode("duels_bridge_threes", "The Bridge (3v3)"),
                    new Mode("duels_bridge_four", "The Bridge (4v4)"),
                    new Mode("duels_uhc_duel", "UHC Duels (1v1)"),
                    new Mode("duels_uhc_doubles", "UHC Duels (2v2)"),
                    new Mode("duels_uhc_four", "UHC Duels (4v4)"),
                    new Mode("duels_uhc_meetup", "UHC Deathmatch"),
                    new Mode("duels_bow_duel", "Bow Duels")
            )
    };

    static {
        for (final Game game : GAMES) {
            for (final Mode mode : game.modes) {
                for (final String alias : mode.aliases)
                    modeAliasMap.put(alias, mode);
            }
        }
    }

    @Override
    public String getCommandName() {
        return "play";
    }

    @Override
    protected void onCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            final String subcommand = StringUtil.toLowerCase(args[0]);
            if (isHelpSubcommand(subcommand)) {
                printGamesHelp(args);
                return;
            }
            if (args.length == 1) {
                final Mode mode = modeAliasMap.get(subcommand);
                if (mode != null) {
                    sendCommand(mode.key);
                    return;
                }
            }
        }
        sendCommand(args);
    }

    @Override
    protected void sendCommand(String[] args) {
        if (MoreMWEConfig.playWarpProtection && !MWEApi.Squad.getSquadMap().isEmpty()) {
            if (warpProtectionTimer.update()) {
                final String bar = EnumChatFormatting.YELLOW + ChatUtil.bar();
                ChatUtil.addChatMessage(bar + "\n" + EnumChatFormatting.YELLOW + "You have players in your Squad, run the command again to warp\n" + bar);
                return;
            }
            warpProtectionTimer.reset();
        }
        super.sendCommand(args);
    }

    @Override
    protected List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            final String lastArg = StringUtil.toLowerCase(args[args.length-1]);
            final List<String> modes = new ArrayList<>();
            for (final Game game : GAMES) {
                for (final Mode mode : game.modes) {
                    if (mode.key.startsWith(lastArg)) {
                        modes.add(mode.key);
                    }
                }
            }
            return modes;
        }
        if (args.length > 1 && isHelpSubcommand(StringUtil.toLowerCase(args[0]))) {
            final String lastArg = StringUtil.toLowerCase(args[args.length-1]);
            final List<String> gameNames = new ArrayList<>();
            for (final Game game : GAMES) {
                if (game.key.startsWith(lastArg)) {
                    gameNames.add(game.key);
                }
            }
            return gameNames;
        }
        return Collections.emptyList();
    }

    private static void printGamesHelp(String[] args) {
        if (args.length > 1) {
            final int cap = args.length - 1;
            final Set<String> checkedNames = new HashSet<>(cap);
            final Set<Game> addedGames = new HashSet<>(cap);
            final Set<String> invalidNames = new LinkedHashSet<>(cap);
            boolean printedGameHelpBlock = false;
            for (int i = 1; i < args.length; i++) {
                final String gameArg = args[i];
                if (gameArg.isEmpty() || !checkedNames.add(gameArg)) continue;
                Game matchedGame = null;
                for (final Game game : GAMES) {
                    if (game.matches(gameArg)) {
                        matchedGame = game;
                        break;
                    }
                }
                if (matchedGame == null) {
                    invalidNames.add(gameArg);
                }
                else if (addedGames.add(matchedGame)) {
                    if (printedGameHelpBlock) ChatUtil.addChatMessage("");
                    else ChatUtil.addChatMessage(getHelpBarColor() + ChatUtil.bar());
                    printGameHelpBlock(matchedGame);
                    printedGameHelpBlock = true;
                }
            }
            if (!invalidNames.isEmpty()) {
                ChatUtil.addChatMessage((printedGameHelpBlock ? "\n" : ChatUtil.getModTag()) + getSkippedMessage(invalidNames, "unknown games"));
                if (!printedGameHelpBlock) return;
            }
            if (printedGameHelpBlock) {
                ChatUtil.addChatMessage(getHelpBarColor() + ChatUtil.bar());
                return;
            }
        }
        final String bar = getHelpBarColor() + ChatUtil.bar();
        ChatUtil.addChatMessage(bar + "\n" + ChatUtil.centerLine(EnumChatFormatting.GRAY + EnumChatFormatting.BOLD.toString() + "Hypixel Play Commands"));
        for (final Game game : GAMES) {
            ChatUtil.addChatMessage("");
            printGameHelpBlock(game);
        }
        ChatUtil.addChatMessage(bar);
    }

    private static void printGameHelpBlock(Game game) {
        final String[][] helpLines = new String[game.modes.length][];
        int i = 0;
        for (final Mode mode : game.modes) {
            helpLines[i++] = new String[]{ "/play " + mode.key, mode.name };
        }
        printCommandHelpBlock(game.name, helpLines);
    }

    private static class Game {
        public final String key;
        public final String name;
        private final String[] aliases;
        public final Mode[] modes;

        public Game(String key, String name, String[] aliases, Mode... modes) {
            if (modes == null || modes.length == 0) {
                throw new MoreMWEException();
            }
            this.key = key;
            this.name = name;
            this.aliases = aliases;
            this.modes = modes;
        }

        public Game(String key, String name, String alias, Mode... modes) {
            this(key, name, new String[]{alias}, modes);
        }

        public Game(String key, String name, Mode... modes) {
            this(key, name, NO_ALIASES, modes);
        }

        public Game(Mode mode) {
            this(mode.key, mode.name, mode.aliases, mode);
        }

        public boolean matches(String gameStr) {
            if (key.equals(gameStr)) return true;
            for (final String alias : aliases)
                if (alias.equals(gameStr)) return true;
            return false;
        }
    }

    private static class Mode {
        public final String key;
        public final String name;
        public final String[] aliases;

        public Mode(String key, String name, String... aliases) {
            this.key = key;
            this.name = name;
            this.aliases = aliases;
        }

        public Mode(String key, String name) {
            this(key, name, NO_ALIASES);
        }
    }
}