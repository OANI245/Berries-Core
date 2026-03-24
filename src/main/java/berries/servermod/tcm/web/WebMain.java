package berries.servermod.tcm.web;

import berries.servermod.tcm.ServerConfig;
import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebMain {
    public static Consumer<Runnable> callback = Runnable::run;

    private static Server webServer;
    private static ServerConnector serverConnector;

    public static void init() {
        webServer = new Server(new QueuedThreadPool(40, 1, 120));
        serverConnector = new ServerConnector(webServer);
        webServer.setConnectors(new Connector[]{serverConnector});
        ServletContextHandler context = new ServletContextHandler();
        context.setDefaultRequestCharacterEncoding("UTF-8");
        webServer.setHandler(context);
        URL url = TCM.class.getResource("/assets/tcm/webpages/");
        if (url != null) {
            try {
                context.setBaseResource(Resource.newResource(url.toURI()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ServletHolder holder = new ServletHolder("default", DefaultServlet.class);
        holder.setInitParameter("dirAllowed", "true");
        holder.setInitParameter("cacheControl", "max-age=0,public");
        context.addServlet(holder, "/");
        context.addServlet(ModInfoServlet.class, "/info");
        context.addServlet(ModLogoServlet.class, "/logo");
        context.addServlet(ModFileServlet.class, "/download");

        context.setErrorHandler(new TCMErrorHandler());

        TCM.LOGGER.info("TCM Download WebServer is initialized.");
    }

    public static void start() {
        int port = ServerConfig.INSTANCE.port;
        serverConnector.setPort(port);
        try {
            webServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            webServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(HttpServletResponse resp, AsyncContext asyncContext, ByteBuffer contentBytes) throws IOException {
        final ServletOutputStream servletOutputStream = resp.getOutputStream();
        servletOutputStream.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                while (servletOutputStream.isReady()) {
                    if (!contentBytes.hasRemaining()) {
                        resp.setStatus(200);
                        asyncContext.complete();
                        return;
                    }
                    servletOutputStream.write(contentBytes.get());
                }
            }

            @Override
            public void onError(Throwable t) {
                asyncContext.complete();
            }
        });
    }
}
