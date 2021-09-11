package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.gg2.GitHubArchiveMapRepository;
import fr.yb.discordybot.gg2.MapRepository;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapModule extends BotModule {
    private static final String REPO_OWNER = "Derpduck";
    private static final String REPO_NAME = "GG2-Map-Archive";
    private static final String BRANCH_NAME = "master";
    private static final MapRepository mapRepository = new GitHubArchiveMapRepository(
        REPO_OWNER, REPO_NAME, BRANCH_NAME);

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public String getCommand() {
        return "map";
    }

    @Override
    public String help() {
        return "**MapModule**: Returns a map. `" + this.getFullCommand() + " map_name `\n";

    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        String response = null;
        String inputCommand = t.getMessage().getContent();  // full command + args
        String mapName = parseMapName(inputCommand);
        if (mapName != null) {
            try {
                response = mapRepository.getMapFileURL(mapName);
            } catch (Exception e) {
                Logger.getLogger(MapModule.class.getName()).log(Level.WARNING, null, e);
                response = "Failed to get map. Error in console.";
            }
        } else {
            try {
                mapName = mapRepository.getRandomMapName();
                response = mapRepository.getMapFileURL(mapName);
            } catch (Exception e) {
                Logger.getLogger(MapModule.class.getName()).log(Level.WARNING, null, e);
                response = "Failed to get map. Error in console.";
            }
        }
        try {
            if (response == null) {
                t.getMessage().getChannel().sendMessage(mapRepository.getNotFoundMessage(mapName));
            } else {
                t.getMessage().getChannel().sendMessage(response);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            Logger.getLogger(MapModule.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    private String parseMapName(String inputCommand) {
        String pattern = "^" + this.getFullCommand() + " (\\w+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(inputCommand.toLowerCase());
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }
}