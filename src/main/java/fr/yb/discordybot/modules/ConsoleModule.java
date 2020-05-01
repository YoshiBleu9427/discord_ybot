/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.Launcher;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 *
 * @author Nicolas
 */
public class ConsoleModule extends BotModule {

    public boolean handle(MessageReceivedEvent t) {
        try {
            String cmd = t.getMessage().getContent().substring(5);
            Launcher.module(cmd, this.getBot());
        } catch (Exception ex) {
            Logger.getLogger(ConsoleModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String help() {
        return "";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (this.getUtil().isMessageFromOwner(t) && super.isInterestedIn(t)) {
            return true;
        }
        return false;
    }

    @Override
    public String getCommand() {
        return "module";
    }
}
