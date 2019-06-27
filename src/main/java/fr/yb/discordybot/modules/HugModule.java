package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
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
public class HugModule extends BotModule {
    
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
            // get currents
            int frenLvl = this.getBot().getModel().find(t.getAuthor()).getFren();
            Hug lastHug = this.lastHugs.getOrDefault(t.getAuthor(), new Hug());
            
            // update last hug
            LocalDateTime now = LocalDateTime.now();
            if (lastHug.last.plus(COOLDOWN_RESET_TIME).isBefore(LocalDateTime.now())) {
                lastHug.count = 0;
            }
            lastHug.count ++;
            lastHug.last = now;
            
            int modifier = MODIFIER_NORMAL;
            String response = this.getRandomFrom(REPLIES);
            if (lastHug.count >= MODIFIER_DECREASES_AFTER) {
                // if harassed
                modifier -= lastHug.count - MODIFIER_DECREASES_AFTER;
                if (modifier < MODIFIER_WORST) {
                    modifier = MODIFIER_WORST;
                }
                if (modifier > 0) {
                    response = this.getRandomFrom(HARASS);
                } else {
                    response = this.getRandomFrom(HARASS_BAD);
                }
            } else {
                // else, crit chance
                if (BotUtil.random.nextDouble() <= CHANCE_EXTRA) {
                    modifier = MODIFIER_EXTRA;
                    response = REPLY_EXTRA;
                }
            }
            
            // save and send
            frenLvl += modifier;
            this.getBot().getModel().find(t.getAuthor()).setFren(frenLvl);
            this.lastHugs.put(t.getAuthor(), lastHug);
            t.getMessage().getChannel().sendMessage(response);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(HugModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String getRandomFrom(List<String> l) {
        int index = BotUtil.random.nextInt(l.size());
        return l.get(index);
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public String help() {
        return "**HugModule**: Allows YBot to enjoy !cuddles, !hugs and !brushies.\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        String lowerMsg = t.getMessage().getContent().toLowerCase();
        for (String trigger : TRIGGERS) {
            if (lowerMsg.contains(trigger)) {
                return true;
            }
        }
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        return lowerMsg.contains("i love you");
    }

}
