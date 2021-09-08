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
 * @Date 2021/8/11 9:26
 * @Description 判断用户是否具备管理员权限
 * @Version 1.0
 */

@WebServlet("/admin/vertify")
public class AmAdmin extends HttpServlet {

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
                Claims claims = JjwtUtil.parseJWT(jwt);
                String userId = claims.getSubject();
                Connection conn = SqlCon.getInstance();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("SELECT 1 FROM Admins WHERE user_id = ?");
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        pw.write("{\"code\": 0, \"data\":\"欢迎管理员\"}");
                    } else {
                        pw.write("{\"code\": -2, \"data\":\"无管理员权限\"}");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
    }
}
