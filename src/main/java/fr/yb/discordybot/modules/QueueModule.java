package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotReactableModule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
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
public class QueueModule extends BotReactableModule {

    public static final ReactionEmoji EMOJI = ReactionEmoji.of("\u2705");
    public static final int MIN_QUEUE_SIZE = 2;
    public static final int MAX_QUEUE_SIZE = 16;

    private final Map<IMessage, Queue> queues = new HashMap<>();

    public class Queue {

        public IMessage msg;
        public int size;
        public String name;

        public Queue(IMessage msg, int size, String name) {
            this.msg = msg;
            this.size = size;
            this.name = name;
        }
    }

    protected Queue createQueue(IMessage msg, int size, String name) throws AlreadyMarkedException {
        if (this.queues.containsKey(msg)) {
            throw new BotReactableModule.AlreadyMarkedException();
        }
        Queue queue = new Queue(msg, size, name);
        this.queues.put(queue.msg, queue);
        this.markMessage(msg);
        return queue;
    }

    protected void dropQueue(Queue queue) {
        this.unmarkMessage(queue.msg);
        this.queues.remove(queue.msg);
    }

    protected void onQueueEnd(Queue queue) {
        StringBuilder sb = new StringBuilder();
        for (IUser user : queue.msg.getReactionByEmoji(EMOJI).getUsers()) {
            sb.append(user.mention()).append(", ");
        }
        sb.append("your queue `").append(queue.name).append("` is ready!");
        queue.msg.getChannel().sendMessage(sb.toString());
        queue.msg.edit("Queue completed!");
    }

    protected void onQueueEmpty(Queue queue) {
        queue.msg.edit("Your queue `" + queue.name + "` was abandoned and dropped.");
        queue.msg.removeReaction(this.getClient().getOurUser(), EMOJI);
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String msgtext = t.getMessage().getContent().trim();
            String cmdArgs = msgtext.substring(this.getFullCommand().length()).trim();
            int nbSpacesInCmd = this.getFullCommand().length() - this.getFullCommand().replace(" ", "").length();
            String[] msgsplit = cmdArgs.split(" ", 2);
            String queueName;
            int queueSize;
            try {
                queueSize = Integer.valueOf(msgsplit[0]);
                if ((queueSize < MIN_QUEUE_SIZE) || (queueSize > MAX_QUEUE_SIZE)) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                t.getMessage().getChannel().sendMessage(
                        "Please specify a valid number of players for your queue.\n"
                        + "`" + this.getFullCommand() + " <" + MIN_QUEUE_SIZE + "-" + MAX_QUEUE_SIZE + "> <name>`"
                );
                return false;
            }
            if (msgsplit.length == 1) {
                queueName = "default";
            } else {
                queueName = msgsplit[1];
            }

            String reply = "Starting queue...";
            IMessage posted = t.getMessage().getChannel().sendMessage(reply);
            Queue queue;
            try {
                queue = this.createQueue(posted, queueSize, queueName);
                posted.addReaction(EMOJI);
                this.updateMessageBody(queue);
            } catch (AlreadyMarkedException ex) {
                Logger.getLogger(QueueModule.class.getName()).log(Level.SEVERE, null, ex);
                posted.edit("Starting queue failed because there already is a queue in this channel.");
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(QueueModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void updateMessageBody(Queue queue) {
        queue.msg.edit(this.generateMessageBody(queue));
    }

    public String generateMessageBody(Queue queue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue `")
                .append(queue.name)
                .append("` started! React with ")
                .append(EMOJI.getName())
                .append(" to join. ");
        IReaction reacts = queue.msg.getReactionByEmoji(EMOJI);
        if (reacts == null) {
            sb.append("**0/")
                    .append(queue.size)
                    .append("**");
        } else {
            List<IUser> users = reacts.getUsers();
            sb.append("**")
                    .append(users.size() - 1)
                    .append("/")
                    .append(queue.size)
                    .append("**\nIn queue:  ");
            boolean first = true;
            for (IUser user : users) {
                if (user == this.getClient().getOurUser()) {
                    continue;
                }
                if (first) {
                    sb.append(user.mention());
                    first = false;
                } else {
                    sb.append(", ").append(user.mention());
                }
            }
        }
        return sb.toString();
    }

    protected Queue findQueueFromEvent(ReactionEvent evt) {
        return this.queues.get(evt.getMessage());
    }

    @Override
    public void onReactAdd(ReactionAddEvent evt) {
        Queue q = this.findQueueFromEvent(evt);
        this.updateMessageBody(q);
        List<IUser> users = evt.getMessage().getReactionByEmoji(EMOJI).getUsers();
        int cnt = users.size();
        if ((cnt - 1) >= q.size) {
            this.onQueueEnd(q);
            this.dropQueue(q);
        }
    }

    @Override
    public void onReactRemove(ReactionRemoveEvent evt) {
        Queue q = this.findQueueFromEvent(evt);
        List<IUser> users = evt.getMessage().getReactionByEmoji(EMOJI).getUsers();
        int cnt = users.size();
        if ((cnt - 1) > 0) {
            this.updateMessageBody(q);
        } else {
            this.onQueueEmpty(q);
            this.dropQueue(q);
        }
    }

    @Override
    public String help() {
        return "**QueueModule**: Start a queue, react to the message, "
                + "get notified when x people reacted too. "
                + "`" + this.getFullCommand() + " <queueSize>`,"
                + "`" + this.getFullCommand() + " <queueSize> <name>`.\n";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public String getCommand() {
        return "queue";
    }

}
