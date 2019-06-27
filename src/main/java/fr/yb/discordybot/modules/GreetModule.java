package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import fr.yb.discordybot.model.UserModel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
public class GreetModule extends BotModule {

    public GreetModule() {
    }

    @Override
    public void start() {
        super.start();
        Map<String, UserModel> ums = this.getBot().getModel().getUsers();
        ums.forEach((String t, UserModel um) -> {
            int fren = um.getFren();
            /*if (fren > 0) {
                um.setFren(fren - 1);
            }*/
        });
    }
    
    public static final int GIB_LIMIT = 100;
    
    public static final int REACT_MIN_VERY_NICE = 25;
    public static final int REACT_MIN_NICE = 5;
    public static final int REACT_MIN_WHO = 2;
    
    public static final List<String> REACTIONS = Arrays.asList(
            "Fuck you {name}",
            "Fuck off {name}",
            "Eat shit {name}",
            "Go fuck yourself {name}",
            "Go to hell {name}",
            "Pls die {name}",
            "Drown in a sewer and die {name}",
            "I want {name} to leave"
    );
    
    public static final List<String> WHO_REACTIONS = Arrays.asList(
            "Who are you {name}?",
            "Who's {name}?",
            "Oh hey, uh, {name}?",
            "Why are you talking to me, {name}?",
            "I don't know you, {name}",
            "{name} is making me uneasy"
    );
    
    public static final List<String> NICE_REACTIONS = Arrays.asList(
            "Hi {name}",
            "Oh hi {name}",
            "Hey, {name}",
            "Welcome back {name}",
            "'sup {name}",
            "Oh hey {name}",
            "How are you {name}?"
    );
    
    public static final List<String> VERY_NICE_REACTIONS = Arrays.asList(
            "{name} <3",
            "I love you {name}",
            "{name}!",
            "You're great {name}",
            "Hi {name} you're good",
            "Hi {name}!!"
    );

    @Override
    public int getPriority() {
        return 10000;
    }

    @Override
    public String help() {
        return "**GreetModule**: Says hi to people who mention ybot! Or fuck off if ybot doesn't know or like them.\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        return this.getUtil().isMessageForMe(t);
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            IUser author = t.getMessage().getAuthor();
            UserModel authorData = this.getBot().getModel().find(author);
            String reply = null;
            if (this.getUtil().isMessageForMe(t)) {
                int frensLvl = authorData.getFren();
                if (t.getMessage().getContent().toLowerCase().contains("show fren")) {
                    reply = "I have " + frensLvl + " friendship with " + t.getAuthor().getDisplayName(t.getGuild());
                } else if (t.getMessage().getContent().toLowerCase().contains("reset fren")) {
                    authorData.setFren(0);
                    reply = "Forgetting all friendship with " + t.getAuthor().getDisplayName(t.getGuild());
                } else if (t.getMessage().getContent().toLowerCase().contains("list fren")) {
                    reply = this.listFren(t);
                } else if (t.getMessage().getContent().toLowerCase().contains("gib fren") ||
                        t.getMessage().getContent().toLowerCase().contains("give fren")) {
                    String[] split = t.getMessage().getContent().toLowerCase().split(" ");
                    if (split.length < 4) {
                        reply = "Did you mean `ybot gib fren <@person> <number>`?";
                    } else {
                        String last;
                        last = split[split.length - 1];
                        reply = this.giveFren(t, last);
                    }
                } else if (
                        t.getMessage().getContent().toLowerCase().contains("fuck") ||
                        t.getMessage().getContent().toLowerCase().contains("dumb") ||
                        t.getMessage().getContent().toLowerCase().contains("shit") ||
                        t.getMessage().getContent().toLowerCase().contains("stupid")
                    ) {
                    authorData.setFren(frensLvl - 2);
                    reply = "Rude!";
                } else {
                    frensLvl += 1;
                    authorData.setFren(frensLvl);
                    reply = this.getMessageFor(author, frensLvl);
                }
            }
            if (reply != null) {
                t.getMessage().getChannel().sendMessage(reply);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(GreetModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * 
     * @param author
     * @return 
     */
    private String getMessageFor(IUser author, int frensLvl) {
        String authorId = author.getStringID();
        String authorName = author.getName();
        String reply;
        if (frensLvl >= REACT_MIN_VERY_NICE) {
            reply = VERY_NICE_REACTIONS.get(BotUtil.random.nextInt(VERY_NICE_REACTIONS.size()));
        } else if (frensLvl >= REACT_MIN_NICE) {
            reply = NICE_REACTIONS.get(BotUtil.random.nextInt(NICE_REACTIONS.size()));
        } else if (frensLvl >= REACT_MIN_WHO) {
            reply = WHO_REACTIONS.get(BotUtil.random.nextInt(WHO_REACTIONS.size()));
        } else {
            reply = REACTIONS.get(BotUtil.random.nextInt(REACTIONS.size()));
        }
        return reply.replace("{name}", authorName);
    }
    
    /**
     * 
     * @param t
     * @return 
     */
    private String listFren(MessageReceivedEvent t) {
        StringBuilder sb = new StringBuilder("```\n");
        // don't list all if DM from not me
        if (this.getUtil().isDM(t) && !this.getUtil().isMessageFromOwner(t)) {
            return "Nope";
        }
        Map<String, UserModel> ums = this.getBot().getModel().getUsers();
        Map<String, Integer> frenlevels = new HashMap<>();
        ums.keySet().forEach((String strId) -> {
            long id = Long.parseLong(strId);
            IUser user = t.getClient().getUserByID(id);
            if (user == null) {
                System.err.println(" --- [!] GreetModule listFren 185 - Can't find userID " + strId);
                return;
            }
            String username = null;
            if (!this.getUtil().isDM(t)) {
                // list in server only
                if (!t.getGuild().getUsers().contains(user)) {
                    return;
                }
                username = user.getNicknameForGuild(t.getGuild());
            }
            if (username == null) {
                username = user.getName();
            }
            int frenshop = this.getBot().getModel().find(strId).getFren();
            frenlevels.put(username, frenshop);
        });
        
        // Convert the map into a sorted stream https://stackoverflow.com/a/23846961
        Stream<Map.Entry<String,Integer>> sorted =
            frenlevels.entrySet().stream()
               .sorted(Map.Entry.comparingByValue());
        sorted.forEachOrdered((entry) -> {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        });
        sb.append("```");
        return sb.toString();
    }

    /**
     * @param t
     * @param last
     * @return 
     */
    private String giveFren(MessageReceivedEvent t, String last) {
        final List<IUser> giveToList = t.getMessage().getMentions();
        int nbReceivers = giveToList.size();
        int modifier = 0;
        try {
            modifier = Integer.parseInt(last);
        } catch (NumberFormatException e) {
            return "I need the last thing in your message to be a number, please.\nI'm a bit stupid. Sorry :(";
        }
        int cost = Math.abs(modifier) * nbReceivers;
        int giverFren = this.getBot().getModel().find(t.getAuthor()).getFren();
        if (nbReceivers == 0) {
            return "Who do you want to give friendship to?";
        }
        if (modifier == 0) {
            return "Give 0, nothing to do";
        }
        if (modifier > GIB_LIMIT) {
            return "That's a bit too much";
        }
        if (modifier < -GIB_LIMIT) {
            return "That's a bit too much";
        }
        if (giverFren < cost) {
            return "You only have " + giverFren + " friendship, and need " + cost + " to modify that much friendship!";
        }
        
        giverFren -= cost;
        this.getBot().getModel().find(t.getAuthor()).setFren(giverFren);
        String username = this.getUtil().getLocalUsername(t.getAuthor(), t);
        StringBuilder result = new StringBuilder(username).append(" now has **").append(giverFren).append("** (-").append(cost).append(") friendship!\n");
        for (IUser u : giveToList) {
            username = this.getUtil().getLocalUsername(u, t);
            int fren = this.getBot().getModel().find(u).getFren();
            result.append(username).append(": ").append(fren);
            fren += modifier;
            this.getBot().getModel().find(u).setFren(fren);
            result.append(" (");
            if (modifier > 0) {
                result.append("+");
            }
            result.append(modifier).append(") -> **").append(fren).append("**\n");
        }
        
        String finalResult = result.toString();
        if (finalResult.isEmpty()) {
            finalResult = "Nothing happened?!";
        }
        return finalResult;
    }
    
    public void cmdSetFren(String cmd) {
        String[] split = cmd.split(" ");
        int value = Integer.parseInt(split[split.length-1]);
        String userName = "";
        for (int i = 0; i < split.length - 1; i++) {
            if (!"".equals(userName)) {
                userName += " ";
            }
            userName += split[i];
        }
        
        List<IUser> users = this.getClient().getUsersByName(userName);
        if (users.size() < 1) {
            System.out.println("Couldn't find a user with that name: " + userName);
            return;
        }
        if (users.size() > 1) {
            System.out.println("Too many users found:");
            for (IUser u : users) {
                System.out.print(u.getName());
                System.out.print(", ");
            }
            System.out.println();
            return;
        }
        IUser u = users.get(0);
        this.getBot().getModel().find(u).setFren(value);
        System.out.println(userName + " (" + u.getStringID() + ")  set to " + value);
    }

}
