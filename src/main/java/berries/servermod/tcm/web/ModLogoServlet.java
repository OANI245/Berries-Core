package berries.servermod.tcm.web;

import berries.servermod.tcm.TCM;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ModLogoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        try (BufferedInputStream logo = new BufferedInputStream(Objects.requireNonNull(TCM.class.getResourceAsStream("/assets/tcm/textures/gui/logo.png")))) {
            try {
                resp.addHeader("Access-Control-Allow-Origin", "*");
                resp.addHeader("Content-Type", "image/png");
                final ByteBuffer contentBytes = ByteBuffer.wrap(logo.readAllBytes());
                WebMain.send(resp, asyncContext, contentBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            resp.sendError(404, e.getMessage());
        }
    }
}
