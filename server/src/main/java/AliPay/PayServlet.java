package AliPay;

import Utils.SqlCon;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/23 17:57
 * @Description 生成支付页面
 * @Version 1.0
 */

@WebServlet("/alipay/pay")
public class PayServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        //初始化
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
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //在公共参数中设置回跳和通知地址
        alipayRequest.setReturnUrl(AlipayConfig.RETURN_URL);
        alipayRequest.setNotifyUrl(AlipayConfig.NOTIFY_URL);
        //填充业务参数
        //必填
        //销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
        String product_code = "FAST_INSTANT_TRADE_PAY";
        String ticketId = req.getParameter("ticket");
        String out_trade_no = ticketId;
        Connection conn = SqlCon.getInstance();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(price) price, purchase_time FROM Tickets WHERE ticket_id = ? GROUP BY purchase_time");
            ps.setString(1, ticketId.substring(1));
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                float total = rs.getFloat("price");
                String purchaseTime = rs.getString("purchase_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date expire = sdf.parse(purchaseTime);
                expire.setTime(expire.getTime() + (30 * 60 * 1000));
                DecimalFormat df = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                //订单标题
                String subject = "车票订单";
                //选填
                alipayRequest.setBizContent("{" +
                        "\"out_trade_no\":\"" + out_trade_no + "\"," +
                        "\"product_code\":\"" + product_code + "\"," +
                        "\"total_amount\":\"" + df.format(total) + "\"," +
                        "\"subject\":\"" + subject + "\"," +
                        "\"timeout_expire\":\"" + expire + "\"}" );
                //请求
                String form = "";
                try {
                    form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
                } catch (AlipayApiException e) {
                    e.printStackTrace();
                    resp.getWriter().write("捕获异常出错");
                    resp.getWriter().flush();
                    resp.getWriter().close();
                }
                resp.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
                resp.getWriter().write(form); // 直接将完整的表单html输出到页面
                resp.getWriter().flush();
                resp.getWriter().close();
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }
}
