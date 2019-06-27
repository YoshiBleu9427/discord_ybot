package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
public class RollModule extends BotModule {
    
    public static String PREFIX = "ybot roll";
    public static int ROLL_DEFAULT = 20;
    public static int ROLL_MIN = 2;
    public static int ROLL_MAX = 999;
    public static int DICE_MIN = 1;
    public static int DICE_MAX = 50;
    public static int MOD_MIN = -99999;
    public static int MOD_MAX = 99999;
    
    public class ParseResult {
        private int diceCount = 1;
        private int diceFaces = ROLL_DEFAULT;
        private int modifier = 0;
        private boolean hasModifier = false;

        public ParseResult() {
        }

        public ParseResult(int diceFaces) {
            this.diceFaces = diceFaces;
        }

        public ParseResult(int diceCount, int diceFaces) {
            this.diceCount = diceCount;
            this.diceFaces = diceFaces;
        }

        public ParseResult(int diceCount, int diceFaces, int modifier) {
            this.diceCount = diceCount;
            this.diceFaces = diceFaces;
            this.modifier = modifier;
            this.hasModifier = true;
        }

        public int getDiceCount() {
            return diceCount;
        }

        public void setDiceCount(int diceCount) {
            this.diceCount = diceCount;
        }

        public int getDiceFaces() {
            return diceFaces;
        }

        public void setDiceFaces(int diceFaces) {
            this.diceFaces = diceFaces;
        }
        
        public boolean hasModifier() {
            return this.hasModifier;
        }

        public int getModifier() {
            return modifier;
        }

        public void setModifier(int modifier) {
            this.modifier = modifier;
            this.hasModifier = true;
        }
        
        public void clearModifier() {
            this.hasModifier = false;
        }
    }
    
    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String reply;
            try {
                ParseResult data = this.parseStringData(t.getMessage().getContent());
                
                if (data.getDiceCount() > DICE_MAX) {
                    reply = "Too many dice given, please no more than " + DICE_MAX;
                }
                else if (data.getDiceCount() < DICE_MIN) {
                    reply = "Not enough dice given, please no less than " + DICE_MIN;
                }
                else if (data.getDiceFaces() > ROLL_MAX) {
                    reply = "Roll number given too high, please no more than " + ROLL_MAX;
                }
                else if (data.getDiceFaces() < ROLL_MIN) {
                    reply = "Roll number given too low, please no less than " + ROLL_MIN;
                }
                else if (data.hasModifier() && data.getModifier() > MOD_MAX) {
                    reply = "Roll number given too high, please no more than " + MOD_MAX;
                }
                else if (data.hasModifier() && data.getModifier() < MOD_MIN) {
                    reply = "Roll number given too low, please no less than " + MOD_MIN;
                }
                else {
                    int[] results = this.getRandoms(data.getDiceCount(), data.getDiceFaces());
                    int sum = 0;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < results.length; i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(results[i]);
                        sum += results[i];
                    }
                    if (data.hasModifier()) {
                        sum += data.getModifier();
                    }
                    
                    String modifierString = "";
                    if (data.hasModifier()) {
                        modifierString =
                                (data.getModifier() > 0 ? "+" : "") // needs sign if positive
                                + String.valueOf(data.getModifier());
                    }

                    reply = String.format(
                            "%dd%d%s: [%s]%s = **%d**",
                            data.getDiceCount(),
                            data.getDiceFaces(),
                            modifierString,
                            sb.toString(),
                            modifierString,
                            sum
                    );
                }
            } catch (NumberFormatException ex) {
                reply = "Bad roll format! Try:\n"
                        + "```"
                        + "ybot roll <1-999>\n"
                        + "ybot roll <1-50>d<1-999>\n"
                        + "ybot roll <1-50>d<1-999>(+/-<1-9999>)\n"
                        + "ybot roll <1-50> <1-999>"
                        + "```";
            }
            
            t.getMessage().getChannel().sendMessage(reply);
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(RollModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private int[] getRandoms(int dice, int roll) {
        int[] results = new int[dice];
        for (int i = 0; i < dice; i++) {
            results[i] = this.getRandomInt(roll) + 1;
        }
        return results;
    }
    
    private ParseResult parseStringData(String msg) {
        String withoutPrefix = msg.substring(PREFIX.length()).trim();
        
        if (withoutPrefix.isEmpty()) {
            return new ParseResult(ROLL_DEFAULT);
        }
        
        try {
            return new ParseResult(Integer.parseInt(withoutPrefix));
        } catch (NumberFormatException e) {
            if (withoutPrefix.contains("d")) {
                String[] splitByD = withoutPrefix.split("d", 2);
                
                if (splitByD[1].contains("+")) {
                    String[] splitByPlus = splitByD[1].split("\\+", 2);
                    return new ParseResult(
                            Integer.parseInt(splitByD[0]),
                            Integer.parseInt(splitByPlus[0].trim()),
                            Integer.parseInt(splitByPlus[1].trim())
                    );
                } else if (splitByD[1].contains("-")) {
                    String[] splitByMinus = splitByD[1].split("-", 2);
                    return new ParseResult(
                            Integer.parseInt(splitByD[0]),
                            Integer.parseInt(splitByMinus[0].trim()),
                            -1 * Integer.parseInt(splitByMinus[1].trim())
                    );
                } else {
                    return new ParseResult(
                            Integer.parseInt(splitByD[0]),
                            Integer.parseInt(splitByD[1])
                    );
                }
            }
            else if (withoutPrefix.contains(" ")) {
                String[] splitBySpace = withoutPrefix.split("\\ ", 2);
                return new ParseResult(
                        Integer.parseInt(splitBySpace[0]),
                        Integer.parseInt(splitBySpace[1])
                );
            }
            else {
                throw new NumberFormatException("bad format");
            }
        }
    }

    private int getRandomInt(int ceil) {
        return BotUtil.random.nextInt(ceil);
    }

    @Override
    public String help() {
        return "**RollModule**: Rolls dice. `ybot roll 20`, `ybot roll 2 6` = 2d6, `ybot roll 2d6, `ybot roll 2d4-1, `ybot roll 1d6+3`. `ybot roll` will roll a single d20\n";
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        String lowerMsg = t.getMessage().getContent().toLowerCase();
        return lowerMsg.startsWith(PREFIX);
    }

}
