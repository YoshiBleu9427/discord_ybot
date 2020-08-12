/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot;

import java.util.HashMap;
import java.util.Map;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 *
 * @author Nicolas
 */
public abstract class BotReactableModule extends BotModule {

    public class AlreadyMarkedException extends Exception {

    }

    private Map<IChannel, IMessage> messages;

    public Map<IChannel, IMessage> getMarkedMessages() {
        return messages;
    }

    public BotReactableModule() {
        super();
        this.messages = new HashMap<>();
    }

    public void handleReact(ReactionEvent evt) {
        IChannel chanID = evt.getChannel();
        if (this.messages.containsKey(chanID)
                && this.messages.get(chanID).getLongID() != evt.getMessage().getLongID()) {
            return;
        }
        if (evt instanceof ReactionAddEvent) {
            this.onReactAdd((ReactionAddEvent) evt);
        }
        if (evt instanceof ReactionRemoveEvent) {
            this.onReactRemove((ReactionRemoveEvent) evt);
        }
    }

    public void markMessage(IMessage m) throws AlreadyMarkedException {
        if (this.messages.containsKey(m.getChannel())) {
            throw new AlreadyMarkedException();
        }
        this.messages.put(m.getChannel(), m);
    }

    public void unmarkMessage(IMessage m) {
        if (!this.messages.containsKey(m.getChannel())) {
            return;
        }
        this.messages.remove(m.getChannel());
    }

    public abstract void onReactAdd(ReactionAddEvent evt);
    public abstract void onReactRemove(ReactionRemoveEvent evt);

}
