package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.listener;

import com.atlassian.bitbucket.event.auth.AuthenticationFailureEvent;
import com.atlassian.bitbucket.event.auth.AuthenticationSuccessEvent;
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
    }

    //--> Auth events

    @EventListener
    public void authenticationSuccessEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
        authenticationSuccessEvent.getSource();
    }

    @EventListener
    public void authenticationFailureEvent(AuthenticationFailureEvent authenticationFailureEvent) {
        authenticationFailureEvent.getException();
    }
}
