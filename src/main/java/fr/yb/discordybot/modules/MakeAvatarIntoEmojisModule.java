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
public class MakeAvatarIntoEmojisModule extends BotModule {

    @Override
    public boolean handle(MessageReceivedEvent t) {
        this.getBot().getClient().getGuilds().forEach((g) -> {
            g.getUsers().forEach((u) -> {
                System.out.println(u.getAvatarURL() + " - " + u.getName());
            });
        });
        return false;
    }

    @Override
    public String help() {
        return "**MakeAvatarIntoEmojisModule**: Uhhhhhh, too specific. Should be disabled\n";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageFromOwner(t)) {
            return false;
        }
        return (t.getMessage().getContent().toLowerCase().contains("get all avatar links"));
    }
}
