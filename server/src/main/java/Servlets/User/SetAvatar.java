package Servlets.User;

import Utils.JjwtUtil;
import Utils.SqlCon;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Base64;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/10 17:11
 * @Description 设置头像
 * @Version 1.0
 */

@WebServlet("/user/setAvatar")
public class SetAvatar extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        String jwt = req.getHeader("Authorization");
        String base64 = req.getParameter("avatar");
        if (jwt == null || jwt.equals("Bearer null")) {
            pw.write("{\"code\": -1, \"data\":\"用户未登录\"}");
        } else {
            int jwtVer = JjwtUtil.vertify(jwt);
            if (jwtVer == 0) {
                Connection conn = SqlCon.getInstance();
                try {
                    PreparedStatement ps = conn.prepareStatement("UPDATE Users SET user_avatar = ?, avatar = ? WHERE user_id = ?");
                    String userId = JjwtUtil.parseJWT(jwt).getSubject();
                    Blob blob = new SerialBlob(base64.getBytes(StandardCharsets.UTF_8));
                    ps.setBlob(1, blob);
                    ps.setBlob(2, thumbnailAvatar(base64.substring(base64.indexOf(",") + 1)));
                    ps.setString(3, userId);
                    ps.execute();
                    pw.write("{\"code\":0, \"data\":\"上传成功\"}");
                    conn.commit();
                } catch (SQLException throwables) {
                    try {
                        conn.rollback();
                        throwables.printStackTrace();
                        pw.write("{\"code\":-2, \"data\":\"上传失败\"}");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        conn.rollback();
                        pw.write("{\"code\":-2, \"data\":\"上传失败\"}");
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }
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

    private Blob thumbnailAvatar(String base64) throws IOException, SQLException {
        Base64.Decoder decoder = Base64.getDecoder();
        ByteArrayInputStream bais = new ByteArrayInputStream(decoder.decode(base64));
        BufferedImage src = ImageIO.read(bais);
        BufferedImage output = Thumbnails.of(src).size(src.getWidth() / 3, src.getHeight() / 3).asBufferedImage();
        base64 = imageToBase64(output);
        while (base64.length() > 65000) {
            System.out.println(base64.length());
            output = Thumbnails.of(output).scale(0.9f).asBufferedImage();
            base64 = imageToBase64(output);
        }
        return new SerialBlob(("data:image/png;base64," + base64).getBytes(StandardCharsets.UTF_8));
    }

    private String imageToBase64(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return Base64.getEncoder().encodeToString((baos.toByteArray()));
    }
}
