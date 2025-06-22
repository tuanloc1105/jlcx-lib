package vn.com.lcx.common.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class SerializeUtils {

    private SerializeUtils() {
    }

    /**
     * Serializes the given object to a file in the given path and fileName.
     *
     * @param object   the object to serialize
     * @param path     the path to the file
     * @param fileName the name of the file
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Deserializes the given object from a file in the given path and fileName.
     *
     * @param path     the path to the file
     * @param fileName the name of the file
     * @return the deserialized object
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of the object cannot be found
     */
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
