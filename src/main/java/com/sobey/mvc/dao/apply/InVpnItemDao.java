package com.sobey.mvc.dao.apply;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.sobey.mvc.entity.InVpnItem;

public interface InVpnItemDao extends PagingAndSortingRepository<InVpnItem, Integer> {

	InVpnItem findByApply_Id(Integer applyId);

}
