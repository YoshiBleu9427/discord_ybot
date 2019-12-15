package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
public class SayModule extends BotModule {

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String reply = t.getMessage().getContent().substring("ybot say ".length());
            t.getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(SayModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String help() {
        return "**SayModule**: Takes control of YBot's mouth and makes it say whatever you say. Except it only works with the owner <3\n";
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getBot().getUtil().isMessageFromOwner(t)) {
            return false;
        }
        String lowerMsg = t.getMessage().getContent().toLowerCase();
        if (lowerMsg.startsWith("ybot say ")) {
            return true;
        }
        return false;
    }

}
