package berries.servermod.tcm.web;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minecraft.DetectedVersion;
import net.minecraft.WorldVersion;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class ModFileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(180000);
        try {
            String jarPath = TCM.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath();
            File file = new File(jarPath);
            if (!file.exists()) {
                throw new FileNotFoundException(jarPath);
            }

            try (BufferedInputStream jar = new BufferedInputStream(new FileInputStream(file))) {
                resp.addHeader("Access-Control-Allow-Origin", "*");
                resp.addHeader("Content-Type", "application/octet-stream");
                resp.addHeader("Content-Length", String.valueOf(file.length()));
                resp.addHeader("Content-Disposition", "attachment; filename=" + String.format("TCM-%s-build.%s.jar", UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION));
                final ByteBuffer contentBytes = ByteBuffer.wrap(jar.readAllBytes());
                WebMain.send(resp, asyncContext, contentBytes);
                // 这里绝不能 complete() —— send 的 WriteListener 会在写完/onError 时自动 complete
            }
            // 仅在 send 尚未接管异步写入（同步阶段就出错）时才手动 complete
            catch (FileNotFoundException e) {
                e.printStackTrace();
                resp.sendError(404, e.getMessage());
                asyncContext.complete();
            } catch (Throwable e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
                asyncContext.complete();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            resp.sendError(404, e.getMessage());
            asyncContext.complete();
        } catch (Throwable e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
            asyncContext.complete();
        }
    }
}
