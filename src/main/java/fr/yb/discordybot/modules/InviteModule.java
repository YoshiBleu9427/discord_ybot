package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nicolas
 */
public class InviteModule extends BotModule {

    @Override
    public boolean handle(MessageReceivedEvent t) {
        this.getBot().getClient().getOurUser().getLongID();
        String url = String.format(
            "https://discord.com/api/oauth2/authorize?client_id=%d&permissions=2112&scope=bot",
            this.getBot().getClient().getOurUser().getLongID()
        );
        t.getChannel().sendMessage(String.format("You want to invite me to your server? %s", url));
        return false;
    }

    @Override
    public String help() {
        return "**InviteModule**: Generate an invite link. `" + this.getBot().getConfig().getPrefix() + "bot invite link`\n";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        String msg = t.getMessage().getContent().toLowerCase();
        return (msg.contains("invite link") && msg.contains("bot"));
    }

    @Override
    public String getCommand() {
        return "invite";
    }
}
