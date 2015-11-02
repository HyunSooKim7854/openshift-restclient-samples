package com.redhat.openshift.rest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.internal.restclient.capability.resources.BuildTrigger;
import com.openshift.internal.restclient.capability.server.ServerTemplateProcessing;
import com.openshift.internal.restclient.http.UrlConnectionHttpClient;
import com.openshift.internal.restclient.model.Project;
import com.openshift.internal.restclient.model.project.OpenshiftProjectRequest;
import com.openshift.internal.restclient.model.properties.ResourcePropertiesRegistry;
import com.openshift.internal.restclient.model.template.Template;
import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ISSLCertificateCallback;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.BasicAuthorizationStrategy;
import com.openshift.restclient.authorization.IAuthorizationContext;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IBuildConfig;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IPort;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.IServicePort;
import com.openshift.restclient.model.template.IParameter;
import com.openshift.restclient.model.template.ITemplate;
import com.openshift.restclient.utils.Samples;

public class TestClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(UrlConnectionHttpClient.class);
	private static final String VERSION = "v1";

	public static void main(String[] args) {
		String url = "https://master.rhkose3.com:8443";

		ISSLCertificateCallback sslCertCallback = new NoopSSLCertificateCallback();
		IClient client = new ClientFactory().create(url, sslCertCallback);
		final String user = "user02";
		final String password = "user)@";

		// userid/password를 사용한 로그인
		client.setAuthorizationStrategy(new BasicAuthorizationStrategy(user,
				password, "token-anything"));

		IAuthorizationContext context = client.getContext(client.getBaseURL()
				.toString()); // Login

		LOGGER.debug(context.getToken()); // get User's Token

		// Token을 사용하는 인증 방식으로 변경
		client.setAuthorizationStrategy(new TokenAuthorizationStrategy(context.getToken()));
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 프로그램 수행전에, 반드시 한개의 프로젝트는 생성해두어야 함(이 프로젝트에서 여러가지 생성/수정/삭제 테스트를 수행하기 때문)

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Project
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================Project LIST");
		// 사용자의 전체 project list 얻기
		List<IProject> projects = client.list(ResourceKind.PROJECT); // 사용자의 프로젝트 리스트 가져오기
		for (int i = 0; i < projects.size(); i++) {
			IProject iproj = projects.get(i);
//			LOGGER.debug(iproj.toJson());

			String name = iproj.getName(); // 프로젝트 이름 가져오기
			List<IPod> ipods = client.list(ResourceKind.POD, name); // // 프로젝트 이름으로 프로젝트 내의 POD 리스트 가져오기
			for (int j = 0; j < ipods.size(); j++) {
				IPod pod = ipods.get(j);
				LOGGER.debug("Project Name:[" + iproj.getName() + "]");
				LOGGER.debug("POD Name:[" + pod.getName() + "]");
				LOGGER.debug("POD Host:[" + pod.getHost() + "]");
				LOGGER.debug("POD IP:[" + pod.getIP() + "]");
				Set<IPort> iports = pod.getContainerPorts();
				for (IPort port : iports) {
					LOGGER.debug("POD Port:[" + port.getContainerPort() + "]");
				}
			}

			List<IService> iservice = client.list(ResourceKind.SERVICE, name); // 프로젝트 이름으로 프로젝트 내의 Service 리스트 가져오기
			for (int j = 0; j < iservice.size(); j++) {
				IService service = iservice.get(j);
				LOGGER.debug("Project Name:[" + iproj.getName() + "]");
				LOGGER.debug("Service name:[" + service.getName() + "]");
				LOGGER.debug("Service PortalIP:[" + service.getPortalIP() + "]");
				LOGGER.debug("Service Port:[" + service.getPort() + "]");
				LOGGER.debug("Service TargetPort:[" + service.getTargetPort() + "]");
				List<IServicePort> iserviceports = service.getPorts();
				LOGGER.debug("ServicePorts======================");
				for (IServicePort serviceport : iserviceports) {
					LOGGER.debug("ServicePort Name:[" + serviceport.getName() + "]");
					LOGGER.debug("ServicePort Port:[" + serviceport.getPort() + "]");
					LOGGER.debug("ServicePort Protocol:[" + serviceport.getProtocol() + "]");
					LOGGER.debug("ServicePort TargetPort:[" + serviceport.getTargetPort() + "]");
				}
			}
		}

		// 프로젝트 생성하기
		LOGGER.debug("=========================================================================Project CREATE");
		ModelNode node = ModelNode.fromJSONString(Samples.V1_PROJECT_REQUEST.getContentAsString());
		OpenshiftProjectRequest createProj = new OpenshiftProjectRequest(node, client, ResourcePropertiesRegistry.getInstance().get(VERSION, ResourceKind.PROJECT_REQUEST));
		
		createProj.setName("testtesttestproj");
		createProj.setDescription("testesttestproj-description");
		createProj.setDisplayName("testesttestproj-dsiplayname");
//		LOGGER.debug(createProj.toJson());
		IProject result = (IProject)client.create(createProj); // 생성한 후, 리턴되는 객체는 사용하지 말 것(항상 새로 조회해서 사용해야 함)
//		LOGGER.debug(result.toJson());
		
		// 프로젝트 수정하기
		LOGGER.debug("=========================================================================Project GET");
		Project new_project = (Project)client.get(ResourceKind.PROJECT, "testtesttestproj", "testtesttestproj"); // 프로젝트 이름으로 프로젝트 가져오기
		LOGGER.debug(new_project.toJson()); // 조회한 결과를 JSON 형태로 출력
		new_project.setDescription("hskim-description"); // 수정
		new_project.setDisplayName("hskim-dsiplayname"); // 수정
		
		LOGGER.debug("=========================================================================Project UPDATE");
//		LOGGER.debug(new_project.toJson()); // 수정직전 JSON
		IProject update_result = (IProject)client.update(new_project);	// 프로젝트 수정하기
//		LOGGER.debug(update_result.toJson()); // 수정직후 리턴된 JSON
		
		// 수정한 project 정보 조회
		LOGGER.debug("=========================================================================Project GET");
		IProject inq_projects = client.get(ResourceKind.PROJECT, "testtesttestproj", "testtesttestproj"); // 프로젝트 이름으로 프로젝트 가져오기
		LOGGER.debug("생성후, 조회한 Project Name:[" + inq_projects.getName() + "]");
		LOGGER.debug("생성후, 조회한 Project Description:[" + inq_projects.getDescription() + "]");
		LOGGER.debug("생성후, 조회한 Project displayName:[" + inq_projects.getDisplayName() + "]");
	
		// 조회한 후, 프로젝트 삭제하기
		LOGGER.debug("=========================================================================Project DELETE");
		client.delete(inq_projects);	// 프로젝트 삭제하기
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Template
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================Template LIST");
		List<ITemplate> templates = client.list(ResourceKind.TEMPLATE, projects.get(0).getName()); // 특정 프로젝트에만 추가한 Template 리스트 가져오기
		for (ITemplate template : templates) {
//			LOGGER.debug(template.toJson());
			LOGGER.debug("Template Name: [" + template.getName() + "]");
			LOGGER.debug("Template Namespace: [" + template.getNamespace() + "]");
			LOGGER.debug("Template iconClass: [" + template.getAnnotation("iconClass") + "]");
			Map<String, IParameter> params = template.getParameters();	// Template의 Parameter 가져오기
			for( String key : params.keySet() ){
	            LOGGER.debug(String.format("Key: %s, Name : %s, Value : %s", key, params.get(key).getName(), params.get(key).getValue()) ); // Key와 Name은 동일하며, Value는 디폴트값이 있을 수도 있음
	        }
			
		}
		List<ITemplate> ose_templates = client.list(ResourceKind.TEMPLATE, "openshift"); // openshift 프로젝트의 Template 리스트 가져오기
		String templateName = null;
		for (ITemplate template : ose_templates) {
//			LOGGER.debug(template.toJson());
			LOGGER.debug("Template Name: [" + template.getName() + "]");
			LOGGER.debug("Template Namespace: [" + template.getNamespace() + "]");
			LOGGER.debug("Template iconClass: [" + template.getAnnotation("iconClass") + "]");
			Map<String, IParameter> params = template.getParameters();	// Template의 Parameter 가져오기
			for( String key : params.keySet() ){
	            LOGGER.debug(String.format("Key: %s, Name : %s, Value : %s", key, params.get(key).getName(), params.get(key).getValue()) ); // Key와 Name은 동일하며, Value는 디폴트값이 있을 수도 있음
	        }
			
			if ("jws-tomcat7-basic-sti".equals(template.getName())) {	// 아래에서 한개의 template만 조회하는 것에 대한 테스트를 위해서
				templateName = template.getName();
			}
		}
		
		LOGGER.debug("=========================================================================Template GET");
		ITemplate specific_template = client.get(ResourceKind.TEMPLATE, templateName, "openshift"); // openshift 프로젝트의 특정 이름의 Template 리스트 가져오기
		LOGGER.debug("Template Name: [" + specific_template.getName() + "]");
		LOGGER.debug("Template Namespace: [" + specific_template.getNamespace() + "]");
		LOGGER.debug("Template iconClass: [" + specific_template.getAnnotation("iconClass") + "]");
		
		LOGGER.debug("=========================================================================Template CREATE");
		ModelNode templateNode = ModelNode.fromJSONString(Samples.V1_TEMPLATE.getContentAsString());	// Template 생성을 위한 JSON 파일을 입력받음
		Template createTemplate = new Template(templateNode, client, ResourcePropertiesRegistry.getInstance().get(VERSION, ResourceKind.PROCESSED_TEMPLATES));
		
		String newTemplateName = "test-template";
		createTemplate.setName(newTemplateName);
		createTemplate.setNamespace(projects.get(0).getName());	// Template을 생성하고자 하는 특정 프로젝트이름을 넣어야 함. 여기서는 (그냥 샘플이니까...) 첫번재 얻어걸리는 프로젝트에다가 생성하려고 설정함
		createTemplate.setAnnotation("iconClass", "TEST");
		
//		LOGGER.debug(createTemplate.toJson());
		Template resultTemplate = client.create(createTemplate);
//		LOGGER.debug(resultTemplate.toJson());
		
		LOGGER.debug("=========================================================================Template UPDATE");	// openshift 프로젝트에 있는 template은 normal user는 수정 불가능(참고)
		resultTemplate.setAnnotation("iconClass", "hskim");
//		LOGGER.debug(resultTemplate.toJson());
		ITemplate specific_template2 = client.update(resultTemplate); // 특정 이름의 Template 수정하기
		LOGGER.debug("Template Name: [" + specific_template2.getName() + "]");
		LOGGER.debug("Template Namespace: [" + specific_template2.getNamespace() + "]");
		LOGGER.debug("Template iconClass: [" + specific_template2.getAnnotation("iconClass") + "]");
		
		LOGGER.debug("=========================================================================Template DELETE");	// openshift 프로젝트에 있는 template은 normal user는 삭제 불가능(참고)
		client.delete(specific_template2); // 특정 이름의 Template 삭제하기
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Create Application Resources(POD, Service...) by using Template
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================Application CREATE by Template");
		ServerTemplateProcessing serverTemplateProcessing = new ServerTemplateProcessing(client);
		
		// 화면으로부터 입력받는 입력값에 대해서 셋팅해야 함
		Map<String, IParameter> temp_params = specific_template.getParameters();	// 화면으로부터 입력받은 값을 셋팅하기 위해서 Parameter 항목 얻기(얻은 후, 화면에 해당 파라미터명을 표현하여 입력을 받아야 함)
		for( String key : temp_params.keySet() ){
			IParameter iparam = temp_params.get(key);
            if ("GIT_URI".equals(iparam.getName())) {
            	iparam.setValue("https://github.com/");	// 화면으로부터 입력받은 값을 셋팅하면 됨
            }
        }
		specific_template.updateParameterValues(temp_params.values());
		
		LOGGER.debug(specific_template.toJson());
		// template processing을 수행하여, 화면으로부터 입력받은 parameter 값을 사용하여 변수를 치환한다
		ITemplate result4Create = serverTemplateProcessing.process(specific_template, projects.get(0).getName());	// Application을 생성하고자 하는 특정 프로젝트이름을 넣어야 함. 여기서는 (그냥 샘플이니까...) 첫번재 얻어걸리는 프로젝트에다가 생성하려고 설정함
//		LOGGER.debug(result4Create.toJson());
		
		// 서버로부터 리턴된 정보를 해석해서, object 하나씩 만드는 요청을 수행해야 함(예, services, routes, imagestreams, buildconfigs, deploymentconfigs...)
		LOGGER.debug("=========================================================================Items CREATE by Template");
		Map<String, String> labels = null;
		Collection<IResource> items = result4Create.getItems();
		for (IResource item : items) {
//			LOGGER.debug(item.toJson());
			IResource resultRes = client.create(item, projects.get(0).getName());
//			LOGGER.debug(resultRes.toJson());
			if ("Service".equals(resultRes.getKind())) {
				labels = resultRes.getLabels();
			}
		}
	
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Service
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================Service LIST");
		List<IService> services = client.list(ResourceKind.SERVICE, projects.get(0).getName(), labels); // 특정 프로젝트에 있는, 특정 label을 가지고 있는 Service 리스트 가져오기
		for (int j = 0; j < services.size(); j++) {
			IService service = services.get(j);
			LOGGER.debug("Project Name:[" + projects.get(0).getName() + "]");
			LOGGER.debug("Service name:[" + service.getName() + "]");
			LOGGER.debug("Service PortalIP:[" + service.getPortalIP() + "]");
			LOGGER.debug("Service Port:[" + service.getPort() + "]");
			LOGGER.debug("Service TargetPort:[" + service.getTargetPort() + "]");
			List<IServicePort> iserviceports = service.getPorts();
			LOGGER.debug("ServicePorts======================");
			for (IServicePort serviceport : iserviceports) {
				LOGGER.debug("ServicePort Name:[" + serviceport.getName() + "]");
				LOGGER.debug("ServicePort Port:[" + serviceport.getPort() + "]");
				LOGGER.debug("ServicePort Protocol:[" + serviceport.getProtocol() + "]");
				LOGGER.debug("ServicePort TargetPort:[" + serviceport.getTargetPort() + "]");
			}
		}
		
		LOGGER.debug("=========================================================================Service CREATE");
		/*
		new Service
		List<IService> services = client.create(ResourceKind.SERVICE, projects.get(0).getName()); // 특정 프로젝트에 특정 label을 가지고 있는 Service 생성하기
		for (int j = 0; j < services.size(); j++) {
			IService service = services.get(j);
			LOGGER.debug("Project Name:[" + projects.get(0).getName() + "]");
			LOGGER.debug("Service name:[" + service.getName() + "]");
			LOGGER.debug("Service PortalIP:[" + service.getPortalIP() + "]");
			LOGGER.debug("Service Port:[" + service.getPort() + "]");
			LOGGER.debug("Service TargetPort:[" + service.getTargetPort() + "]");
			List<IServicePort> iserviceports = service.getPorts();
			LOGGER.debug("ServicePorts======================");
			for (IServicePort serviceport : iserviceports) {
				LOGGER.debug("ServicePort Name:[" + serviceport.getName() + "]");
				LOGGER.debug("ServicePort Port:[" + serviceport.getPort() + "]");
				LOGGER.debug("ServicePort Protocol:[" + serviceport.getProtocol() + "]");
				LOGGER.debug("ServicePort TargetPort:[" + serviceport.getTargetPort() + "]");
			}
		}
		*/
		
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
		// BuildConfig
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// 웹콘솔에서 "Start Build"버튼 클릭------------------------------------------
		LOGGER.debug("=========================================================================BuildConfig LIST");
		List<IBuildConfig> buildConfigs = client.list(ResourceKind.BUILD_CONFIG, projects.get(0).getName(), labels); // 특정 프로젝트에 있는, 특정 label을 가지고 있는 BuildConfigs 리스트 가져오기
		for (int j = 0; j < buildConfigs.size(); j++) {
			IBuildConfig buildConfig = buildConfigs.get(j);
			LOGGER.debug("Project Name:[" + projects.get(0).getName() + "]");
			LOGGER.debug("BuildConfig name:[" + buildConfig.getName() + "]");
		}
		LOGGER.debug("=========================================================================create instantiate of a BuildRequest");	 // 웹콘솔에서 "Start Build"버튼 클릭시 수행되는 API
		IBuildConfig buildConfig = buildConfigs.get(0);
		BuildTrigger buildTrigger1 = new BuildTrigger(buildConfig, client);
		buildTrigger1.trigger();

		// 웹콘솔에서 "Rebuild"버튼 클릭------------------------------------------
		LOGGER.debug("=========================================================================Build LIST");
		List<IBuild> builds = client.list(ResourceKind.BUILD, projects.get(0).getName(), labels); // 특정 프로젝트에 있는, 특정 label을 가지고 있는 Build 리스트 가져오기
		for (int j = 0; j < builds.size(); j++) {
			IBuild build = builds.get(j);
			LOGGER.debug("Project Name:[" + projects.get(0).getName() + "]");
			LOGGER.debug("Build name:[" + build.getName() + "]");
		}
		LOGGER.debug("=========================================================================create clone of a BuildRequest");	// 웹콘솔에서 "Rebuild"버튼 클릭시 수행되는 API
		IBuild build = builds.get(0);
		BuildTrigger buildTrigger2 = new BuildTrigger(build, client);
		buildTrigger2.trigger();		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// DeploymentConfig
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		LOGGER.debug("=========================================================================read the specified DeploymentConfig");// 특정 프로젝트에 있는, 특정 label을 가지고 있는 DeploymentConfig 리스트 가져오기
		List<IDeploymentConfig> deploymentConfigs = client.list(ResourceKind.DEPLOYMENT_CONFIG, projects.get(0).getName(), labels); // 특정 프로젝트에 있는, 특정 label을 가지고 있는 BuildConfigs 리스트 가져오기
		for (int j = 0; j < deploymentConfigs.size(); j++) {
			IDeploymentConfig deploymentConfig = deploymentConfigs.get(j);
			LOGGER.debug("Project Name:[" + projects.get(0).getName() + "]");
			LOGGER.debug("DeploymentConfig name:[" + deploymentConfig.getName() + "]");
		}
		
		LOGGER.debug("=========================================================================replace the specified DeploymentConfig"); // 웹콘솔에서 "Start Deployment"버튼 클릭시 수행되는 API
		IDeploymentConfig deploymentConfig = deploymentConfigs.get(0);
		client.update(deploymentConfig);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Resource Quota
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Limit Range
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Route
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ImageStream
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Replication Controller
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// POD
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Delete All Application Resources(POD, Service...) by using Label
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Build
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Policy
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Role
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// User
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Persistent Volume
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Service Account
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	}

}
