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
            String reply = t.getMessage().getContent().substring((this.getFullCommand()+" ").length());
            t.getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(SayModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String help() {
        return "**SayModule**: Allows the owner to make "+this.getUtil().getName()+" say something. `"+this.getFullCommand()+" something`\n";
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        return this.getBot().getUtil().isMessageFromOwner(t) && super.isInterestedIn(t);
    }

    @Override
    public String getCommand() {
        return "say";
    }

}
