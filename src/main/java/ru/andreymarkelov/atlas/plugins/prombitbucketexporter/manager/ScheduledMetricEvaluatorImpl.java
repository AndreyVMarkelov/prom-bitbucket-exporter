package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ScheduledMetricEvaluatorImpl implements ScheduledMetricEvaluator, DisposableBean, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ScheduledMetricEvaluator.class);

    @Override
    public void destroy() throws Exception {
        log.info("Stop");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Start");
    }
}
