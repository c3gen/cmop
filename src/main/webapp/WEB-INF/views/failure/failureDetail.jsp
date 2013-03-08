<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/layouts/taglib.jsp"%>

<html>
<head>

	<title>故障申报</title>
	
	<script>
		$(document).ready(function() {
			
			$("ul#navbar li#failure").addClass("active");
			
		});
		
	</script>
	
</head>

<body>
	
	<style>body{background-color: #f5f5f5;}</style>

	<form id="inputForm" action="." method="post" class="form-horizontal input-form">
		
		<fieldset>
		
			<legend><small>故障申报</small></legend>
			
			<dl class="dl-horizontal">
			
				<dt>申报标题</dt>
				<dd>${failure.title}&nbsp;</dd>
				
				<dt>申报时间</dt>
				<dd><fmt:formatDate value="${failure.createTime}" pattern="yyyy年MM月dd日  HH时mm分ss秒" />&nbsp;</dd>
				
				
				<dt>故障类型</dt>
				<dd><c:forEach var="map" items="${applyServiceTypeMap }"><c:if test="${map.key == failure.faultType }">${map.value }</c:if></c:forEach>&nbsp;</dd>
				
				<dt>优先级</dt>
				<dd><c:forEach var="map" items="${priorityMap }"><c:if test="${map.key == failure.level }">${map.value }</c:if></c:forEach>&nbsp;</dd>
				
				<dt>受理人</dt>
				<dd><c:forEach var="map" items="${assigneeMap }"><c:if test="${map.key == failure.assignee }">${map.value }</c:if></c:forEach>&nbsp;</dd>
				
				<dt>故障现象及描述</dt>
				<dd>${failure.description}&nbsp;</dd>
				
				
				<div class="page-header"><em>处理反馈</em></div>
				
				<c:choose>
				
					<c:when test="${ empty issue.journals }"><p class="text-center">没有反馈结果.</p></c:when>
					
					<c:otherwise>
						<c:if test="${not empty issue.journals}">
							<div class="span8">
						    
					    		<c:forEach var="journal" items="${issue.journals}" varStatus="status">
					    			
					    			<input type="hidden" id="operator" name="operator" value="${journal.user.id}"/>
					    			
						    		<p class="help-inline plain-text span8">
						    			
										<strong>#${status.index+1} Updated by:</strong>&nbsp;${journal.user}&nbsp;&nbsp;<fmt:formatDate value="${journal.createdOn}" pattern="yyyy-MM-dd HH:mm:ss" />
						    			
						    			<ul>
											<c:forEach var="detail" items="${journal.details}">
				   								<li>
				   								
				   									${detail.name}:&nbsp;
				   									
					   								<c:if test="${detail.name=='status_id'}">
														<c:forEach var="map" items="${operateStatusMap}"><c:if test="${detail.oldValue==map.key}">${map.value}</c:if></c:forEach> &#8594; 
														<c:forEach var="map" items="${operateStatusMap}"><c:if test="${detail.newValue==map.key}">${map.value}</c:if></c:forEach>
													</c:if>
													
													<c:if test="${detail.name=='assigned_to_id'}">
														<c:forEach var="map" items="${assigneeMap}"><c:if test="${detail.oldValue==map.key}">${map.value}</c:if></c:forEach>&#8594;
														<c:forEach var="map" items="${assigneeMap}"><c:if test="${detail.newValue==map.key}">${map.value}</c:if></c:forEach>
													</c:if>
													
													<c:if test="${detail.name=='priority_id'}">
														<c:forEach var="map" items="${priorityMap}"><c:if test="${detail.oldValue==map.key}">${map.value}</c:if></c:forEach>&#8594;
														<c:forEach var="map" items="${priorityMap}"><c:if test="${detail.newValue==map.key}">${map.value}</c:if></c:forEach>
													</c:if>
													
													<c:if test="${detail.name=='project_id'}">
														<c:forEach var="map" items="${projectMap}"><c:if test="${detail.oldValue==map.key}">${map.value}</c:if></c:forEach>&#8594;
														<c:forEach var="map" items="${projectMap}"><c:if test="${detail.newValue==map.key}">${map.value}</c:if></c:forEach>
													</c:if>
													
													<c:if test="${detail.name=='estimated_hours'}">
														<c:choose><c:when test="${not empty detail.oldValue }">detail.oldValue</c:when><c:otherwise>0.00</c:otherwise></c:choose>
													</c:if>
													
													<c:if test="${detail.name!='priority_id' && detail.name!='assigned_to_id' && detail.name!='project_id' && detail.name!='status_id'}">
														${detail.oldValue}&#8594;${detail.newValue}
													</c:if>
												
												</li>
											</c:forEach>
										</ul>
							    	</p>
						    	</c:forEach><br>
						    </div>
						</c:if>

					</c:otherwise>
				</c:choose>
				
				<div class="page-header"><em>故障相关资源</em></div>
			
				<!-- 实例Compute -->
				<c:if test="${not empty computeItems}">
					
					<dt>PCS & ECS实例</dt>
					<c:forEach var="item" items="${computeItems}">
					
						<dd><em>标识符</em>&nbsp;&nbsp;${item.identifier}</dd>
						
						<dd><em>用途信息</em>&nbsp;&nbsp;${item.remark}</dd>
						
						<dd>
							<em>基本信息</em>
							&nbsp;&nbsp;<c:forEach var="map" items="${osTypeMap}"><c:if test="${item.osType == map.key}">${map.value}</c:if></c:forEach>
							&nbsp;&nbsp;<c:forEach var="map" items="${osBitMap}"><c:if test="${item.osBit == map.key}">${map.value}</c:if></c:forEach>
							&nbsp;&nbsp;
							<c:choose>
								<c:when test="${item.computeType == 1}"><c:forEach var="map" items="${pcsServerTypeMap}"><c:if test="${item.serverType == map.key}">${map.value}</c:if></c:forEach></c:when>
								<c:otherwise><c:forEach var="map" items="${ecsServerTypeMap}"><c:if test="${item.serverType == map.key}">${map.value}</c:if></c:forEach></c:otherwise>
							</c:choose>
						</dd>
						
						<dd><em>关联ESG</em>&nbsp;&nbsp;${item.networkEsgItem.identifier}(${item.networkEsgItem.description})</dd>
						
						<br>
						
					</c:forEach>
					
					<hr>
					
				</c:if>
				
				<!-- 存储空间ES3 -->
				<c:if test="${not empty storageItems}">
					
					<dt>ES3存储空间</dt>
					<c:forEach var="item" items="${apply.storageItems}">
					
						<dd><em>标识符</em>&nbsp;&nbsp;${item.identifier}</dd>
						
						<dd><em>存储类型</em>&nbsp;&nbsp;<c:forEach var="map" items="${storageTypeMap}"><c:if test="${item.storageType == map.key}">${map.value}</c:if></c:forEach></dd>
						
						<dd><em>容量空间</em>&nbsp;&nbsp;${item.space}&nbsp;GB</dd>
						
						<dd><em>挂载实例</em>&nbsp;&nbsp;${item.mountComputes}</dd>
						
						<br>
						
					</c:forEach>
					
					<hr>
					
				</c:if>
				
				<!-- 负载均衡器ELB -->
				<c:if test="${not empty elbItems}">
					
					<dt>负载均衡器ELB</dt>
					<c:forEach var="item" items="${apply.networkElbItems}">
					
						<dd><em>标识符</em>&nbsp;&nbsp;${item.identifier}</dd>
						
						<dd><em>是否保持会话</em>&nbsp;<c:forEach var="map" items="${keepSessionMap}"><c:if test="${item.keepSession == map.key }">${map.value}</c:if></c:forEach></dd>
						
						<dd><em>关联实例</em>&nbsp; 
							<c:forEach var="compute" items="${allComputes}">
								<c:if test="${compute.networkElbItem.id == item.id }">${compute.identifier}
									<c:if test="${not empty compute.innerIp }">(${compute.innerIp})</c:if>&nbsp;&nbsp;
								</c:if>
							</c:forEach>
						</dd>
						
						<dd><em>端口映射（协议、源端口、目标端口）</em></dd>
						
						<c:forEach var="port" items="${item.elbPortItems }">
							<dd>&nbsp;&nbsp;${port.protocol}&nbsp;,&nbsp;${port.sourcePort}&nbsp;,&nbsp;${port.targetPort}</dd>
						</c:forEach>
							
						<br>
						
					</c:forEach>
					
					<hr>
					
				</c:if>
				
				<!-- IP地址EIP -->
				<c:if test="${not empty eipItems}">
				
					<hr>
					<dt>EIP</dt>
					<c:forEach var="item" items="${eipItems}">
					
						<dd><em>标识符</em>&nbsp;&nbsp;${item.identifier}</dd>
						
						<dd><em>ISP运营商</em>&nbsp;&nbsp;<c:forEach var="map" items="${ispTypeMap}"><c:if test="${item.ispType == map.key }">${map.value}</c:if></c:forEach></dd>
						
						<dd>
							<c:choose>
								<c:when test="${not empty item.computeItem }"><em>关联实例</em>&nbsp;&nbsp;${item.computeItem.identifier }(${item.computeItem.innerIp })</c:when>
								<c:otherwise><em>关联ELB</em>&nbsp;&nbsp;${item.networkElbItem.identifier }(${item.networkElbItem.virtualIp })</c:otherwise>
							</c:choose>
						</dd>
						
						<dd><em>端口映射（协议、源端口、目标端口）</em></dd>
						
						<c:forEach var="port" items="${item.eipPortItems }">
							<dd>&nbsp;&nbsp;${port.protocol}&nbsp;,&nbsp;${port.sourcePort}&nbsp;,&nbsp;${port.targetPort}</dd>
						</c:forEach>
							
						<br>
						
					</c:forEach>
				
				</c:if>
				
				
				<!-- DNS -->
				
				<!-- DNS -->
				<c:if test="${not empty dnsItems}">
				
					<hr>
					<dt>DNS域名映射</dt>
					<c:forEach var="item" items="${dnsItems}">
					
						<dd><em>标识符</em>&nbsp;&nbsp;${item.identifier}</dd>
						
						<dd><em>域名</em>&nbsp;&nbsp;${item.domainName }</dd>
						
						<dd><em>域名类型</em>&nbsp;&nbsp;<c:forEach var="map" items="${domainTypeMap}"><c:if test="${item.domainType == map.key }">${map.value}</c:if></c:forEach></dd>
						
						<dd>
							<c:choose>
								<c:when test="${item.domainType != 3 }"><em>目标IP</em>&nbsp;&nbsp;${item.mountElbs }</c:when>
								<c:otherwise><em>CNAME域名</em>&nbsp;&nbsp;${item.cnameDomain }</c:otherwise>
							</c:choose>
						</dd>
						
						<br>
						
					</c:forEach>
					
				</c:if>
				
				<!-- 服务器监控monitorCompute -->
				
					<!-- ELB监控monitorElb -->
				
			</dl>
			
			<div class="form-actions">
				<input class="btn" type="button" value="返回" onclick="history.back()">
			</div>
			
		</fieldset>
		
	</form>
	
</body>
</html>
