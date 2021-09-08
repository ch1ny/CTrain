package Servlets.User;

import Utils.JjwtUtil;
import Utils.SqlCon;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 21:24
 * @Description 恢复默认头像
 * @Version 1.0
 */

@WebServlet("/user/defaultAvatar")
public class DefaultAvatar extends HttpServlet {
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
                String userId = JjwtUtil.parseJWT(jwt).getSubject();
                Connection conn = SqlCon.getInstance();
                try {
                    PreparedStatement ps = conn.prepareStatement("UPDATE Users SET user_avatar = NULL, avatar = NULL WHERE user_id = ?");
                    ps.setString(1, userId);
                    ps.execute();
                    conn.commit();
                    pw.write("{\"code\":0, \"data\":\"设置成功\"}");
                } catch (SQLException e) {
                    pw.write("{\"code\":-2, \"data\":\"设置失败\"}");
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    e.printStackTrace();
                }
                try {
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
}
