package me.shvaich.moremwe.commands.mwcommands;

import net.minecraft.command.CommandBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractMWCommand {

//    private final EnumSet<MWClass> allowedClasses;
//
//    protected AbstractMWCommand() {
//        this.allowedClasses = null;
//    }
//
//    protected AbstractMWCommand(MWClass... allowedClasses) {
//        this.allowedClasses = EnumSet.noneOf(MWClass.class);
//        Collections.addAll(this.allowedClasses, allowedClasses);
//    }

    public abstract String getCommandName();

    public List<String> getCommandAliases() { return Collections.emptyList(); }

    public abstract void onCommand(String[] args);

    public abstract List<String> onTabComplete(String[] args);

    protected static List<String> getListOfStringsMatchingLastWord(String[] args, String... options) {
        final String lastWord = args[args.length-1];
        final List<String> o = new ArrayList<>();
        for (final String option : options) {
            if (CommandBase.doesStringStartWith(lastWord, option)) {
                o.add(option);
            }
        }
        return o;
    }
}