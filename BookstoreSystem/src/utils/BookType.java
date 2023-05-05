package utils;

public enum BookType {
    IT("IT"),
    SCIENCE("Science"),
    FICTION("Fiction"),
    HORROR("Horror");

    private final String displayName;

    BookType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}