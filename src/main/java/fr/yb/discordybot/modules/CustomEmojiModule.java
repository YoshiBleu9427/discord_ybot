/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author Nicolas
 */
public class CustomEmojiModule extends BotModule {
    
    public static final Pattern REGEX_REACT = Pattern.compile("(.*) react with (?<name>.*)");
    public static final Pattern REGEX_POST = Pattern.compile("(.*) post emoji (?<name>.*)");
    public static final Pattern REGEX_DECOMPOSE = Pattern.compile(":?(?<name>.*):(?<id>\\d*)");
    public static final Pattern REGEX_SET = Pattern.compile("(.*) get emojis from (?<name>.*)");
    
    public static final String SET_SOURCE = "set emoji source here";
    
    private IGuild preferredGuildEmojis = null;

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            ReactionEmoji emoji = null;
            String msg = t.getMessage().getContent();
            Matcher post = REGEX_POST.matcher(msg);
            if (msg.toLowerCase().endsWith(SET_SOURCE)) {
                this.preferredGuildEmojis = t.getGuild();
                t.getChannel().sendMessage("Will get emojis by name from this server!");
                return false;
            }
            if (post.matches()) {
                emoji = this.getEmoji(post.group("name"), t.getGuild());
                t.getChannel().sendMessage("<:" + emoji.getName() + ":" + emoji.getStringID() + ">");
            }
            Matcher react = REGEX_REACT.matcher(msg);
            if (react.matches()) {
                MessageHistory history = t.getChannel().getMessageHistory(2);
                IMessage prevMsg = history.get(1);
                emoji = this.getEmoji(react.group("name"), t.getGuild());
                prevMsg.addReaction(emoji);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(TemModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private ReactionEmoji getEmoji(String emojiPart, IGuild msgGuild) {
        Logger.getLogger(TemModule.class.getName()).log(Level.INFO, emojiPart);
        Matcher decompose = REGEX_DECOMPOSE.matcher(emojiPart);
        if (decompose.matches()) {
            ReactionEmoji result = ReactionEmoji.of(decompose.group("name"), Long.parseLong(decompose.group("id")));
            if (result != null) {
                return result;
            }
        }
        if (this.preferredGuildEmojis != null) {
            IEmoji result = this.preferredGuildEmojis.getEmojiByName(emojiPart);
            if (result != null) {
                return ReactionEmoji.of(result);
            }
        }
        IEmoji result = msgGuild.getEmojiByName(emojiPart);
        if (result != null) {
            return ReactionEmoji.of(result);
        }
        return ReactionEmoji.of(emojiPart);
    }

    @Override
    public String help() {
        return "**CustomEmojiModule**: Gives "+this.getUtil().getName()+" access to emojis from other servers, for reacting or posting. "
                + "`" + this.getBot().getConfig().getPrefix() + " post emoji emojiname[:emojiid]`, "
                + "`" + this.getBot().getConfig().getPrefix() + " react with emojiname[:emojiid]`\n";
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        String preftrim = (this.getBot().getConfig().getPrefix() + " ").trim();
        String msgtrim = t.getMessage().getContent().toLowerCase().trim();
        return (msgtrim.startsWith(preftrim + " react with ") || msgtrim.startsWith(preftrim + " post emoji ") || msgtrim.startsWith(preftrim + " " + SET_SOURCE));
    }

    @Override
    public String getCommand() {
        return "";
    }
}
