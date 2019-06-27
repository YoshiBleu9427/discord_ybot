/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *
 * @author Nicolas
 */
public class ModuleLoader {
    
    private final Map<String, BotModule> instances;
    private final Bot bot;

    public Map<String, BotModule> getInstances() {
        return instances;
    }

    public ModuleLoader(Bot bot) {
        this.bot = bot;
        this.instances = new HashMap<>();
    }
    
    public void load(String moduleName) throws Exception {
        String defaultPath = Paths.get(".").toAbsolutePath().normalize().toString();
        this.load(moduleName, defaultPath);
    }
    
    public void load(String moduleName, String modulePath) throws Exception {
        if (this.getInstances().containsKey(moduleName)) {
            System.err.println("Already have loaded module " + moduleName);
            return;
        }
        
        Class<?> cls;
        cls = Class.forName("fr.yb.discordybot.modules." + moduleName);
        if (!BotModule.class.isAssignableFrom(cls)) {
            throw new Exception("Module " + moduleName + " isn't a BotModule");
        }
        
        if (cls == null) {
            throw new Exception("Couldn't find module " + moduleName);
        }
        BotModule instance = (BotModule)cls.newInstance();
        instance.setBot(this.bot);
        instance.start();
        this.getInstances().put(moduleName, instance);
    }
    
    public URLClassLoader compile(String sourceFilePath, String rootPath) throws ClassNotFoundException, InstantiationException, IllegalAccessException, MalformedURLException {
        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFilePath);
        // Load and instantiate compiled class.
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { new URL(rootPath) });
        return classLoader;
    }
    
    public void start(String moduleName) throws Exception {
        BotModule module = this.getInstances().get(moduleName);
        if (!module.isActive()) {
            module.start();
        }
    }
    
    public void stop(String moduleName) {
        if (!this.getInstances().containsKey(moduleName)) {
            return;
        }
        BotModule module = this.getInstances().get(moduleName);
        if (module.isActive()) {
            module.stop();
        }
    }
    
    public void stopAll() {
        this.getInstances().forEach((key, module) -> {
            if (module.isActive()) {
                module.stop();
            }
        });
    }
    
    public void unload(String moduleName) {
        this.stop(moduleName);
        if (!this.getInstances().containsKey(moduleName)) {
            return;
        }
        this.getInstances().remove(moduleName);
    }
}
