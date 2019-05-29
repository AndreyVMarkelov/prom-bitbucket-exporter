package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ru.andreymarkelov.atlas.plugins.prombitbucketexporter.monitor.MonitoringUtils.initializeMap;

public class RepositoriesMonitor {
    private static final Logger log = LoggerFactory.getLogger(RepositoriesMonitor.class);

    public static final String REPOSITORIES_OBJECT_NAME = "com.atlassian.bitbucket:name=Repositories";
    public static final String[] REPOSITORIES_ATTRIBUTES = new String[]{ "Count" };

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    public Map<String, String> getConnectionPoolStats() throws Exception {
        Map<String, String> result = new HashMap();
        ObjectName partialObjectName = new ObjectName(REPOSITORIES_OBJECT_NAME);
        Set<ObjectName> objectNameSet = mBeanServer.queryNames(partialObjectName, null);
        if (objectNameSet.size() < 1) {
            log.error("No connection pool MBeans detected");
            result = initializeMap(REPOSITORIES_ATTRIBUTES, Collections.<String>emptySet());
        }
        if (objectNameSet.size() > 1) {
            log.error("More than one connection pool!");
        }
        for (ObjectName objectName : objectNameSet) {
            AttributeList attributeList = mBeanServer.getAttributes(objectName, REPOSITORIES_ATTRIBUTES);
            Map<String, String> attributeMap = MonitoringUtils.convertAttributeListToMap(attributeList);
            result = MonitoringUtils.combineMaps(result, attributeMap);
        }
        return result;
    }
}
