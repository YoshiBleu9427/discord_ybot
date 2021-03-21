package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.gg2.LobbyData;
import fr.yb.discordybot.gg2.LobbyReader;
import fr.yb.discordybot.gg2.LobbyServerData;
import fr.yb.discordybot.model.LobbySubModel;
import fr.yb.discordybot.model.LobbyUpdatableSub;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
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
public class LobbySubscriptionModule extends BotModule {
    
    private int previousPlayerCount = 0;
    private Timer timer;
    
    public static final String COMMAND_ROOT = "sub";
    public static final String COMMAND_CHANNEL_ROOT = "subchan";
    public static final String COMMAND_UPDATABLE_ROOT = "sub autoupdate";
    public static final String COMMAND_ADMIN_ROOT = "sub admin";
    
    public static final String COMMAND_ADD = COMMAND_ROOT;
    public static final String COMMAND_REMOVE = COMMAND_ROOT + " remove";
    public static final String COMMAND_LIST = COMMAND_ROOT + " show";
    public static final String COMMAND_COOLDOWN = COMMAND_ROOT + " cool";
    
    public static final String COMMAND_ADD_CHANNEL = COMMAND_CHANNEL_ROOT;
    public static final String COMMAND_REMOVE_CHANNEL = COMMAND_CHANNEL_ROOT + " remove";
    public static final String COMMAND_LIST_CHANNEL = COMMAND_CHANNEL_ROOT + " show";
    public static final String COMMAND_COOLDOWN_CHANNEL = COMMAND_CHANNEL_ROOT + " cool";
    
    public static final String COMMAND_ADD_UPDATABLE = COMMAND_UPDATABLE_ROOT;
    public static final String COMMAND_REMOVE_UPDATABLE = COMMAND_UPDATABLE_ROOT + " remove";
    public static final String COMMAND_LIST_UPDATABLE = COMMAND_UPDATABLE_ROOT + " show";
    
    public static final String COMMAND_STATUS = COMMAND_ADMIN_ROOT + " status";
    public static final String COMMAND_START = COMMAND_ADMIN_ROOT + " start";
    public static final String COMMAND_STOP = COMMAND_ADMIN_ROOT + " stop";
    
    public static final String NOTIFY_TEXT = "Gang Garrison 2 is alive with **%d** players online!";
    
    public class LobbyTask extends TimerTask {
    
        protected LobbySubscriptionModule module;

        public LobbyTask(LobbySubscriptionModule module) {
            this.module = module;
        }

        @Override
        public void run() {
            try {
                this.module.updateCountFromLobby();
            } catch (IOException ex) {
                Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void stop() {
        this.stopRequests();
        super.stop();
    }

    @Override
    public void start() {
        super.start();
        this.startRequests();
    }
    
    
    
    public void startRequests() {
        if (this.timer != null) {
            return;
        }
        this.timer = new Timer();
        this.timer.schedule(new LobbyTask(this), 0, 30 * 1000);
    }
    
    public void stopRequests() {
        if (this.timer != null) {        
            this.timer.cancel();
            this.timer = null;
        }
    }
    
    private String urlToMessage(long msgId, MessageReceivedEvent t) {
        return String.format(
                "<https://discord.com/channels/%d/%d/%d>",
                t.getGuild().getLongID(),
                t.getChannel().getLongID(),
                msgId
        );
    }
    
    public void updateCountFromLobby() throws IOException {
        Socket s = LobbyReader.sendLobbyRequest();
        LobbyData datagram = LobbyReader.readResponse(s);
        int nbPlayers = 0;
        for (LobbyServerData serverDatagram : datagram.getServerData()) {
            nbPlayers += serverDatagram.getPlayers();
        }
        
        
        final int prevNb = this.previousPlayerCount;
        final int currNb = nbPlayers;
        Date now = new Date();
        this.getBot().getModel().getLobbySubscriptions().forEach((k,v) -> {
            try {
                // cooldown
                Instant cooldownOver = v.getLastPost().toInstant().plusSeconds(v.getCooldown().getSeconds());
                if (now.toInstant().isBefore(cooldownOver)) {
                    return;
                }
                // if previously was under threshold, but now is above, notify
                if ((prevNb < v.getLevel()) && (currNb > v.getLevel())) {
                    long longID = Long.parseLong(v.getId());
                    String notification = String.format(NOTIFY_TEXT, currNb);
                    if (v.isChannel()) {
                        IChannel chan = this.getBot().getClient().getChannelByID(longID);
                        if (chan != null) {
                            this.getUtil().sendWithRateLimit(notification, chan);
                            v.setLastPost(now);
                        } else {
                            Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.WARNING, "Unknown channel ID {0}", longID);
                        }
                    } else {
                        IUser target = this.getBot().getClient().getUserByID(longID);
                        if (target != null) {
                            IChannel chan = target.getOrCreatePMChannel();
                            if (chan != null) {
                                this.getUtil().sendWithRateLimit(notification, chan);
                                v.setLastPost(now);
                            } else {
                                Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.WARNING, "Cannot get channel for user ID {0}", longID);
                            }
                        } else {
                            Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.WARNING, "Unknown user ID {0}", longID);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.SEVERE, "Uncaught exception in the lobby subscription loop", ex);
            }
        });
        
        this.getBot().getModel().getLobbyUpdatableSubscriptions().forEach((chanID, sub) -> {
            try {
                IChannel chan = this.getClient().getChannelByID(chanID);
                if (chan == null) {
                    throw new NullPointerException("getChannelByID " + chanID + " failed");
                }
                IMessage msg = chan.fetchMessage(sub.getMsgId());
                if (msg == null) {
                    throw new NullPointerException("fetchMessage " + sub.getMsgId() + " failed");
                }
                int style = this.getBot().getModel().getLobbyStylesByGuild().getOrDefault(msg.getGuild().getLongID(), LobbyModule.STYLE_DEFAULT);
                if (style == LobbyModule.STYLE_EMBED) {
                    EmbedObject eo = LobbyModule.dataToEmbed(datagram);
                    eo.timestamp = DateTimeFormatter.ISO_INSTANT.format(now.toInstant());
                    this.getUtil().editWithRateLimit(eo, msg);
                } else { // text
                    String eo = LobbyModule.dataToString(datagram);
                    this.getUtil().editWithRateLimit(eo, msg);
                }
            } catch (RateLimitException ex) {
                Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.SEVERE, "RateLimitException in the lobby AUTOUPDATE subscription loop: " + ex.getMethod() + ", " + ex.getRetryDelay(), ex);
            } catch (Exception ex) {
                Logger.getLogger(LobbySubscriptionModule.class.getName()).log(Level.SEVERE, "Uncaught exception in the lobby AUTOUPDATE subscription loop", ex);
            }
        });
        
        this.previousPlayerCount = nbPlayers;
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        String cmd = t.getMessage().getContent().toLowerCase();
        String reply = this.help();
        
        // do the chans first because they're longer
        if (cmd.contains(COMMAND_ADMIN_ROOT)) {
            if (this.getUtil().isMessageFromOwner(t)) {
                if (cmd.contains(COMMAND_STATUS)) {
                    if (this.timer == null) {
                        reply = "Timer null, must have stopped";
                    } else {
                        reply = "Timer running";
                    }
                } else if (cmd.contains(COMMAND_START)) {
                    this.startRequests();
                    reply = "Restarted timer";
                } else if (cmd.contains(COMMAND_STOP)) {
                    this.stopRequests();
                    reply = "Stopped timer";
                }
            }
        }
        
        // autoupdates
        else if (cmd.contains(COMMAND_UPDATABLE_ROOT)) {
            // TODO fix permissions (issue with Discord4J?)
            boolean isOwnerOrMe = (
                this.getUtil().isMessageFromOwner(t)
                    || t.getGuild().getOwner().getLongID() == t.getAuthor().getLongID()
            );
            if (!isOwnerOrMe) {
                reply = "You need to be the server owner or bot owner to run this command!";
                } else {
                if (cmd.contains(COMMAND_LIST_UPDATABLE)) {
                    LobbyUpdatableSub sub = this.getBot().getModel().getLobbyUpdatableSubscriptions().get(t.getChannel().getLongID());
                    if (sub == null) {
                        reply = "No autoupdate message in this channel";
                    } else {
                        String url = this.urlToMessage(sub.getMsgId(), t);
                        reply = "An autoupdating status message exists in this channel: " + url;
                    }
                } else if (cmd.contains(COMMAND_REMOVE_UPDATABLE)) {
                    LobbyUpdatableSub sub = this.getBot().getModel().getLobbyUpdatableSubscriptions().get(t.getChannel().getLongID());
                    if (sub == null) {
                        reply = "No autoupdate message in this channel";
                    } else {
                        this.getBot().getModel().getLobbyUpdatableSubscriptions().remove(t.getChannel().getLongID());
                        String url = this.urlToMessage(sub.getMsgId(), t);
                        reply = "The autoupdate message in this channel (" + url + ") has been frozen and will no longer be edited.";
                    }
                } else if (cmd.contains(COMMAND_ADD_UPDATABLE)) {
                    LobbyUpdatableSub sub = this.getBot().getModel().getLobbyUpdatableSubscriptions().get(t.getChannel().getLongID());
                    if (sub == null) {
                        reply = "This message will update every 30 seconds with the freshest gg2 lobby status data!";
                        IMessage sentMsg = this.getUtil().sendWithRateLimit(reply, t.getChannel());
                        this.getBot().getModel().putLobbyUpdatableSubscription(
                                new LobbyUpdatableSub(
                                        sentMsg.getLongID(),
                                        t.getChannel().getLongID()
                                )
                        );
                        return false;
                    } else {
                        String url = this.urlToMessage(sub.getMsgId(), t);
                        reply = "An autoupdating status message exists in this channel: " + url;
                    }
                }
            }
        }
        
        // do the chans first because they're longer
        else if (cmd.contains(COMMAND_CHANNEL_ROOT)) {
            //if (!t.getAuthor().getPermissionsForGuild(t.getGuild()).contains(Permissions.ADMINISTRATOR)) {
            // TODO fix permissions (issue with Discord4J?)
            boolean isOwnerOrMe = (
                this.getUtil().isMessageFromOwner(t)
                    || t.getGuild().getOwner().getLongID() == t.getAuthor().getLongID()
            );
            if (!isOwnerOrMe) {
                reply = "You need to be the server owner or bot owner to run this command!";
            } else {
                final String chanID = String.valueOf(t.getChannel().getLongID());
                if (cmd.contains(COMMAND_LIST_CHANNEL)) {
                    reply = this.commandList(chanID, cmd, true);
                }
                else if (cmd.contains(COMMAND_REMOVE_CHANNEL)) {
                    reply = this.commandRemove(chanID, cmd, true);
                }
                else if (cmd.contains(COMMAND_COOLDOWN_CHANNEL)) {
                    reply = this.commandCooldown(chanID, cmd, true);
                }
                else if (cmd.contains(COMMAND_ADD_CHANNEL)) {
                    reply = this.commandAdd(chanID, cmd, true);
                }
            }
        }
        // other commands
        else if (cmd.contains(COMMAND_ROOT)) {
            final String authorID = String.valueOf(t.getAuthor().getLongID());
            // TODO do the things
            if (cmd.contains(COMMAND_LIST)) {
                reply = this.commandList(authorID, cmd, false);
            }
            else if (cmd.contains(COMMAND_REMOVE)) {
                reply = this.commandRemove(authorID, cmd, false);
            }
            else if (cmd.contains(COMMAND_COOLDOWN)) {
                reply = this.commandCooldown(authorID, cmd, false);
            }
            else if (cmd.contains(COMMAND_ADD)) {
                reply = this.commandAdd(authorID, cmd, false);
            }
        }
        
        this.getUtil().sendWithRateLimit(reply, t.getChannel());
        return false;
    }
    
    private String commandAdd(String longID, String cmd, boolean isChan) {
        String goodCommand = (isChan ? COMMAND_ADD_CHANNEL : COMMAND_ADD);
        int substringStart = cmd.indexOf(goodCommand) + goodCommand.length() + 1;
        if (substringStart > cmd.length()) {
            return this.help();
        }
        
        String secondPart = cmd.substring(substringStart);
        int value;
        try {
            value = Integer.parseUnsignedInt(secondPart);
        } catch(NumberFormatException ex) {
            return this.help();
        }

        LobbySubModel mod = new LobbySubModel(longID, value, isChan);
        this.getBot().getModel().putLobbySubscription(mod);
        
        if (isChan) {
            return String.format(
                "Subscribed this channel to gg2 lobby. I will send a message in this channel when there are at least %d people online on GG2! Cancel this with `%s`",
                value,
                COMMAND_REMOVE_CHANNEL
            );
        } else {
            return String.format(
                "You subscribed to gg2 lobby. I will send you a DM when there are at least %d people online on GG2! Cancel this with `%s`",
                value,
                COMMAND_REMOVE
            );
        }
    }
    
    private String commandRemove(String longID, String cmd, boolean isChan) {
        this.getBot().getModel().getLobbySubscriptions().remove(longID);
        if (isChan) {
            return "This channel will no longer receive messages about gg2 players.";
        } else {
            return "You will no longer receive messages about gg2 players.";
        }
        
    }
    
    private String commandList(String longID, String cmd, boolean isChan) {
        LobbySubModel existingSub = this.getBot().getModel().getLobbySubscriptions().get(longID);
        if (existingSub == null) {
            return "You did not subscribe to the gg2lobby. Use `"+this.getFullCommand()+" <nbPlayers>` to get messages when there are at least <nbPlayers> online on gg2!";
        } else {
            return String.format(
                "You will receive messages when there are at least %d players on gg2. Cooldown is %d hours %d minutes.",
                existingSub.getLevel(),
                existingSub.getCooldown().getSeconds() / 3600,
                (existingSub.getCooldown().getSeconds() / 60) % 60
            );
        }
    }
    
    private String commandCooldown(String longID, String cmd, boolean isChan) {
        String goodCommand = (isChan ? COMMAND_COOLDOWN_CHANNEL : COMMAND_COOLDOWN);
        LobbySubModel mod = this.getBot().getModel().getLobbySubscriptions().get(longID);
        try {
            String secondPart = cmd.substring(cmd.indexOf(goodCommand) + goodCommand.length() + 1);
            // make sure a subscription exists
            if (mod == null) {
                return String.format(
                    "%s not have a gg2lobby subscription set. Please run `%s` before updating cooldown settings.",
                    (isChan ? "This channel does" : "You do"),
                    this.getBot().getConfig().getPrefix() + goodCommand
                );
            } else {
                // all good, do the thing, start parsing
                boolean isHours = false;
                Character lastChar = secondPart.toLowerCase().charAt(secondPart.length()-1);
                if (Character.isAlphabetic(lastChar)) {
                    switch (lastChar) {
                        case 'h':
                            isHours = true;
                            break;
                        case 'm':
                            break;
                        default:
                            throw new NumberFormatException();
                    }
                    secondPart = secondPart.substring(0, secondPart.length() - 1);
                }
                int value = Integer.parseUnsignedInt(secondPart);

                // hours offset
                if (isHours) {
                    mod.setCooldown(Duration.ofHours(value));
                } else {
                    mod.setCooldown(Duration.ofMinutes(value));
                }

                // update
                this.getBot().getModel().putLobbySubscription(mod);

                return String.format(
                    "Set notification cooldown for #%s to %d %s!",
                    (isChan ? "this channel" : "you"),
                    value,
                    (isHours ? "hours" : "minutes")
                );
            }

        } catch(NumberFormatException | StringIndexOutOfBoundsException ex) {
            return this.help();
        }
    }

    @Override
    public String help() {
        return "**LobbySubscriptionModule**: Makes "+this.getUtil().getName()+" notify you when there are "
                + "at least X people playing Gang Garrison 2. Commands: `"
                + COMMAND_ADD + " <number>`, `" + COMMAND_LIST + "`, `" + COMMAND_REMOVE
                + "`. Comes with a default cooldown of 60 minutes, change it with `" + COMMAND_COOLDOWN
                + "`. Add an extra `chan` to the command to notify a channel "
                + "instead of getting a DM, if you're a server admin (ie. `"
                + COMMAND_ADD_CHANNEL + " <number>`). Create an autoupdating status with `" + COMMAND_ADD_UPDATABLE
                + "`, cancel with `" + COMMAND_REMOVE_UPDATABLE + ".`\n";
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public String getCommand() {
        return COMMAND_ROOT;
    }
}
