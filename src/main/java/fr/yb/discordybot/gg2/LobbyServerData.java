/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.gg2;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Nicolas
 */
public class LobbyServerData {
    
    int serverInfoLen;

    int protocol;
    int serverPort;

    String serverIP;

    int slots;
    int players;
    int bots;
    boolean isPrivate;

    Map<String, String> infos;

    public LobbyServerData() {
        this.infos = new HashMap<>();
    }

    public int getServerInfoLen() {
        return serverInfoLen;
    }

    public void setServerInfoLen(int serverInfoLen) {
        this.serverInfoLen = serverInfoLen;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getBots() {
        return bots;
    }

    public void setBots(int bots) {
        this.bots = bots;
    }

    public boolean isIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Map<String, String> getInfos() {
        return infos;
    }

    public void setInfos(Map<String, String> infos) {
        this.infos = infos;
    }
    
    
}
