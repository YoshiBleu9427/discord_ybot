package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import fr.yb.discordybot.model.UserModel;
import java.util.Arrays;
import java.util.List;
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
public class EveryoneShunModule extends BotModule {

    public EveryoneShunModule() {
    }
    
    public static final int MODIFIER_EVERYONE = -50;
    public static final int MODIFIER_HERE = -10;
    
    public static final List<String> REACTIONS_EVERYONE = Arrays.asList(
            "Jesus fucking christ {name} you can fuck right off with that",
            "Oh you think you're so smart mentioning everyone, huh? How about you go fuck yourself with a keg",
            "Eat shit and die {name}",
            "Go fuck yourself {name}"
    );
    
    public static final List<String> REACTIONS_HERE = Arrays.asList(
            "Fuck off {name}",
            "Don't do that {name}. Seriously.",
            "Eat shit and die {name}",
            "Go fuck yourself {name}"
    );

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        return t.getMessage().mentionsEveryone() || t.getMessage().mentionsHere();
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            IUser author = t.getMessage().getAuthor();
            UserModel authorData = this.getBot().getModel().find(author);
            String reply = "Huh. Something went wrong with the EveryoneShunModule";
            int frensLvl = authorData.getFren();
            if (t.getMessage().mentionsHere()) {
                frensLvl += MODIFIER_HERE;
                reply = this.getMessageFor(author, REACTIONS_HERE);
            }
            if (t.getMessage().mentionsEveryone()) {
                frensLvl += MODIFIER_EVERYONE;
                reply = this.getMessageFor(author, REACTIONS_EVERYONE);
            }
            t.getMessage().getChannel().sendMessage(reply);
            authorData.setFren(frensLvl);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(EveryoneShunModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String help() {
        return "**EveryoneShunModule**: Removes a lot of friendship with people who (at)everyone\n";
    }

    /**
     * 
     * @param author
     * @return 
     */
    private String getMessageFor(IUser author, List<String> inList) {
        String authorId = author.getStringID();
        String authorName = author.getName();
        String reply = inList.get(BotUtil.random.nextInt(inList.size()));
        return reply.replace("{name}", authorName);
    }

    @Override
    public String getCommand() {
        return "";
    }

}
