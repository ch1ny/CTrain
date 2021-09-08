package AliPay;

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
 * @Date 2021/8/23 19:41
 * @Description 后端支付成功回调接口
 * @Version 1.0
 */

@WebServlet("/alipay/notify")
public class NotifyServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        String tradeId = req.getParameter("out_trade_no");
        PrintWriter pw = resp.getWriter();
        Connection conn = SqlCon.getInstance();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE Tickets SET purchased = 1 WHERE ticket_id = ? AND TIMESTAMPDIFF(MINUTE , purchase_time, sysdate()) <= 30");
            ps.setString(1, tradeId.substring(1));
            ps.execute();
            conn.commit();
            conn.close();
            pw.write("success");
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            e.printStackTrace();
            pw.write("fail");
        }
        pw.close();
    }
}
