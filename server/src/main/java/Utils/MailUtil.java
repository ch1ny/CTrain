package Utils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/15 19:56
 * @Description SMTP工具类
 * @Version 1.0
 */

public class MailUtil {

    // 此处MyConfig.java未上传，内部仅配置部分项目口令，可自行修改
    private static final String MAIL_HOST = MyConfig.SMTP_HOST; // 发送邮件的主机
    private static final String FROM = MyConfig.SMTP_SENDER; // 发件人邮箱地址
    private static Properties props = System.getProperties();
    private static Session session = Session.getInstance(props, null);// 获得Session对象
    private static Message message = new MimeMessage(session);

    public static void sendCaptcha(String receive, String title, String context) throws MessagingException {
        props.setProperty("mail.smtp.host", MAIL_HOST); // 发送邮件的主机
        props.setProperty("mail.smtp.auth", "true");
        session.setDebug(false); // 设置是否显示debug信息,true 会在控制台显示相关信息
        /*
         * 创建邮件消息，发送邮件
         */
        message.setFrom(new InternetAddress(FROM));
        // To: 收件人
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(receive, false));
        message.setSubject(title); // 邮件标题
        message.setText(context); // 邮件内容
        // 简单发送邮件的方式
        Transport.send(message, FROM, MyConfig.SMTP_TOKEN);
    }

}
