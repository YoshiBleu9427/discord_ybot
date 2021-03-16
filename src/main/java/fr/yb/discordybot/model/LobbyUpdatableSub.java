/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.model;

import java.io.Serializable;

/**
 *
 * @author Nicolas
 */
public class LobbyUpdatableSub implements Serializable {
    
    private long msgId; // longID
    private long channelId; // longID

    public LobbyUpdatableSub() {
    }

    public LobbyUpdatableSub(long msgId, long channelId) {
        this.msgId = msgId;
        this.channelId = channelId;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
}