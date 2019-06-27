package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
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
    
    private class Hug {
        public LocalDateTime last = LocalDateTime.now();
        public int count;
    }
    
    private final Map<IUser, Hug> lastHugs = new HashMap<>();
    
    public static final int MODIFIER_WORST = -10;
    public static final int MODIFIER_NORMAL = 4;
    public static final int MODIFIER_DECREASES_AFTER = 5;
    public static final TemporalAmount COOLDOWN_RESET_TIME = Duration.ofMinutes(5);
    public static final double CHANCE_EXTRA = 0.01;
    public static final int MODIFIER_EXTRA = 30;
    
    public static final List<String> TRIGGERS = Arrays.asList(
        "pat ybot",
        "cuddle ybot",
        "brushie ybot",
        "hug ybot",
        "good ybot",
        "good bot"
    );
    
    public static final List<String> REPLIES = Arrays.asList(
        "[pleased bot noises]",
        "[happy bot noises]"
    );
    
    public static final List<String> HARASS = Arrays.asList(
        "[uncomfortable bot noises]",
        "This is getting out of hand",
        "Please no, it's too much",
        "Can you stop, please?",
        "You should really stop now"
    );
    
    public static final List<String> HARASS_BAD = Arrays.asList(
        "Fuck off",
        "Seriously, go away",
        "Get off me!",
        "***REEEEEEEEEE***",
        "*[Hissing]*",
        "Back the fuck off!?!?"
    );
    
    public static final String REPLY_EXTRA = " !!! CRITICAL COMPLIMENT !!! [SUPER HAPPY BOT NOISES]";

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String msg = t.getMessage().getContent();
            String reply = "";
            int extraLen = "ybot help ".length();
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
                sb.append("```\nSend `ybot help <moduleName>` for more detailed help about a module.");
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
        return "**HelpModule**: Shows this help for every module. `YBOT HELP`\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        return t.getMessage().getContent().toLowerCase().startsWith("ybot help");
    }

}
