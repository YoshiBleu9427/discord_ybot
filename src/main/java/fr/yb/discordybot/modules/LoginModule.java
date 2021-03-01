/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author Nicolas
 */
public class LoginModule extends BotModule {

    @Override
    public void start() {
        super.start();
        try {
            this.getBot().connect();
            this.getClient().changePresence(StatusType.ONLINE, ActivityType.WATCHING, "you | " + this.getBot().getConfig().getPrefix() + "help");
        } catch (DiscordException | RateLimitException | InterruptedException ex) {
            Logger.getLogger(LoginModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            t.getMessage().getChannel().sendMessage("Bye!");
            this.getBot().disconnect();
            System.exit(0);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(LoginModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void stop() {
        super.stop();
        try {
            this.getBot().disconnect();
        } catch (DiscordException ex) {
            Logger.getLogger(LoginModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String help() {
        return "**LoginModule**: So "+this.getUtil().getName()+" can connect\n";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        return this.getUtil().isMessageFromOwner(t) && super.isInterestedIn(t);
    }

    @Override
    public String getCommand() {
        return "quit";
    }
}
