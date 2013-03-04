package com.sobey.cmop.mvc.web.basicdata;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sobey.cmop.mvc.comm.BaseController;
import com.sobey.cmop.mvc.constant.HostServerConstant;
import com.sobey.cmop.mvc.constant.IpPoolConstant;
import com.sobey.cmop.mvc.entity.HostServer;
import com.sobey.cmop.mvc.entity.Location;
import com.sobey.framework.utils.Servlets;

@Controller
@RequestMapping(value = "/basicdata/host")
public class HostServerController extends BaseController {

	private static final String REDIRECT_SUCCESS_URL = "redirect:/basicdata/host/";

	@RequestMapping(value = { "list", "" })
	public String list(@RequestParam(value = "page", defaultValue = "1") int pageNumber, @RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize,
			@RequestParam(value = "serverType", required = false, defaultValue = "") String serverType, Model model, ServletRequest request) {
		Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, REQUEST_PREFIX);
		// 将搜索条件编码成字符串,分页的URL
		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, REQUEST_PREFIX));
		model.addAttribute("page", comm.hostServerService.getHostServerPageable(searchParams, pageNumber, pageSize));
		return "basicdata/host/hostList";
	}

	/**
	 * 跳转到新增页面
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.GET)
	public String createForm(Model model) {
		return "basicdata/host/hostForm";
	}

	/**
	 * 新增
	 * 
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(HostServer hostServer, RedirectAttributes redirectAttributes) {
		boolean flag = comm.hostServerService.saveHostServer(hostServer);
		// 更改IP状态为 已使用
		comm.ipPoolService.updateIpPoolByHostServer(hostServer, IpPoolConstant.IP_STATUS_2);

		if (flag) {
			redirectAttributes.addFlashAttribute("message", "创建服务器成功！");
			return REDIRECT_SUCCESS_URL;
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "创建服务器失败！");
			return "redirect:/basicdata/host/save/";
		}
	}

	@RequestMapping(value = "delete/{id}")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		// HostServer hostServer = comm.hostServerService.findById(id);
		// String ipPoolName =
		// ipPoolManager.getOneCMDBPoolName(hostServer.getPoolType());
		// // 删除oneCMDB下的ip
		// List<CiBean> ciBeanList = new ArrayList<CiBean>();
		// CiBean serverRouter = new CiBean("Server", "Server" +
		// hostServer.getAlias(), false); // Server
		// ciBeanList.add(serverRouter);
		// CiBean serverPortRouter = new CiBean("ServerPort", "ServerPort" +
		// hostServer.getIpAddress(), false); // ServerPort
		// CiBean ipPoolRouter = new CiBean(ipPoolName, ipPoolName + "-" +
		// hostServer.getIpAddress(), false); // IpPool
		// ciBeanList.add(serverPortRouter);
		// ciBeanList.add(ipPoolRouter);
		// OneCmdbUitl.delete(ciBeanList);
		//
		// ipPoolManager.initIpPoolStatus(hostServer.getIpAddress());

		boolean flag = comm.hostServerService.delete(id);
		if (flag) {
			redirectAttributes.addFlashAttribute("message", "删除服务器成功！");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "删除服务器失败！");
		}

		return REDIRECT_SUCCESS_URL;
	}

	@RequestMapping(value = { "ecs/{id}" })
	public String ecs(@PathVariable("id") Integer id, Model model) {
		model.addAttribute("hostServer", comm.hostServerService.findById(id));
		model.addAttribute("ecsList", comm.hostServerService.getEcsByHost(id));
		return "basicdata/host/ecsList";
	}

	@RequestMapping(value = { "syn" })
	public String syn(Model model, ServletRequest request) {
		boolean flag = comm.hostServerService.syn();
		return list(1, Integer.parseInt(PAGE_SIZE), "", model, request);
	}

	// ============== 返回页面的参数 =============

	/**
	 * Ip类型
	 * 
	 * @return
	 */
	@ModelAttribute("poolTypeMap")
	public Map<Integer, String> poolTypeMap() {
		return IpPoolConstant.PoolType.map;
	}

	/**
	 * 服务器类型
	 * 
	 * @return
	 */
	@ModelAttribute("hostServerTypeMap")
	public Map<Integer, String> hostServerTypeMap() {
		return HostServerConstant.HostServerType.map;
	}

	/**
	 * LOCATION
	 * 
	 * @return
	 */
	@ModelAttribute("locationList")
	public List<Location> getLocationList() {
		return comm.locationService.getLocationList();
	}

}