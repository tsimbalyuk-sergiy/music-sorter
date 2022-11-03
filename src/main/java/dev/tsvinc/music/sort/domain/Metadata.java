package dev.tsvinc.music.sort.domain;

public record Metadata(String genre, String format, String artist, int year, int audioFilesCount, boolean invalid) {
    public static MetadataBuilder builder() {
        return new MetadataBuilder();
    }

    public static class MetadataBuilder {

        private String genre;
        private String format;
        private String artist;
        private int year;
        private int audioFilesCount;
        private boolean invalid;

        MetadataBuilder() {}

        public MetadataBuilder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public MetadataBuilder format(String format) {
            this.format = format;
            return this;
        }

        public MetadataBuilder artist(String artist) {
            this.artist = artist;
            return this;
        }

        public MetadataBuilder year(int year) {
            this.year = year;
            return this;
        }

        public MetadataBuilder audioFilesCount(int audioFilesCount) {
            this.audioFilesCount = audioFilesCount;
            return this;
        }

        public MetadataBuilder invalid(boolean invalid) {
            this.invalid = invalid;
            return this;
        }

        public Metadata build() {
            return new Metadata(genre, format, artist, year, audioFilesCount, invalid);
        }

        public String toString() {
            return "Metadata.MetadataBuilder(genre=" + this.genre + ", format=" + this.format + ", artist="
                    + this.artist + ", year=" + this.year + ", audioFilesCount=" + this.audioFilesCount + ", invalid="
                    + this.invalid + ")";
        }
    }
}
