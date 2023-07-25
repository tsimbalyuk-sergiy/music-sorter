package dev.tsvinc.music.sort.util;

public enum LiveReleasesPatterns {
    SAT("-SAT-"),
    DVBS("-DVBS-"),
    SBD("-SBD-"),
    DAB("-DAB-"),
    FM("-FM-"),
    CABLE("-CABLE-"),
    DVBC("-DVBC-"),
    DVBT("-DVBT-"),
    LINE("-LINE-"),
    STREAM("-STREAM-)");

    private final String pattern;

    LiveReleasesPatterns(final String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
