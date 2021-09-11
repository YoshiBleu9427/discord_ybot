package fr.yb.discordybot.gg2;

public interface MapRepository {
    String getMapFileURL(String mapName) throws Exception;
    String getNotFoundMessage(String mapName);
    String getRandomMapName() throws Exception;
}
