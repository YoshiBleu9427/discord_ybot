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

/**
 *
 * @author Nicolas
 */
public class LogModule extends BotModule {

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            boolean isDm = this.getUtil().isDM(t);
            String src;
            if (isDm) {
                src = "[Direct Message] ";
            } else {
                src = "[From Server " + t.getGuild().getName() + "#" + t.getChannel().getName() + "] ";
            }
            String author = "<";
            if (t.getAuthor() != null) {
                if (isDm) {
                    author += t.getAuthor().getName();
                } else {
                    author += t.getAuthor().getDisplayName(t.getGuild());
                }
            } else {
                author += "noone";
            }
            author += "> ";
            System.out.println(src + author + t.getMessage().getContent());
        } catch (Exception ex) {
            Logger.getLogger(LogModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public String help() {
        return "";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        return true;
    }
}
