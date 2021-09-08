package AliPay;

import Utils.MyConfig;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/23 17:51
 * @Description Alipay配置文件
 * @Version 1.0
 */

public class AlipayConfig {

    // 此处MyConfig.java未上传，内部仅配置部分项目口令，可自行修改

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数
    public static String RETURN_URL = "https://12306.aiolia.top/#/pay/success";
    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String NOTIFY_URL = "http://121.4.250.38:8080/ctrain/alipay/notify";
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String APPID = MyConfig.ALIPAY_APPID;
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String ALIPAY_PUBLIC_KEY = MyConfig.ALIPAY_PUBLIC_KEY;
    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String RSA_PRIVATE_KEY = MyConfig.RSA_PRIVATE_KEY;
    // 签名方式
    public static String SIGNTYPE = "RSA2";
    // 字符编码格式
    public static String CHARSET = "utf-8";
    // 支付宝网关
    public static String URL = "https://openapi.alipaydev.com/gateway.do";

    public static String FORMAT = "json";
}
