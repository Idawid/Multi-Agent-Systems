package utils;

import java.util.List;
import java.util.stream.Collectors;

public class MyListSerializer {
    // Unique delimiter in my context
    public static String serialize(List<?> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining("!_!"));
    }

    public static List<?> deserialize(String str) {
        return List.of(str.split("!_!"));
    }
}
