package com.nklcbdty.batch.nklcbdty.batch.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobMstRepository;

@Service
public class BatchService {

	@Autowired
	private JobMstRepository batchRepository;
	
	public void insert() {
		
	}
}
