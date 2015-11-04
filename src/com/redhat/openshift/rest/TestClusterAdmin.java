package com.redhat.openshift.rest;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.internal.restclient.http.UrlConnectionHttpClient;
import com.openshift.internal.restclient.model.LimitRange;
import com.openshift.internal.restclient.model.ResourceQuota;
import com.openshift.internal.restclient.model.properties.ResourcePropertiesRegistry;
import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ISSLCertificateCallback;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.BasicAuthorizationStrategy;
import com.openshift.restclient.authorization.IAuthorizationContext;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.limit.ILimit;
import com.openshift.restclient.utils.Samples;

public class TestClusterAdmin {
	private static final Logger LOGGER = LoggerFactory.getLogger(UrlConnectionHttpClient.class);
	private static final String VERSION = "v1";

	public static void main(String[] args) {
		String url = "https://master.rhkose3.com:8443";

		ISSLCertificateCallback sslCertCallback = new NoopSSLCertificateCallback();
		IClient client = new ClientFactory().create(url, sslCertCallback);
		final String user = "cloudadmin";
		final String password = "cloudadmin";

		// userid/password를 사용한 로그인
		client.setAuthorizationStrategy(new BasicAuthorizationStrategy(user,
				password, "token-anything"));

		IAuthorizationContext context = client.getContext(client.getBaseURL()
				.toString()); // Login

		LOGGER.debug(context.getToken()); // get User's Token

		// Token을 사용하는 인증 방식으로 변경
		client.setAuthorizationStrategy(new TokenAuthorizationStrategy(context.getToken()));
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// describe Pod
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================Pod LIST");
/*
		List<IPod> pods = client.list(ResourceKind.POD, projects.get(0).getName()); // 특정 프로젝트에만 추가한 Template 리스트 가져오기
		for (IPod pod : pods) {
//			LOGGER.debug(template.toJson());
			LOGGER.debug("Pod Name: [" + pod.getName() + "]");
			LOGGER.debug("Pod Namespace: [" + pod.getNamespace() + "]");
			pod.
			
		}
*/
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// describe Node
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================Node LIST");
		List<IResource> nodes = client.list(ResourceKind.NODE); // 전체 노드 리스트 가져오기
		LOGGER.debug("Node count:[" + nodes.size() +"]");
		for (IResource node : nodes) {
			IResource resourceNode = client.get(ResourceKind.NODE, node.getName(), "");
			LOGGER.debug(resourceNode.toJson());
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Resource Quota
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================create a ResourceQuota");
		ModelNode resourceQuotaNode = ModelNode.fromJSONString(Samples.V1_RESOURCE_QUOTA.getContentAsString());
		ResourceQuota createResourceQuota = new ResourceQuota(resourceQuotaNode, client, ResourcePropertiesRegistry.getInstance().get(VERSION, ResourceKind.RESOURCE_QUOTA));
		createResourceQuota.setName("test-resourcequota");
		createResourceQuota.setCpu("10");
		LOGGER.debug(createResourceQuota.toJson());
		ResourceQuota resultResourceQuota = client.create(createResourceQuota, "testproject"); // 생성한 후, 리턴되는 객체는 사용하지 말 것(항상 새로 조회해서 사용해야 함)
		LOGGER.debug(resultResourceQuota.toJson());
		
		LOGGER.debug("=========================================================================get a ResourceQuota");
		ResourceQuota getResourceQuota = client.get(ResourceKind.RESOURCE_QUOTA, "test-resourcequota", "testproject");
		LOGGER.debug(getResourceQuota.toJson());
		
		LOGGER.debug("=========================================================================replace a ResourceQuota");
		getResourceQuota.setCpu("20");
		LOGGER.debug(getResourceQuota.toJson());
		ResourceQuota updateResourceQuota = client.update(getResourceQuota);
		LOGGER.debug(updateResourceQuota.toJson());

		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Limit Range
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================create a LimitRange");
		ModelNode limitRangeNode = ModelNode.fromJSONString(Samples.V1_LIMIT_RANGE.getContentAsString());
		LimitRange createLimitRange = new LimitRange(limitRangeNode, client, ResourcePropertiesRegistry.getInstance().get(VERSION, ResourceKind.LIMIT_RANGE));
		createLimitRange.setName("test-limitrange");
		List<ILimit> createLimits = createLimitRange.getLimits();
		ILimit podLimit = createLimits.get(0);
		podLimit.setMinCpu("1");
		ILimit containerLimit = createLimits.get(1);
		containerLimit.setMinCpu("100m");
		createLimitRange.setLimits(createLimits);
		LOGGER.debug(createLimitRange.toJson());
		LimitRange resultLimitRange = client.create(createLimitRange, "testproject"); // 생성한 후, 리턴되는 객체는 사용하지 말 것(항상 새로 조회해서 사용해야 함)
		LOGGER.debug(resultLimitRange.toJson());
		
		LOGGER.debug("=========================================================================get a LimitRange");
		LimitRange getLimitRange = client.get(ResourceKind.LIMIT_RANGE, "test-limitrange", "testproject");
		LOGGER.debug(getLimitRange.toJson());
		
		LOGGER.debug("=========================================================================replace a LimitRange");
		List<ILimit> limits = getLimitRange.getLimits();
		ILimit replacePodLimit = limits.get(0);
		replacePodLimit.setMaxCpu("3");
		getLimitRange.setLimits(limits);
		LOGGER.debug(getLimitRange.toJson());
		LimitRange updateLimitRange = client.update(getLimitRange);
		LOGGER.debug(getLimitRange.toJson());
		
		
	}

}
