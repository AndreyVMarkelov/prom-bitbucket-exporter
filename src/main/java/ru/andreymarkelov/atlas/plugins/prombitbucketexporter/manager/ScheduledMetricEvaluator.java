package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

public interface ScheduledMetricEvaluator {
    long getLastExecutionTimestamp();
    void restartScraping(int newDelay);
    long getTotalProjects();
    long getTotalRepositories();
    long getTotalPullRequests();
}
