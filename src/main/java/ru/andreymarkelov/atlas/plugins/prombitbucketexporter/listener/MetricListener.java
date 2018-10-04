package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.listener;

import com.atlassian.bitbucket.event.auth.AuthenticationFailureEvent;
import com.atlassian.bitbucket.event.auth.AuthenticationSuccessEvent;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryCloneEvent;
import com.atlassian.bitbucket.event.repository.RepositoryForkedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryPushEvent;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
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
        Repository repository = repositoryPushEvent.getRepository();
        metricCollector.pushCounter(repository.getProject().getKey(), repository.getName(), username(repositoryPushEvent.getUser()));
    }

    @EventListener
    public void repositoryCloneEvent(RepositoryCloneEvent repositoryCloneEvent) {
        Repository repository = repositoryCloneEvent.getRepository();
        metricCollector.cloneCounter(repository.getProject().getKey(), repository.getName(), username(repositoryCloneEvent.getUser()));
    }

    @EventListener
    public void repositoryForkedEvent(RepositoryForkedEvent repositoryForkedEvent) {
        Repository repository = repositoryForkedEvent.getRepository();
        metricCollector.forkCounter(repository.getProject().getKey(), repository.getName(), username(repositoryForkedEvent.getUser()));
    }

    //--> Pull Requests

    @EventListener
    public void pullRequestOpenedEvent(PullRequestOpenedEvent pullRequestOpenedEvent) {
        Repository repository = pullRequestOpenedEvent.getPullRequest().getFromRef().getRepository();
        metricCollector.openPullRequest(repository.getProject().getKey(), repository.getName());
    }

    @EventListener
    public void pullRequestMergedEvent(PullRequestMergedEvent pullRequestMergedEvent) {
        Repository repository = pullRequestMergedEvent.getPullRequest().getFromRef().getRepository();
        metricCollector.mergePullRequest(repository.getProject().getKey(), repository.getName());
    }

    @EventListener
    public void pullRequestDeclinedEvent(PullRequestDeclinedEvent pullRequestDeclinedEvent) {
        Repository repository = pullRequestDeclinedEvent.getPullRequest().getFromRef().getRepository();
        metricCollector.declinePullRequest(repository.getProject().getKey(), repository.getName());
    }

    //--> Auth events

    @EventListener
    public void authenticationSuccessEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
        metricCollector.successAuthCounter(username(authenticationSuccessEvent.getUsername()));
    }

    @EventListener
    public void authenticationFailureEvent(AuthenticationFailureEvent authenticationFailureEvent) {
        metricCollector.failedAuthCounter(username(authenticationFailureEvent.getUsername()));
    }

    private static String username(String username) {
        return username != null ? username : "unknown";
    }

    private static String username(ApplicationUser applicationUser) {
        return applicationUser != null ? applicationUser.getName() : "unknown";
    }
}
