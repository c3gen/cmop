package com.sobey.mvc.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.collect.Lists;
import com.sobey.framework.utils.Collections3;

/**
 * User entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "user", catalog = "cmop")
public class User implements java.io.Serializable {

	// Fields

	private Integer id;
	private String name;
	private String password;
	private String email;
	private String phonenum;
	private Integer department;
	private Integer leaderId;
	private String type;
	private Date createTime;
	private Date loginTime;
	private Integer status;
	private Set<AuditFlow> auditFlows = new HashSet<AuditFlow>(0);
	private Set<Fault> faults = new HashSet<Fault>(0);
	private Set<Apply> applies = new HashSet<Apply>(0);
	private List<Group> groupList = Lists.newArrayList();// 有序的关联对象集合

	// Constructors

	/** default constructor */
	public User() {
	}

	/** minimal constructor */
	public User(String name, String password, String email, String phonenum, Integer department, String type, Date createTime, Integer status) {
		this.name = name;
		this.password = password;
		this.email = email;
		this.phonenum = phonenum;
		this.department = department;
		this.type = type;
		this.createTime = createTime;
		this.status = status;
	}

	/** full constructor */
	public User(String name, String password, String email, String phonenum, Integer department, Integer leaderId, String type, Date createTime,
			Date loginTime, Integer status, Set<AuditFlow> auditFlows, Set<Fault> faults, Set<Apply> applies) {
		this.name = name;
		this.password = password;
		this.email = email;
		this.phonenum = phonenum;
		this.department = department;
		this.leaderId = leaderId;
		this.type = type;
		this.createTime = createTime;
		this.loginTime = loginTime;
		this.status = status;
		this.auditFlows = auditFlows;
		this.faults = faults;
		this.applies = applies;
	}

	// Property accessors
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "name", nullable = false, length = 10)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "password", nullable = false, length = 20)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "email", nullable = false, length = 45)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "phonenum", nullable = false, length = 45)
	public String getPhonenum() {
		return this.phonenum;
	}

	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}

	@Column(name = "department", nullable = false)
	public Integer getDepartment() {
		return this.department;
	}

	public void setDepartment(Integer department) {
		this.department = department;
	}

	@Column(name = "leader_id")
	public Integer getLeaderId() {
		return this.leaderId;
	}

	public void setLeaderId(Integer leaderId) {
		this.leaderId = leaderId;
	}

	@Column(name = "type", nullable = false, length = 1)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "create_time", nullable = false, length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "login_time", length = 19)
	public Date getLoginTime() {
		return this.loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	@Column(name = "status", nullable = false)
	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
	public Set<AuditFlow> getAuditFlows() {
		return this.auditFlows;
	}

	public void setAuditFlows(Set<AuditFlow> auditFlows) {
		this.auditFlows = auditFlows;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Fault> getFaults() {
		return this.faults;
	}

	public void setFaults(Set<Fault> faults) {
		this.faults = faults;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Apply> getApplies() {
		return this.applies;
	}

	public void setApplies(Set<Apply> applies) {
		this.applies = applies;
	}
	// 多对多定义
		@ManyToMany
		@JoinTable(name = "user_group", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = { @JoinColumn(name = "group_id") })
		// Fecth策略定义
		@Fetch(FetchMode.SUBSELECT)
		// 集合按id排序.
		@OrderBy("id")
		// 集合中对象id的缓存.
		@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
		public List<Group> getGroupList() {
			return groupList;
		}

		public void setGroupList(List<Group> groupList) {
			this.groupList = groupList;
		}

		/**
		 * 用户拥有的权限组名称字符串, 多个权限组名称用','分隔.
		 */
		// 非持久化属性.
		@Transient
		public String getGroupNames() {
			return Collections3.extractToString(groupList, "name", ", ");
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
}