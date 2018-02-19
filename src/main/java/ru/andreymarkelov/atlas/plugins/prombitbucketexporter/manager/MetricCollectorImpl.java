package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import com.atlassian.bitbucket.license.LicenseService;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.extras.api.bitbucket.BitbucketServerLicense;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

import java.util.ArrayList;
import java.util.List;

public class MetricCollectorImpl extends Collector implements MetricCollector {
    private final LicenseService licenseService;
    private final UserService userService;

    public MetricCollectorImpl(
            LicenseService licenseService,
            UserService userService) {
        this.licenseService = licenseService;
        this.userService = userService;
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
            .labelNames("repository", "username")
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
    public void pushCounter(String repository, String username) {
        pushCounter.labels(repository, username).inc();
    }

    @Override
    public Collector getCollector() {
        return this;
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        BitbucketServerLicense bitbucketServerLicense = licenseService.get();
        if (bitbucketServerLicense != null) {
            maintenanceExpiryDaysGauge.set(bitbucketServerLicense.getNumberOfDaysBeforeMaintenanceExpiry());
            activeUsersGauge.set(bitbucketServerLicense.getMaximumNumberOfUsers());
            allUsersGauge.set(userService.findUsers(new PageRequestImpl(0, bitbucketServerLicense.getMaximumNumberOfUsers())).getSize());
        }

        List<Collector.MetricFamilySamples> result = new ArrayList<>();
        result.addAll(successAuthCounter.collect());
        result.addAll(failedAuthCounter.collect());
        result.addAll(pushCounter.collect());
        result.addAll(maintenanceExpiryDaysGauge.collect());
        result.addAll(allUsersGauge.collect());
        result.addAll(activeUsersGauge.collect());
        return result;
    }
}
