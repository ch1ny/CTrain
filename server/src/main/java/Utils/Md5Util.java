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
        sb.append(info.charAt(0) + info.charAt(info.length() - 1));
        int saltIndex = info.charAt(0);
        for (int i = 1; i < info.length(); i++) {
            sb.append(info.charAt(i - 1) + info.charAt(i));
            saltIndex += info.charAt(i);
        }
        saltIndex %= 10;
        String salt = sb.toString();
        return PswToMd5(salt.substring(0, saltIndex) + password + salt.substring(saltIndex));
    }

}
