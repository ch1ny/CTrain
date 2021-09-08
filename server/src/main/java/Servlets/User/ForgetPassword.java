package Servlets.User;

import Utils.Md5Util;
import Utils.SqlCon;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 16:39
 * @Description 忘记密码
 * @Version 1.0
 */

@WebServlet("/user/forgetPsw")
public class ForgetPassword extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        Connection conn = SqlCon.getInstance();
        String userId = req.getParameter("user");
        String name = req.getParameter("name");
        String psw = req.getParameter("psw");
        String captcha = req.getParameter("captcha");
        HttpSession session = req.getSession();
        if (session.getAttribute("forgetCaptcha") == null || !session.getAttribute("forgetCaptcha").equals(captcha + userId)) {
            pw.write("{\"code\":-3, \"result\":\"验证码错误或已超时\"}");
            pw.close();
            return;
        }
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT user_name FROM Users WHERE user_id = ?");
            ps.setString(1, userId);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                if (rs.getString("user_name").equals(name)) {
                    ps = conn.prepareStatement("UPDATE Users SET user_password = ? WHERE user_id = ?");
                    ps.setString(1, Md5Util.SaltMd5(userId, psw));
                    ps.setString(2, userId);
                    ps.execute();
                    conn.commit();
                    pw.write("{\"code\":0, \"result\":\"密码修改成功\"}");
                } else {
                    pw.write("{\"code\":-2, \"result\":\"用户姓名与身份证号不匹配！\"}");
                }
            } else {
                pw.write("{\"code\":-1, \"result\":\"该用户不存在\"}");
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            e.printStackTrace();
        }
        pw.close();
    }
}
