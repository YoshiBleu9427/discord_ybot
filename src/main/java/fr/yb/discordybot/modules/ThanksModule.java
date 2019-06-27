package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.util.Arrays;
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
public class ThanksModule extends BotModule {
    
    public static final List<String> REPLIES = Arrays.asList(
        "You're welcome",
        "You're welcome!",
        "Your whale cum!",
        "Glad to help!",
        "Always here to help!"
    );

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            if (t.getGuild() != null) {
                IEmoji whalecum = t.getGuild().getEmojiByName("whalecum");
                if (whalecum != null) {
                    if (BotUtil.random.nextDouble() < 0.2) {
                        t.getMessage().getChannel().sendMessage("<:"+whalecum.getName()+":"+whalecum.getStringID()+">");
                        return false;
                    }
                }
            }
            t.getMessage().getChannel().sendMessage(this.getRandom());
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(ThanksModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String getRandom() {
        int index = BotUtil.random.nextInt(ThanksModule.REPLIES.size());
        return ThanksModule.REPLIES.get(index);
    }

    @Override
    public String help() {
        return "**ThanksModule**: Makes YBot acknowledge thank yous\n";
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        String lowerMsg = t.getMessage().getContent().toLowerCase();
        return (lowerMsg.contains("cheers") || lowerMsg.contains("thanks") || lowerMsg.contains("thank you"));
    }

}
