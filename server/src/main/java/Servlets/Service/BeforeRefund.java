package Servlets.Service;

import Utils.JjwtUtil;
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
 * @Date 2021/9/5 22:04
 * @Description 发送退票验证码
 * @Version 1.0
 */

@WebServlet("/service/beforeRefund")
public class BeforeRefund extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        String jwt = req.getHeader("Authorization");
        String ticketId = req.getParameter("ticket");
        if (jwt == null || jwt.equals("Bearer null")) {
            pw.write("{\"code\": -1, \"data\":\"用户未登录\"}");
        } else {
            int jwtVer = JjwtUtil.vertify(jwt);
            if (jwtVer == 0) {
                String userId = JjwtUtil.parseJWT(jwt).getSubject();
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
                        String title = "【CTrain】退款身份验证";
                        String context = "【山东大学2019级软件工程数据库课程设计】您的验证码为" + captcha + "，该验证码将用于执行退款操作，10分钟内有效，请勿泄露给他人";
                        try {
                            MailUtil.sendCaptcha(email, title, context);
                            session.setMaxInactiveInterval(600);
                            session.setAttribute("refundCaptcha", captcha + ticketId);
                            pw.write("{\"code\":0, \"data\":\"" + email.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4") + "\"}");
                        } catch (MessagingException e) {
                            e.printStackTrace();
                            pw.write("{\"code\":-1, \"data\":\"验证码发送失败\"}");
                        }
                    } else {
                        pw.write("{\"code\":-1, \"data\":\"该用户不存在\"}");
                    }
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
        pw.close();
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
