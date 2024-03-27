package dev.glowning.ff15.commands.util;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

public class CommandBuilder extends ListenerAdapter {
    private final String name;
    private final String description;
    private final List<CommandOption> commandOptions = new ArrayList<>();

    public CommandBuilder(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addCommandOption(OptionType type, String name, String description) {
        CommandOption option = new CommandOption(type, name, description, false, false);
        this.commandOptions.add(option);
    }

    public void addCommandOption(OptionType type, String name, String description, boolean required) {
        CommandOption option = new CommandOption(type, name, description, required, false);
        this.commandOptions.add(option);
    }

    public void addCommandOption(OptionType type, String name, String description, boolean required, boolean autoComplete) {
        CommandOption option = new CommandOption(type, name, description, required, autoComplete);
        this.commandOptions.add(option);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<CommandOption> getCommandOptions() {
        return commandOptions;
    }
}
