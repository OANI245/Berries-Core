package berries.servermod.tcm.web;

import berries.servermod.tcm.TCM;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.ByteBufferOutputStream;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.StringUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TCMErrorHandler extends ErrorHandler {
    @Override
    protected void writeErrorPageHead(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
        writer.write("<title>" + code + "</title>\n");
        writer.write("""
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">
                    <link rel="stylesheet" href="/css/index.css"/>
                    <link rel="stylesheet" href="/css/controls.css"/>
                    <script type="module" src="/js/jquery-3.7.1.min.js"></script>
                    <script type="module" src="/js/defines.js"></script>
                    <script type="module" src="/js/fetch.js"></script>
                    <script type="module" src="/js/info_data.js"></script>
                    <script type="module" src="/js/bundle.js"></script>
                    <script type="module" src="/js/index.js"></script>\
                """);
    }

    @Override
    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        writer.write(String.format("""
                <div class="acrylic-layer" id="layer1" style="display: none;"></div>
                <div class="color-layer" id="layer2" style="display: none;"></div>
                <div class="container" id="main" style="display: none;">
                    <p style="font-size: 72px">%s</p>
                    <p>%s ≧ ﹏ ≦</p>
                    <p>正常使用出现这种情况的话，找管理员反馈问题喵~</p>
                    <button class="button-glass button-glass-green" onclick="window.location.replace('/')">返回</button>
                    <p>%s</p>
                </div>""", String.valueOf(code), code >= 400 && code < 500 ? "什么东西也找不到" : "服务器出了点问题喵", Jetty.POWERED_BY));
    }
}
