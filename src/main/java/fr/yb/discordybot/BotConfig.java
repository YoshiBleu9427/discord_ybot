/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nicolas
 */
public class BotConfig implements Serializable {
    
    private String ownerID;
    private String token;
    private String modelFile;
    private List<String> modules;

    public BotConfig() {
        this.modules = new ArrayList<>();
    }

    public BotConfig(String ownerID, String token, String modelFile, List<String> modules) {
        this.ownerID = ownerID;
        this.token = token;
        this.modelFile = modelFile;
        this.modules = modules;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getModelFile() {
        return modelFile;
    }

    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }
    
    public void putModule(String module) {
        this.modules.add(module);
    }
}
