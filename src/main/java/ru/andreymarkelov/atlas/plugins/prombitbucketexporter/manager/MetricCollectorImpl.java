package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import com.atlassian.bitbucket.license.LicenseService;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class MetricCollectorImpl extends Collector implements MetricCollector {
    //--> Login/Logout

    private final LicenseService licenseService;
    private final ApplicationPropertiesService applicationPropertiesService;

    @Autowired
    public MetricCollectorImpl(@ComponentImport LicenseService licenseService, @ComponentImport ApplicationPropertiesService applicationPropertiesService) {
        this.licenseService = licenseService;
        this.applicationPropertiesService = applicationPropertiesService;
    }

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

    //--> license related
    private final Gauge licenseExpiryDaysGauge = Gauge.build()
            .name("bitbucket_license_expiry_days_gauge")
            .help("Maintenance Expiry Days Gauge")
            .create();

    private final Gauge maintenanceExpiryDaysGauge = Gauge.build()
            .name("bitbucket_maintenance_expiry_days_gauge")
            .help("Maintenance Expiry Days Gauge")
            .create();

    private final Gauge activeUsersGauge = Gauge.build()
            .name("bitbucket_active_users_gauge")
            .help("Active Users Gauge")
            .create();

    private final Gauge allowedUsersGauge = Gauge.build()
            .name("bitbucket_allowed_users_gauge")
            .help("Allowed Users Gauge")
            .create();

    private List<MetricFamilySamples> collectInternal() {

        List<Collector.MetricFamilySamples> result = new ArrayList<>();
        // license
        if (licenseService != null) {
            maintenanceExpiryDaysGauge.set(licenseService.get().getNumberOfDaysBeforeMaintenanceExpiry());
            licenseExpiryDaysGauge.set(licenseService.get().getNumberOfDaysBeforeExpiry());
            allowedUsersGauge.set(licenseService.get().getMaximumNumberOfUsers());
            activeUsersGauge.set(licenseService.getLicensedUsersCount());
            result.addAll(licenseExpiryDaysGauge.collect());
            result.addAll(activeUsersGauge.collect());
            result.addAll(allowedUsersGauge.collect());
            result.addAll(maintenanceExpiryDaysGauge.collect());
        }
        return result;
    }

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
        List<Collector.MetricFamilySamples> result = new ArrayList<>();
        result.addAll(successAuthCounter.collect());
        result.addAll(failedAuthCounter.collect());
        result.addAll(pushCounter.collect());
        result.addAll(collectInternal());
        return result;
    }
}
