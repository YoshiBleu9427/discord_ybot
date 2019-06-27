/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.model;

import java.util.Date;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 *
 * @author Nicolas
 */
public class ReminderModel {
    private String reminder;
    private long remindedId;
    private long originId;
    private Date scheduleTime;

    public ReminderModel() {
    }

    public ReminderModel(String reminder, long remindedId, long originId, Date scheduleTime) {
        this.reminder = reminder;
        this.remindedId = remindedId;
        this.originId = originId;
        this.scheduleTime = scheduleTime;
    }
    
    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public long getRemindedId() {
        return remindedId;
    }

    public void setRemindedId(long remindedId) {
        this.remindedId = remindedId;
    }

    public long getOriginId() {
        return originId;
    }

    public void setOriginId(long originId) {
        this.originId = originId;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }
}
