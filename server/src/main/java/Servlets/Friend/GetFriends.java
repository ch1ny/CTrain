package Servlets.Friend;

import Utils.JjwtUtil;
import Utils.SqlCon;
import com.alibaba.fastjson.JSON;

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
 * @Date 2021/8/10 15:54
 * @Description 获取同行旅客名单
 * @Version 1.0
 */

@WebServlet("/friend/get")
public class GetFriends extends HttpServlet {

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
                    PreparedStatement ps = conn.prepareStatement("SELECT friend_id, friend_name FROM Friends WHERE user_id = ?");
                    String userId = JjwtUtil.parseJWT(jwt).getSubject();
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    List<Map> friends = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getString("friend_id"));
                        map.put("name", rs.getString("friend_name"));
                        friends.add(map);
                    }
                    String json = JSON.toJSONString(friends);
                    pw.write("{\"code\":0, \"data\":" + json + "}");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
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
