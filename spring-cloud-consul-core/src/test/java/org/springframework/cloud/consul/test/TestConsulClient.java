/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.test;

import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.acl.model.Acl;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.ecwid.consul.v1.acl.model.UpdateAcl;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.NewCheck;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.CatalogNodesRequest;
import com.ecwid.consul.v1.catalog.CatalogServiceRequest;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogNode;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.catalog.model.Node;
import com.ecwid.consul.v1.coordinate.model.Datacenter;
import com.ecwid.consul.v1.event.EventListRequest;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.query.model.QueryExecution;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;

public class TestConsulClient extends ConsulClient {

	private ConsulClient consulClient;

	public TestConsulClient(ConsulClient consulClient) {
		this.consulClient = consulClient;
	}

	@Override
	public Response<String> aclCreate(NewAcl newAcl, String token) {
		return consulClient.aclCreate(newAcl, token);
	}

	@Override
	public Response<Void> aclUpdate(UpdateAcl updateAcl, String token) {
		return consulClient.aclUpdate(updateAcl, token);
	}

	@Override
	public Response<Void> aclDestroy(String aclId, String token) {
		return consulClient.aclDestroy(aclId, token);
	}

	@Override
	public Response<Acl> getAcl(String id) {
		return consulClient.getAcl(id);
	}

	@Override
	public Response<String> aclClone(String aclId, String token) {
		return consulClient.aclClone(aclId, token);
	}

	@Override
	public Response<List<Acl>> getAclList(String token) {
		return consulClient.getAclList(token);
	}

	@Override
	public Response<Map<String, Check>> getAgentChecks() {
		return consulClient.getAgentChecks();
	}

	@Override
	public Response<Map<String, Service>> getAgentServices() {
		return consulClient.getAgentServices();
	}

	@Override
	public Response<List<Member>> getAgentMembers() {
		return consulClient.getAgentMembers();
	}

	@Override
	public Response<Self> getAgentSelf() {
		return consulClient.getAgentSelf();
	}

	@Override
	public Response<Self> getAgentSelf(String token) {
		return consulClient.getAgentSelf(token);
	}

	@Override
	public Response<Void> agentSetMaintenance(boolean maintenanceEnabled) {
		return consulClient.agentSetMaintenance(maintenanceEnabled);
	}

	@Override
	public Response<Void> agentSetMaintenance(boolean maintenanceEnabled, String reason) {
		return consulClient.agentSetMaintenance(maintenanceEnabled, reason);
	}

	@Override
	public Response<Void> agentJoin(String address, boolean wan) {
		return consulClient.agentJoin(address, wan);
	}

	@Override
	public Response<Void> agentForceLeave(String node) {
		return consulClient.agentForceLeave(node);
	}

	@Override
	public Response<Void> agentCheckRegister(NewCheck newCheck) {
		return consulClient.agentCheckRegister(newCheck);
	}

	@Override
	public Response<Void> agentCheckRegister(NewCheck newCheck, String token) {
		return consulClient.agentCheckRegister(newCheck, token);
	}

	@Override
	public Response<Void> agentCheckDeregister(String checkId) {
		return consulClient.agentCheckDeregister(checkId);
	}

	@Override
	public Response<Void> agentCheckDeregister(String checkId, String token) {
		return consulClient.agentCheckDeregister(checkId, token);
	}

	@Override
	public Response<Void> agentCheckPass(String checkId) {
		return consulClient.agentCheckPass(checkId);
	}

	@Override
	public Response<Void> agentCheckPass(String checkId, String note) {
		return consulClient.agentCheckPass(checkId, note);
	}

	@Override
	public Response<Void> agentCheckPass(String checkId, String note, String token) {
		return consulClient.agentCheckPass(checkId, note, token);
	}

	@Override
	public Response<Void> agentCheckWarn(String checkId) {
		return consulClient.agentCheckWarn(checkId);
	}

	@Override
	public Response<Void> agentCheckWarn(String checkId, String note) {
		return consulClient.agentCheckWarn(checkId, note);
	}

	@Override
	public Response<Void> agentCheckWarn(String checkId, String note, String token) {
		return consulClient.agentCheckWarn(checkId, note, token);
	}

	@Override
	public Response<Void> agentCheckFail(String checkId) {
		return consulClient.agentCheckFail(checkId);
	}

	@Override
	public Response<Void> agentCheckFail(String checkId, String note) {
		return consulClient.agentCheckFail(checkId, note);
	}

	@Override
	public Response<Void> agentCheckFail(String checkId, String note, String token) {
		return consulClient.agentCheckFail(checkId, note, token);
	}

	@Override
	public Response<Void> agentServiceRegister(NewService newService) {
		return consulClient.agentServiceRegister(newService);
	}

	@Override
	public Response<Void> agentServiceRegister(NewService newService, String token) {
		return consulClient.agentServiceRegister(newService, token);
	}

	@Override
	public Response<Void> agentServiceDeregister(String serviceId) {
		return consulClient.agentServiceDeregister(serviceId);
	}

	@Override
	public Response<Void> agentServiceDeregister(String serviceId, String token) {
		return consulClient.agentServiceDeregister(serviceId, token);
	}

	@Override
	public Response<Void> agentServiceSetMaintenance(String serviceId, boolean maintenanceEnabled) {
		return consulClient.agentServiceSetMaintenance(serviceId, maintenanceEnabled);
	}

	@Override
	public Response<Void> agentServiceSetMaintenance(String serviceId, boolean maintenanceEnabled, String reason) {
		return consulClient.agentServiceSetMaintenance(serviceId, maintenanceEnabled, reason);
	}

	@Override
	public Response<Void> agentReload() {
		return consulClient.agentReload();
	}

	@Override
	public Response<Void> catalogRegister(CatalogRegistration catalogRegistration) {
		return consulClient.catalogRegister(catalogRegistration);
	}

	@Override
	public Response<Void> catalogRegister(CatalogRegistration catalogRegistration, String token) {
		return consulClient.catalogRegister(catalogRegistration, token);
	}

	@Override
	public Response<Void> catalogDeregister(CatalogDeregistration catalogDeregistration) {
		return consulClient.catalogDeregister(catalogDeregistration);
	}

	@Override
	public Response<Void> catalogDeregister(CatalogDeregistration catalogDeregistration, String token) {
		return consulClient.catalogDeregister(catalogDeregistration, token);
	}

	@Override
	public Response<List<String>> getCatalogDatacenters() {
		return consulClient.getCatalogDatacenters();
	}

	@Override
	@Deprecated
	public Response<List<Node>> getCatalogNodes(QueryParams queryParams) {
		return consulClient.getCatalogNodes(queryParams);
	}

	@Override
	public Response<List<Node>> getCatalogNodes(CatalogNodesRequest catalogNodesRequest) {
		return consulClient.getCatalogNodes(catalogNodesRequest);
	}

	@Override
	@Deprecated
	public Response<Map<String, List<String>>> getCatalogServices(QueryParams queryParams) {
		return consulClient.getCatalogServices(queryParams);
	}

	@Override
	@Deprecated
	public Response<Map<String, List<String>>> getCatalogServices(QueryParams queryParams, String token) {
		return consulClient.getCatalogServices(queryParams, token);
	}

	@Override
	public Response<Map<String, List<String>>> getCatalogServices(CatalogServicesRequest catalogServicesRequest) {
		return consulClient.getCatalogServices(catalogServicesRequest);
	}

	@Override
	@Deprecated
	public Response<List<CatalogService>> getCatalogService(String serviceName, QueryParams queryParams) {
		return consulClient.getCatalogService(serviceName, queryParams);
	}

	@Override
	@Deprecated
	public Response<List<CatalogService>> getCatalogService(String serviceName, String tag, QueryParams queryParams) {
		return consulClient.getCatalogService(serviceName, tag, queryParams);
	}

	@Override
	@Deprecated
	public Response<List<CatalogService>> getCatalogService(String serviceName, QueryParams queryParams, String token) {
		return consulClient.getCatalogService(serviceName, queryParams, token);
	}

	@Override
	@Deprecated
	public Response<List<CatalogService>> getCatalogService(String serviceName, String tag, QueryParams queryParams,
			String token) {
		return consulClient.getCatalogService(serviceName, tag, queryParams, token);
	}

	@Override
	@Deprecated
	public Response<List<CatalogService>> getCatalogService(String serviceName, String[] tags, QueryParams queryParams,
			String token) {
		return consulClient.getCatalogService(serviceName, tags, queryParams, token);
	}

	@Override
	public Response<List<CatalogService>> getCatalogService(String serviceName,
			CatalogServiceRequest catalogServiceRequest) {
		return consulClient.getCatalogService(serviceName, catalogServiceRequest);
	}

	@Override
	public Response<CatalogNode> getCatalogNode(String nodeName, QueryParams queryParams) {
		return consulClient.getCatalogNode(nodeName, queryParams);
	}

	@Override
	public Response<List<Datacenter>> getDatacenters() {
		return consulClient.getDatacenters();
	}

	@Override
	public Response<List<com.ecwid.consul.v1.coordinate.model.Node>> getNodes(QueryParams queryParams) {
		return consulClient.getNodes(queryParams);
	}

	@Override
	public Response<Event> eventFire(String event, String payload, EventParams eventParams, QueryParams queryParams) {
		return consulClient.eventFire(event, payload, eventParams, queryParams);
	}

	@Override
	@Deprecated
	public Response<List<Event>> eventList(QueryParams queryParams) {
		return consulClient.eventList(queryParams);
	}

	@Override
	@Deprecated
	public Response<List<Event>> eventList(String event, QueryParams queryParams) {
		return consulClient.eventList(event, queryParams);
	}

	@Override
	public Response<List<Event>> eventList(EventListRequest eventListRequest) {
		return consulClient.eventList(eventListRequest);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksForNode(String nodeName,
			QueryParams queryParams) {
		return consulClient.getHealthChecksForNode(nodeName, queryParams);
	}

	@Override
	@Deprecated
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksForService(String serviceName,
			QueryParams queryParams) {
		return consulClient.getHealthChecksForService(serviceName, queryParams);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksForService(String serviceName,
			HealthChecksForServiceRequest healthChecksForServiceRequest) {
		return consulClient.getHealthChecksForService(serviceName, healthChecksForServiceRequest);
	}

	@Override
	@Deprecated
	public Response<List<HealthService>> getHealthServices(String serviceName, boolean onlyPassing,
			QueryParams queryParams) {
		return consulClient.getHealthServices(serviceName, onlyPassing, queryParams);
	}

	@Override
	@Deprecated
	public Response<List<HealthService>> getHealthServices(String serviceName, String tag, boolean onlyPassing,
			QueryParams queryParams) {
		return consulClient.getHealthServices(serviceName, tag, onlyPassing, queryParams);
	}

	@Override
	@Deprecated
	public Response<List<HealthService>> getHealthServices(String serviceName, boolean onlyPassing,
			QueryParams queryParams, String token) {
		return consulClient.getHealthServices(serviceName, onlyPassing, queryParams, token);
	}

	@Override
	@Deprecated
	public Response<List<HealthService>> getHealthServices(String serviceName, String tag, boolean onlyPassing,
			QueryParams queryParams, String token) {
		return consulClient.getHealthServices(serviceName, tag, onlyPassing, queryParams, token);
	}

	@Override
	@Deprecated
	public Response<List<HealthService>> getHealthServices(String serviceName, String[] tags, boolean onlyPassing,
			QueryParams queryParams, String token) {
		return consulClient.getHealthServices(serviceName, tags, onlyPassing, queryParams, token);
	}

	@Override
	public Response<List<HealthService>> getHealthServices(String serviceName,
			HealthServicesRequest healthServicesRequest) {
		return consulClient.getHealthServices(serviceName, healthServicesRequest);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksState(QueryParams queryParams) {
		return consulClient.getHealthChecksState(queryParams);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksState(
			com.ecwid.consul.v1.health.model.Check.CheckStatus checkStatus, QueryParams queryParams) {
		return consulClient.getHealthChecksState(checkStatus, queryParams);
	}

	@Override
	public Response<GetValue> getKVValue(String key) {
		return consulClient.getKVValue(key);
	}

	@Override
	public Response<GetValue> getKVValue(String key, String token) {
		return consulClient.getKVValue(key, token);
	}

	@Override
	public Response<GetValue> getKVValue(String key, QueryParams queryParams) {
		return consulClient.getKVValue(key, queryParams);
	}

	@Override
	public Response<GetValue> getKVValue(String key, String token, QueryParams queryParams) {
		return consulClient.getKVValue(key, token, queryParams);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key) {
		return consulClient.getKVBinaryValue(key);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key, String token) {
		return consulClient.getKVBinaryValue(key, token);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key, QueryParams queryParams) {
		return consulClient.getKVBinaryValue(key, queryParams);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key, String token, QueryParams queryParams) {
		return consulClient.getKVBinaryValue(key, token, queryParams);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix) {
		return consulClient.getKVValues(keyPrefix);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix, String token) {
		return consulClient.getKVValues(keyPrefix, token);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix, QueryParams queryParams) {
		return consulClient.getKVValues(keyPrefix, queryParams);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix, String token, QueryParams queryParams) {
		return consulClient.getKVValues(keyPrefix, token, queryParams);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix) {
		return consulClient.getKVBinaryValues(keyPrefix);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix, String token) {
		return consulClient.getKVBinaryValues(keyPrefix, token);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix, QueryParams queryParams) {
		return consulClient.getKVBinaryValues(keyPrefix, queryParams);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix, String token, QueryParams queryParams) {
		return consulClient.getKVBinaryValues(keyPrefix, token, queryParams);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix) {
		return consulClient.getKVKeysOnly(keyPrefix);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix, String separator, String token) {
		return consulClient.getKVKeysOnly(keyPrefix, separator, token);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix, QueryParams queryParams) {
		return consulClient.getKVKeysOnly(keyPrefix, queryParams);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix, String separator, String token,
			QueryParams queryParams) {
		return consulClient.getKVKeysOnly(keyPrefix, separator, token, queryParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value) {
		return consulClient.setKVValue(key, value);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, PutParams putParams) {
		return consulClient.setKVValue(key, value, putParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, String token, PutParams putParams) {
		return consulClient.setKVValue(key, value, token, putParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, QueryParams queryParams) {
		return consulClient.setKVValue(key, value, queryParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, PutParams putParams, QueryParams queryParams) {
		return consulClient.setKVValue(key, value, putParams, queryParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, String token, PutParams putParams,
			QueryParams queryParams) {
		return consulClient.setKVValue(key, value, token, putParams, queryParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value) {
		return consulClient.setKVBinaryValue(key, value);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value, PutParams putParams) {
		return consulClient.setKVBinaryValue(key, value, putParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value, String token, PutParams putParams) {
		return consulClient.setKVBinaryValue(key, value, token, putParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value, QueryParams queryParams) {
		return consulClient.setKVBinaryValue(key, value, queryParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value, PutParams putParams, QueryParams queryParams) {
		return consulClient.setKVBinaryValue(key, value, putParams, queryParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value, String token, PutParams putParams,
			QueryParams queryParams) {
		return consulClient.setKVBinaryValue(key, value, token, putParams, queryParams);
	}

	@Override
	public Response<Void> deleteKVValue(String key) {
		return consulClient.deleteKVValue(key);
	}

	@Override
	public Response<Void> deleteKVValue(String key, String token) {
		return consulClient.deleteKVValue(key, token);
	}

	@Override
	public Response<Void> deleteKVValue(String key, QueryParams queryParams) {
		return consulClient.deleteKVValue(key, queryParams);
	}

	@Override
	public Response<Void> deleteKVValue(String key, String token, QueryParams queryParams) {
		return consulClient.deleteKVValue(key, token, queryParams);
	}

	@Override
	public Response<Void> deleteKVValues(String key) {
		return consulClient.deleteKVValues(key);
	}

	@Override
	public Response<Void> deleteKVValues(String key, String token) {
		return consulClient.deleteKVValues(key, token);
	}

	@Override
	public Response<Void> deleteKVValues(String key, QueryParams queryParams) {
		return consulClient.deleteKVValues(key, queryParams);
	}

	@Override
	public Response<Void> deleteKVValues(String key, String token, QueryParams queryParams) {
		return consulClient.deleteKVValues(key, token, queryParams);
	}

	@Override
	public Response<QueryExecution> executePreparedQuery(String uuid, QueryParams queryParams) {
		return consulClient.executePreparedQuery(uuid, queryParams);
	}

	@Override
	public Response<String> sessionCreate(NewSession newSession, QueryParams queryParams) {
		return consulClient.sessionCreate(newSession, queryParams);
	}

	@Override
	public Response<String> sessionCreate(NewSession newSession, QueryParams queryParams, String token) {
		return consulClient.sessionCreate(newSession, queryParams, token);
	}

	@Override
	public Response<Void> sessionDestroy(String session, QueryParams queryParams) {
		return consulClient.sessionDestroy(session, queryParams);
	}

	@Override
	public Response<Void> sessionDestroy(String session, QueryParams queryParams, String token) {
		return consulClient.sessionDestroy(session, queryParams, token);
	}

	@Override
	public Response<Session> getSessionInfo(String session, QueryParams queryParams) {
		return consulClient.getSessionInfo(session, queryParams);
	}

	@Override
	public Response<Session> getSessionInfo(String session, QueryParams queryParams, String token) {
		return consulClient.getSessionInfo(session, queryParams, token);
	}

	@Override
	public Response<List<Session>> getSessionNode(String node, QueryParams queryParams) {
		return consulClient.getSessionNode(node, queryParams);
	}

	@Override
	public Response<List<Session>> getSessionNode(String node, QueryParams queryParams, String token) {
		return consulClient.getSessionNode(node, queryParams, token);
	}

	@Override
	public Response<List<Session>> getSessionList(QueryParams queryParams) {
		return consulClient.getSessionList(queryParams);
	}

	@Override
	public Response<List<Session>> getSessionList(QueryParams queryParams, String token) {
		return consulClient.getSessionList(queryParams, token);
	}

	@Override
	public Response<Session> renewSession(String session, QueryParams queryParams) {
		return consulClient.renewSession(session, queryParams);
	}

	@Override
	public Response<Session> renewSession(String session, QueryParams queryParams, String token) {
		return consulClient.renewSession(session, queryParams, token);
	}

	@Override
	public Response<String> getStatusLeader() {
		return consulClient.getStatusLeader();
	}

	@Override
	public Response<List<String>> getStatusPeers() {
		return consulClient.getStatusPeers();
	}

}
