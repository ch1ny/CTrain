package Servlets.User;

import Utils.MailUtil;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/15 20:01
 * @Description 注册发送验证码
 * @Version 1.0
 */

@WebServlet("/user/sendRegisterCaptcha")
public class SendRegisterCaptcha extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        String email = req.getParameter("email");
        HttpSession session = req.getSession();
        String captcha = buildCaptcha();
        try {
            String title = "【CTrain】注册验证码";
            String context = "【山东大学2019级软件工程数据库课程设计】您的验证码为" + captcha + "，10分钟内有效，请勿泄露给他人";
            MailUtil.sendCaptcha(email, title, context);
            session.setMaxInactiveInterval(600);
            session.setAttribute("registerCaptcha", captcha + email);
            PrintWriter pw = resp.getWriter();
            pw.write("{\"code\":0, \"data\":\"验证码已发送\"}");
            pw.close();
        } catch (MessagingException e) {
            PrintWriter pw = resp.getWriter();
            pw.write("{\"code\":-1, \"data\":\"验证码发送失败\"}");
            pw.close();
            e.printStackTrace();
        }
    }

    private String buildCaptcha() {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }
}
