package com.uddernetworks.paintassist;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;
import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

public class CustomLocalServerReceiver implements VerificationCodeReceiver {
    private static final String LOCALHOST = "localhost";
    private static final String CALLBACK_PATH = "/callback";
    private Server server;
    String code;
    String error;
    final Semaphore waitUnlessSignaled;
    private int port;
    private final String host;
    private final String callbackPath;
    private String successLandingPageUrl;
    private String failureLandingPageUrl;

    public static final int SUGGESTED_PORT = 21385;

    public CustomLocalServerReceiver() {
        this("localhost", SUGGESTED_PORT, "/callback", null, null);
    }

    CustomLocalServerReceiver(String host, int port, String successLandingPageUrl, String failureLandingPageUrl) {
        this(host, port, "/callback", successLandingPageUrl, failureLandingPageUrl);
    }

    CustomLocalServerReceiver(String host, int port, String callbackPath, String successLandingPageUrl, String failureLandingPageUrl) {
        this.waitUnlessSignaled = new Semaphore(0);
        this.host = host;
        this.port = port;
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }

    public String getRedirectUri() throws IOException {
        this.server = new Server(this.port != -1 ? this.port : 0);
        Connector connector = this.server.getConnectors()[0];
        connector.setHost(this.host);
        this.server.addHandler(new CustomLocalServerReceiver.CallbackHandler());

        try {
            this.server.start();
            this.port = connector.getLocalPort();
        } catch (Exception var5) {
            Throwables.propagateIfPossible(var5);
            throw new IOException(var5);
        }

        String var2 = String.valueOf(String.valueOf(this.host));
        int var3 = this.port;
        String var4 = String.valueOf(String.valueOf(this.callbackPath));
        return (new StringBuilder(19 + var2.length() + var4.length())).append("http://").append(var2).append(":").append(var3).append(var4).toString();
    }

    public String waitForCode() throws IOException {
        this.waitUnlessSignaled.acquireUninterruptibly();
        if (this.error != null) {
            String var1 = String.valueOf(String.valueOf(this.error));
            throw new IOException((new StringBuilder(28 + var1.length())).append("User authorization failed (").append(var1).append(")").toString());
        } else {
            return this.code;
        }
    }

    public void stop() throws IOException {
        this.waitUnlessSignaled.release();
        if (this.server != null) {
            try {
                this.server.stop();
            } catch (Exception var2) {
                Throwables.propagateIfPossible(var2);
                throw new IOException(var2);
            }

            this.server = null;
        }

    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getCallbackPath() {
        return this.callbackPath;
    }

    class CallbackHandler extends AbstractHandler {
        CallbackHandler() {
        }

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException {
            if ("/callback".equals(target)) {
                try {
                    ((Request) request).setHandled(true);
                    error = request.getParameter("error");
                    code = request.getParameter("code");
                    if (error == null && successLandingPageUrl != null) {
                        response.sendRedirect(successLandingPageUrl);
                    } else if (error != null && failureLandingPageUrl != null) {
                        response.sendRedirect(failureLandingPageUrl);
                    } else {
                        this.writeLandingHtml(response);
                    }

                    response.flushBuffer();
                } finally {
                    waitUnlessSignaled.release();
                }

            }
        }

        private void writeLandingHtml(HttpServletResponse response) throws IOException {
            response.setStatus(200);
            response.setContentType("text/html");
            PrintWriter doc = response.getWriter();

            doc.println(IOUtils.toString(CustomLocalServerReceiver.class.getResourceAsStream("/landing.html")));
            doc.flush();
        }
    }

    public static final class Builder {
        private String host = "localhost";
        private int port = -1;
        private String successLandingPageUrl;
        private String failureLandingPageUrl;
        private String callbackPath = "/callback";

        public Builder() {
        }

        public CustomLocalServerReceiver build() {
            return new CustomLocalServerReceiver(this.host, this.port, this.callbackPath, this.successLandingPageUrl, this.failureLandingPageUrl);
        }

        public String getHost() {
            return this.host;
        }

        public CustomLocalServerReceiver.Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return this.port;
        }

        public CustomLocalServerReceiver.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public String getCallbackPath() {
            return this.callbackPath;
        }

        public CustomLocalServerReceiver.Builder setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
            return this;
        }

        public CustomLocalServerReceiver.Builder setLandingPages(String successLandingPageUrl, String failureLandingPageUrl) {
            this.successLandingPageUrl = successLandingPageUrl;
            this.failureLandingPageUrl = failureLandingPageUrl;
            return this;
        }
    }
}
