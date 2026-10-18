package me.shvaich.moremwe.features;

import fr.alexdoru.mwe.api.IScoreboardParser;
import fr.alexdoru.mwe.api.enums.MWTeam;
import me.shvaich.moremwe.utils.PlayerUtil;
import net.minecraft.client.Minecraft;

import java.util.function.Predicate;

public enum MWGameState {
    DURING_GAME(parser -> true),
    AFTER_WALLS_FALL(parser -> !parser.isPrepPhase()),
    AFTER_FIRST_WITHER_DEATH(parser -> parser.getWitherCount() < 4),
    AFTER_SELF_WITHER_DEATH(parser -> {
        if (parser.isDeathmatch()) return true;
        final MWTeam team = PlayerUtil.getPlayerTeam(Minecraft.getMinecraft().thePlayer);
        return team != null && !parser.getAliveWithers().contains(team);
    }),
    DURING_DEATHMATCH(IScoreboardParser::isDeathmatch);

    private final Predicate<IScoreboardParser> predicate;

    MWGameState(Predicate<IScoreboardParser> predicate) { this.predicate = predicate; }

    /** For correct values you must confirm {@link IScoreboardParser#isInMwGame()} is {@code true} before calling this */
    public boolean isActive(IScoreboardParser parser) { return predicate.test(parser); }
}
