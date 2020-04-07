/*
 * Copyright 2010-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.lhfei.databox.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.lhfei.databox.config.HBaseConfig;
import cn.lhfei.databox.service.PaybillService;

/**
 * @version 0.1
 *
 * @author Hefei Li
 *
 * @created Mar 25, 2020
 */
@RestController
@RequestMapping("/paybill")
public class PaybillResources extends AbstractBasicResource {
	private static Logger LOG = LoggerFactory.getLogger(PaybillResources.class);

	@RequestMapping(value = "/_search", method = { RequestMethod.GET })
	public String BySample(@RequestParam(required = false) String account_detail_id, @RequestParam String member_id,
			@RequestParam(required = false) String start_create_date,
			@RequestParam(required = false) String end_create_date, @RequestParam(required = false) String balance_type,
			@RequestParam(required = false) Double detail_amount_gte,
			@RequestParam(required = false) Double detail_amount_lte,
			@RequestParam(required = false) String out_trade_no, @RequestParam(required = false) String trade_no,
			@RequestParam(required = false) String source_id, @RequestParam(required = false) String detail_desc,
			@RequestParam(required = false) String accountreqcode, @RequestParam(required = false) String trade_type,
			@RequestParam(required = false, defaultValue = "0") int page,
			@RequestParam(required = false, defaultValue = "500") int size) throws IOException {
		LOG.info("member_id: {}", member_id);
		
		return paybillService.findBySample(account_detail_id, member_id, start_create_date, end_create_date,
				balance_type, detail_amount_gte, detail_amount_lte, out_trade_no, trade_no, source_id, detail_desc,
				accountreqcode, trade_type, page, size);

	}
	
	@RequestMapping(value = "/_count", method = { RequestMethod.GET })
	public Map<String, Long> countBySample(@RequestParam(required = false) String account_detail_id, @RequestParam String member_id,
			@RequestParam(required = false) String start_create_date,
			@RequestParam(required = false) String end_create_date, @RequestParam(required = false) String balance_type,
			@RequestParam(required = false) Double detail_amount_gte,
			@RequestParam(required = false) Double detail_amount_lte,
			@RequestParam(required = false) String out_trade_no, @RequestParam(required = false) String trade_no,
			@RequestParam(required = false) String source_id, @RequestParam(required = false) String detail_desc,
			@RequestParam(required = false) String accountreqcode, @RequestParam(required = false) String trade_type,
			@RequestParam(required = false, defaultValue = "0") int page,
			@RequestParam(required = false, defaultValue = "500") int size) throws IOException {
		LOG.info("member_id: {}", member_id);
		
		Map<String, Long> map = new HashMap<String, Long>();
		long count = paybillService.countBySample(account_detail_id, member_id, start_create_date, end_create_date,
				balance_type, detail_amount_gte, detail_amount_lte, out_trade_no, trade_no, source_id, detail_desc,
				accountreqcode, trade_type);
		
		Map<String, String> hm = hConfig.getColumnsWithFamily();
		hm.keySet().forEach(key -> { // key as column-family, value as columns
			String[] columns = hm.get(key).split(",");
			Arrays.stream(columns).forEach(column -> {
				LOG.info("{}: {}",key, column);
			});
		});
		
		map.put("count", count);
		
		return map;
	}
	
	
	@RequestMapping(value = "/_bulk", method = { RequestMethod.GET })
	public List<Map<String, String>> batchLoad(@RequestParam(required = false) String account_detail_id, @RequestParam String[] rowkeys) throws IOException {
		List<Map<String, String>> list = new ArrayList<>();
		Table table = conn.getTable(TableName.valueOf(hConfig.getTableName()));
		List<Get> gets = new ArrayList<Get>();
		IntStream.range(0, rowkeys.length).forEach(idx -> {
			Get get = new Get(rowkeys[idx].getBytes());
			gets.add(get);
		});
		
		try {
			Result[] results = table.get(gets);
			
			IntStream.range(0, results.length).forEach(idx -> {
				Map<String, String> line = new HashMap<>();
				Result row = results[idx];
				LOG.info("Row: {}", Bytes.toString(row.getRow()));
				Map<String, String> map = hConfig.getColumnsWithFamily();
				map.keySet().forEach(key -> { // key as column-family, value as columns
					String[] columns = map.get(key).split(",");
					Arrays.stream(columns).forEach(column -> {
						line.put(column, Bytes.toString(row.getValue(Bytes.toBytes(key), Bytes.toBytes(column))));
					});
				});
				
				list.add(line);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;

	}
	
	@Autowired
	private PaybillService paybillService;
	
	@Autowired
	private HBaseConfig hConfig;
	
	@Autowired
	private Connection conn;
}
