package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import io.prometheus.client.CollectorRegistry;

public interface MetricCollector {
    CollectorRegistry getRegistry();
    void successAuthCounter(String username);
    void failedAuthCounter(String username);
    void repositoryMoveCounter(String oldProject, String newProject);
    void pushCounter(String project, String repository, String username);
    void cloneCounter(String project, String repository, String username);
    void forkCounter(String project, String repository, String username);
    void openPullRequest(String project, String repository);
    void mergePullRequest(String project, String repository);
    void declinePullRequest(String project, String repository);
    void pluginInstalled(String pluginKey);
    void pluginUninstalled(String pluginKey);
    void pluginEnabled(String pluginKey);
    void pluginDisabled(String pluginKey);
}
