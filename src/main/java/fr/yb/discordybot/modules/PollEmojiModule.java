package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
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
public class PollEmojiModule extends BotModule {

    public static final String[] EMOJI_NAMES = {
        "white_check_mark",
        "x"
    };

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            for (String emojiName : EMOJI_NAMES) {
                t.getMessage().addReaction(":" + emojiName + ":"); // TODO find how to get a list of IEmoji for default emojis
                // wait 500ms to prevent getting rate limited (delay time could be tuned)
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PollEmojiModule.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(PollEmojiModule.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @Override
    public String help() {
        return "**PollEmojiModule**: If your message contains `[S]` or starts with `Poll:`"
                + ", makes ybot react with poll emojis (:white_check_mark: and :x:) \n";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (this.getUtil().isDM(t)) {
            return false;
        }
        if (t.getMessage().getContent().toLowerCase().startsWith("poll:")) {
            return true;
        }
        return (t.getMessage().getContent().contains("[S]"));
    }
}
