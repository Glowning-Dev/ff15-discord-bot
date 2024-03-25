package dev.glowning.ff15;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.impl.R4J;

public class FF15 {

    private static FF15 _instance;

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
    }

    public R4J riot() {
        return r4j;
    }

    public JDA discord() {
        return jda;
    }
}
