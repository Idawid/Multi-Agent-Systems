package utils;

import java.io.Serializable;

public enum BookType implements Serializable {
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