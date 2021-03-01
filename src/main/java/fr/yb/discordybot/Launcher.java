package fr.yb.discordybot;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.DiscordException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nicolas
 */
public class Launcher {

    public static CommandLine parseOptions(String[] args) throws ParseException {
        // create Options object
        Options options = new Options();

        options.addRequiredOption(
                null,
                "config-file",
                true,
                "path to file containing bot config"
        );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }

    public static void main(String[] args) {        
        // parse args
        CommandLine cmd = null;
        try {
            cmd = parseOptions(args);
        } catch (ParseException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        // load config
        Gson gson = new Gson();
        BotConfig config = null;
        try {
            config = gson.fromJson(new FileReader(cmd.getOptionValue("config-file")), BotConfig.class);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // set discord api url
        Discord4J.setBaseDiscordUrl("https://discord.com/");

        // build bot
        Bot bot = new Bot(config);
        try {
            bot.buildClient();
        } catch (DiscordException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean running = true;

        // load modules
        try {
            List<String> modules = config.getModules();
            for (String module : modules) {
                bot.loadModule(module);
                bot.startModule(module);
            }
        } catch (Exception ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        // console mode
        Scanner sc = new Scanner(System.in);
        while (running) {
            try {
                System.out.println(" **** ");
                String cmdLine = sc.nextLine();
                String cmdName = cmdLine.split(" ")[0];
                switch (cmdName.trim().toLowerCase()) {
                    case "":
                        break;
                    case "exit":
                        bot.disconnect();
                        running = false;
                        break;
                    case "module":
                        System.out.println(module(cmdLine, bot));
                        break;
                    default:
                        System.out.println("Commands: exit, module");
                }
            } catch (Exception ex) {
                bot.save();
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String module(String cmd, Bot bot) throws Exception {
        String[] args = cmd.split(" ", 5);
        if (args.length < 2) {
            return "Usage: module {list|load|start|exec|stop} [moduleName] [args]";
        }
        BotModule instance;
        switch (args[1].trim().toLowerCase()) {
            case "list":
                final StringBuilder sb = new StringBuilder("Started instances:");
                System.out.println("Started instances:");
                bot.getLoader().getInstances().keySet().forEach((key) -> {
                    sb.append("\n - ").append(key);
                });
                return sb.toString();
            case "load":
                if (args.length < 3) {
                    return "Usage: module load {moduleName} [moduleRootPath]";
                }
                // force unloading if already loaded
                if (bot.getLoader().getInstances().containsKey(args[2])) {
                    bot.getLoader().stop(args[2]);
                }
                if (args.length == 3) {
                    bot.getLoader().load(args[2]);
                } else {
                    bot.getLoader().load(args[2], args[3]);
                }
                return "Done loading " + args[2];
            case "reload":
                if (args.length < 3) {
                    return "Usage: module reload {moduleName} [moduleRootPath]";
                }
                bot.getLoader().stop(args[2]);
                bot.getLoader().unload(args[2]);
                if (args.length == 3) {
                    bot.getLoader().load(args[2]);
                } else {
                    bot.getLoader().load(args[2], args[3]);
                }
                return "Done reloading " + args[2];
            case "restart":
                if (args.length < 3) {
                    return "Usage: module restart {moduleName} [moduleRootPath]";
                }
                bot.getLoader().stop(args[2]);
                bot.getLoader().unload(args[2]);
                if (args.length == 3) {
                    bot.getLoader().load(args[2]);
                } else {
                    bot.getLoader().load(args[2], args[3]);
                }
                bot.getLoader().start(args[2]);
                return "Restarted " + args[2];
            case "start":
                if (args.length < 3) {
                    return "Usage: module start {moduleName}";
                }
                bot.getLoader().start(args[2]);
                return "Started " + args[2];
            case "exec":
                if (args.length < 3) {
                    return "usage: module exec {moduleName} {methodName} [args]";
                }
                instance = bot.getLoader().getInstances().get(args[2]);
                if (args.length == 3) {
                    instance.getClass().getMethod(args[3]).invoke(instance);
                } else {
                    Object result;
                    try {
                        result = instance.getClass().getMethod(args[3], String.class).invoke(instance, args[4]);
                    } catch (NoSuchMethodException ex) {
                        // whoops, maybe this one has no arguments?
                        result = instance.getClass().getMethod(args[3]).invoke(instance);
                    }
                    if (result == null) {
                        return "<success>";
                    } else {
                        return result.toString();
                    }
                }
                break;
            case "stop":
                if (args.length < 3) {
                    return "Usage: module stop {moduleName}";
                }
                bot.getLoader().stop(args[2]);
                return "Stopped " + args[2];
            case "unload":
                if (args.length < 3) {
                    return "Usage: module unload {moduleName}";
                }
                bot.getLoader().unload(args[2]);
                return "Done unloading " + args[2];
            default:
                return "Usage: module {list|load|start|exec|stop} [moduleName] [args]";
        }
        return "Usage: module {list|load|start|exec|stop} [moduleName] [args]";
    }
}
