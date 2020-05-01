package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.Collection;
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
public class HelpModule extends BotModule {

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String msg = t.getMessage().getContent();
            String reply;
            int extraLen = (this.getFullCommand() + " ").length();
            if (msg.length() > extraLen) {
                String rest = msg.substring(extraLen);
                Collection<BotModule> modules = this.getBot().getLoader().getInstances().values();
                reply = "Couldn't find module " + rest;
                for (BotModule m : modules) {
                    String fullClassName = m.getClass().getName();
                    String smolClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
                    if (smolClassName.equalsIgnoreCase(rest)) {
                        reply = m.help();
                    }
                }
            } else {
                Collection<BotModule> modules = this.getBot().getLoader().getInstances().values();
                StringBuilder sb = new StringBuilder("The following modules are active:\n```");
                for (BotModule m : modules) {
                    String fullClassName = m.getClass().getName();
                    String smolClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
                    sb.append(String.format("%1$-32s", smolClassName));
                }
                sb.append("```\nSend `")
                        .append(this.getFullCommand())
                        .append(" <moduleName>` for more detailed help about a module.");
                reply = sb.toString();
            }
            
            t.getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(HelpModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String help() {
        return "**HelpModule**: Shows this help for every module. `"+this.getFullCommand()+"`\n";
    }

    @Override
    public String getCommand() {
        return "help";
    }

}
