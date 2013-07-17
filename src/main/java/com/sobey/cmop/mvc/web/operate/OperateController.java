package com.sobey.cmop.mvc.web.operate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sobey.cmop.mvc.comm.BaseController;
import com.sobey.cmop.mvc.constant.IpPoolConstant;
import com.sobey.cmop.mvc.constant.RedmineConstant;
import com.sobey.cmop.mvc.entity.ComputeItem;
import com.sobey.cmop.mvc.entity.IpPool;
import com.sobey.cmop.mvc.entity.NetworkEipItem;
import com.sobey.cmop.mvc.entity.NetworkElbItem;
import com.sobey.cmop.mvc.entity.RedmineIssue;
import com.sobey.cmop.mvc.entity.StorageItem;
import com.sobey.cmop.mvc.service.redmine.RedmineService;
import com.sobey.framework.utils.Servlets;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

/**
 * OperateController负责工单的管理
 * 
 * @author liukai
 * 
 */
@Controller
@RequestMapping(value = "/operate")
public class OperateController extends BaseController {
	private static Logger logger = LoggerFactory.getLogger(OperateController.class);
	private static final String REDIRECT_SUCCESS_URL = "redirect:/operate/";

	/**
	 * 显示指派给自己的工单operate list
	 */
	@RequestMapping(value = { "list", "" })
	public String assigned(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
			@RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize, Model model,
			ServletRequest request) {
		Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, REQUEST_PREFIX);
		model.addAttribute("page", comm.operateService.getAssignedIssuePageable(searchParams, pageNumber, pageSize));
		// 将搜索条件编码成字符串,分页的URL
		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, REQUEST_PREFIX));
		// 跳转到reported
		model.addAttribute("toReported", "toReported");

		return "operate/operateList";
	}

	/**
	 * 显示所有的工单operate list
	 */
	@RequestMapping(value = "reported")
	public String reported(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
			@RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize, Model model,
			ServletRequest request) {
		Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, REQUEST_PREFIX);
		model.addAttribute("page", comm.operateService.getReportedIssuePageable(searchParams, pageNumber, pageSize));
		// 将搜索条件编码成字符串,分页的URL
		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, REQUEST_PREFIX));

		return "operate/operateList";
	}

	/**
	 * 跳转到更新页面,并替换掉redmine文本中的ip.（100%流程后无法替换.）
	 * 
	 * @param id
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "update/{id}")
	public String update(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		logger.info("--->工单处理...issueId=" + id);
		Issue issue = RedmineService.getIssue(id);
		model.addAttribute("issue", issue);
		model.addAttribute("user", comm.accountService.getCurrentUser());
		RedmineIssue redmineIssue = comm.operateService.findByIssueId(id);
		model.addAttribute("redmineIssue", redmineIssue);
		if (issue != null) {

			String desc = issue.getDescription();

			desc = desc.replaceAll("\\*服务申请的详细信息\\*", "");
			desc = desc.replaceAll("\\*服务变更的详细信息\\*", "");

			if (desc.indexOf("# 基本信息") >= 0) {
				desc = desc.replaceAll("# 基本信息", "<br># 基本信息");
			} else {
				desc = "<br>" + desc;
			}

			// 更新写入Redmine的IP
			List list = comm.operateService.getComputeStorageElbEip(redmineIssue);
			List<ComputeItem> computeList = (List) list.get(0);
			List<StorageItem> storageList = (List) list.get(1);
			List<NetworkElbItem> networkElbList = (List) list.get(2);
			List<NetworkEipItem> networkEipList = (List) list.get(3);

			// oldIP替换分配的IP.

			logger.info("--->更新写入Redmine的IP（计算资源）..." + computeList.size());
			desc = this.replaceComputeTextFromOldIpToInnerIp(desc, computeList);

			logger.info("--->更新写入Redmine的IP（EIP）..." + networkEipList.size());
			desc = this.replaceEIPTextFromOldIpToInnerIp(desc, networkEipList);

			logger.info("--->更新写入Redmine的IP（ELB）..." + networkElbList.size());
			desc = this.replaceELBTextFromOldIpToInnerIp(desc, networkElbList);

			model.addAttribute("description", desc);

			if (!computeList.isEmpty()) {
				model.addAttribute("computeList", computeList);
				int poolType = 0;
				logger.info("--->has compute: " + computeList.size() + ",poolType=" + poolType);
				model.addAttribute("server", comm.operateService.findHostMapByServerType(2)); // 物理机
				model.addAttribute("hostServer", comm.operateService.findHostMapByServerType(1));
				model.addAttribute("osStorage", comm.oneCmdbUtilService.getOsStorageFromOnecmdb());
			}

			if (!storageList.isEmpty()) {
				model.addAttribute("storageList", storageList);
				model.addAttribute("fimasController", comm.oneCmdbUtilService.getFimasHardWareFromOnecmdb());
				model.addAttribute("netappController", comm.oneCmdbUtilService.getNfsHardWareFromOnecmdb());
			}

			if (!networkEipList.isEmpty()) {
				model.addAttribute("eipList", networkEipList);
				logger.info("--->has eip: " + networkEipList.size());

				List<IpPool> ipPools = comm.ipPoolService.getIpPoolByPoolTypeAndStatus(
						IpPoolConstant.PoolType.互联网IP池.toInteger(), IpPoolConstant.IpStatus.未使用.toInteger());

				for (NetworkEipItem networkEipItem : networkEipList) {
					IpPool ipPool = comm.ipPoolService.findIpPoolByIpAddress(networkEipItem.getIpAddress());
					ipPools.add(ipPool);
				}

				model.addAttribute("internetIpPool", ipPools);

			}

			if (!networkElbList.isEmpty()) {
				model.addAttribute("elbList", networkElbList);
				// 暂不处理ELB的虚拟IP池
			}

			model.addAttribute("location", comm.operateService.getLocationFromOnecmdb());

			model.addAttribute("vlan", comm.operateService.getVlanFromOnecmdb());

			return "operate/operateForm";

		} else {

			// 查询Redmine中的Issue信息失败
			redirectAttributes.addFlashAttribute("message", "查询工单信息失败，请稍后重试！");
			return "redirect:/operate";
		}
	}

	/**
	 * 将redmine文本中Compute的oldIP替换成已经分配的IP.
	 * 
	 * @param desc
	 * @param computeList
	 * @return
	 */
	private String replaceComputeTextFromOldIpToInnerIp(String desc, List<ComputeItem> computeList) {

		int size = computeList.size();

		String[] searchList = new String[size];
		String[] replacementList = new String[size];

		for (int i = 0; i < computeList.size(); i++) {

			searchList[i] = computeList.get(i).getIdentifier() + "(" + computeList.get(i).getRemark() + " - "
					+ computeList.get(i).getOldIp() + ")";

			replacementList[i] = computeList.get(i).getIdentifier() + "(" + computeList.get(i).getRemark() + " - "
					+ computeList.get(i).getInnerIp() + ")";
		}

		return StringUtils.replaceEach(desc, searchList, replacementList);
	}

	/**
	 * 将redmine文本中EIP的oldIP替换成已经分配的IP.
	 * 
	 * @param desc
	 * @param networkEipList
	 * @return
	 */
	private String replaceEIPTextFromOldIpToInnerIp(String desc, List<NetworkEipItem> networkEipList) {

		int size = networkEipList.size();

		String[] searchList = new String[size];
		String[] replacementList = new String[size];

		for (int i = 0; i < networkEipList.size(); i++) {

			searchList[i] = networkEipList.get(i).getIdentifier() + "(" + networkEipList.get(i).getOldIp() + ")";

			replacementList[i] = networkEipList.get(i).getIdentifier() + "(" + networkEipList.get(i).getIpAddress()
					+ ")";
		}

		return StringUtils.replaceEach(desc, searchList, replacementList);
	}

	/**
	 * 将redmine文本中ELB的oldIP替换成已经分配的IP.
	 * 
	 * @param desc
	 * @param networkElbList
	 * @return
	 */
	private String replaceELBTextFromOldIpToInnerIp(String desc, List<NetworkElbItem> networkElbList) {
		int size = networkElbList.size();

		String[] searchList = new String[size];
		String[] replacementList = new String[size];

		for (int i = 0; i < networkElbList.size(); i++) {

			searchList[i] = networkElbList.get(i).getIdentifier() + "(" + networkElbList.get(i).getOldIp() + ")";

			replacementList[i] = networkElbList.get(i).getIdentifier() + "(" + networkElbList.get(i).getVirtualIp()
					+ ")";
		}

		return StringUtils.replaceEach(desc, searchList, replacementList);
	}

	/**
	 * 更新工单
	 * 
	 * @param id
	 * @param authorId
	 * @param priority
	 * @param assignTo
	 * @param projectId
	 * @param doneRatio
	 * @param estimatedHours
	 * @param dueDate
	 * @param note
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "update")
	public String updateForm(@RequestParam(value = "issueId") int issueId, @RequestParam("projectId") int projectId,
			@RequestParam("note") String note, @RequestParam("priority") int priority,
			@RequestParam("authorId") int authorId, @RequestParam("assignTo") int assignTo,
			@RequestParam("dueDate") String dueDate, @RequestParam("estimatedHours") float estimatedHours,
			@RequestParam("doneRatio") int doneRatio, @RequestParam("computes") String computes,
			@RequestParam("storages") String storages, @RequestParam("hostNames") String hostNames,
			@RequestParam("serverAlias") String serverAlias, @RequestParam("osStorageAlias") String osStorageAlias,
			@RequestParam("controllerAlias") String controllerAlias, @RequestParam("volumes") String volumes,
			@RequestParam("innerIps") String innerIps,
			@RequestParam(value = "eipIds", required = false) String[] eipIds,
			@RequestParam(value = "eipAddress", required = false) String[] eipAddress,
			@RequestParam("locationAlias") String locationAlias, @RequestParam("elbIds") String elbIds,
			@RequestParam("virtualIps") String virtualIps, RedirectAttributes redirectAttributes) {
		logger.info("[issueId,projectId,priority,assignTo,dueDate,estimatedHours,doneRatio,note]：" + issueId + ","
				+ projectId + "," + priority + "," + assignTo + "," + dueDate + "," + estimatedHours + "," + doneRatio
				+ "," + note);
		logger.info("[computes,storages,hostNames,serverAlias,osStorageAlias,controllerAlias, volumes]：" + computes
				+ "|" + storages + "|" + hostNames + "|" + serverAlias + "|" + osStorageAlias + "|" + controllerAlias
				+ "|" + volumes);
		logger.info("[innerIps,eipIds,eipAddresss,locationAlias,elbIds,virtualIps]：" + innerIps + "|" + eipIds + "|"
				+ eipAddress + "|" + locationAlias + "|" + elbIds + "|" + virtualIps);

		Issue issue = RedmineService.getIssue(issueId);
		// 此处的User是redmine中的User对象.
		User assignee = new User();
		assignee.setId(assignTo);
		User author = new User();
		author.setId(authorId);
		Project project = new Project();
		project.setId(projectId);

		// 当完成度为100时,设置状态 statusId 为 5.关闭; 其它完成度则为 2.处理中.
		Integer statusId = RedmineConstant.MAX_DONERATIO.equals(doneRatio) ? RedmineConstant.Status.关闭.toInteger()
				: RedmineConstant.Status.处理中.toInteger();
		issue.setAssignee(assignee);// 指派给
		issue.setDoneRatio(doneRatio);// 完成率
		issue.setStatusId(statusId); // 设置状态
		issue.setStatusName(RedmineConstant.Status.get(statusId)); // 设置状态名称
		issue.setDueDate(new Date()); // 完成期限
		issue.setEstimatedHours(new Float(estimatedHours)); // 耗费时间
		issue.setNotes(note); // 描述
		issue.setPriorityId(priority); // 优先级
		issue.setProject(project); // 所属项目
		issue.setAuthor(author); // issue作者
		// TODO 还有IP分配等功能,待以后完成.
		boolean result = comm.operateService.updateOperate(issue, computes, storages, hostNames, serverAlias,
				osStorageAlias, controllerAlias, volumes, innerIps, eipIds, eipAddress, locationAlias, elbIds,
				virtualIps);
		String message = result ? "工单更新成功！" : "工单更新失败，请稍后重试或联系管理员！";
		redirectAttributes.addFlashAttribute("message", message);

		return REDIRECT_SUCCESS_URL;

	}
}
