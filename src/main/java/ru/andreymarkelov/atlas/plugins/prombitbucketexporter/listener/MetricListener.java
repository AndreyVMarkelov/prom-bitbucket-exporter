package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.listener;

import com.atlassian.bitbucket.event.auth.AuthenticationFailureEvent;
import com.atlassian.bitbucket.event.auth.AuthenticationSuccessEvent;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryCloneEvent;
import com.atlassian.bitbucket.event.repository.RepositoryForkedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryModifiedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryPushEvent;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginInstalledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager.MetricCollector;

public class MetricListener {
    private final MetricCollector metricCollector;

    public MetricListener(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    //--> Repositories

    @EventListener
    public void repositoryModifiedEvent(RepositoryModifiedEvent repositoryModifiedEvent) {
        Repository newRepo = repositoryModifiedEvent.getNewValue();
        Repository oldRepo = repositoryModifiedEvent.getOldValue();
        if (!oldRepo.getProject().equals(newRepo.getProject())) {
            metricCollector.repositoryMoveCounter(oldRepo.getProject().getKey(), newRepo.getProject().getKey());
        }
    }

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

    //--> Plugins

    @EventListener
    public void pluginInstalledEvent(PluginInstalledEvent pluginInstalledEvent) {
        metricCollector.pluginInstalled(pluginInstalledEvent.getPlugin().getKey());
    }

    @EventListener
    public void pluginUninstalledEvent(PluginUninstalledEvent pluginUninstalledEvent) {
        metricCollector.pluginUninstalled(pluginUninstalledEvent.getPlugin().getKey());
    }

    @EventListener
    public void pluginEnabledEvent(PluginEnabledEvent pluginEnabledEvent) {
        metricCollector.pluginEnabled(pluginEnabledEvent.getPlugin().getKey());
    }

    @EventListener
    public void pluginDisabledEvent(PluginDisabledEvent pluginDisabledEvent) {
        metricCollector.pluginDisabled(pluginDisabledEvent.getPlugin().getKey());
    }

    private static String username(String username) {
        return username != null ? username : "unknown";
    }

    private static String username(ApplicationUser applicationUser) {
        return applicationUser != null ? applicationUser.getName() : "unknown";
    }
}
