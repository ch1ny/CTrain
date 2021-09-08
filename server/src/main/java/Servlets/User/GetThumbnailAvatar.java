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
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/28 20:57
 * @Description 获取头像缩略图
 * @Version 1.0
 */

@WebServlet("/user/getThumbnailAvatar")
public class GetThumbnailAvatar extends HttpServlet {
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
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT avatar FROM Users WHERE user_id = ?");
                    String userId = JjwtUtil.parseJWT(jwt).getSubject();
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    String avatar = "";
                    if (rs.next()) {
                        Blob blob = rs.getBlob("avatar");
                        if (blob != null) {
                            avatar = new String(blob.getBytes(1, (int) blob.length()), StandardCharsets.UTF_8);
                        }
                    }
                    pw.write("{\"code\":0, \"data\":\"" + avatar + "\"}");
                } catch (SQLException throwables) {

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
            pw.close();
        }
    }
}
