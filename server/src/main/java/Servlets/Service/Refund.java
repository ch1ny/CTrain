package Servlets.Service;

import AliPay.AlipayConfig;
import Utils.JjwtUtil;
import Utils.SqlCon;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 11:19
 * @Description 退票
 * @Version 1.0
 */

@WebServlet("/service/refund")
public class Refund extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        HttpSession session = req.getSession();
        String captcha = req.getParameter("captcha");
        if (session.getAttribute("refundCaptcha") == null || !session.getAttribute("refundCaptcha").toString().substring(0, 6).equals(captcha)) {
            pw.write("{\"code\":-3, \"data\":\"验证码错误或已超时\"}");
            pw.close();
            return;
        }
        String ticket = session.getAttribute("refundCaptcha").toString().substring(6);
        String jwt = req.getHeader("Authorization");
        if (jwt == null || jwt.equals("Bearer null")) {
            pw.write("{\"code\": -1, \"data\":\"用户未登录\"}");
        } else {
            int jwtVer = JjwtUtil.vertify(jwt);
            if (jwtVer == 0) {
                Connection conn = SqlCon.getInstance();
                String userId = JjwtUtil.parseJWT(jwt).getSubject();
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT user_id, off_date FROM Tickets WHERE ticket_id = ? ORDER BY off_date");
                    ps.setString(1, ticket.substring(1));
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        String offDate = rs.getString("off_date");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        if (offDate.compareTo(sdf.format(new Date())) > 0) {
                            if (userId.equals(rs.getString("user_id"))) {
                                AlipayClient alipayClient = new DefaultAlipayClient(
                                        AlipayConfig.URL,
                                        AlipayConfig.APPID,
                                        AlipayConfig.RSA_PRIVATE_KEY,
                                        AlipayConfig.FORMAT,
                                        AlipayConfig.CHARSET,
                                        AlipayConfig.ALIPAY_PUBLIC_KEY,
                                        AlipayConfig.SIGNTYPE
                                );
                                //创建API对应的request
                                AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
                                //填充业务参数
                                ps = conn.prepareStatement("SELECT SUM(price) price FROM Tickets WHERE ticket_id = ?");
                                ps.setString(1, ticket.substring(1));
                                ps.execute();
                                rs = ps.getResultSet();
                                ps = conn.prepareStatement("DELETE FROM Tickets WHERE ticket_id = ? AND user_id = ?");
                                ps.setString(1, ticket.substring(1));
                                ps.setString(2, userId);
                                ps.execute();
                                if (rs.next()) {
                                    float total = rs.getFloat("price");
                                    DecimalFormat df = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                                    //订单标题
                                    alipayRequest.setBizContent("{" +
                                            "\"out_trade_no\":\"" + ticket + "\"," +
                                            "\"refund_amount\":\"" + df.format(total) + "\"}" );
                                    AlipayTradeRefundResponse response = alipayClient.execute(alipayRequest);
                                    if (response.isSuccess()) {
                                        conn.commit();
                                        pw.write("{\"code\": 0, \"data\":\"退款成功\"}");
                                    }
                                }
                            } else {
                                pw.write("{\"code\": -2, \"data\":\"订单号为" + ticket + "的订单非该账号所有\"}");
                            }
                        } else {
                            pw.write("{\"code\": -2, \"data\":\"该订单不可退票\"}");
                        }
                    } else {
                        pw.write("{\"code\": -2, \"data\":\"没有该条订单信息\"}");
                    }
                } catch (SQLException | AlipayApiException e) {
                    e.printStackTrace();
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    pw.write("{\"code\": -2, \"data\":\"数据库/阿里退款错误\"}");
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
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
