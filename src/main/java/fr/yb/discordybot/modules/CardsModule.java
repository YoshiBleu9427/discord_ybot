/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.modules;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.yb.discordybot.BotModule;
import fr.yb.discordybot.BotUtil;
import fr.yb.discordybot.model.cards.CardDefinition;
import fr.yb.discordybot.model.cards.CardRarity;
import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author Nicolas
 */
public class CardsModule extends BotModule {
    
    private ArrayList<CardDefinition> cardDefinitions;

    public CardsModule() {
        this.cardDefinitions = new ArrayList<>();
    }
    
    public void loadCardDefinitions() {
        Gson gson = new Gson();

        try {
            Type listType = new TypeToken<ArrayList<CardDefinition>>(){}.getType();
            this.cardDefinitions = gson.fromJson(new FileReader(this.getBot().getConfig().getCardsFile()), listType);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        // set IDs
        for (int i = 0; i < cardDefinitions.size(); i++) {
            cardDefinitions.get(i).setId(i);
        }
    }

    @Override
    public void start() {
        super.start();
        this.loadCardDefinitions();
    }
    
    private int cardRarityToColor(CardRarity r) {
        switch (r) {
            case ULTIMATE:
                return Color.decode("#9E1E9C").getRGB() & 0xFFFFFF;
            case SUPER:
                return Color.decode("#D1A21F").getRGB() & 0xFFFFFF;
            case RARE:
                return Color.decode("#9FE5ED").getRGB() & 0xFFFFFF;
            case COMMON:
                return Color.decode("#D8D8D8").getRGB() & 0xFFFFFF;
        }
        return 0;
    }
    
    private EmbedObject cardToEmbed(CardDefinition card) {
        EmbedObject eo = new EmbedObject();
        
        EmbedObject.ImageObject img = new EmbedObject.ImageObject();
        img.url = card.getImageURL();
        eo.image = img;
        
        eo.title = card.getName();
        eo.description = card.getDescription();
        eo.fields = new EmbedObject.EmbedFieldObject[] {
            new EmbedObject.EmbedFieldObject("HP", String.valueOf(card.getHp()), true),
            new EmbedObject.EmbedFieldObject("ATK", String.valueOf(card.getAtk()), true),
        };
        eo.color = this.cardRarityToColor(card.getRarity());
        eo.footer = new EmbedObject.FooterObject(String.valueOf(card.getId()), null, null);
        return eo;
    }
    
    private CardDefinition getRandomCardDefinition() {
        int index = BotUtil.random.nextInt(this.cardDefinitions.size());
        return this.cardDefinitions.get(index);
    }

    @Override
    public boolean handle(MessageReceivedEvent t) {
        try {
            this.getUtil().sendWithRateLimit(
                    this.cardToEmbed(this.getRandomCardDefinition()),
                    t.getChannel()
            );
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(TemModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public String help() {
        return "**CardsModule**: Collect cards of your favorite characters and local heroes, and make them fight in epic battles! Commands:\n"
                + "`" + this.getBot().getConfig().getPrefix() + " card get`, \n";
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public String getCommand() {
        return "card";
    }
}
