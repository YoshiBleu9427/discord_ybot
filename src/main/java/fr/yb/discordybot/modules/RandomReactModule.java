package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IEmoji;
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
public class RandomReactModule extends BotModule {
    
    private int howManyLeft = 0;

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            this.howManyLeft --;
            if (this.howManyLeft > 0) {
                return true;
            }
            this.howManyLeft = 18 + BotUtil.random.nextInt(17);
            List<IEmoji> emojis = t.getGuild().getEmojis();
            int index = BotUtil.random.nextInt(emojis.size());
            IEmoji em = emojis.get(index);
            if (em != null) {
                t.getMessage().addReaction(em);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(RandomReactModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }

    @Override
    public String help() {
        return "**RandomReactModule**: Makes "+this.getUtil().getName()+" add random reactions to random messages, usually in hilariously obnoxious ways\n";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        return true;
    }

    @Override
    public String getCommand() {
        return "";
    }
}
