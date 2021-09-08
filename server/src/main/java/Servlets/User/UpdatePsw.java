package Servlets.User;

import Utils.JjwtUtil;
import Utils.Md5Util;
import Utils.SqlCon;
import io.jsonwebtoken.Claims;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @Date 2021/8/10 23:22
 * @Description 修改密码
 * @Version 1.0
 */

@WebServlet("/user/updatePsw")
public class UpdatePsw extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        String jwt = req.getHeader("Authorization");
        if (jwt == null || jwt.equals("Bearer null")) {
            pw.write("{\"code\": -1, \"data\":\"用户未登录\"}");
        } else {
            int jwtVer = JjwtUtil.vertify(jwt);
            if (jwtVer == 0) {
                Connection conn = SqlCon.getInstance();
                String userId = JjwtUtil.parseJWT(jwt).getSubject();
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT user_password FROM Users WHERE user_id = ?");
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (!rs.next()) {
                        pw.write("{\"code\":-2, \"data\":\"没有该账号！\"}");
                    } else {
                        String oldPsw = req.getParameter("oldPsw");
                        String newPsw = req.getParameter("newPsw");
                        if (Md5Util.SaltMd5(userId, oldPsw).equals(rs.getString("user_password"))) {
                            ps = conn.prepareStatement("UPDATE Users SET user_password=? WHERE user_id=?");
                            ps.setString(1, Md5Util.SaltMd5(userId, newPsw));
                            ps.setString(2, userId);
                            ps.execute();
                            conn.commit();
                            pw.write("{\"code\":0, \"data\":\"密码修改成功！\"}");
                        } else {
                            pw.write("{\"code\":-3, \"data\":\"原有密码输入错误！\"}");
                        }
                    }
                } catch (SQLException | NoSuchAlgorithmException throwables) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    pw.write("{\"code\":-4, \"data\":\"数据库写入错误！\"}");
                }
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
        pw.close();
    }
}
