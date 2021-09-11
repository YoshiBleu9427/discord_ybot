package fr.yb.discordybot.gg2.map;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;

public class SimilarMapFinder {
    public List<String> getSimilarMapNames(String mapNameToMatch, Stream<String> knownMapNames,
                                           int first, double cutoffDistanceScore) {
        SimilarityScore<Double> stringComparator = new JaroWinklerSimilarity();
        MapName mapToMatch = new MapName(mapNameToMatch);
        Stream<MapName> knownMaps = knownMapNames.map(MapName::new);
        if (mapToMatch.hasGameMode()) {
            knownMaps = knownMaps.filter(mapToMatch::sameGameMode);
        }
        return knownMaps
            .map(mapName -> new AbstractMap.SimpleEntry<>(
                mapName.fullName, stringComparator.apply(mapName.getName(), mapToMatch.getName())))
            .filter(entry -> entry.getValue() >= cutoffDistanceScore)
            .sorted(Map.Entry.comparingByValue(reverseOrder()))
            .limit(first)
            .map(AbstractMap.SimpleEntry::getKey)
            .collect(Collectors.toList());
    }

    private static class MapName {
        private static final Pattern gameModeFinder = Pattern.compile("([a-z0-9]+)_(.+)");
        String fullName;
        private String gameMode;
        private String name;

        MapName(String mapName) {
            this.fullName = mapName;
            parseGameMode(mapName);
        }

        boolean hasGameMode() {
            return this.gameMode != null;
        }

        boolean sameGameMode(MapName other) {
            return this.gameMode.equals(other.gameMode);
        }

        String getName() {
            if (this.name == null) {
                return this.fullName;
            }
            return name;
        }

        private void parseGameMode(String mapName) {
            Matcher matcher = gameModeFinder.matcher(mapName);
            if (matcher.matches()) {
                this.gameMode = matcher.group(1);
                this.name = matcher.group(2);
            } else {
                this.gameMode = null;
                this.name = null;
            }
        }
    }
}
