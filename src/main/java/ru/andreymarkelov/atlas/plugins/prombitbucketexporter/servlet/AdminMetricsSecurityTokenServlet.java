package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.servlet;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager.SecureTokenManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AdminMetricsSecurityTokenServlet extends HttpServlet {
    private static final String TEMPLATE_NAME = "bitbucket.plugin.securetoken";

    private final SecureTokenManager secureTokenManager;
    private final UserManager userManager;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final WebResourceManager webResourceManager;
    private final LoginUriProvider loginUriProvider;

    public AdminMetricsSecurityTokenServlet(
            SecureTokenManager secureTokenManager,
            UserManager userManager,
            SoyTemplateRenderer soyTemplateRenderer,
            WebResourceManager webResourceManager,
            LoginUriProvider loginUriProvider) {
        this.secureTokenManager = secureTokenManager;
        this.userManager = userManager;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.webResourceManager = webResourceManager;
        this.loginUriProvider = loginUriProvider;
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        secureTokenManager.setToken(token);

        Map<String, Object> params = new HashMap<>();
        params.put("token", secureTokenManager.getToken());
        params.put("saved", Boolean.TRUE);
        render(response, params);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        UserKey userKey = userManager.getRemoteUserKey(request);
        if (userKey == null) {
            redirectToLogin(request, response);
            return;
        }

        if (!userManager.isSystemAdmin(userKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("token", secureTokenManager.getToken());
        params.put("saved", Boolean.FALSE);
        render(response, params);
    }

    private void render(
            HttpServletResponse response,
            Map<String, Object> data) throws IOException, ServletException {
        webResourceManager.requireResource("ru.andreymarkelov.atlas.plugins.prom-bitbucket-exporter:commitgraph-resources");
        response.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(
                    response.getWriter(),
                    "ru.andreymarkelov.atlas.plugins.prom-bitbucket-exporter:prom-for-bitbucket-exporter-templates",
                    TEMPLATE_NAME,
                    data
            );
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?").append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
