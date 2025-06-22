package vn.com.lcx.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

public final class BCryptUtils {

    private static final Integer[] HASH_ROUND = new Integer[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private BCryptUtils() {
    }

    public static String hashPassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new NullPointerException("password is null");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(HASH_ROUND[0]));
    }

    public static void comparePassword(String password, String passHash) {
        if (StringUtils.isBlank(password) || StringUtils.isBlank(passHash)) {
            throw new NullPointerException("password or passHash is null");
        }

        if (!BCrypt.checkpw(password, passHash)) {
            throw new IllegalArgumentException("password or passHash is not correct");
        }
    }

}
