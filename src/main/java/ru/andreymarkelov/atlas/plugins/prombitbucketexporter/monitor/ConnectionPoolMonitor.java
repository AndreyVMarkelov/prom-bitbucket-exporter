package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

public class ConnectionPoolMonitor {
    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolMonitor.class);

    private static final String PATH = "database.connectionPool";

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
}
