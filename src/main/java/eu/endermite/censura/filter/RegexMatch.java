package eu.endermite.censura.filter;

import java.util.regex.Pattern;

public class RegexMatch implements MatchType {
    private final Pattern pattern;

    public RegexMatch(String input) {
        this.pattern = Pattern.compile(input);
    }

    @Override
    public boolean match(String message, FilterCache cache) {
        /* This is stupid, "forgemc.pl google.com" won't be filtered. */
        if (message.contains("forgemc.pl")) {
            return false;
        }

        return pattern.matcher(message).matches();
    }

    @Override
    public String getSnippet() {
        return pattern.toString();
    }

    @Override
    public String getType() {
        return "regex";
    }
}
