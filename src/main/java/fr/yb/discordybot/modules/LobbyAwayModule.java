package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nicolas
 */
public class LobbyAwayModule extends BotModule {
    
    public static final String TEMPLATE = "YOUR INQUIRY HAS BEEN NOTED.\n"
            + "In the absence of %s, help will be shortly dispatched to your location **[%s]**. Thank you for your patience.";
    public static final String[] LOCATIONS = {
        "50°36.25′S 165°58.38′E",
        "37°25.37′S 145°8.11′E"
    };

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            int randChoice = (int)(t.getAuthor().getLongID() % LOCATIONS.length);
            String botName = this.getClient().getOurUser().getDisplayName(t.getGuild());
            String args = LOCATIONS[randChoice];
            String toSend = String.format(TEMPLATE, botName, args);
            this.getUtil().sendWithRateLimit(toSend, t.getChannel());
        } catch (DiscordException ex) {
            Logger.getLogger(LobbyAwayModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 69;
    }

    @Override
    public String help() {
        return "**LobbyModule**: Reports on the gg2 lobby. `" + this.getFullCommand()
                + "`. Gets one day off a year.\n";
    }

    @Override
    public String getCommand() {
        return "lobby";
    }

}
