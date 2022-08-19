package dev.tsvinc.music.sort.domain;

public class Metadata {

    private String genre;
    private String format;
    private String artist;
    private int year;
    private int audioFilesCount;
    private boolean invalid;

    public Metadata(
            final String genre,
            final String format,
            final String artist,
            final int audioFilesCount,
            final int year,
            final boolean invalid) {
        this.genre = genre;
        this.format = format;
        this.artist = artist;
        this.year = year;
        this.audioFilesCount = audioFilesCount;
        this.invalid = invalid;
    }

    public Metadata(final String genre, final String format) {
        this.genre = genre;
        this.format = format;
    }

    public Metadata() {}

    public static GenreWithFormatBuilder builder() {
        return new GenreWithFormatBuilder();
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(final String genre) {
        this.genre = genre;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public void setInvalid(final boolean invalid) {
        this.invalid = invalid;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public int getAudioFilesCount() {
        return this.audioFilesCount;
    }

    public void setAudioFilesCount(final int audioFilesCount) {
        this.audioFilesCount = audioFilesCount;
    }

    public static class GenreWithFormatBuilder {

        private String genre;
        private String format;
        private String artist;
        private int year;
        private int audioFilesCount;
        private boolean invalid;

        GenreWithFormatBuilder() {}

        public Metadata.GenreWithFormatBuilder genre(final String genre) {
            this.genre = genre;
            return this;
        }

        public Metadata.GenreWithFormatBuilder format(final String format) {
            this.format = format;
            return this;
        }

        public Metadata.GenreWithFormatBuilder artist(final String artist) {
            this.artist = artist;
            return this;
        }

        public Metadata.GenreWithFormatBuilder year(final int year) {
            this.year = year;
            return this;
        }

        public Metadata.GenreWithFormatBuilder audioFilesCount(final int audioFilesCount) {
            this.audioFilesCount = audioFilesCount;
            return this;
        }

        public Metadata.GenreWithFormatBuilder invalid(final boolean invalid) {
            this.invalid = invalid;
            return this;
        }

        public Metadata build() {
            return new Metadata(this.genre, this.format, this.artist, this.year, this.audioFilesCount, this.invalid);
        }
    }
}
