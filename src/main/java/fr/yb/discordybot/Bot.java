/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot;

import com.google.gson.Gson;
import fr.yb.discordybot.model.BotModel;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author Nicolas
 */
public class Bot implements IListener<MessageReceivedEvent> {
    
    private String token;
    private String ownerID;
    private String saveFileName;
    
    private IDiscordClient client;
    private ModuleLoader loader;
    
    private BotModel model;
    private BotUtil util;

    public ModuleLoader getLoader() {
        return loader;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public Bot(String token, String ownerID, String saveFileName) {
        this.token = token;
        this.ownerID = ownerID;
        this.saveFileName = saveFileName;
    }
    
    private String getToken() {
        return token;
    }
    
    public void buildClient() throws DiscordException {
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(this.getToken()); // Adds the login info to the builder 
        this.client = clientBuilder.build();
        this.util = new BotUtil(this);
        this.model = new BotModel();
        this.load();
        this.loader = new ModuleLoader(this);
    }

    public BotUtil getUtil() {
        return util;
    }
    
    public void save() {
        Gson gson = new Gson();
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getSaveFileName()))) {
            gson.toJson(this.getModel(), bw);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void load() {
        Gson gson = new Gson();
        
        try {
            this.model = gson.fromJson(new FileReader(this.getSaveFileName()), this.model.getClass());
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void connect() throws DiscordException, RateLimitException, InterruptedException {
        this.client.login();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Logging in...");
        this.getClient().getDispatcher().registerListener(this);
        this.getClient().getDispatcher().waitFor(sx.blah.discord.handle.impl.events.ReadyEvent.class);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Logged in!");
    }
    
    public void disconnect() throws DiscordException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Unregistering...");
        this.getClient().getDispatcher().unregisterListener(this);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Stopping modules...");
        this.loader.stopAll();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Saving...");
        this.save();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Logging out...");
        this.client.logout();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Disconnected.");
    }
    
    public void loadModule(String moduleName) throws Exception {
        this.loader.load(moduleName);
    }
    
    public void loadModule(String moduleName, String modulePath) throws Exception {
        this.loader.load(moduleName, modulePath);
    }
    
    public void startModule(String moduleName) throws Exception {
        this.loader.start(moduleName);
    }
    
    public void stopModule(String moduleName) throws Exception {
        this.loader.start(moduleName);
    }

    public IDiscordClient getClient() {
        return client;
    }

    public BotModel getModel() {
        return model;
    }

    @Override
    public void handle(MessageReceivedEvent t) {
        Collection modules = this.getLoader().getInstances().values();
        ArrayList<BotModule> sortedModules = new ArrayList(modules);
        sortedModules.sort((BotModule o1, BotModule o2) -> {
            return ((Integer)o1.getPriority()).compareTo(o2.getPriority());
        });
        for (BotModule m : sortedModules) {
            if (!m.isActive()) {
                continue;
            }
            if (!m.isInterestedIn(t)) {
                continue;
            }
            boolean mayContinue = m.handle(t);
            if (!mayContinue) {
                return;
            }
        }
    }
}
