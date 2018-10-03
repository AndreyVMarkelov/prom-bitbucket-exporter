package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.servlet;

import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.commons.lang3.StringUtils;
import ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager.MetricCollector;
import ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager.SecureTokenManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PrometheusExporter extends HttpServlet {
    private final MetricCollector metricCollector;
    private final SecureTokenManager secureTokenManager;

    public PrometheusExporter(
            MetricCollector metricCollector,
            SecureTokenManager secureTokenManager) {
        this.metricCollector = metricCollector;
        this.secureTokenManager = secureTokenManager;
        DefaultExports.initialize();
    }

    @Override
    protected void doGet(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) throws IOException {
        String paramToken = httpServletRequest.getParameter("token");
        String storedToken = secureTokenManager.getToken();

        if (StringUtils.isNotBlank(storedToken) && !storedToken.equals(paramToken)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setContentType(TextFormat.CONTENT_TYPE_004);

        try (Writer writer = httpServletResponse.getWriter()) {
            TextFormat.write004(writer, metricCollector.getRegistry().filteredMetricFamilySamples(parse(httpServletRequest)));
            writer.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        doGet(httpServletRequest, httpServletResponse);
    }

    private Set<String> parse(HttpServletRequest httpServletRequest) {
        String[] includedParam = httpServletRequest.getParameterValues("name[]");
        if (includedParam == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<>(Arrays.asList(includedParam));
        }
    }
}
