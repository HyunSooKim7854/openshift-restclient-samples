package com.redhat.openshift.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.internal.restclient.http.UrlConnectionHttpClient;
import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ISSLCertificateCallback;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.BasicAuthorizationStrategy;
import com.openshift.restclient.authorization.IAuthorizationContext;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import com.openshift.restclient.model.INode;
import com.openshift.restclient.model.IResource;

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
	}

}
