package dev.glowning.ff15;

import dev.glowning.ff15.commands.SummonerCommand;
import dev.glowning.ff15.commands.util.CommandBuilder;
import dev.glowning.ff15.commands.util.CommandOption;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.impl.R4J;

public class FF15 {

    private static FF15 _instance;
    public static final String DDRAGON_VERSION = "14.6.1";

    private final String discordApiKey;
    private final String lolApiKey;
    private final String tftApiKey;

    private JDA jda;
    private R4J r4j;

    private FF15(String discordApiKey, String lolApiKey, String tftApiKey) {
        this.discordApiKey = discordApiKey;
        this.lolApiKey = lolApiKey;
        this.tftApiKey = tftApiKey;
    }

    public static void main(String[] args) {
        _instance = new FF15(
                args[0],
                args[1],
                args[2]
        );
        _instance.init();
    }

    public static FF15 getInstance() {
        return _instance;
    }

    private void init() {
        // Building Riot API
        APICredentials credentials = new APICredentials(this.lolApiKey, null, this.tftApiKey, null, null);
        this.r4j = new R4J(credentials);
        System.out.println("Successfully connected to Riot API");

        // Building Discord API
        this.jda = JDABuilder.createDefault(this.discordApiKey).build();
        System.out.println("Successfully connected to Discord");

        CommandBuilder[] commands = new CommandBuilder[]{new SummonerCommand()};
        for (CommandBuilder command : commands) {
            jda.addEventListener(command);

            CommandCreateAction commandCreateAction = jda.upsertCommand(command.getName(), command.getDescription());
            if (!command.getCommandOptions().isEmpty()) {
                for (CommandOption commandOption : command.getCommandOptions()) {
                    commandCreateAction = commandCreateAction.addOption(
                            commandOption.type(),
                            commandOption.name(),
                            commandOption.description(),
                            commandOption.required(),
                            commandOption.autoComplete()
                    );
                }
            }
            commandCreateAction.queue();
            System.out.println("Registered command " + command.getName());
        }
    }

    public R4J riot() {
        return r4j;
    }

    public JDA discord() {
        return jda;
    }
}
