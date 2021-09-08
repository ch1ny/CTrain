package Servlets.Admin;

import Utils.JjwtUtil;
import Utils.SqlCon;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
 * @Date 2021/8/11 17:07
 * @Description 设置列车线路时刻表
 * @Version 1.0
 */

@WebServlet("/admin/setRoutes")
public class SetRoutes extends HttpServlet {

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
                        String train_id = req.getParameter("train");
                        ps = conn.prepareStatement("SELECT 1 FROM Trains WHERE train_id = ?");
                        ps.setString(1, train_id);
                        ps.execute();
                        rs = ps.getResultSet();
                        if (rs.next()) {
                            ps = conn.prepareStatement("SELECT 1 FROM Tickets WHERE train_id = ?");
                            ps.setString(1, train_id);
                            ps.execute();
                            rs = ps.getResultSet();
                            if (rs.next()) {
                                pw.write("{\"code\":-5, \"data\":\"该列车已存在购票信息！\"}");
                            } else {
                                ps = conn.prepareStatement("DELETE FROM TrainStation WHERE train_id = ?");
                                ps.setString(1, train_id);
                                ps.execute();
                                String routes = req.getParameter("routes");
                                int resultCode = setRoute(train_id, routes, conn);
                                if (resultCode == 0) {
                                    conn.commit();
                                    pw.write("{\"code\":0, \"data\":\"设定列车路径成功\"}");
                                } else {
                                    conn.rollback();
                                    pw.write("{\"code\":-4, \"data\":\"请提交合理的火车路线(" + resultCode + ")\"}");
                                }
                            }
                        } else {
                            pw.write("{\"code\":-3, \"data\":\"该列车不存在！\"}");
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
                    pw.write("{\"code\":-3, \"data\":\"请提交合理的火车路线！\"}");
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
        pw.close();
    }

    private int setRoute(String train_id, String routeStr, Connection conn) throws SQLException {
        JSONArray arr = JSON.parseArray(routeStr);
        int totalTime = 0;
        int totalMile = -1;
        PreparedStatement ps = conn.prepareStatement("INSERT INTO TrainStation (train_id, station_name, stop_index, day_num, arrive_time, stop_time, distance) VALUES (?,?,?,?,?,?,?)");
        if (arr.size() == 0) {
            return -5; // 防止误操作清空路线
        }
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            int index = json.getIntValue("index");
            if (index - 1 != i) {
                return -1; // 进站次序不对
            }
            int stop = json.getIntValue("stop");
            if (i == 0 || i == arr.size() - 1) {
                if (stop != 0) {
                    return -2; // 始末站无需停靠
                }
            } else {
                if (stop <= 0) {
                    return -2; // 途经站必须停靠
                }
            }
            int distance = json.getIntValue("distance");
            if (distance <= totalMile) {
                return -3; // 行驶里程在变小
            }
            totalMile = distance;
            String arrive = json.getString("arrive").trim();
            int days = json.getIntValue("days");
            int hour = Integer.parseInt(arrive.split(":")[0]);
            int minute = Integer.parseInt(arrive.split(":")[1]);
            int time = ((days - 1) * 24 + hour) * 60 + minute;
            if (time <= totalTime) {
                return -4; // 时间戳不正常
            }
            totalTime = time + stop;
            String station = json.getString("station");
            ps.setString(1, train_id);
            ps.setString(2, station);
            ps.setInt(3, index);
            ps.setInt(4, days);
            ps.setString(5, arrive);
            ps.setInt(6, stop);
            ps.setInt(7, distance);
            ps.execute();
        }
        return 0;
    }

}
