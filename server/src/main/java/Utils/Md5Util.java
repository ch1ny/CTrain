package Utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 20:43
 * @Description MD5工具类
 * @Version 1.0
 */

public class Md5Util {

    public static String PswToMd5(String psw) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(psw.getBytes());
        return new BigInteger(1, md5.digest()).toString(16);
    }

    public static String SaltMd5(String info, String password) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder(18);
        for (int i = 0; i < info.length(); i++) {
            sb.append(info.charAt(i) + info.charAt(i));
        }
        String salt = sb.toString();
        return PswToMd5(password + salt);
    }

}
