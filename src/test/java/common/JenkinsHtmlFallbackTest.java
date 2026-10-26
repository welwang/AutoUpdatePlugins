package common;

import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import static org.junit.Assert.*;

public class JenkinsHtmlFallbackTest {
    @Test public void resolvesCitizensStylePageWhenApiIsDisabledAndIgnoresExternalLinks() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            boolean api = exchange.getRequestURI().getPath().endsWith("api/json");
            byte[] body = (api ? "not found" : "<h1>#4268 (Oct 21, 2026)</h1>"
                    + "<a href='/job/Citizens2/lastSuccessfulBuild/artifact/dist/target/Citizens-2.0.44-b4268.jar'>Citizens</a>"
                    + "<a href='https://other.example/job/Citizens2/lastSuccessfulBuild/artifact/evil.jar'>other</a>")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(api ? 404 : 200, body.length);
            try (java.io.OutputStream out = exchange.getResponseBody()) { out.write(body); }
        });
        server.start();
        try {
            String root = "http://127.0.0.1:" + server.getAddress().getPort() + "/job/Citizens2/";
            ResolvedUpdate result = new JenkinsProvider(Logger.getAnonymousLogger()).resolve("Citizens", root + "lastBuild/");
            assertEquals("4268", result.versionId);
            assertEquals(root + "4268/artifact/dist/target/Citizens-2.0.44-b4268.jar", result.downloadUrl);
            assertEquals("Citizens-2.0.44-b4268.jar", result.fileName);
        } finally { server.stop(0); }
    }
}
