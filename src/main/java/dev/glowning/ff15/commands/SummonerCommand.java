package dev.glowning.ff15.commands;

import dev.glowning.ff15.FF15;
import dev.glowning.ff15.commands.util.CommandBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard;
import no.stelar7.api.r4j.basic.constants.types.ApiKeyType;
import no.stelar7.api.r4j.impl.lol.raw.LeagueAPI;
import no.stelar7.api.r4j.impl.lol.raw.SummonerAPI;
import no.stelar7.api.r4j.impl.shared.AccountAPI;
import no.stelar7.api.r4j.impl.tft.TFTLeagueAPI;
import no.stelar7.api.r4j.impl.tft.TFTSummonerAPI;
import no.stelar7.api.r4j.pojo.lol.league.LeagueEntry;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import no.stelar7.api.r4j.pojo.shared.RiotAccount;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SummonerCommand extends CommandBuilder {
    public SummonerCommand() {
        super("summoner", "Display information about a summoner.");
        super.addCommandOption(OptionType.STRING, "region", "Summoner's region", true, true);
        super.addCommandOption(OptionType.STRING, "summoner-name", "Summoner's name (what precedes the #)", true);
        super.addCommandOption(OptionType.STRING, "tag", "Summoner's tag (what follows the #)");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("summoner"))
            return;

        User user = event.getUser();

        LeagueShard leagueShard = LeagueShard.fromString(event.getOption("region", OptionMapping::getAsString)).orElse(null);
        String name = event.getOption("summoner-name", OptionMapping::getAsString);
        String tag = event.getOption("tag", OptionMapping::getAsString);

        AccountAPI accountAPI = FF15.getInstance().riot().getAccountAPI();
        SummonerAPI summonerAPI = FF15.getInstance().riot().getLoLAPI().getSummonerAPI();
        LeagueAPI leagueAPI = FF15.getInstance().riot().getLoLAPI().getLeagueAPI();
        TFTSummonerAPI tftSummonerAPI = FF15.getInstance().riot().getTFTAPI().getSummonerAPI();
        TFTLeagueAPI tftLeagueAPI = FF15.getInstance().riot().getTFTAPI().getLeagueAPI();

        RiotAccount lolAccount = null, tftAccount;
        Summoner summoner, tftSummoner;

        if (tag != null) {
            lolAccount = accountAPI.getAccountByTag(RegionShard.EUROPE, name, tag, ApiKeyType.LOL);
            tftAccount = accountAPI.getAccountByTag(RegionShard.EUROPE, name, tag, ApiKeyType.TFT);

            summoner = summonerAPI.getSummonerByPUUID(leagueShard, lolAccount.getPUUID());
            tftSummoner = tftSummonerAPI.getSummonerByPUUID(leagueShard, tftAccount.getPUUID());
        } else {
            summoner = summonerAPI.getSummonerByName(leagueShard, name);
            tftSummoner = tftSummonerAPI.getSummonerByName(leagueShard, name);
        }

        assert leagueShard != null;
        List<LeagueEntry> leagueEntries = leagueAPI.getLeagueEntries(leagueShard, summoner.getSummonerId());
        leagueEntries.addAll(tftLeagueAPI.getLeagueEntries(leagueShard, tftSummoner.getSummonerId()));

        String informationContent = String.format(
                "**%s** - Level %d",
                lolAccount == null ? summoner.getName() : lolAccount.getName(),
                summoner.getSummonerLevel()
        ).trim();

        StringBuilder rankedContent = new StringBuilder();
        for (LeagueEntry leagueEntry : leagueEntries) {
            int winrate = Math.round((float) leagueEntry.getWins() / (float) (leagueEntry.getWins() + leagueEntry.getLosses()) * 100f);
            rankedContent.append("**").append(leagueEntry.getQueueType().prettyName()).append("**\n")
                    .append(leagueEntry.getTierDivisionType().prettyName()).append(" - ").append(leagueEntry.getLeaguePoints()).append(" LP\n")
                    .append(leagueEntry.getWins()).append("W / ").append(leagueEntry.getLosses()).append("L (").append(winrate).append("%)\n\n");
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(user.getEffectiveName(), null, user.getAvatarUrl());
        embed.addField("Information", informationContent, false);
        embed.addField("Ranked", rankedContent.toString(), false);
        embed.setThumbnail(String.format("https://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%s.png", FF15.DDRAGON_VERSION, summoner.getProfileIconId()));
        embed.setFooter(leagueShard.prettyName());

        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        String[] regions = new String[]{"BR", "EUNE", "EUW", "JP", "KR", "LAN", "LAS", "NA", "OCE", "PH", "RU", "SG", "TH", "TR", "TW", "VN"};

        if (event.getName().equals("summoner") && event.getFocusedOption().getName().equals("region")) {
            List<Command.Choice> options = Stream.of(regions)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue().toUpperCase()))
                    .map(word -> new Command.Choice(word, word))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }
}
