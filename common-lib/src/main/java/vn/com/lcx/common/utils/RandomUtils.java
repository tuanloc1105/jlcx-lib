package vn.com.lcx.common.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    /**
     * Generates a random string of the specified length.
     *
     * @param length the desired length of the random string
     * @return a random string composed of characters from CHARACTERS
     * @throws IllegalArgumentException if length is negative
     */
    public static String generateRandomString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static int getRandomNumber(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min phải nhỏ hơn hoặc bằng max");
        }
        return RANDOM.nextInt((max - min) + 1) + min;
    }

    public static <T> T getRandomElementAndRemove(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Danh sách không được rỗng");
        }
        int index = RANDOM.nextInt(list.size());
        return list.remove(index);
    }

    public static <T> void shuffleList(List<T> list) {
        Collections.shuffle(list);
    }

}
