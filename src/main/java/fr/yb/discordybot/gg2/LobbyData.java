/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.gg2;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nicolas
 */
public class LobbyData {
    
    private int nbServers;
    private List<LobbyServerData> serverData;

    public LobbyData() {
        this.serverData = new ArrayList<>();
    }

    public int getNbServers() {
        return nbServers;
    }

    public void setNbServers(int nbServers) {
        this.nbServers = nbServers;
    }

    public List<LobbyServerData> getServerData() {
        return serverData;
    }

    public void setServerData(List<LobbyServerData> serverData) {
        this.serverData = serverData;
    }
}
