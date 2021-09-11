package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.model.ReminderModel;
import fr.yb.discordybot.model.UserModel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
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
public class ReminderModule extends BotModule {
    
    public static final String REGEX = "remind(er| me) (to |for |about )?";
    public static final String REGEX_CAPTURE = "remind(er| me) (to |for |about )?(.*)";
    public static final String REPLY = "Reminder set at %s!";
    public static final String HELP = "`ybot remind(er|me ) <something> (in <x> (seconds|minutes|hours) | at hh:mm[:ss])`"; // TODO
    public static final String REMIND_TEXT = "%s, reminder for %s";
    
    public class MisunderstandException extends Exception {

        public MisunderstandException() {
        }

        public MisunderstandException(String message) {
            super(message);
        }

        public MisunderstandException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    public class Reminder extends TimerTask {
        private String reminder = "";
        private IUser reminded = null;
        private IChannel origin = null;
        private Date scheduleTime = null;
        private boolean done = false;
        
        public void start() {
            if (this.scheduleTime.before(new Date())) {
                this.run();
            } else {
                Timer timer = new Timer();
                timer.schedule(this, this.scheduleTime);
            }
        }
        
        public Reminder initAndStart(String reminder, IUser reminded, IChannel origin, Date scheduleTime) {
            this.reminder = reminder;
            this.reminded = reminded;
            this.origin = origin;
            this.scheduleTime = scheduleTime;
            this.start();
            return this;
        }
        
        public void setDataFromMessage(MessageReceivedEvent t) throws MisunderstandException {
            this.reminded = t.getAuthor();
            this.origin = t.getChannel();
            String msg = t.getMessage().getFormattedContent();
            this.reminder = this.processReminderText(msg);
            
            Timer timer = new Timer();
            
            String lowerMsg = msg.toLowerCase();
            int lastIn = lowerMsg.lastIndexOf("in ");
            int lastAt = lowerMsg.lastIndexOf("at ");
            if (lastIn > lastAt) {
                int scheduleTimeMillis = this.calculateMillisFromMessage(msg.toLowerCase());
                timer.schedule(this, scheduleTimeMillis);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.SECOND, scheduleTimeMillis / 1000);
                this.scheduleTime = c.getTime();
            } else {
                this.scheduleTime = this.calculateDateFromMessage(msg.toLowerCase());
                timer.schedule(this, this.scheduleTime);
            }
        }
        
        public String processReminderText(String msg) throws MisunderstandException {
            Pattern p = Pattern.compile(REGEX_CAPTURE);
            Matcher m = p.matcher(msg);
            if (!m.find()) {
                throw new MisunderstandException("Didn't match the capture");
            }
            String interestingPart = m.group(3);
            int lastIn = interestingPart.lastIndexOf("in ");
            int lastAt = interestingPart.lastIndexOf("at ");
            int lastInAt = Integer.max(lastIn, lastAt);
            
            // TODO
            int lastYbot = interestingPart.lastIndexOf("ybot");
            int last = Integer.min(lastInAt, lastYbot);
            if (lastInAt < 0) {
                last = lastYbot;
            }
            if (lastAt < 0) {
                last = lastYbot;
            }
            if (lastYbot < 0) {
                last = lastInAt;
            }
            if ((lastInAt < 0) && (lastYbot < 0)) {
                last = interestingPart.length();
            }
            return interestingPart.substring(0, last);
        }
        
        public Date calculateDateFromMessage(String msg) throws MisunderstandException {
            int timeTextIndex = msg.lastIndexOf("at ");
            if (timeTextIndex == -1) {
                throw new MisunderstandException("At when?");
            }
            String timeTextRaw = msg.substring(timeTextIndex + "at ".length());
            String[] timeInterests = timeTextRaw.split(" ");
            if (timeInterests.length < 1) {
                throw new MisunderstandException("No time value");
            }
            String timeValueText = timeInterests[0];
            String[] timeValues = timeValueText.split(":");
            if (timeValues.length < 2) {
                throw new MisunderstandException("No :");
            }
            int hours = Integer.parseInt(timeValues[0]);
            int minutes = Integer.parseInt(timeValues[1]);
            int seconds = 0;
            if (timeValues.length > 2) {
                seconds = Integer.parseInt(timeValues[2]);
            }
            
            // Do we need to add a day?
            Calendar calendarToBuildResultDate = Calendar.getInstance();
            calendarToBuildResultDate.set(Calendar.HOUR_OF_DAY, hours);
            calendarToBuildResultDate.set(Calendar.MINUTE, minutes);
            calendarToBuildResultDate.set(Calendar.SECOND, seconds);
            boolean isRequestedTimeBeforeNow = Calendar.getInstance().after(calendarToBuildResultDate);
            if (isRequestedTimeBeforeNow) {
                calendarToBuildResultDate.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            return calendarToBuildResultDate.getTime();
        }
        
        public int calculateMillisFromMessage(String msg) throws MisunderstandException {
            int timeTextIndex = msg.lastIndexOf("in ");
            if (timeTextIndex == -1) {
                throw new MisunderstandException("When?");
            }
            String timeTextRaw = msg.substring(timeTextIndex + "in ".length());
            String[] timeInterests = timeTextRaw.split(" ");
            if (timeInterests.length < 2) {
                throw new MisunderstandException("No time unit");
            }
            String timeValueText = timeInterests[0];
            String timeUnitText = timeInterests[1];
            if (timeUnitText.length() < 3) {
                throw new MisunderstandException("Bad time unit");
            }
            int multiplier = 1000;
            switch (timeUnitText.substring(0, 3)) {
                case "hou":
                    multiplier *= 60 * 60;
                    break;
                case "min":
                    multiplier *= 60;
                    break;
                case "sec":
                    // all good
                    break;
            }
            int timeValue;
            if (("an".equals(timeValueText)) || ("a".equals(timeValueText))) {
                timeValue = 1;
            } else {
                timeValue = Integer.parseInt(timeValueText);
            }
            return timeValue * multiplier;
        }

        @Override
        public void run() {
            String msg;
            IChannel sendTo;
            if (this.origin == null) {
                sendTo = this.reminded.getOrCreatePMChannel();
            } else {
                sendTo = this.origin;
            }
            msg = String.format(REMIND_TEXT, this.reminded.mention(), this.reminder);
            sendTo.sendMessage(msg);
            this.done = true;
        }
    }
    
    private List<Reminder> reminders = new ArrayList<>();

    @Override
    public void stop() {
        for (Reminder r : this.getReminders()) {
            this.getBot().getModel().find(r.reminded).getReminders().add(
                    new ReminderModel(
                            r.reminder,
                            r.reminded.getLongID(),
                            r.origin.getLongID(),
                            r.scheduleTime
                    )
            );
        }
        super.stop();
    }

    @Override
    public void start() {
        super.start();
        for (UserModel user : this.getBot().getModel().getUsers().values() ) {
            for (ReminderModel r : user.getReminders()) {
                try {
                    Reminder importedReminder = new Reminder();
                    importedReminder.initAndStart(
                            r.getReminder(),
                            this.getBot().getClient().fetchUser(r.getRemindedId()),
                            this.getBot().getClient().getChannelByID(r.getOriginId()),
                            r.getScheduleTime()
                    );
                    this.reminders.add(importedReminder);
                    Logger.getLogger(ReminderModule.class.getName()).log(Level.INFO,
                            "Restored reminder for user " + user.getName() + " at " + r.getScheduleTime().toLocaleString());
                } catch (DiscordException ex) {
                    Logger.getLogger(ReminderModule.class.getName()).log(Level.WARNING, "Couldn't restore reminder for user " + user.getName(), ex);
                }
            }
        }
        // all loaded, can unload
        for (UserModel user : this.getBot().getModel().getUsers().values() ) {
            user.getReminders().clear();
        }
    }
    
    

    public List<Reminder> getReminders() {
        this.reminders.removeIf((t) -> {
            return t.done;
        });
        return reminders;
    }
    
    public String getRemindersAsText() {
        List<Reminder> reminds = this.getReminders();
        String sb = this.textify(reminds.stream());
        return sb;
    }
    
    public String textify(Stream<Reminder> reminds) {
        StringBuilder sb = new StringBuilder("List of reminders:\n```\n");
        reminds.forEach((remind) -> {
            sb.append(remind.reminded.getName());
            sb.append(" [").append(DateFormat.getInstance().format(remind.scheduleTime)).append("] : ");
            sb.append(remind.reminder);
            sb.append('\n');
        });
        sb.append("```");
        return sb.toString();
    }
    
    public void cmdGet() {
        System.out.println(this.getRemindersAsText());
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String lowerMsg = t.getMessage().getContent().toLowerCase();
            if (lowerMsg.endsWith("reminder list")) {
                // get only reminders for people who are here
                Stream<Reminder> reminds = this.getReminders().stream().filter((r) -> {
                    return t.getChannel().getUsersHere().contains(r.reminded);
                });
                String reply = this.textify(reminds);
                t.getMessage().getChannel().sendMessage(reply);
                return false;
            }
            Reminder reminder = new Reminder();
            try {
                reminder.setDataFromMessage(t);
                this.reminders.add(reminder);
                t.getMessage().getChannel().sendMessage(String.format(REPLY, DateFormat.getInstance().format(reminder.scheduleTime)));
                return false;
            } catch (MisunderstandException ex) {
                t.getMessage().getChannel().sendMessage(HELP);
                Logger.getLogger(ReminderModule.class.getName()).log(Level.SEVERE, null, ex);
                return true;
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(ReminderModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String help() {
        return "**ReminderModule**: Gives "+this.getUtil().getName()+" more memory than you. `"
                +this.getUtil().getName()+" remind me to take out the laundry in x minutes`. Ask `"+this.getUtil().getName()+" reminder` for details\n";
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        String lowerMsg = t.getMessage().getContent().toLowerCase();
        if (lowerMsg.endsWith("reminder list")) {
            return true;
        }
        Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(lowerMsg);
        if (!(m.find())) {
            return false;
        }
        if (!(lowerMsg.contains("in ") || lowerMsg.contains("min") || lowerMsg.contains("hour") || lowerMsg.contains("sec"))) {
            return false;
        }
        return true;
    }

    @Override
    public String getCommand() {
        return "";
    }

}
