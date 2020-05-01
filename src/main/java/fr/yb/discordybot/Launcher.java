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

        // build bot
        Bot bot = new Bot(
                config.getToken(),
                config.getOwnerID(),
                config.getModelFile()
        );
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
                        module(cmdLine, bot);
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

    public static void module(String cmd, Bot bot) throws Exception {
        String[] args = cmd.split(" ", 5);
        if (args.length < 2) {
            System.out.println("Usage: module {list|load|start|exec|stop} [moduleName] [args]");
            return;
        }
        BotModule instance;
        switch (args[1].trim().toLowerCase()) {
            case "list":
                System.out.println("Started instances:");
                bot.getLoader().getInstances().keySet().forEach((key) -> {
                    System.out.println(" - " + key);
                });
                break;
            case "load":
                if (args.length < 3) {
                    System.out.println("Usage: module load {moduleName} [moduleRootPath]");
                    return;
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
                break;
            case "reload":
                if (args.length < 3) {
                    System.out.println("Usage: module reload {moduleName} [moduleRootPath]");
                    return;
                }
                bot.getLoader().stop(args[2]);
                bot.getLoader().unload(args[2]);
                if (args.length == 3) {
                    bot.getLoader().load(args[2]);
                } else {
                    bot.getLoader().load(args[2], args[3]);
                }
                break;
            case "restart":
                if (args.length < 3) {
                    System.out.println("Usage: module restart {moduleName} [moduleRootPath]");
                    return;
                }
                bot.getLoader().stop(args[2]);
                bot.getLoader().unload(args[2]);
                if (args.length == 3) {
                    bot.getLoader().load(args[2]);
                } else {
                    bot.getLoader().load(args[2], args[3]);
                }
                bot.getLoader().start(args[2]);
                break;
            case "start":
                if (args.length < 3) {
                    System.out.println("Usage: module start {moduleName}");
                    return;
                }
                bot.getLoader().start(args[2]);
                break;
            case "exec":
                if (args.length < 3) {
                    System.out.println("usage: module exec {moduleName} {methodName} [args]");
                    return;
                }
                instance = bot.getLoader().getInstances().get(args[2]);
                if (args.length == 3) {
                    instance.getClass().getMethod(args[3]).invoke(instance);
                } else {
                    try {
                        instance.getClass().getMethod(args[3], String.class).invoke(instance, args[4]);
                    } catch (NoSuchMethodException ex) {
                        // whoops, maybe this one has no arguments?
                        instance.getClass().getMethod(args[3]).invoke(instance);
                    }
                }
                break;
            case "stop":
                if (args.length < 3) {
                    System.out.println("Usage: module stop {moduleName}");
                    return;
                }
                bot.getLoader().stop(args[2]);
                break;
            case "unload":
                if (args.length < 3) {
                    System.out.println("Usage: module unload {moduleName}");
                    return;
                }
                bot.getLoader().unload(args[2]);
                break;
            default:
                System.out.println("Usage: module {list|load|start|exec|stop} [moduleName] [args]");
        }
    }
}
