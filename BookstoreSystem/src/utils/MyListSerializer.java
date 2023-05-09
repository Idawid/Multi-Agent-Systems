package utils;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class MyListSerializer {
    public static <T extends Serializable> String serializeList(List<T> list) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {

            out.writeObject(list);
            out.flush();
            byte[] bytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends Serializable> List<T> deserializeList(String str) {
        try {
            byte[] bytes = Base64.getDecoder().decode(str);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream in = new ObjectInputStream(bis)) {

                List<T> list = (List<T>)in.readObject();
                return list;

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
