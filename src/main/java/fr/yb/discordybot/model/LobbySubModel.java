/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 *
 * @author Nicolas
 */
public class LobbySubModel implements Serializable {
    
    private boolean channel = false;
    private String id; // longID
    private int level;
    private Date lastPost = Date.from(Instant.EPOCH);
    private Duration cooldown = Duration.ofHours(1);

    public Date getLastPost() {
        return lastPost;
    }

    public void setLastPost(Date lastPost) {
        this.lastPost = lastPost;
    }

    public Duration getCooldown() {
        return cooldown;
    }

    public void setCooldown(Duration cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isChannel() {
        return channel;
    }

    public void setChannel(boolean channel) {
        this.channel = channel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
       
}