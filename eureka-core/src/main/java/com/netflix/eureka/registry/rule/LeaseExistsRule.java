package com.netflix.eureka.registry.rule;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.lease.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This rule matches if we have an existing lease for the instance that is UP or OUT_OF_SERVICE.
 *
 * Created by Nikos Michalakis on 7/13/16.
 */
public class LeaseExistsRule implements InstanceStatusOverrideRule {

    private static final Logger logger = LoggerFactory.getLogger(LeaseExistsRule.class);

    @Override
    public StatusOverrideResult apply(InstanceInfo instanceInfo,
                                      Lease<InstanceInfo> existingLease,
                                      boolean isReplication) {
        // This is for backward compatibility until all applications have ASG
        // names, otherwise while starting up
        // the client status may override status replicated from other servers
        if (!isReplication) {
            InstanceInfo.InstanceStatus existingStatus = null;
            if (existingLease != null) {
                // 获取注册表里的status
                existingStatus = existingLease.getHolder().getStatus();
            }
            // Allow server to have its way when the status is UP or OUT_OF_SERVICE
            if ((existingStatus != null)
                    && (InstanceInfo.InstanceStatus.OUT_OF_SERVICE.equals(existingStatus)
                    || InstanceInfo.InstanceStatus.UP.equals(existingStatus))) {
                // 注册表中该实例status是up/outOfService，则返回匹配成功
                // 前面的缓存map没有匹配上，如果这个也没有匹配上，则注册表里面的这个client的状态为unknown
                // 什么时候会出现这种情况？只有client删除overridden状态请求，会把缓存map中的client删除，注册表里面的状态改为unknown
                logger.debug("There is already an existing lease with status {}  for instance {}",
                        existingLease.getHolder().getStatus().name(),
                        existingLease.getHolder().getId());
                return StatusOverrideResult.matchingStatus(existingLease.getHolder().getStatus());
            }
        }
        return StatusOverrideResult.NO_MATCH;
    }

    @Override
    public String toString() {
        return LeaseExistsRule.class.getName();
    }
}
