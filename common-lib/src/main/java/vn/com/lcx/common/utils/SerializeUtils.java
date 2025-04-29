package vn.com.lcx.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializeUtils {

    public static <T extends Serializable> void serialize(final T object, final String path, final String fileName) throws IOException {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(
                                FileUtils.pathJoining(
                                        path,
                                        String.format("%s.ser", fileName)
                                )
                        )
                );
        ) {
            oos.writeObject(object);
        }
    }

    public static <T extends Serializable> T deserialize(final String path, final String fileName) throws IOException, ClassNotFoundException {
        try (
                ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(
                                FileUtils.pathJoining(
                                        path,
                                        String.format("%s.ser", fileName)
                                )
                        )
                );
        ) {
            //noinspection unchecked
            return (T) ois.readObject();
        }

    }

}
