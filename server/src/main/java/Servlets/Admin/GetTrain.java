package Servlets.Admin;

import Utils.JjwtUtil;
import Utils.SqlCon;
import com.alibaba.fastjson.JSON;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/11 8:50
 * @Description 获取列车表
 * @Version 1.0
 */

@WebServlet("/admin/getTrains")
public class GetTrain extends HttpServlet {
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
                        ps = conn.prepareStatement("SELECT COUNT(train_id) total FROM Trains");
                        ps.execute();
                        rs = ps.getResultSet();
                        if (rs.next()) {
                            int total = rs.getInt("total");
                            int pageNum = Integer.parseInt(req.getParameter("pageNum"));
                            int pageSize = Integer.parseInt(req.getParameter("pageSize"));
                            ps = conn.prepareStatement("SELECT train_id, train_type FROM Trains LIMIT ?, ?");
                            int current = (pageNum - 1) * pageSize;
                            ps.setInt(1, current);
                            ps.setInt(2, pageSize);
                            ps.execute();
                            rs = ps.getResultSet();
                            List<Map> trains = new ArrayList<>();
                            while (rs.next()) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", rs.getString("train_id"));
                                map.put("type", rs.getString("train_type"));
                                trains.add(map);
                            }
                            pw.write("{\"code\":0, \"data\": {\"total\":" + total + ", \"trains\":" + JSON.toJSONString(trains) + "}}");
                        }
                    } else {
                        pw.write("{\"code\":-2, \"data\":\"权限不足\"}");
                    }
                    if (conn != null) {
                        conn.close();
                    }
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
