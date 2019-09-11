package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import com.atlassian.bitbucket.license.LicenseService;
import com.atlassian.bitbucket.mail.MailService;
import com.atlassian.extras.api.bitbucket.BitbucketServerLicense;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import ru.andreymarkelov.atlas.plugins.prombitbucketexporter.monitor.RepositoriesMonitor;

import java.util.ArrayList;
import java.util.List;

public class MetricCollectorImpl extends Collector implements MetricCollector, DisposableBean, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(MetricCollectorImpl.class);

    private final LicenseService licenseService;
    private final ScheduledMetricEvaluator scheduledMetricEvaluator;
    private final CollectorRegistry registry;
    private final MailService mailService;
    private final RepositoriesMonitor repositoriesMonitor;

    public MetricCollectorImpl(
            LicenseService licenseService,
            ScheduledMetricEvaluator scheduledMetricEvaluator,
            MailService mailService,
            RepositoriesMonitor repositoriesMonitor) {
        this.licenseService = licenseService;
        this.scheduledMetricEvaluator = scheduledMetricEvaluator;
        this.registry = CollectorRegistry.defaultRegistry;
        this.mailService = mailService;
        this.repositoriesMonitor = repositoriesMonitor;
    }

    //--> License

    private final Gauge maintenanceExpiryDaysGauge = Gauge.build()
            .name("bitbucket_maintenance_expiry_days_gauge")
            .help("Maintenance Expiry Days Gauge")
            .create();

    private final Gauge licenseExpiryDaysGauge = Gauge.build()
            .name("bitbucket_license_expiry_days_gauge")
            .help("License Expiry Days Gauge")
            .create();

    private final Gauge allowedUsersGauge = Gauge.build()
            .name("bitbucket_allowed_users_gauge")
            .help("Maximum Allowed Users")
            .create();

    private final Gauge activeUsersGauge = Gauge.build()
            .name("bitbucket_active_users_gauge")
            .help("Active Users Gauge")
            .create();

    //<-- License

    //--> Login/Logout

    private final Counter successAuthCounter = Counter.build()
            .name("bitbucket_success_auth_count")
            .help("User Success Auth Count")
            .labelNames("username")
            .create();

    private final Counter failedAuthCounter = Counter.build()
            .name("bitbucket_failed_auth_count")
            .help("User Failed Auth Count")
            .labelNames("username")
            .create();

    //--> Repositories
    private final Counter repositoryMoveCounter = Counter.build()
            .name("bitbucket_repo_move_count")
            .help("Repository Moves Count")
            .labelNames("oldProject", "NewProject")
            .create();

    //--> Pushes

    private final Counter pushCounter = Counter.build()
            .name("bitbucket_repo_push_count")
            .help("Repository Pushes Count")
            .labelNames("project", "repository", "username")
            .create();

    //--> Clones

    private final Counter cloneCounter = Counter.build()
            .name("bitbucket_repo_clone_count")
            .help("Repository Clones Count")
            .labelNames("project", "repository", "username")
            .create();

    //--> Forks

    private final Counter forkCounter = Counter.build()
            .name("bitbucket_repo_fork_count")
            .help("Repository Forks Count")
            .labelNames("project", "repository", "username")
            .create();

    //--> Pull requests

    private final Counter openPullRequest = Counter.build()
            .name("bitbucket_pull_request_open")
            .help("Opened Pull Requests Count")
            .labelNames("project", "repository")
            .create();

    private final Counter mergePullRequest = Counter.build()
            .name("bitbucket_pull_request_merge")
            .help("Merged Pull Requests Count")
            .labelNames("project", "repository")
            .create();

    private final Counter declinePullRequest = Counter.build()
            .name("bitbucket_pull_request_decline")
            .help("Declined Pull Requests Count")
            .labelNames("project", "repository")
            .create();

    //--> Plugin

    private final Counter pluginInstalled = Counter.build()
            .name("bitbucket_plugin_installed")
            .help("Plugin Installed Count")
            .labelNames("pluginKey")
            .create();

    private final Counter pluginUninstalled = Counter.build()
            .name("bitbucket_plugin_uninstalled")
            .help("Plugin Uninstalled Count")
            .labelNames("pluginKey")
            .create();

    private final Counter pluginEnabled = Counter.build()
            .name("bitbucket_plugin_enabled")
            .help("Plugin Enabled Count")
            .labelNames("pluginKey")
            .create();

    private final Counter pluginDisabled = Counter.build()
            .name("bitbucket_plugin_disabled")
            .help("Plugin Disabled Count")
            .labelNames("pluginKey")
            .create();

    //--> Scheduled

    private final Gauge totalProjectsGauge = Gauge.build()
            .name("bitbucket_total_projects_gauge")
            .help("Total Projects Gauge")
            .create();

    private final Gauge totalRepositoriesGauge = Gauge.build()
            .name("bitbucket_total_repositories_gauge")
            .help("Total Repositories Gauge")
            .create();

    private final Gauge totalPullRequestsGauge = Gauge.build()
            .name("bitbucket_total_pull_requests_gauge")
            .help("Total Pull Requests Gauge")
            .create();

    @Override
    public void successAuthCounter(String username) {
        successAuthCounter.labels(username).inc();
    }

    @Override
    public void failedAuthCounter(String username) {
        failedAuthCounter.labels(username).inc();
    }

    @Override
    public void repositoryMoveCounter(String oldProject, String newProject) {
        repositoryMoveCounter.labels(oldProject, newProject).inc();
    }

    @Override
    public void pushCounter(String project, String repository, String username) {
        pushCounter.labels(project, repository, username).inc();
    }

    @Override
    public void cloneCounter(String project, String repository, String username) {
        cloneCounter.labels(project, repository, username).inc();
    }

    @Override
    public void forkCounter(String project, String repository, String username) {
        forkCounter.labels(project, repository, username);
    }

    @Override
    public void openPullRequest(String project, String repository) {
        openPullRequest.labels(project, repository).inc();
    }

    @Override
    public void mergePullRequest(String project, String repository) {
        mergePullRequest.labels(project, repository).inc();
    }

    @Override
    public void declinePullRequest(String project, String repository) {
        declinePullRequest.labels(project, repository).inc();
    }

    @Override
    public void pluginInstalled(String pluginKey) {
        pluginInstalled.labels(pluginKey).inc();
    }

    @Override
    public void pluginUninstalled(String pluginKey) {
        pluginUninstalled.labels(pluginKey).inc();
    }

    @Override
    public void pluginEnabled(String pluginKey) {
        pluginEnabled.labels(pluginKey).inc();
    }

    @Override
    public void pluginDisabled(String pluginKey) {
        pluginDisabled.labels(pluginKey).inc();
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        licenseMetrics();

        totalProjectsGauge.set(scheduledMetricEvaluator.getTotalProjects());
        totalRepositoriesGauge.set(scheduledMetricEvaluator.getTotalRepositories());
        totalPullRequestsGauge.set(scheduledMetricEvaluator.getTotalPullRequests());

        List<Collector.MetricFamilySamples> result = new ArrayList<>();
        result.addAll(successAuthCounter.collect());
        result.addAll(failedAuthCounter.collect());
        result.addAll(repositoryMoveCounter.collect());
        result.addAll(pushCounter.collect());
        result.addAll(cloneCounter.collect());
        result.addAll(forkCounter.collect());
        result.addAll(openPullRequest.collect());
        result.addAll(mergePullRequest.collect());
        result.addAll(declinePullRequest.collect());
        //--> license
        result.addAll(maintenanceExpiryDaysGauge.collect());
        result.addAll(licenseExpiryDaysGauge.collect());
        result.addAll(activeUsersGauge.collect());
        result.addAll(allowedUsersGauge.collect());
        //<-- license
        result.addAll(totalProjectsGauge.collect());
        result.addAll(totalRepositoriesGauge.collect());
        result.addAll(totalPullRequestsGauge.collect());
        result.addAll(pluginInstalled.collect());
        result.addAll(pluginUninstalled.collect());
        result.addAll(pluginEnabled.collect());
        result.addAll(pluginDisabled.collect());
        return result;
    }

    @Override
    public CollectorRegistry getRegistry() {
        return registry;
    }

    @Override
    public void destroy() {
        this.registry.unregister(this);
    }

    @Override
    public void afterPropertiesSet() {
        this.registry.register(this);
        DefaultExports.initialize();
    }

    private void licenseMetrics() {
        activeUsersGauge.set(licenseService.getLicensedUsersCount());
        BitbucketServerLicense bitbucketServerLicense = licenseService.get();
        if (bitbucketServerLicense != null) {
            maintenanceExpiryDaysGauge.set(bitbucketServerLicense.getNumberOfDaysBeforeMaintenanceExpiry());
            licenseExpiryDaysGauge.set(bitbucketServerLicense.getNumberOfDaysBeforeExpiry());
            allowedUsersGauge.set(bitbucketServerLicense.getMaximumNumberOfUsers());
        }
    }
}
