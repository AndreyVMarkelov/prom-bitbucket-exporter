package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.atlassian.bitbucket.pull.PullRequestState.OPEN;
import static java.lang.Thread.MIN_PRIORITY;
import static java.util.concurrent.Executors.defaultThreadFactory;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class ScheduledMetricEvaluatorImpl implements ScheduledMetricEvaluator, DisposableBean, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ScheduledMetricEvaluator.class);

    /**
     * Bitbucket components.
     */
    private final ScrapingSettingsManager scrapingSettingsManager;
    private final RepositoryService repositoryService;
    private final ProjectService projectService;
    private final PullRequestService pullRequestService;
    private final SecurityService securityService;

    /**
     * Scheduled executor to grab metrics.
     */
    private final ScheduledExecutorService executorService;
    private final AtomicLong lastExecutionTimestamp;
    private final Lock lock;

    private final AtomicInteger totalProjects;
    private final AtomicInteger totalRepositories;
    private final AtomicLong totalPullRequests;

    private ScheduledFuture<?> scraper;

    public ScheduledMetricEvaluatorImpl(
            ScrapingSettingsManager scrapingSettingsManager,
            RepositoryService repositoryService,
            ProjectService projectService,
            PullRequestService pullRequestService,
            SecurityService securityService) {
        this.scrapingSettingsManager = scrapingSettingsManager;
        this.repositoryService = repositoryService;
        this.projectService = projectService;
        this.pullRequestService = pullRequestService;
        this.securityService = securityService;
        this.executorService = newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@Nonnull Runnable r) {
                Thread thread = defaultThreadFactory().newThread(r);
                thread.setPriority(MIN_PRIORITY);
                return thread;
            }
        });
        this.lastExecutionTimestamp = new AtomicLong(-1);
        this.totalProjects = new AtomicInteger(0);
        this.totalRepositories = new AtomicInteger(0);
        this.totalPullRequests = new AtomicLong(0);
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
            startScraping(scrapingSettingsManager.getDelay());
        } finally {
            lock.unlock();
        }
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

    @Override
    public long getTotalProjects() {
        return totalProjects.get();
    }

    @Override
    public long getTotalRepositories() {
        return totalRepositories.get();
    }

    @Override
    public long getTotalPullRequests() {
        return totalPullRequests.get();
    }

    private void startScraping(int delay) {
        scraper = executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                calculateTotalProjects();
                calculateTotalRepositories();
                calculateTotalPullRequests();
                lastExecutionTimestamp.set(System.currentTimeMillis());
            }
        }, 0, delay, TimeUnit.MINUTES);
    }

    private void calculateTotalProjects() {
        try {
            securityService.withPermission(Permission.ADMIN, "Read all projects").call(new Operation<Object, Throwable>() {
                @Override
                public Object perform() {
                    totalProjects.set(projectService.findAllKeys().size());
                    return null;
                }
            });
        } catch (Throwable th) {
            log.error("Cannot read all projects", th);
        }
    }

    private void calculateTotalRepositories() {
        try {
            securityService.withPermission(Permission.ADMIN, "Read all repositories").call(new Operation<Object, Throwable>() {
                @Override
                public Object perform() {
                    int repositories = 0;
                    PageRequest nextPage = new PageRequestImpl(0, 10000);
                    do {
                        Page<Repository> repositoryPage = repositoryService.findAll(nextPage);
                        repositories += repositoryPage.getSize();
                        nextPage = repositoryPage.getNextPageRequest();
                    } while (nextPage != null);
                    totalRepositories.set(repositories);
                    return null;
                }
            });
        } catch (Throwable th) {
            log.error("Cannot read all repositories", th);
        }
    }

    private void calculateTotalPullRequests() {
        try {
            securityService.withPermission(Permission.ADMIN, "Read all pull requests").call(new Operation<Object, Throwable>() {
                @Override
                public Object perform() {
                    int pullRequests = 0;
                    PageRequest nextPage = new PageRequestImpl(0, 10000);
                    do {
                        Page<PullRequest> pullRequestPage = pullRequestService.search(new PullRequestSearchRequest.Builder().state(OPEN).build(), nextPage);
                        pullRequests += pullRequestPage.getSize();
                        nextPage = pullRequestPage.getNextPageRequest();
                    } while (nextPage != null);
                    totalPullRequests.set(pullRequests);
                    return null;
                }
            });
        } catch (Throwable th) {
            log.error("Cannot read all pull requests", th);
        }
    }

    private void stopScraping() {
        if (!scraper.cancel(true)) {
            log.debug("Unable to cancel scraping, typically because it has already completed.");
        }
    }
}
