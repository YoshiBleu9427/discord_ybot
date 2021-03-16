package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nicolas
 */
public class MicrowaveModule extends BotModule {
    
    public static final int DURATION_SECS = 15;
    private MicrowaveRunnable instance = null;
    
    public static final String[] MICROWAVE_STATES = {
              " ________________________            \n"  
            + "|  ____________   _| ___ |  mmmmm    \n"  
            + "| |            | | | 000 |           \n"  
            + "| |            | | | ___ |           \n"  
            + "| |     .      | | |     |           \n"  
            + "| |____________| |_| O O |      mmm  \n"  
            + " __________________|_____            \n"  
            ,
              " ________________________            \n"  
            + "|  ____________   _| ___ |      mmm  \n"  
            + "| |            | | | 000 |           \n"  
            + "| |            | | | ___ |           \n"  
            + "| |        .   | | |     |mmm        \n"  
            + "| |____________| |_| O O |         mm\n"  
            + " __________________|_____            \n"  
            ,
              " ________________________            \n"  
            + "|  ____________   _| ___ |         m \n"  
            + "| |            | | | 000 |     mm    \n"  
            + "| |            | | | ___ |   mmm     \n"  
            + "| |          * | | |     |           \n"  
            + "| |____________| |_| O O |           \n"  
            + " __________________|_____   mm       \n"  
            ,
              " ________________________            \n"  
            + "|  ____________   _| ___ |mmm        \n"  
            + "| |            | | | 000 |           \n"  
            + "| |         .  | | | ___ |        mmm\n"  
            + "| |            | | |     |           \n"  
            + "| |____________| |_| O O |    mmm    \n"  
            + " __________________|_____            \n"  
            ,
              " ________________________            \n"  
            + "|  ____________   _| ___ |           \n"  
            + "| |            | | | 000 | mmmmmm    \n"  
            + "| |   .        | | | ___ |     mmm   \n"  
            + "| |            | | |     | mm        \n"  
            + "| |____________| |_| O O |    m mm   \n"  
            + " __________________|_____            \n"  
            ,
              " ________________________     mmmm   \n"  
            + "|  ____________   _| ___ |           \n"  
            + "| |            | | | 000 |  mm       \n"  
            + "| |            | | | ___ |       mm  \n"  
            + "| | *          | | |     |     mmm   \n"  
            + "| |____________| |_| O O |           \n"  
            + " __________________|_____  mm        \n"  
            ,
              " ________________________            \n"  
            + "|  ____________   _| ___ |           \n"  
            + "| |            | | | 000 |           \n"  
            + "| |            | | | ___ |           \n"  
            + "| |   .        | | |     |  mmmmm    \n"  
            + "| |____________| |_| O O |     mmmm  \n"  
            + " __________________|_____            \n"  
            ,
    };
    
    public static final String MICROWAVE_STATE_FINAL = 
              "   ________ ________________________            \n"  
            + "  | _____  |XXXXXXXXXXXXXXXXXX| ___ |   DING    \n"  
            + " |||     | |X      ~~~~      X|   / |           \n"  
            + " |||     | |X    ~~~~        X| _V_ |           \n"  
            + " |||     | |X      .         X|     |           \n"  
            + " |||_____| |X\\______________/X| O O |           \n"  
            + "  |________ __________________|_____            \n"  
    ;
    
    
    public class MicrowaveRunnable implements Runnable {
        
        private MicrowaveModule parent;
        private IMessage msg;

        public MicrowaveRunnable(MicrowaveModule parent, IMessage msg) {
            this.parent = parent;
            this.msg = msg;
        }

        public MicrowaveModule getParent() {
            return parent;
        }

        public IMessage getMsg() {
            return msg;
        }

        @Override
        public void run() {
            EmbedObject eo = new EmbedObject();
            int microwaveState = 0;
            try {
                for (int i = DURATION_SECS; i > 0; i--) {
                    eo.description = "```" + MICROWAVE_STATES[microwaveState].replace("000", String.format("%3d", i)) + "```";
                    msg.edit(eo);
                    Thread.sleep(1000);
                    microwaveState = (microwaveState + 1) % MICROWAVE_STATES.length;
                }
                msg.edit("```" + MICROWAVE_STATE_FINAL + "```");
                
            } catch (InterruptedException ex) {
                Logger.getLogger(MicrowaveModule.class.getName()).log(Level.SEVERE, null, ex);
                eo.description = "<the microwave exploded>";
                msg.edit(eo);
            } finally {
                // detach
                this.parent.instance = null;
            }
        }
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public String help() {
        return "**MicrowaveModule**: Starts a microwave. Trigger with any sentence containing `start` and `microwave`.\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        String msg = t.getMessage().getContent().toLowerCase();
        if (msg.contains("microwave") && msg.contains("start")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String reply;
            if (this.instance != null) {
                t.getMessage().getChannel().sendMessage("The microwave is already taken!");
            } else {
                IMessage msg = t.getMessage().getChannel().sendMessage("BEEP BEEP");
                this.instance = new MicrowaveRunnable(this, msg);
                new Thread(this.instance).start();
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(MicrowaveModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public String getCommand() {
        return "microwave";
    }
}
