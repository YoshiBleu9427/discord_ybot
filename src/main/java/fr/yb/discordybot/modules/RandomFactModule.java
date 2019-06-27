package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
public class RandomFactModule extends BotModule {
    
    public static final String FILE_PATH = "./facts.txt";
    public static final List<String> FACTS = new ArrayList<>();

    @Override
    public void start() {
        super.start();
        try (FileReader fr = new FileReader(FILE_PATH)) {
            BufferedReader br = new BufferedReader(fr);
            String nuLine = br.readLine();
            while (nuLine != null) {
                FACTS.add(nuLine);
                nuLine = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RandomFactModule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RandomFactModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean add(String fact) {
        return FACTS.add(fact);
    }
    
    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            t.getMessage().getChannel().sendMessage(this.getRandomFact());
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(RandomFactModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String getRandomFact() {
        int index = BotUtil.random.nextInt(RandomFactModule.FACTS.size());
        return RandomFactModule.FACTS.get(index);
    }

    @Override
    public String help() {
        return "**RandomFactModule**: Makes YBot interesting. `YBot, say something nice!` or `ybot fact`\n";
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
        return (lowerMsg.contains("say something") || lowerMsg.contains("fact"));
    }

}
