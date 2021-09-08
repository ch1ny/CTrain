package Servlets.Admin;

import Utils.JjwtUtil;
import Utils.SqlCon;
import io.jsonwebtoken.Claims;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/12 17:19
 * @Description 该列车是否拥有座位表
 * @Version 1.0
 */

@WebServlet("/admin/ifGeneratedSeats")
public class IfGeneratedSeats extends HttpServlet {
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
                Claims c = JjwtUtil.parseJWT(jwt);
                Connection conn = null;
                try {
                    String userId = c.getSubject();
                    conn = SqlCon.getInstance();
                    PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Admins WHERE user_id = ?");
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        String id = req.getParameter("train");
                        ps = conn.prepareStatement("SELECT 1 FROM Seats WHERE train_id = ?");
                        ps.setString(1, id);
                        ps.execute();
                        rs = ps.getResultSet();
                        if (rs.next()) {
                            pw.write("{\"code\":0, \"data\":" + true + "}");
                        } else {
                            pw.write("{\"code\":0, \"data\":" + false + "}");
                        }
                    } else {
                        pw.write("{\"code\":-2, \"data\":\"权限不足\"}");
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException throwables) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    pw.write("{\"code\":-3, \"data\":\"数据库出错\"}");
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
