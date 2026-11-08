package me.shvaich.moremwe.commands.mwcommands;

import fr.alexdoru.mwe.api.enums.MWClass;

import java.util.List;

public interface IMWCommand {
    String getCommandName();

    List<String> getCommandAliases();

    void onCommand(String[] args);

    List<String> onTabComplete(String[] args);

    boolean isMWClassAllowed(MWClass mwClass);
}
