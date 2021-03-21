package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.gg2.LobbyData;
import fr.yb.discordybot.gg2.LobbyReader;
import fr.yb.discordybot.gg2.LobbyServerData;
import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
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

    public static final int STYLE_EMBED = 0;
    public static final int STYLE_TEXT = 1;
    public static final int STYLE_DEFAULT = STYLE_TEXT;

    public static EmbedObject dataToEmbed(LobbyData datagram) {
        EmbedObject eo = new EmbedObject();
        int nbPlayers = countPlayers(datagram);
        eo.title = "Gang Garrison 2 Lobby Status";
        eo.description = String.format("There are **%d** players online.", nbPlayers);
        eo.url = "https://www.ganggarrison.com/lobby/status";
        eo.color = Color.decode("#A55420").getRGB() & 0xFFFFFF;
        eo.thumbnail = new EmbedObject.ThumbnailObject();
        eo.thumbnail.url = "https://cdn.discordapp.com/icons/699590084218847282/be805b3d3557a9cc2bf98ae19c5ae27c.webp?size=256";
        eo.fields = dataToEmbedFields(datagram);
        return eo;
    }

    public static EmbedObject.EmbedFieldObject[] dataToEmbedFields(LobbyData datagram) {
        // init embed fields
        EmbedObject.EmbedFieldObject[] fieldObjects = new EmbedObject.EmbedFieldObject[datagram.getServerData().size()];
        for (int i = 0; i < datagram.getServerData().size(); i++) {
            LobbyServerData data = datagram.getServerData().get(i);

            int serverPort = data.getServerPort();
            String serverIP = data.getServerIP();
            int slots = data.getSlots();
            int players = data.getPlayers();
            Map<String, String> infos = data.getInfos();

            StringBuilder sb = new StringBuilder();
            sb.append("  ");
            sb.append(String.format("%1$-8s", players + "/" + slots));
            sb.append("\n  ");
            if (infos.containsKey("map")) {
                sb.append(infos.get("map")).append(" ");
                sb.append("\n  ");
            }
            if (infos.containsKey("game_short") && !infos.get("game_short").isEmpty()) {
                sb.append(infos.get("game_short")).append(" ");
            }
            if (infos.containsKey("game_ver") && !infos.get("game_ver").isEmpty()) {
                sb.append("(").append(infos.get("game_ver")).append(")");
            }
            fieldObjects[i] = new EmbedObject.EmbedFieldObject(
                    infos.get("name"),
                    "```" + sb.toString() + "```",
                    false
            );
        }
        return fieldObjects;
    }

    public static String dataToString(LobbyData datagram) {
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
            sb.append(String.format("%1$-8s", players + "/" + slots));
            if (infos.containsKey("game_short") && !infos.get("game_short").isEmpty()) {
                String fullThing;
                if (infos.containsKey("game_ver") && !infos.get("game_ver").isEmpty()) {
                    fullThing = String.format("%1$-16s", infos.get("game_short") + infos.get("game_ver"));
                } else {
                    fullThing = String.format("%1$-16s", infos.get("game_short"));
                }
                sb.append(fullThing);
                sb.append("\t");
            }
            if (infos.containsKey("map")) {
                sb.append(infos.get("map")).append("\t");
            }
            sb.append("\n");
        }
        sb.append("```");
        return sb.toString();
    }

    private static int countPlayers(LobbyData datagram) {
        int nbPlayers = 0;
        for (LobbyServerData serverDatagram : datagram.getServerData()) {
            nbPlayers += serverDatagram.getPlayers();
        }
        return nbPlayers;
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String errMsg = "";
            String recvMsg = t.getMessage().getContent().toLowerCase();

            // style
            if (recvMsg.contains(" style ")) {
                String styleType = recvMsg.substring(recvMsg.indexOf(" style ") + " style".length()).trim();
                int style = STYLE_DEFAULT;
                String styleStr = "default (plain text)";
                if (styleType.equals("text")) {
                    style = STYLE_TEXT;
                    styleStr = "plain text";
                } else if (styleType.equals("embed")) {
                    style = STYLE_EMBED;
                    styleStr = "embed";
                }
                this.getBot().getModel().putLobbyStylesByGuild(t.getGuild().getLongID(), style);
                this.getUtil().sendWithRateLimit("Updated lobby style to " + styleStr, t.getChannel());
                return false;
            }

            // default
            IMessage message = this.getUtil().sendWithRateLimit("Requesting lobby...", t.getChannel());
            try {
                errMsg = "Something went wrong contacting the lobby!";
                Socket s = LobbyReader.sendLobbyRequest();
                this.getUtil().editWithRateLimit("Waiting for response from lobby...", message);
                LobbyData datagram = LobbyReader.readResponse(s);
                String reply = this.dataToString(datagram);
                int nbPlayers = this.countPlayers(datagram);
                if (recvMsg.contains("count")) {
                    this.getUtil().editWithRateLimit(String.format("Done! There are **%d** players online.", nbPlayers, reply), message);
                } else {
                    int style = this.getBot().getModel().getLobbyStylesByGuild().getOrDefault(t.getGuild().getLongID(), STYLE_DEFAULT);
                    if (style == STYLE_EMBED) {
                        EmbedObject eo = dataToEmbed(datagram);
                        this.getUtil().editWithRateLimit(eo, message);
                    } else { // it's text
                        String eo = this.dataToString(datagram);
                        this.getUtil().editWithRateLimit(String.format("Done! There are **%d** players online. %s", nbPlayers, eo), message);
                    }
                }
            } catch (IOException ex) {
                this.getUtil().editWithRateLimit(errMsg + " " + ex.toString(), message);
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
        return "**LobbyModule**: Reports on the gg2 lobby. `" + this.getFullCommand()
                + "` or `" + this.getFullCommand() + " count`. "
                + "Change format with `" + this.getFullCommand() + " style text` or "
                + "`" + this.getFullCommand() + " style embed`.\n";
    }

    @Override
    public String getCommand() {
        return "lobby";
    }

}
