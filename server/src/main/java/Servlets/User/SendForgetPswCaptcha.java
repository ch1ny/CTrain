package Servlets.User;

import Utils.MailUtil;
import Utils.SqlCon;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 16:28
 * @Description 忘记密码发送验证码
 * @Version 1.0
 */

@WebServlet("/user/sendFPC")
public class SendForgetPswCaptcha extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        String userId = req.getParameter("user");
        Connection conn = SqlCon.getInstance();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT user_email FROM Users WHERE user_id = ?");
            ps.setString(1, userId);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                String email = rs.getString("user_email");
                HttpSession session = req.getSession();
                String captcha = buildCaptcha();
                try {
                    String title = "【CTrain】重置密码";
                    String context = "【山东大学2019级软件工程数据库课程设计】您的验证码为" + captcha + "，该验证码将用于重置账号密码，10分钟内有效，请勿泄露给他人";
                    MailUtil.sendCaptcha(email, title, context);
                    session.setMaxInactiveInterval(600);
                    session.setAttribute("forgetCaptcha", captcha + userId);
                    PrintWriter pw = resp.getWriter();
                    pw.write("{\"code\":0, \"data\":\"" + email.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4") + "\"}");
                    pw.close();
                } catch (MessagingException e) {
                    PrintWriter pw = resp.getWriter();
                    pw.write("{\"code\":-1, \"data\":\"验证码发送失败\"}");
                    pw.close();
                    e.printStackTrace();
                }
            } else {
                PrintWriter pw = resp.getWriter();
                pw.write("{\"code\":-1, \"data\":\"该用户不存在\"}");
                pw.close();
            }
            conn.close();
        } catch (SQLException e) {
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
