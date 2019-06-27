package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
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
public class ProfileModule extends BotModule {
    
    public static final String UPDATE_PROFILE_PIC = "ybot update profile pic";
    public static final String STEAL_PROFILE_PIC = "ybot take profile pic from";
    public static final String UPDATE_PROFILE_NICK = "ybot update profile nick";
    public static final String UPDATE_PROFILE_PLAY = "ybot update profile play";
    
    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String item, msg, msgLower, reply = null;
            msg = t.getMessage().getContent();
            msgLower = msg.toLowerCase();
            
            if (ProfileModule.UPDATE_PROFILE_PIC.equals(msgLower)) {
                Attachment att = t.getMessage().getAttachments().get(0);
                String urlStr = att.getUrl();
                String type = urlStr.substring(urlStr.lastIndexOf("."));
                this.getClient().changeAvatar(Image.forUrl(type, urlStr));
                reply = "Updated profile pic!";
            }
            else if (msgLower.startsWith(ProfileModule.STEAL_PROFILE_PIC)) {
                item = msg.substring(ProfileModule.STEAL_PROFILE_PIC.length()).trim();
                List<IUser> users = t.getGuild().getUsersByName(item);
                if (users.isEmpty()) {
                    reply = String.format("No user named `%s` found, no updaterino", item);
                }
                else if (users.size() > 1) {
                    reply = String.format("Multiple users match `%s`, no updaterino", item);
                }
                else {
                    IUser user = users.get(0);
                    this.getClient().changeAvatar(Image.forUser(user));
                    reply = String.format("Updated profile pic from `%s`!", item);
                }
            }
            else if (msgLower.startsWith(ProfileModule.UPDATE_PROFILE_NICK)) {
                item = msg.substring(ProfileModule.UPDATE_PROFILE_NICK.length()).trim();
                t.getGuild().setUserNickname(this.getClient().getOurUser(), item);
                reply = String.format("Set nickname to `%s`!", item);
            }
            else if (msgLower.startsWith(ProfileModule.UPDATE_PROFILE_PLAY)) {
                item = msg.substring(ProfileModule.UPDATE_PROFILE_PLAY.length()).trim();
                this.getClient().changePlayingText(item);
                reply = String.format("Set playing text to `%s`!", item);
            }
            t.getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(ProfileModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String help() {
        return "**ProfileModule**: Changes YBot's nick, avatar, etc. `ybot update profile (pic|nick|play)` or `ybot take profile pic from (user)`\n";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getBot().getUtil().isMessageFromOwner(t)) {
            return false;
        }
        String msg = t.getMessage().getContent();
        if (msg.startsWith(ProfileModule.UPDATE_PROFILE_PLAY)) {
            return true;
        }
        if (msg.startsWith(ProfileModule.UPDATE_PROFILE_NICK)) {
            return true;
        }
        if (msg.startsWith(ProfileModule.STEAL_PROFILE_PIC)) {
            return true;
        }
        if (msg.startsWith(ProfileModule.UPDATE_PROFILE_PIC)) {
            if (!t.getMessage().getAttachments().isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
