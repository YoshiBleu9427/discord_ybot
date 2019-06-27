package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class ChooseModule extends BotModule {
    
    public static final List<String> REPLY_TEMPLATES = Arrays.asList(
        "You should go with {}!",
        "Definitely {}!",
        "Probably {}.",
        "I don't know, maybe {}?",
        "{}!",
        "Let's say {}."
    );
    

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String msg = t.getMessage().getContent();
            List<String> splits = this.tokenize(msg);
            if (splits.isEmpty()) {
                return true;
            }
            String rawReply = this.selectRandomAnswer(splits);
            String reply = this.getBeautifiedReply(rawReply);
            t.getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(ChooseModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private List<String> tokenize(String answer) {
        if (answer.toLowerCase().startsWith("ybot,")) {
            answer = answer.substring("ybot,".length());
        }
        answer = answer.replace("?", "");
        String[] split = answer.split("(?i)( or )|\\,");
        List<String> replies = new ArrayList<>(split.length);
        for (String string : split) {
            string = string.trim();
            if (string.length() > 0) {
                replies.add(string.trim());
            }
        }
        return replies;
    }

    private String selectRandomAnswer(List<String> answers) {
        int index = BotUtil.random.nextInt(answers.size());
        String reply = answers.get(index);
        return reply;
    }

    private String getBeautifiedReply(String answer) {
        int index = BotUtil.random.nextInt(ChooseModule.REPLY_TEMPLATES.size());
        String reply = ChooseModule.REPLY_TEMPLATES.get(index);
        String replace = reply.replace("{}", answer);
        return replace;
    }

    @Override
    public int getPriority() {
        return 52;
    }

    @Override
    public String help() {
        return "**ChooseModule**: Selects a random item in a sentence. `Ybot, a, b or c?`\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        String loMsg = t.getMessage().getContent().toLowerCase();
        
        Pattern pattern = Pattern.compile(",(.*)(( or ))+");
        Matcher matcher = pattern.matcher(loMsg);
        return matcher.find();
    }

}
