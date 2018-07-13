package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import io.prometheus.client.Collector;

public interface MetricCollector {
    Collector getCollector();
    void successAuthCounter(String username);
    void failedAuthCounter(String username);
    void pushCounter(String project, String repository, String username);
    void openPullRequest(String project, String repository);
    void mergePullRequest(String project, String repository);
    void declinePullRequest(String project, String repository);
}
