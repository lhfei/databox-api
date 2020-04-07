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

package cn.lhfei.databox.service;

import java.io.IOException;

import cn.lhfei.databox.model.Paybill;

/**
 * @version 0.1
 *
 * @author Hefei Li
 *
 * @created Mar 25, 2020
 */

public interface PaybillService {

	String findBySample(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type, int page, int size) throws IOException;
	
	Long countBySample(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type) throws IOException;
	
	String agg(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type) throws IOException;
	
	String createDocument(Paybill paybill);
	
	void bulkLoad(String jsonArray) throws IOException;
}
