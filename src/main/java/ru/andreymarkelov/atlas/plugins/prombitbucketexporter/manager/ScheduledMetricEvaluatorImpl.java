package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static java.lang.Thread.MIN_PRIORITY;
import static java.util.concurrent.Executors.defaultThreadFactory;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class ScheduledMetricEvaluatorImpl implements ScheduledMetricEvaluator, DisposableBean, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ScheduledMetricEvaluator.class);

    /**
     * Bitbucket components.
     */
    private final ScrapingSettingsManager scrapingSettingsManager;

    /**
     * Scheduled executor to grab metrics.
     */
    private final ScheduledExecutorService executorService;
    private final AtomicLong lastExecutionTimestamp;
    private final Lock lock;

    private ScheduledFuture<?> scraper;

    public ScheduledMetricEvaluatorImpl(
            ScrapingSettingsManager scrapingSettingsManager
    ) {
        this.scrapingSettingsManager = scrapingSettingsManager;
        this.executorService = newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@Nonnull Runnable r) {
                Thread thread = defaultThreadFactory().newThread(r);
                thread.setPriority(MIN_PRIORITY);
                return thread;
            }
        });
        this.lastExecutionTimestamp = new AtomicLong(-1);
        this.lock = new ReentrantLock();
    }

    @Override
    public long getLastExecutionTimestamp() {
        return lastExecutionTimestamp.get();
    }

    @Override
    public void restartScraping(int newDelay) {
        lock.lock();
        try{
            stopScraping();
            startScraping(newDelay);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void afterPropertiesSet() {
        lock.lock();
        try {
            startScraping(getDelay());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getDelay() {
        return scrapingSettingsManager.getDelay();
    }

    @Override
    public void setDelay(int delay) {
        scrapingSettingsManager.setDelay(delay);
    }

    @Override
    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private void startScraping(int delay) {
        scraper = executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                log.info("scrapping");
                lastExecutionTimestamp.set(System.currentTimeMillis());
            }
        }, 0, delay, TimeUnit.MINUTES);
    }

    private void stopScraping() {
        if (!scraper.cancel(true)) {
            log.debug("Unable to cancel scraping, typically because it has already completed.");
        }
    }
}
