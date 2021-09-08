package Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/8 1:22
 * @Description Jjwt工具类
 * @Version 1.0
 */

public class JjwtUtil {

    // 加密密文，私钥
    // 此处MyConfig.java未上传，内部仅配置部分项目口令，可自行修改
    public static final String JWT_SECRET = MyConfig.JWT_SECRET;

    // 过期时间，单位毫秒
    public static final int EXPIRE_TIME_HOUR = 60 * 60 * 1000; // 一个小时
	public static final long EXPIRE_TIME_WEEK = 7 * 24 * 3600 * 1000; // 一个星期

    // 由字符串生成加密key
    public static SecretKey generalKey() {
        // 本地的密码解码
        byte[] encodedKey = Base64.getDecoder().decode(JWT_SECRET);
        // 根据给定的字节数组使用AES加密算法构造一个密钥
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }

    public static String createJWT(String userId, String userEmail, String userName) {
        // 指定header那部分签名的时候使用的签名算法，jjwt已经将这部分内容封装好了，只有{"alg":"HS256"}
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        // 生成JWT的时间
        long nowTime = System.currentTimeMillis();
        Date issuedAt = new Date(nowTime);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEmail);
        // 生成签名的时候使用的秘钥secret，切记这个秘钥不能外露，是你服务端的私钥，在任何场景都不应该流露出去，一旦客户端得知这个secret，那就意味着客户端是可以自我签发jwt的
        SecretKey key = generalKey();
        JwtBuilder jb = Jwts.builder();
        jb.setSubject(userId);
        jb.setAudience(userName);
        jb.addClaims(claims);
        jb.setIssuedAt(issuedAt);
        jb.signWith(signatureAlgorithm, key);
        // 设置过期时间
        long expTime = EXPIRE_TIME_WEEK;
        if (expTime >= 0) {
            long exp = nowTime + expTime;
            jb.setExpiration(new Date(exp));
        }
        return jb.compact();
    }

    // 解密jwt
    public static Claims parseJWT(String jwt) {
        if (jwt.indexOf("Bearer ") == 0) {
            jwt = jwt.substring(jwt.indexOf(" "));
        }
        SecretKey key = generalKey(); // 签名秘钥，和生成的签名的秘钥一模一样
        Claims claims = Jwts.parser() // 得到DefaultJwtParser
                .setSigningKey(key) // 设置签名的秘钥
                .parseClaimsJws(jwt).getBody(); // 设置需要解析的jwt
        return claims;
    }

    // 验证jwt
    public static int vertify(String jwt) {
        Date now = new Date();
        Claims claims = null;
        try {
            claims = parseJWT(jwt);
        } catch (Exception e) {
            return -1;
        }
        long iatLong = Long.valueOf(claims.get("iat").toString());
        long expLong = Long.valueOf(claims.get("exp").toString());
        Date iat = new Date(iatLong * 1000);
        Date exp = new Date(expLong * 1000);
        if (now.after(iat) && now.before(exp)) {
            return 0;
        } else {
            return -2;
        }
    }

}
