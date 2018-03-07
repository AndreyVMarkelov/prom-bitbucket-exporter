package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.listener;

import com.atlassian.bitbucket.event.auth.AuthenticationFailureEvent;
import com.atlassian.bitbucket.event.auth.AuthenticationSuccessEvent;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryPushEvent;
import com.atlassian.event.api.EventListener;
import ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager.MetricCollector;

public class MetricListener {
    private final MetricCollector metricCollector;

    public MetricListener(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    //--> Push

    @EventListener
    public void repositoryPushEvent(RepositoryPushEvent repositoryPushEvent) {
        metricCollector.pushCounter(
                repositoryPushEvent.getRepository().getProject().getKey(),
                repositoryPushEvent.getRepository().getName(),
                repositoryPushEvent.getUser() != null ? repositoryPushEvent.getUser().getName() : "unknown"
        );
    }

    //--> Pull Requests

    @EventListener
    public void pullRequestOpenedEvent(PullRequestOpenedEvent pullRequestOpenedEvent) {

    }

    @EventListener
    public void pullRequestMergedEvent(PullRequestMergedEvent pullRequestMergedEvent) {

    }

    @EventListener
    public void pullRequestDeclinedEvent(PullRequestDeclinedEvent pullRequestDeclinedEvent) {

    }

    //--> Auth events

    @EventListener
    public void authenticationSuccessEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
        metricCollector.successAuthCounter(
                authenticationSuccessEvent.getUsername() != null ? authenticationSuccessEvent.getUsername() : "unknown"
        );
    }

    @EventListener
    public void authenticationFailureEvent(AuthenticationFailureEvent authenticationFailureEvent) {
        metricCollector.failedAuthCounter(
                authenticationFailureEvent.getUsername() != null ? authenticationFailureEvent.getUsername() : "unknown"
        );
    }
}
