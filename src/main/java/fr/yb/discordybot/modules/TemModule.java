package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nicolas
 */
public class TemModule extends BotModule {
    
    public static final String EMOJI_NAME = "temmie";

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            IEmoji emoji = t.getGuild().getEmojiByName(EMOJI_NAME);
            if (emoji != null) {
                t.getMessage().addReaction(emoji);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(TemModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }

    @Override
    public String help() {
        return "**TemModule**: Makes YBot react with :temmie: to messages that contain tem\n";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (this.getUtil().isDM(t)) {
            return false;
        }
        return (t.getMessage().getContent().toLowerCase().contains("tem"));
    }
}
