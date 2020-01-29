package fr.yb.discordybot.modules;

import fr.yb.discordybot.BotModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * 
 * @author Void
 */
public class MinesweeperModule extends BotModule {

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            String[] commandSplit = t.getMessage().getContent().split(" ");
            int mines = 20;
            int size = 3;
            //   0         1   2 3 4
            // "ybot minesweep 1 2";
            if (commandSplit.length > 2) {            
                mines = Integer.parseInt(commandSplit[2]);
            }
            if (commandSplit.length > 3) {            
                size = Integer.parseInt(commandSplit[3]);
            }
            String reply = this.makeField(mines, size);
            if (reply.length() > 2000) {
                t.getChannel().sendMessage("Minefield too big to fit in a message, maybe reduce the field size");
            } else {
                t.getChannel().sendMessage(reply);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(HelpModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String help() {
        return "**MinesweeperModule**: Made by Beryllium#2215. Generates a minesweeper field. `ybot minesweeper <mines> <field_size>`\n";
    }

    @Override
    public boolean isInterestedIn(MessageReceivedEvent t) {
        if (!this.getUtil().isMessageForMe(t)) {
            return false;
        }
        return t.getMessage().getContent().toLowerCase().startsWith("ybot minesweeper");
    }

    public String makeField(int mines, int sizemult) {
        ////////vars
        final int xWidth = 6 + sizemult; //discord supports 21x9 or 20x10 (with the bottom right two missing)
        final int yWidth = 3 + sizemult; //BASE
        // mines = 40; //recommended 40-100, this is old code now
        if (mines > xWidth * yWidth || mines < 0) {
            throw new IllegalArgumentException("Invalid amount of mines! (must be an integer between 1 and " + String.valueOf(xWidth * yWidth) + ")");
        }
        ////////init
        char[][] area = new char[xWidth][yWidth];
        for (int i = 0; i < xWidth; i += 1) {
            for (int j = 0; j < yWidth; j += 1) {
                area[i][j] = " ".charAt(0); //fill with space
            }
        }
        ////////make mines
        for (int i = 0; i < mines; i += 1) {
            int x = (int) Math.floor(Math.random() * xWidth);
            int y = (int) Math.floor(Math.random() * yWidth);
            if (area[x][y] != "X".charAt(0)) {
                for (int j = -1; j < 2; j += 1) {
                    for (int k = -1; k < 2; k += 1) {
                        if (!(x + j < 0 || y + k < 0 || x + j >= xWidth || y + k >= yWidth || area[x + j][y + k] == "X".charAt(0))) {
                            if (area[x + j][y + k] == " ".charAt(0)) {
                                area[x + j][y + k] = "1".charAt(0);
                            } else {
                                area[x + j][y + k] += 1;
                            }
                        }
                    }
                }

                area[x][y] = "X".charAt(0);
            }
        }
        ////////output
        String output = "";
        for (int i = 0; i < yWidth; i += 1) {
            for (int j = 0; j < xWidth; j += 1) {
                output += "||`" + area[j][i] + "`||";
            }
            output += "\n";
        }
        return output;
    }
}
