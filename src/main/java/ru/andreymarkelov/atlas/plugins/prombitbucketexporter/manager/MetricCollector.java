package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import io.prometheus.client.Collector;

public interface MetricCollector {
    Collector getCollector();
}
