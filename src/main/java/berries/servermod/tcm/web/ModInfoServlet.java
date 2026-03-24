package berries.servermod.tcm.web;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import com.google.gson.JsonObject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ModInfoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        JsonObject modInfo = new JsonObject();
        modInfo.addProperty("mod_name", UFEInfo.MOD_NAME);
        modInfo.addProperty("mod_version", TCM.getFullVersion());
        modInfo.addProperty("mod_file_path", String.format("TCM-%s-build.%s.jar", UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION));
        String content = modInfo.toString();
        final ByteBuffer contentBytes = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
        try {
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Content-Type", "application/json");
            WebMain.send(resp, asyncContext, contentBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
