package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.bitbucket.license.LicenseService;
import com.atlassian.bitbucket.mail.MailService;
import com.atlassian.extras.api.bitbucket.BitbucketServerLicense;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricCollectorImpl extends Collector implements MetricCollector {
    private static final Logger log = LoggerFactory.getLogger(MetricCollectorImpl.class);

    private final LicenseService licenseService;
    private final ScheduledMetricEvaluator scheduledMetricEvaluator;
    private final MailService mailService;

    public MetricCollectorImpl(
            LicenseService licenseService,
            ScheduledMetricEvaluator scheduledMetricEvaluator,
            MailService mailService) {
        this.licenseService = licenseService;
        this.scheduledMetricEvaluator = scheduledMetricEvaluator;
        this.mailService = mailService;
    }

    //--> License

    private final Gauge maintenanceExpiryDaysGauge = Gauge.build()
            .name("bitbucket_maintenance_expiry_days_gauge")
            .help("Maintenance Expiry Days Gauge")
            .create();

    private final Gauge allUsersGauge = Gauge.build()
            .name("bitbucket_all_users_gauge")
            .help("All Users Gauge")
            .create();

    private final Gauge activeUsersGauge = Gauge.build()
            .name("bitbucket_active_users_gauge")
            .help("Active Users Gauge")
            .create();

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

    //--> Pushes

    private final Counter pushCounter = Counter.build()
            .name("bitbucket_repo_push_count")
            .help("Repository Pushes Count")
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
    public void pushCounter(String project, String repository, String username) {
        pushCounter.labels(project, repository, username).inc();
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
    public Collector getCollector() {
        return this;
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        BitbucketServerLicense bitbucketServerLicense = licenseService.get();
        if (bitbucketServerLicense != null) {
            log.debug("License info: {}", bitbucketServerLicense);
            maintenanceExpiryDaysGauge.set(bitbucketServerLicense.getNumberOfDaysBeforeMaintenanceExpiry());
            activeUsersGauge.set(bitbucketServerLicense.getMaximumNumberOfUsers());
            allUsersGauge.set(licenseService.getLicensedUsersCount());
        }

        totalProjectsGauge.set(scheduledMetricEvaluator.getTotalProjects());
        totalRepositoriesGauge.set(scheduledMetricEvaluator.getTotalRepositories());
        totalPullRequestsGauge.set(scheduledMetricEvaluator.getTotalPullRequests());

        List<Collector.MetricFamilySamples> result = new ArrayList<>();
        result.addAll(successAuthCounter.collect());
        result.addAll(failedAuthCounter.collect());
        result.addAll(pushCounter.collect());
        result.addAll(openPullRequest.collect());
        result.addAll(mergePullRequest.collect());
        result.addAll(declinePullRequest.collect());
        result.addAll(maintenanceExpiryDaysGauge.collect());
        result.addAll(allUsersGauge.collect());
        result.addAll(activeUsersGauge.collect());
        result.addAll(totalProjectsGauge.collect());
        result.addAll(totalRepositoriesGauge.collect());
        result.addAll(totalPullRequestsGauge.collect());
        return result;
    }
}
