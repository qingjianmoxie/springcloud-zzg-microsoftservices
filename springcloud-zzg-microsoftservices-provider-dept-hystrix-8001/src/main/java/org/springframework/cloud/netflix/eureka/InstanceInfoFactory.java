/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.eureka;

import java.util.Map;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;

import lombok.extern.apachecommons.CommonsLog;

/**
 * See com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider
 * @author Spencer Gibb
 */
@CommonsLog
public class InstanceInfoFactory {

	public InstanceInfo create(EurekaInstanceConfig config) {
		LeaseInfo.Builder leaseInfoBuilder = LeaseInfo.Builder.newBuilder()
				.setRenewalIntervalInSecs(config.getLeaseRenewalIntervalInSeconds())
				.setDurationInSecs(config.getLeaseExpirationDurationInSeconds());

		// Builder the instance information to be registered with eureka
		// server
		InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder();

		String namespace = config.getNamespace();
		if (!namespace.endsWith(".")) {
			namespace = namespace + ".";
		}
		EurekaInstanceConfigBean configBean = (EurekaInstanceConfigBean) config;
		configBean.setIpAddress("127.0.0.1");
		configBean.setHostname("127.0.0.1");
		configBean.setNonSecurePort(80);
		builder.setNamespace(namespace).setAppName(configBean.getAppname())
				.setInstanceId(configBean.getInstanceId())
				.setAppGroupName(configBean.getAppGroupName())
				.setDataCenterInfo(configBean.getDataCenterInfo())
				//.setIPAddr(config.getIpAddress()).setHostName(config.getHostName(false))
				.setIPAddr(configBean.getIpAddress())
				.setHostName(configBean.getHostName(false))
				.setPort(configBean.getNonSecurePort())
				.enablePort(InstanceInfo.PortType.UNSECURE,
						configBean.isNonSecurePortEnabled())
				.setSecurePort(configBean.getSecurePort())
				.enablePort(InstanceInfo.PortType.SECURE, configBean.getSecurePortEnabled())
				.setVIPAddress(configBean.getVirtualHostName())
				.setSecureVIPAddress(configBean.getSecureVirtualHostName())
				.setHomePageUrl(configBean.getHomePageUrlPath(), configBean.getHomePageUrl())
				.setStatusPageUrl(configBean.getStatusPageUrlPath(),
						configBean.getStatusPageUrl())
				.setHealthCheckUrls(configBean.getHealthCheckUrlPath(),
						configBean.getHealthCheckUrl(), configBean.getSecureHealthCheckUrl())
				.setASGName(configBean.getASGName());

		// Start off with the STARTING state to avoid traffic
		if (!configBean.isInstanceEnabledOnit()) {
			InstanceInfo.InstanceStatus initialStatus = InstanceInfo.InstanceStatus.STARTING;
			if (log.isInfoEnabled()) {
				log.info("Setting initial instance status as: " + initialStatus);
			}
			builder.setStatus(initialStatus);
		}
		else {
			if (log.isInfoEnabled()) {
				log.info("Setting initial instance status as: "
						+ InstanceInfo.InstanceStatus.UP
						+ ". This may be too early for the instance to advertise itself as available. "
						+ "You would instead want to control this via a healthcheck handler.");
			}
		}

		// Add any user-specific metadata information
		for (Map.Entry<String, String> mapEntry : configBean.getMetadataMap().entrySet()) {
			String key = mapEntry.getKey();
			String value = mapEntry.getValue();
			builder.add(key, value);
		}

		InstanceInfo instanceInfo = builder.build();
		instanceInfo.setLeaseInfo(leaseInfoBuilder.build());
		return instanceInfo;
	}
}
