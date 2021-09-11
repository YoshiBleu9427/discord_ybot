/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.model;

import fr.yb.discordybot.model.cards.CardOwnership;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nicolas
 */
public class UserModel implements Serializable {
    
    private String name; // User#1324
    private String id; // longID
    private List<String> aliases;
    private List<ReminderModel> reminders;
    private List<CardOwnership> cards;
    
    private int fren;

    public UserModel() {
        this.aliases = new ArrayList<>();
        this.reminders = new ArrayList<>();
        this.cards = new ArrayList<>();
    }
    
    public boolean is(String alias) {
        if (this.id.equalsIgnoreCase(alias)) {
            return true;
        }
        if (this.name.equalsIgnoreCase(alias)) {
            return true;
        }
        if (this.aliases.stream().anyMatch((a) -> (a.equalsIgnoreCase(alias)))) {
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<ReminderModel> getReminders() {
        return reminders;
    }

    public List<CardOwnership> getCards() {
        return cards;
    }

    public int getFren() {
        return fren;
    }

    public void setFren(int fren) {
        this.fren = fren;
    }
    
}
