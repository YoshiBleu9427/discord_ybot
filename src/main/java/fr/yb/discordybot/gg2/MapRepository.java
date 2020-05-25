package fr.yb.discordybot.gg2;

public interface MapRepository {
    String getMapLocation(String mapName) throws Exception;
    String getNotFoundMessage(String mapName);
}
