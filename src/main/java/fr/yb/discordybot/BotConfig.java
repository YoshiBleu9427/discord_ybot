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
    private String prefix;
    private String token;
    private String modelFile;
    private String cardsFile;
    private List<String> modules;

    public BotConfig() {
        this.modules = new ArrayList<>();
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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

    public String getCardsFile() {
        return cardsFile;
    }

    public void setCardsFile(String cardsFile) {
        this.cardsFile = cardsFile;
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
