package eu.endermite.censura.filter;

import eu.endermite.censura.Censura;
import java.util.regex.Pattern;

public class ContainMatch implements MatchType {
    private final String snippet;
    private static final Pattern singleLetterSurroundedBySpacers = Pattern.compile("^(\\w)\\W+(?=\\w\\w)|\\W+(\\w)((\\W+(?=\\w\\w))|(?!\\w))");

    public ContainMatch(String input) {
        this.snippet = input;
    }

    @Override
    public boolean match(String message, FilterCache cache) {
        if (cache.spammySpacesRemoved == null) {
            cache.spammySpacesRemoved = singleLetterSurroundedBySpacers.matcher(message).replaceAll("$1$2");
        }
        if (cache.spammySpacesRemovedNoRepeat == null) {
            cache.spammySpacesRemovedNoRepeat = noRepeatChars(cache.spammySpacesRemoved);
        }

        if (cache.spammySpacesRemovedNoRepeat.contains(snippet) ||
                cache.spammySpacesRemoved.contains(snippet)) {
            if (Censura.getCachedConfig().isLogDetections())
                Censura.getPlugin().getLogger().info("Detected \""+snippet+"\" in phrase \""+message+"\" (Contain match)");
            return true;
        }
        return false;
    }

    private static String noRepeatChars(String string) {
        char[] chars;
        chars = string.toCharArray();
        StringBuilder result = new StringBuilder();
        char lastChar = ' ';
        for (char c : chars) {
            if (lastChar == c)
                continue;
            lastChar = c;
            result.append(c);
        }
        return result.toString();
    }
}