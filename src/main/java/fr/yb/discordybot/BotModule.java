/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 *
 * @author Nicolas
 */
public abstract class BotModule {
    
    private Bot bot;
    private boolean isActive = true;

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public IDiscordClient getClient() {
        return this.bot.getClient();
    }
    
    public boolean isActive() {
        return this.isActive;
    }
    
    public void start() {
        this.isActive = true;
    }
    
    public void stop() {
        this.isActive = false;
    }
    
    public BotUtil getUtil() {
        return this.getBot().getUtil();
    }
    
    public abstract int getPriority();
    public abstract boolean isInterestedIn(MessageReceivedEvent t);
    public abstract String help();
    public abstract boolean handle(MessageReceivedEvent t);

}
