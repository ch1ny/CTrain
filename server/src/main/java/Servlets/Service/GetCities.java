package Servlets.Service;

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
import java.util.List;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/9 20:55
 * @Description 获取可用城市
 * @Version 1.0
 */

@WebServlet("/service/getCities")
public class GetCities extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        Connection conn = SqlCon.getInstance();
        PrintWriter pw = resp.getWriter();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT location FROM Stations");
            ps.execute();
            ResultSet rs = ps.getResultSet();
            List<String> cities = new ArrayList<>();
            while (rs.next()) {
                cities.add(rs.getString("location"));
            }
            pw.write("{\"code\":0, \"data\":" + JSON.toJSONString(cities) + "}");
        } catch (SQLException throwables) {
            pw.write("{\"code\":-1, \"data\":\"获取城市列表数据失败\"}");
        }
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        pw.close();
    }
}
