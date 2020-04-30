package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.gg2.LobbyData;
import fr.yb.discordybot.gg2.LobbyReader;
import fr.yb.discordybot.gg2.LobbyServerData;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
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
public class LobbyModule extends BotModule {
    
    public static final int MODIFIER_WORST = -10;
    public static final int MODIFIER_NORMAL = 4;
    public static final int MODIFIER_DECREASES_AFTER = 5;
    public static final TemporalAmount COOLDOWN_RESET_TIME = Duration.ofMinutes(5);
    public static final double CHANCE_EXTRA = 0.01;
    public static final int MODIFIER_EXTRA = 30;
    
    public String dataToString(LobbyData datagram) {
        StringBuilder sb = new StringBuilder("```\n");
        
        int nbServers = datagram.getNbServers();
        
        for (LobbyServerData serverDatagram : datagram.getServerData()) {
            
            int serverPort = serverDatagram.getServerPort();
            String serverIP = serverDatagram.getServerIP();
            
            int slots = serverDatagram.getSlots();
            int players = serverDatagram.getPlayers();
            int bots = serverDatagram.getBots();
            
            Map<String, String> infos = serverDatagram.getInfos();
            
            sb.append(String.format("%1$-32s", infos.get("name")));
            sb.append(String.format("%1$-21s", serverIP + ":" + serverPort));
            sb.append(String.format("%1$-8s", players + "/" + slots));
            if (infos.containsKey("game_short")) {
                sb.append(infos.get("game_short")).append(" ");
            }
            if (infos.containsKey("game_ver")) {
                sb.append(infos.get("game_ver")).append(" ");
            }
            if (infos.containsKey("map")) {
                sb.append(infos.get("map")).append(" ");
            }
            sb.append("\n");
        }
        sb.append("```");
        return sb.toString();
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            IMessage message = t.getMessage().getChannel().sendMessage("Requesting lobby...");
            String errMsg = "";
            
            try {
                errMsg = "Something went wrong contacting the lobby!";
                Socket s = LobbyReader.sendLobbyRequest();
                message.edit("Waiting for response from lobby...");
                LobbyData datagram = LobbyReader.readResponse(s);
                String reply = this.dataToString(datagram);
                message.edit("Done! " + reply);
            } catch (IOException ex) {
                message.edit(errMsg + " " + ex.toString());
                Logger.getLogger(LobbyModule.class.getName()).log(Level.WARNING, null, ex);
            }
            
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(LobbyModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public String help() {
        return "**LobbyModule**: Reports on the gg2 lobby. `ybot lobby`\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        String lowerMsg = t.getMessage().getContent().toLowerCase();
        return lowerMsg.startsWith("ybot lobby");
    }

}
