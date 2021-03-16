/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author Nicolas
 */
public class BotUtil {
    
    private final Bot bot;
    private String cachedName;
    public static final Random random = new Random();

    public BotUtil(Bot bot) {
        this.bot = bot;
    }
    
    public class MultipleResultException extends Exception {

        public MultipleResultException() {
        }

        public MultipleResultException(String message) {
            super(message);
        }

        public MultipleResultException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    public String getName() {
        if (this.cachedName == null) {
            this.cachedName = this.bot.getClient().getOurUser().getName();
        }
        return this.cachedName;
    }
    
    public boolean isDM(MessageReceivedEvent t) {
        return (t.getGuild() == null);
    }
    
    public boolean isMessageForMe(MessageReceivedEvent t) {
        if (isDM(t)) {
            return true;
        }
        if (t.getMessage().getMentions().contains(this.bot.getClient().getOurUser())) {
            return true;
        }
        return (t.getMessage().getContent().toLowerCase().contains(this.getName().toLowerCase()));
    }
    
    public boolean isMessageFromOwner(MessageReceivedEvent t) {
        return t.getAuthor().getStringID().equals(this.bot.getConfig().getOwnerID());
    }
    
    public String getLocalUsername(IUser u, MessageReceivedEvent t) {
        String username = null;
        if (!isDM(t)) {
            username = u.getNicknameForGuild(t.getGuild());
        }
        if (username == null) {
            username = u.getName();
        }
        return username;
    }
    
    public IUser getUserByNameInGuild(String name, MessageReceivedEvent t) throws MultipleResultException {
        // try without nick first, then with
        // if multiple results possible with one method, return multiple exception
        for (boolean withNicks : Arrays.asList(false, true)) {
            List<IUser> users = t.getGuild().getUsersByName(name, withNicks);
            if (users.isEmpty()) {
                continue;
            }
            if (users.size() > 1) {
                String concatNames = "";
                for (IUser u : users) {
                    if (concatNames.isEmpty()) {
                        concatNames += getLocalUsername(u, t);
                    } else {
                        concatNames += ", " + getLocalUsername(u, t);
                    }
                }
                throw new MultipleResultException(concatNames);
            }
            return users.get(0);
        }
        return null;
    }
    
    public IMessage sendWithRateLimit(String msg, IChannel chan) {
        while (true) {
            try {
                return chan.sendMessage(msg);
            } catch (RateLimitException ex) {
                try {
                    Thread.sleep(ex.getRetryDelay());
                } catch (InterruptedException ex1) {
                    Logger.getLogger(BotUtil.class.getName()).log(Level.SEVERE, null, ex1);
                    return null;
                }
            }
        }
    }
    
    public IMessage sendWithRateLimit(EmbedObject msg, IChannel chan) {
        while (true) {
            try {
                return chan.sendMessage(msg);
            } catch (RateLimitException ex) {
                try {
                    Thread.sleep(ex.getRetryDelay());
                } catch (InterruptedException ex1) {
                    Logger.getLogger(BotUtil.class.getName()).log(Level.SEVERE, null, ex1);
                    return null;
                }
            }
        }
    }
    
    public IMessage editWithRateLimit(String msg, IMessage message) {
        while (true) {
            try {
                return message.edit(msg);
            } catch (RateLimitException ex) {
                try {
                    Thread.sleep(ex.getRetryDelay());
                } catch (InterruptedException ex1) {
                    Logger.getLogger(BotUtil.class.getName()).log(Level.SEVERE, null, ex1);
                    return null;
                }
            }
        }
    }
    
    public IMessage editWithRateLimit(EmbedObject msg, IMessage message) {
        while (true) {
            try {
                return message.edit(msg);
            } catch (RateLimitException ex) {
                try {
                    Thread.sleep(ex.getRetryDelay());
                } catch (InterruptedException ex1) {
                    Logger.getLogger(BotUtil.class.getName()).log(Level.SEVERE, null, ex1);
                    return null;
                }
            }
        }
    }
}

