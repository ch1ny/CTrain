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
import java.util.*;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/11 11:08
 * @Description 管理员新增线路
 * @Version 1.0
 */

@WebServlet("/admin/addTrain")
public class AddTrain extends HttpServlet {

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
                        String id = req.getParameter("id").toUpperCase(Locale.ROOT);
                        String type = req.getParameter("type");
                        if (vertifyType(id, type)) {
                            ps = conn.prepareStatement("INSERT INTO Trains (train_id, train_type) VALUES (?,?)");
                            ps.setString(1, id);
                            ps.setString(2, type);
                            ps.execute();
                            conn.commit();
                            pw.write("{\"code\":0, \"data\":\"添加成功\"}");
                        } else {
                            pw.write("{\"code\":-2, \"data\":\"请确认列车类型\"}");
                        }
                    } else {
                        pw.write("{\"code\":-2, \"data\":\"权限不足\"}");
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException throwables) {
                    try {
                        conn.rollback();
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    pw.write("{\"code\":-3, \"data\":\"数据库写入失败\"}");
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
            pw.close();
        }
    }

    // 验证列车车次号与列车类型是否一致
    private boolean vertifyType(String id, String type) {
        switch (id.substring(0, 1)) {
            case "G":
                return "高铁".equals(type);
            case "D":
                return "动车".equals(type);
            case "Z":
                return "直达".equals(type);
            case "T":
                return "特快".equals(type);
            case "K":
                return "快速".equals(type);
            case "3": case "4":
                return "普快".equals(type);
            case "1": case "2":
                return "快速".equals(type);
            default:
                return false;
        }
    }

}
