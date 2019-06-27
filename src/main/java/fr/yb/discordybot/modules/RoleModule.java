package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
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
public class RoleModule extends BotModule {
    
    public static final String START = "ybot role";
    public static final String CREATE = START + " create ";
    public static final String ADD = START + " add ";
    public static final String REMOVE = START + " remove ";
    
    
    
    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String item, msg, msgLower, reply = null;
            msg = t.getMessage().getContent();
            msgLower = msg.toLowerCase();
               
            if (!this.getBot().getClient().getOurUser().getPermissionsForGuild(t.getGuild()).contains(Permissions.MANAGE_ROLES)) {
                reply = "Sorry, I can't create roles :(";
                t.getChannel().sendMessage(reply);
                return false;
            }
            
            if (msgLower.startsWith(RoleModule.CREATE)) {
                if (t.getAuthor().getPermissionsForGuild(t.getGuild()).contains(Permissions.MANAGE_ROLES)) {
                    item = msg.substring(RoleModule.CREATE.length());
                    IRole newRole = t.getGuild().createRole();
                    newRole.changeName(item);
                    newRole.changeMentionable(true);
                    reply = "Created role '" + newRole.getName() + "'!";
                } else {
                    reply = "You don't have permissions to create roles, sorry";
                }    
            }
            
            else if (msgLower.startsWith(ADD)) {
                item = msg.substring(ADD.length());
                List<IRole> roles = t.getGuild().getRolesByName(item);
                switch (roles.size()) {
                    case 0:
                        reply = "No role found matching name " + item;
                        break;
                    case 1:
                        IRole role = roles.get(0);
                        if (!t.getAuthor().getPermissionsForGuild(t.getGuild()).containsAll(role.getPermissions())) {
                            reply = "That role has more permissions than you do, I'm not touching that!";
                        } else {
                            t.getAuthor().addRole(role);
                            reply = "Role '" + role.getName() + "' added!";
                        }
                        break;
                    default: // more than 1
                        reply = "There are " + roles.size() + " roles matching name " + item + ", I don't know which one to add";
                }
            }
            
            else if (msgLower.startsWith(REMOVE)) {
                item = msg.substring(REMOVE.length());
                List<IRole> roles = t.getGuild().getRolesByName(item);
                switch (roles.size()) {
                    case 0:
                        reply = "No role found matching name " + item;
                        break;
                    case 1:
                        IRole role = roles.get(0);
                        
                        if (!t.getAuthor().getPermissionsForGuild(t.getGuild()).containsAll(role.getPermissions())) {
                            reply = "That role has more permissions than you do, I'm not touching that!";
                        } else {
                            t.getAuthor().removeRole(role);
                            reply = "Role '" + role.getName() + "' removed!";
                        }
                        break;
                    default: // more than 1
                        reply = "There are " + roles.size() + " roles matching name " + item + ", I don't know which one to remove";
                }
            }
            
            else {
                reply = "Did you mean `ybot role (create|add|remove) <role name>`?";
            }
            
            t.getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(RoleModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String help() {
        return "**RoleModule**: YBot creates roles and assigns them to you. `ybot role (create|add|remove) <role name>`\n";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        String msg = t.getMessage().getContent().toLowerCase();
        if (msg.startsWith(RoleModule.START)) {
            return true;
        }
        return false;
    }

}
