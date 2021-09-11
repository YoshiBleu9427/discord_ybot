package fr.yb.discordybot.gg2.map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubArchiveMapRepository implements MapRepository {
    private static Map<String, String> cachedNameToFileLocationMapping;
    private String repoOwner;
    private String repoName;
    private String branchName;

    public GitHubArchiveMapRepository(String repoOwner, String repoName, String branchName) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.branchName = branchName;
    }

    @Override
    public String getNotFoundMessage(String mapName) {
        String notFoundMessage = String.format("`%s` wasn't found in the map archives", mapName);
        try {
            SimilarMapFinder similarMapFinder = new SimilarMapFinder();
            double cutoffSimilarityScore = 0.85;  // magic value
            List<String> matches = similarMapFinder.getSimilarMapNames(
                mapName, this.getKnownMapNames(), 5, cutoffSimilarityScore);
            if (matches.isEmpty()) {
                return notFoundMessage;
            } else {
                StringBuilder sb = new StringBuilder(notFoundMessage);
                sb.append(". Did you mean ```");
                for (String possibleMapName : matches) {
                    sb.append(possibleMapName).append(System.lineSeparator());
                }
                sb.append("```");
                return sb.toString();
            }
        } catch (Exception e) {
            return notFoundMessage;
        }
    }

    @Override
    public String getMapFileURL(String mapName) throws IOException {
        Map<String, String> nameToFileLocationMapping = getNameToFileLocationMapping();
        String mapFilePath = nameToFileLocationMapping.get(mapName);
        if (mapFilePath == null) {
            return null;
        }
        return getContentURL(mapFilePath);
    }

    @Override
    public String getRandomMapName() throws Exception {
        Map<String, String> nameToFileLocationMapping = getNameToFileLocationMapping();
        Random rand = new Random();
        int nextInt = rand.nextInt(nameToFileLocationMapping.size());
        return nameToFileLocationMapping.keySet().toArray()[nextInt].toString(); // lmao
    }

    private String getContentURL(String mapFilePath) {
        try {
            return new URI(
                    "https",
                    "raw.githubusercontent.com",
                    String.format(
                            "/%s/%s/%s/%s",
                            repoOwner,
                            repoName,
                            branchName,
                            mapFilePath
                    ),
                    null
            ).normalize().toASCIIString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(GitHubArchiveMapRepository.class.getName()).log(Level.SEVERE, null, ex);
            return String.format("https://raw.githubusercontent.com/%s/%s/%s/%s",
                    repoOwner, repoName, branchName, mapFilePath); 
        }
    }

    private Stream<String> getKnownMapNames() throws IOException {
        Map<String, String> nameToFileLocationMapping = getNameToFileLocationMapping();
        return nameToFileLocationMapping.keySet().stream();
    }

    private Map<String, String> getNameToFileLocationMapping() throws IOException {
        if (cachedNameToFileLocationMapping == null) {
            generateMapArchive();
        }
        return cachedNameToFileLocationMapping;
    }

    private void generateMapArchive() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        URL treeReadPoint = new URL(getRepoTreeReadPoint(objectMapper) + "?recursive=1");
        Tree tree = objectMapper.readValue(treeReadPoint, Tree.class);
        if (tree.truncated) {
            Logger.getLogger(GitHubArchiveMapRepository.class.getName()).log(Level.WARNING,
                    "Tree couldn't be fetched completely, the archive might be too big");
        }
        cachedNameToFileLocationMapping = tree.tree.stream()
                .filter(TreeEntry::isMap)
                .collect(Collectors.toMap(TreeEntry::mapName, TreeEntry::getPath,
                        (entry1, entry2) -> {
                            Logger.getLogger(GitHubArchiveMapRepository.class.getName()).log(Level.INFO,
                                    String.format("Map name duplicate: %s - %s", entry1, entry2));
                            return entry1;
                        })
                );
    }

    private String getRepoTreeReadPoint(ObjectMapper objectMapper) throws IOException {
        URL readPoint = new URL(String.format(
                "https://api.github.com/repos/%s/%s/branches/%s", repoOwner, repoName, branchName));
        Branch response = objectMapper.readValue(readPoint, Branch.class);
        return response.commit.commit.tree.url;
    }

    static class Branch {

        //StATiCAlLY TypeD LaNgUaGE
        public HeadCommit commit;
    }

    static class HeadCommit {

        public Commit commit;
    }

    static class Commit {

        public CommitTree tree;
    }

    static class CommitTree {

        public String url;
    }

    static class TreeEntry {

        // this assumes that a non directory file ending with `.png` is a map
        static final Pattern mapNameFinder = Pattern.compile(".*/(\\w+)\\.png");
        public String path;
        public String type;
        private Matcher matcher;

        public boolean isMap() {
            matcher = mapNameFinder.matcher(path);
            return (isFile() && matcher.matches());
        }

        private boolean isFile() {
            return type.equals("blob");
        }

        public String mapName() {
            return matcher.group(1).toLowerCase();
        }

        public String getPath() {
            return path;
        }
    }

    private static class Tree {

        public boolean truncated;
        public List<TreeEntry> tree;
    }
}
