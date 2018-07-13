package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

public interface ScheduledMetricEvaluator {
    long getLastExecutionTimestamp();
    void restartScraping(int newDelay);
    int getDelay();
    void setDelay(int delay);
    long getTotalProjects();
    long getTotalRepositories();
    long getTotalPullRequests();
}
