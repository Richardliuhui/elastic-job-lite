/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.internal.listener;

import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.internal.config.RescheduleListenerManager;
import io.elasticjob.lite.internal.election.ElectionListenerManager;
import io.elasticjob.lite.internal.failover.FailoverListenerManager;
import io.elasticjob.lite.internal.guarantee.GuaranteeListenerManager;
import io.elasticjob.lite.internal.instance.ShutdownListenerManager;
import io.elasticjob.lite.internal.instance.TriggerListenerManager;
import io.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager;
import io.elasticjob.lite.internal.sharding.ShardingListenerManager;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 作业注册中心的监听器管理者.
 * 
 * @author zhangliang
 */
public final class ListenerManager {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ElectionListenerManager electionListenerManager;
    
    private final ShardingListenerManager shardingListenerManager;
    
    private final FailoverListenerManager failoverListenerManager;
    
    private final MonitorExecutionListenerManager monitorExecutionListenerManager;
    
    private final ShutdownListenerManager shutdownListenerManager;
    
    private final TriggerListenerManager triggerListenerManager;
    
    private final RescheduleListenerManager rescheduleListenerManager;

    private final GuaranteeListenerManager guaranteeListenerManager;
    
    private final RegistryCenterConnectionStateListener regCenterConnectionStateListener;
    
    public ListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        electionListenerManager = new ElectionListenerManager(regCenter, jobName);
        shardingListenerManager = new ShardingListenerManager(regCenter, jobName);
        failoverListenerManager = new FailoverListenerManager(regCenter, jobName);
        monitorExecutionListenerManager = new MonitorExecutionListenerManager(regCenter, jobName);
        shutdownListenerManager = new ShutdownListenerManager(regCenter, jobName);
        triggerListenerManager = new TriggerListenerManager(regCenter, jobName);
        rescheduleListenerManager = new RescheduleListenerManager(regCenter, jobName);
        guaranteeListenerManager = new GuaranteeListenerManager(regCenter, jobName, elasticJobListeners);
        regCenterConnectionStateListener = new RegistryCenterConnectionStateListener(regCenter, jobName);
    }
    
    /**
     * 开启所有监听器.
     */
    public void startAllListeners() {
        //开启选举监听
        electionListenerManager.start();
        //分片监听
        shardingListenerManager.start();
        //failover监听
        failoverListenerManager.start();
        //监听配置属性,如果isMonitor=false,则清除running结点
        monitorExecutionListenerManager.start();
        //shutdown监听,关闭quartz调度
        shutdownListenerManager.start();
        triggerListenerManager.start();
        //cron表达式修改监听
        rescheduleListenerManager.start();
        guaranteeListenerManager.start();
        //添加zk连接监听,如果zk失去连接,所有job暂停执行,重连后恢复运行
        jobNodeStorage.addConnectionStateListener(regCenterConnectionStateListener);
    }
}
