package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.util.Arrays;
import java.util.List;
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
public class QuestionModule extends BotModule {
    
    public static final List<String> YES = Arrays.asList(
        "Absolutely!",
        "Probably, yeah",
        "Definitely yes",
        "I think so",
        "That would be nice indeed",
        "Yes",
        "Yeah",
        "You'd like that wouldn't you? Yes"
    );
    
    public static final List<String> NO = Arrays.asList(
        "Nnnnnnno",
        "Nah",
        "Nope",
        "Please don't",
        "Please no",
        "I'd rather not",
        "Honestly? No"
    );

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            t.getMessage().getChannel().sendMessage(this.getRandomFact());
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(QuestionModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String getRandomFact() {
        if (BotUtil.random.nextFloat() < 0.05) {
            return "Restarting computer";
        }
        if (BotUtil.random.nextBoolean()) {
            int index = BotUtil.random.nextInt(QuestionModule.YES.size());
            return QuestionModule.YES.get(index);
        }
        int index = BotUtil.random.nextInt(QuestionModule.NO.size());
        return QuestionModule.NO.get(index);
    }

    @Override
    public String help() {
        return "**QuestionModule**: Answers yes/no questions. `"+this.getUtil().getName()+", am I cool?`\n";
    }

    @Override
    public int getPriority() {
        return 88;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        return t.getMessage().getContent().toLowerCase().endsWith("?");
    }

    @Override
    public String getCommand() {
        return "";
    }

}
