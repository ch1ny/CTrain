package Servlets.User;

import Utils.JjwtUtil;
import Utils.Md5Util;
import Utils.SqlCon;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/7 23:12
 * @Description 用户登录
 * @Version 1.0
 */

@WebServlet("/user/login")
public class Login extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        Connection conn = null;
        try {
            String email = req.getParameter("email");
            String psw = req.getParameter("psw");
            conn = SqlCon.getInstance();
            PreparedStatement ps = conn.prepareStatement("SELECT user_id, user_name, user_password, user_avatar FROM Users WHERE user_email = ?");
            ps.setString(1, email);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            int code;
            String result;
            if (rs.next()) {
                String userId = rs.getString("user_id");
                psw = Md5Util.SaltMd5(userId, psw);
                if (rs.getString("user_password").equals(psw)) {
                    code = 0;
                    String name = rs.getString("user_name");
                    result = JjwtUtil.createJWT(userId, email, name);
                } else {
                    code = -1;
                    result = "用户名或密码错误";
                }
            } else {
                code = -2;
                result = "该用户不存在";
            }
            PrintWriter pw = resp.getWriter();
            pw.write("{\"code\":" + code + ", \"result\":\"" + result + "\"}");
            pw.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}
