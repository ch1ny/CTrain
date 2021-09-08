package Servlets.Service;

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
 * @Date 2021/8/12 10:19
 * @Description 检索直达方案
 * @Version 1.0
 */

@WebServlet("/service/search")
public class Search extends HttpServlet {

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
                    PreparedStatement ps = conn.prepareStatement(SearchSQL);
                    String start = req.getParameter("start");
                    String dest = req.getParameter("dest");
                    ps.setString(1, start);
                    ps.setString(2, dest);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    List<Map> routes = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("train", rs.getString("train_id"));
                        ps = conn.prepareStatement("SELECT TrainStation.station_name, stop_index, day_num, arrive_time, stop_time, distance, location FROM TrainStation INNER JOIN Stations ON TrainStation.station_name = Stations.station_name WHERE train_id = ? ORDER BY stop_index ASC");
                        ps.setString(1, rs.getString("train_id"));
                        ps.execute();
                        ResultSet _rs = ps.getResultSet();
                        List<Map> stations = new ArrayList<>();
                        boolean access = false;
                        String startTime = null, endTime = null;
                        int startDay = 1;
                        while (_rs.next()) {
                            Map<String, Object> station = new HashMap<>();
                            if (_rs.getString("location").equals(start)) {
                                access = true;
                                startDay = _rs.getInt("day_num");
                                startTime = _rs.getString("arrive_time");
                                int hour = Integer.parseInt(startTime.split(":")[0]);
                                int minute = Integer.parseInt(startTime.split(":")[1]);
                                minute += _rs.getInt("stop_time");
                                if (minute >= 60) {
                                    minute -= 60;
                                    hour += 1;
                                    if (hour >= 24) {
                                        hour -= 24;
                                        startDay++;
                                    }
                                }
                                startTime = String.format("%02d", hour) + ":" + String.format("%02d", minute);
                                item.put("from", _rs.getString("station_name"));
                            }
                            station.put("station", _rs.getString("station_name"));
                            station.put("index", _rs.getInt("stop_index"));
                            station.put("days", _rs.getInt("day_num"));
                            station.put("arrive", _rs.getString("arrive_time"));
                            station.put("stop", _rs.getInt("stop_time"));
                            station.put("distance", _rs.getInt("distance"));
                            station.put("access", access);
                            if (_rs.getString("location").equals(dest)) {
                                access = false;
                                endTime = _rs.getString("arrive_time");
                                int days = _rs.getInt("day_num") - startDay;
                                item.put("days", days);
                                item.put("to", _rs.getString("station_name"));
                            }
                            stations.add(station);
                        }
                        item.put("startTime", startTime);
                        item.put("endTime", endTime);
                        item.put("stations", stations);
                        item.put("start", stations.get(0).get("station"));
                        item.put("end", stations.get(stations.size() - 1).get("station"));
                        routes.add(item);
                    }
                    pw.write("{\"code\":0, \"data\":" + JSON.toJSONString(routes) + "}");
                } catch (SQLException throwables) {
                    pw.write("{\"code\":-2, \"data\":\"数据库错误\"}");
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

    private String SearchSQL = "SELECT X.train_id\n" +
            "FROM (\n" +
            "         SELECT A.train_id, A.stop_index\n" +
            "         FROM TrainStation A\n" +
            "         WHERE EXISTS (\n" +
            "                       SELECT 1\n" +
            "                       FROM Stations B\n" +
            "                       WHERE A.station_name = B.station_name AND B.location = ?\n" +
            "                   )\n" +
            "     ) X\n" +
            "         INNER JOIN (\n" +
            "    SELECT A.train_id, A.stop_index\n" +
            "    FROM TrainStation A\n" +
            "    WHERE EXISTS (\n" +
            "                  SELECT 1\n" +
            "                  FROM Stations B\n" +
            "                  WHERE A.station_name = B.station_name AND B.location = ?\n" +
            "              )\n" +
            ") Y\n" +
            "ON X.train_id = Y.train_id\n" +
            "WHERE X.stop_index < Y.stop_index";
}
