package com.sobey.cmop.mvc.service.redmine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sobey.cmop.mvc.comm.BaseSevcie;
import com.sobey.cmop.mvc.constant.ApplyConstant;
import com.sobey.cmop.mvc.constant.FieldNameConstant;
import com.sobey.cmop.mvc.constant.IpPoolConstant;
import com.sobey.cmop.mvc.constant.RedmineConstant;
import com.sobey.cmop.mvc.constant.ResourcesConstant;
import com.sobey.cmop.mvc.entity.Apply;
import com.sobey.cmop.mvc.entity.Change;
import com.sobey.cmop.mvc.entity.ChangeItem;
import com.sobey.cmop.mvc.entity.ComputeItem;
import com.sobey.cmop.mvc.entity.CpItem;
import com.sobey.cmop.mvc.entity.Failure;
import com.sobey.cmop.mvc.entity.MdnItem;
import com.sobey.cmop.mvc.entity.MonitorCompute;
import com.sobey.cmop.mvc.entity.MonitorElb;
import com.sobey.cmop.mvc.entity.MonitorMail;
import com.sobey.cmop.mvc.entity.MonitorPhone;
import com.sobey.cmop.mvc.entity.NetworkDnsItem;
import com.sobey.cmop.mvc.entity.NetworkEipItem;
import com.sobey.cmop.mvc.entity.NetworkElbItem;
import com.sobey.cmop.mvc.entity.Resources;
import com.sobey.cmop.mvc.entity.ServiceTag;
import com.sobey.cmop.mvc.entity.StorageItem;
import com.sobey.cmop.mvc.entity.User;
import com.sobey.framework.utils.DateUtil;

/**
 * 生成满足 Redmine格式的文本(用于通过API插入redmine).
 * 
 * @author liukai
 * 
 */
@Service
@Transactional(readOnly = true)
public class RedmineUtilService extends BaseSevcie {

	private static Logger logger = LoggerFactory.getLogger(RedmineUtilService.class);

	/**
	 * 换行
	 */
	private static final String NEWLINE = "\r\n";

	/**
	 * 一个空格
	 */
	private static final String BLANK = " ";

	/**
	 * 箭头.用于资源变更时旧值和新值的比较
	 */
	private static final String RARR = BLANK + "→" + BLANK;

	/**
	 * 因为oneCMDB中的update方法,没有更改复杂对象的功能.
	 * 故只能通过redmine显示的方法通知管理人员手动修改变更对象在oneCMDB中的数据.
	 */
	private static final String CHANGE_ONECMDB_NOTIFICATION = "(提交后,请按工单手动修改oneCMDB中的数据)";

	/**
	 * 生成满足redmine显示的服务申请Apply文本.
	 */
	public String applyRedmineDesc(Apply apply) {
		try {

			StringBuilder content = new StringBuilder();

			content.append("*服务申请的详细信息*").append(NEWLINE + NEWLINE);
			content.append("# +*基本信息*+").append(NEWLINE);
			content.append("<pre>").append(NEWLINE);
			content.append("申请标题: ").append(apply.getTitle()).append(NEWLINE);
			content.append("服务标签: ").append(apply.getServiceTag()).append(NEWLINE);
			content.append("优先级: ").append(RedmineConstant.Priority.get(apply.getPriority())).append(NEWLINE);
			content.append("服务起止日期: ").append(apply.getServiceStart()).append(" 至 ").append(apply.getServiceEnd()).append(NEWLINE);
			content.append("用途描述: ").append(apply.getDescription()).append(NEWLINE);
			content.append("申请人: ").append(apply.getUser().getName()).append(NEWLINE);
			content.append("申请时间: ").append(DateUtil.formatDate(apply.getCreateTime())).append(NEWLINE);
			content.append("</pre>");
			content.append(NEWLINE);

			// 拼装计算资源Compute信息

			this.generateContentByLists(apply.getUser(), content, new ArrayList<ComputeItem>(apply.getComputeItems()), new ArrayList<StorageItem>(apply.getStorageItems()),
					new ArrayList<NetworkElbItem>(apply.getNetworkElbItems()), new ArrayList<NetworkEipItem>(apply.getNetworkEipItems()), new ArrayList<NetworkDnsItem>(apply.getNetworkDnsItems()),
					new ArrayList<MonitorMail>(apply.getMonitorMails()), new ArrayList<MonitorPhone>(apply.getMonitorPhones()), new ArrayList<MonitorCompute>(apply.getMonitorComputes()),
					new ArrayList<MonitorElb>(apply.getMonitorElbs()), new ArrayList<MdnItem>(apply.getMdnItems()), new ArrayList<CpItem>(apply.getCpItems()));

			return content.toString();

		} catch (Exception e) {

			logger.error("--->服务申请Apply拼装Redmine内容出错：" + e.getMessage());

			return null;

		}
	}

	/**
	 * 生成满足redmine显示的资源回收Resources文本.
	 */
	public String recycleResourcesRedmineDesc(User user, List<ComputeItem> computeItems, List<StorageItem> storageItems, List<NetworkElbItem> elbItems, List<NetworkEipItem> eipItems,
			List<NetworkDnsItem> dnsItems, List<MonitorMail> monitorMails, List<MonitorPhone> monitorPhones, List<MonitorCompute> monitorComputes, List<MonitorElb> monitorElbs,
			List<MdnItem> mdnItems, List<CpItem> cpItems) {

		try {

			StringBuilder content = new StringBuilder();

			// 拼装资源信息

			this.generateContentByLists(user, content, computeItems, storageItems, elbItems, eipItems, dnsItems, monitorMails, monitorPhones, monitorComputes, monitorElbs, mdnItems, cpItems);

			return content.toString();

		} catch (Exception e) {

			logger.error("--->资源变更Resources拼装Redmine内容出错：" + e.getMessage());

			return null;

		}
	}

	/**
	 * 生成满足redmine显示的故障申报Failure文本.
	 */
	public String failureResourcesRedmineDesc(Failure failure, List<ComputeItem> computeItems, List<StorageItem> storageItems, List<NetworkElbItem> elbItems, List<NetworkEipItem> eipItems,
			List<NetworkDnsItem> dnsItems, List<MonitorMail> monitorMails, List<MonitorPhone> monitorPhones, List<MonitorCompute> monitorComputes, List<MonitorElb> monitorElbs,
			List<MdnItem> mdnItems, List<CpItem> cpItems) {

		try {

			StringBuilder content = new StringBuilder();

			content.append("# +*故障申报信息*+").append(NEWLINE);
			content.append("<pre>").append(NEWLINE);
			content.append("申报人：").append(failure.getUser().getName()).append(NEWLINE);
			content.append("申报标题：").append(failure.getTitle()).append(NEWLINE);
			content.append("申报时间：").append(failure.getCreateTime()).append(NEWLINE);
			content.append("故障类型：").append(ApplyConstant.ServiceType.get(failure.getFaultType())).append(NEWLINE);
			content.append("优先级：").append(RedmineConstant.Priority.get(failure.getLevel())).append(NEWLINE);
			content.append("受理人：").append(RedmineConstant.Assignee.get(failure.getAssignee())).append(NEWLINE);
			content.append("故障现象及描述：").append(failure.getDescription()).append(NEWLINE);
			content.append("</pre>");

			this.generateContentByLists(failure.getUser(), content, computeItems, storageItems, elbItems, eipItems, dnsItems, monitorMails, monitorPhones, monitorComputes, monitorElbs, mdnItems,
					cpItems);

			return content.toString();

		} catch (Exception e) {

			logger.error("--->故障申报Failure拼装Redmine内容出错：" + e.getMessage());

			return null;

		}

	}

	/**
	 * 将各个资源相关的参数生成符合redmine格式的文本.
	 * 
	 * @param user
	 *            资源创建人
	 * @param content
	 * @param computeItems
	 * @param storageItems
	 * @param elbItems
	 * @param eipItems
	 * @param dnsItems
	 * @param monitorMails
	 * @param monitorPhones
	 * @param monitorComputes
	 * @param monitorElbs
	 * @param mdnItems
	 * @param cpItems
	 */
	private void generateContentByLists(User user, StringBuilder content, List<ComputeItem> computeItems, List<StorageItem> storageItems, List<NetworkElbItem> elbItems, List<NetworkEipItem> eipItems,
			List<NetworkDnsItem> dnsItems, List<MonitorMail> monitorMails, List<MonitorPhone> monitorPhones, List<MonitorCompute> monitorComputes, List<MonitorElb> monitorElbs,
			List<MdnItem> mdnItems, List<CpItem> cpItems) {

		RedmineTextUtil.generateCompute(content, computeItems);
		RedmineTextUtil.generateStorage(content, storageItems);
		RedmineTextUtil.generateElb(content, elbItems);
		RedmineTextUtil.generateEip(content, eipItems);
		RedmineTextUtil.generateDNS(content, dnsItems);
		RedmineTextUtil.generateMonitorMail(content, monitorMails);
		RedmineTextUtil.generateMonitorPhone(content, monitorPhones);
		RedmineTextUtil.generateMonitorCompute(content, monitorComputes);
		RedmineTextUtil.generateMonitorElb(content, monitorElbs);
		RedmineTextUtil.generateMdn(content, mdnItems);
		RedmineTextUtil.generateCP(content, cpItems);

	}

	/**
	 * 将变更前和变更后的值拼装成StringBuilder content
	 * 
	 * @param fieldName
	 *            变更项
	 * @param content
	 *            redmine 文本
	 * @param changeItem
	 *            changeItem对象
	 */
	private void saveChangeText(String fieldName, StringBuilder content, ChangeItem changeItem) {
		content.append(fieldName + ":" + BLANK).append(changeItem.getOldString()).append(RARR).append(changeItem.getNewString()).append(NEWLINE);
	}

	/**
	 * 将变更前和变更后的值拼装成StringBuilder content(包括换行)
	 * 
	 * @param fieldName
	 *            变更项
	 * @param content
	 *            redmine 文本
	 * @param changeItem
	 *            changeItem对象
	 */
	private void saveChangeTexts(String fieldName, StringBuilder content, ChangeItem changeItem) {
		content.append(fieldName + ":" + BLANK).append(NEWLINE).append(changeItem.getOldString()).append(RARR).append(NEWLINE).append(changeItem.getNewString()).append(NEWLINE);
	}

	/**
	 * 生成满足redmine显示的资源变更Resources文本.
	 */
	public String resourcesRedmineDesc(ServiceTag serviceTag) {
		try {

			StringBuilder content = new StringBuilder();

			content.append("*服务变更的详细信息*").append(NEWLINE + NEWLINE);
			content.append("* +*服务标签基本信息*+").append(NEWLINE);
			content.append("<pre>").append(NEWLINE);
			content.append("标签名: ").append(serviceTag.getName()).append(NEWLINE);
			content.append("优先级: ").append(RedmineConstant.Priority.get(serviceTag.getPriority())).append(NEWLINE);
			content.append("服务起止日期: ").append(serviceTag.getServiceStart()).append(" 至 ").append(serviceTag.getServiceEnd()).append(NEWLINE);
			content.append("用途描述: ").append(serviceTag.getDescription()).append(NEWLINE);
			content.append("申请人: ").append(serviceTag.getUser().getName()).append(NEWLINE);
			content.append("</pre>");
			content.append(NEWLINE + NEWLINE);
			content.append("* +*变更信息*+").append(NEWLINE);

			content.append("<pre>").append(NEWLINE);

			List<Resources> resourcesList = comm.resourcesService.getCommitedResourcesListByServiceTagId(serviceTag.getId());

			for (Resources resources : resourcesList) {

				Integer serviceType = resources.getServiceType();

				for (Change change : resources.getChanges()) {

					// 资源标识符(标识符) + 变更说明

					content.append("变更资源标识符:" + BLANK).append(resources.getServiceIdentifier());

					// 只有当ip不为0.0.0.0时才插入IP(即资源本身有IP时,像DNS,ES3这些没有IP的资源将不显示ip)
					if (!IpPoolConstant.DEFAULT_IPADDRESS.equals(resources.getIpAddress())) {
						content.append("(" + resources.getIpAddress() + ")");
					}

					content.append(BLANK + BLANK).append("变更描述:" + BLANK).append(change.getDescription()).append(NEWLINE);
					content.append(NEWLINE).append("变更项:");

					if (change.getSubResourcesId() != null) {
						content.append("(服务子项ID:" + change.getSubResourcesId() + ")");
					}

					content.append(BLANK + "旧值").append(RARR).append("新值").append(NEWLINE);

					for (ChangeItem changeItem : change.getChangeItems()) {

						String fieldName = changeItem.getFieldName();

						if (serviceType.equals(ResourcesConstant.ServiceType.PCS.toInteger()) || serviceType.equals(ResourcesConstant.ServiceType.ECS.toInteger())) {

							// 计算资源Compute信息

							if (FieldNameConstant.Compate.操作系统.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Compate.操作系统.toString(), content, changeItem);

							} else if (FieldNameConstant.Compate.操作位数.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Compate.操作位数.toString(), content, changeItem);

							} else if (FieldNameConstant.Compate.规格.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Compate.规格.toString(), content, changeItem);

							} else if (FieldNameConstant.Compate.用途信息.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Compate.用途信息.toString(), content, changeItem);

							} else if (FieldNameConstant.Compate.应用信息.toString().equals(fieldName)) {

								this.saveChangeTexts(FieldNameConstant.Compate.应用信息.toString(), content, changeItem);

							} else if (FieldNameConstant.Compate.ESG.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Compate.ESG.toString() + CHANGE_ONECMDB_NOTIFICATION, content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.ES3.toInteger())) {

							// 存储空间Storage信息

							if (FieldNameConstant.Storage.存储类型.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Storage.存储类型.toString(), content, changeItem);

							} else if (FieldNameConstant.Storage.容量空间.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Storage.容量空间.toString() + "(GB)", content, changeItem);

							} else if (FieldNameConstant.Storage.挂载实例.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Storage.挂载实例.toString(), content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.ELB.toInteger())) {

							// 变更负载均衡器ELB

							if (FieldNameConstant.Elb.是否保持会话.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Elb.是否保持会话.toString(), content, changeItem);

							} else if (FieldNameConstant.Elb.端口信息.toString().equals(fieldName)) {

								this.saveChangeTexts(FieldNameConstant.Elb.端口信息.toString(), content, changeItem);

							} else if (FieldNameConstant.Elb.关联实例.toString().equals(fieldName)) {

								this.saveChangeTexts(FieldNameConstant.Elb.关联实例.toString() + CHANGE_ONECMDB_NOTIFICATION, content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.EIP.toInteger())) {

							// EIP

							if (FieldNameConstant.Eip.关联实例.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Eip.关联实例.toString(), content, changeItem);

							} else if (FieldNameConstant.Eip.关联ELB.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Eip.关联ELB.toString(), content, changeItem);

							} else if (FieldNameConstant.Eip.端口信息.toString().equals(fieldName)) {

								this.saveChangeTexts(FieldNameConstant.Eip.端口信息.toString(), content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.DNS.toInteger())) {

							// 拼装DNS信息

							if (FieldNameConstant.Dns.域名类型.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Dns.域名类型.toString(), content, changeItem);

							} else if (FieldNameConstant.Dns.域名.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Dns.域名.toString(), content, changeItem);

							} else if (FieldNameConstant.Dns.CNAME域名.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Dns.CNAME域名.toString(), content, changeItem);

							} else if (FieldNameConstant.Dns.目标IP.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.Dns.目标IP.toString(), content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.MONITOR_COMPUTE.toInteger())) {

							// monitorCompute

							if (FieldNameConstant.monitorCompute.监控实例.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.监控实例.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.监控端口.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.监控端口.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.监控进程.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.监控进程.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.挂载路径.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.挂载路径.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.CPU占用率报警阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.CPU占用率报警阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.CPU占用率警告阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.CPU占用率警告阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.内存占用率报警阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.内存占用率报警阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.内存占用率警告阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.内存占用率警告阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.网络丢包率报警阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.网络丢包率报警阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.网络丢包率警告阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.网络丢包率警告阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.硬盘可用率报警阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.硬盘可用率报警阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.硬盘可用率警告阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.硬盘可用率警告阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.网络延时率报警阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.网络延时率报警阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.网络延时率警告阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.网络延时率警告阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.最大进程数报警阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.最大进程数报警阀值.toString(), content, changeItem);

							} else if (FieldNameConstant.monitorCompute.最大进程数警告阀值.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorCompute.最大进程数警告阀值.toString(), content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.MONITOR_ELB.toInteger())) {

							// monitorElb

							if (FieldNameConstant.monitorElb.监控ELB.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.monitorElb.监控ELB.toString(), content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.MDN.toInteger())) {

							// MDN
							if (FieldNameConstant.MdnItem.重点覆盖地域.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnItem.重点覆盖地域.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnItem.重点覆盖ISP.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnItem.重点覆盖ISP.toString(), content, changeItem);
							}

							// MDNVod
							if (FieldNameConstant.MdnVodItem.点播服务域名.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnVodItem.点播服务域名.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnVodItem.点播播放协议选择.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnVodItem.点播播放协议选择.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnVodItem.点播加速服务带宽.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnVodItem.点播加速服务带宽.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnVodItem.点播出口带宽.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnVodItem.点播出口带宽.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnVodItem.Streamer地址.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnVodItem.Streamer地址.toString(), content, changeItem);
							}

							// MDNLive
							if (FieldNameConstant.MdnLiveItem.直播服务域名.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.直播服务域名.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.直播播放协议选择.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.直播播放协议选择.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.直播加速服务带宽.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.直播加速服务带宽.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.直播出口带宽.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.直播出口带宽.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.频道名称.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.频道名称.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.频道GUID.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.频道GUID.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.直播流输出模式.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.直播流输出模式.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.编码器模式.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.编码器模式.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.拉流地址.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.拉流地址.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.拉流混合码率.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.拉流混合码率.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.推流地址.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.推流地址.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.推流混合码率.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.推流混合码率.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.HTTP流地址.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.HTTP流地址.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.HTTP流混合码率.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.HTTP流混合码率.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.HSL流地址.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.HSL流地址.toString(), content, changeItem);

							} else if (FieldNameConstant.MdnLiveItem.HSL流混合码率.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.MdnLiveItem.HSL流混合码率.toString(), content, changeItem);
							}

						} else if (serviceType.equals(ResourcesConstant.ServiceType.CP.toInteger())) {

							if (FieldNameConstant.CpItem.收录流URL.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.收录流URL.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.收录码率.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.收录码率.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.输出编码.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.输出编码.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.收录类型.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.收录类型.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.收录时段.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.收录时段.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.发布接口地址.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.发布接口地址.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.是否推送内容交易平台.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.是否推送内容交易平台.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频FTP上传IP.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频FTP上传IP.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频端口.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频端口.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频FTP用户名.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频FTP用户名.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频FTP密码.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频FTP密码.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频FTP根路径.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频FTP根路径.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频FTP上传路径.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频FTP上传路径.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.视频输出组类型.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.视频输出组类型.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.输出方式配置.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.输出方式配置.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片FTP上传IP.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片FTP上传IP.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片端口.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片端口.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片FTP用户名.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片FTP用户名.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片FTP密码.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片FTP密码.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片FTP根路径.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片FTP根路径.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片FTP上传路径.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片FTP上传路径.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.图片输出组类型.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.图片输出组类型.toString(), content, changeItem);

							} else if (FieldNameConstant.CpItem.输出媒体类型.toString().equals(fieldName)) {

								this.saveChangeText(FieldNameConstant.CpItem.输出媒体类型.toString(), content, changeItem);
							}

						}

					}

				}

				content.append(NEWLINE);
			}

			content.append("</pre>").append(NEWLINE);

			return content.toString();

		} catch (Exception e) {

			e.printStackTrace();

			logger.error("--->拼装Redmine内容出错：" + e.getMessage());

			return null;
		}
	}

}
