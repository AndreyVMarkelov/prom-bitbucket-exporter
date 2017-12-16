package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;

public class MetricCollectorImpl extends Collector implements MetricCollector {
    @Override
    public Collector getCollector() {
        return this;
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        List<Collector.MetricFamilySamples> result = new ArrayList<>();
        return result;
    }
}
