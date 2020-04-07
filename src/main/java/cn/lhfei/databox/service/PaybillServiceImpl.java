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
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import cn.lhfei.databox.config.IndexConfig;
import cn.lhfei.databox.model.Paybill;

/**
 * @version 0.1
 *
 * @author Hefei Li
 *
 * @created Mar 25, 2020
 */
@Service
public class PaybillServiceImpl implements PaybillService {
	private static final Logger LOG = LoggerFactory.getLogger(PaybillServiceImpl.class);
	private Gson gson = new Gson();
	
	@Override
	public void bulkLoad(String jsonArray) throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		JsonArray paybills = gson.fromJson(jsonArray, JsonArray.class);

		paybills.forEach(paybill -> {
			LOG.info("{}", paybill);
			try {
				bulkRequest.add(
						new IndexRequest(indexConfig.getIndexName()).source(gson.toJson(paybill), XContentType.JSON));

			} catch (JsonSyntaxException e) {
				LOG.info("Message parse errored. {}", e.getMessage());

				new IOException("JSON message parsed exception", e);
			}
		});

		client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {

			@Override
			public void onResponse(BulkResponse bulkResponse) {
				LOG.info("Documents created in batches.");
			}

			@Override
			public void onFailure(Exception e) {
				LOG.info("Document failed. {}", e.getMessage(), e);
				// TODO persisting failed data
			}
		});
	}
	
	@Override
	public String findBySample(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type, int page, int size) throws IOException {
		List<Paybill> result = new ArrayList<>();
		SearchRequest searchRequest = new SearchRequest(indexConfig.getIndexName());
		SearchSourceBuilder searchSourceBuilder = this.wrapSourceBuilder(account_detail_id, member_id,
				start_create_date, end_create_date, balance_type, detail_amount_gte, detail_amount_lte, out_trade_no,
				trade_no, source_id, detail_desc, accountreqcode, trade_type);
		
		searchSourceBuilder.from(page);
		searchSourceBuilder.size(size);
		
		searchRequest.source(searchSourceBuilder);

		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
		LOG.info("Total: {}", response.getHits().getTotalHits());

		response.getHits().forEach(hit -> {
			Paybill paybill = gson.fromJson(hit.getSourceAsString(), Paybill.class);
			paybill.setId(hit.getId());
			result.add(paybill);
		});
		
		return gson.toJson(result);
	}

	@Override
	public Long countBySample(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type) throws IOException {
		CountRequest countRequest = new CountRequest(indexConfig.getIndexName());
		SearchSourceBuilder searchSourceBuilder = this.wrapSourceBuilder(account_detail_id, member_id,
				start_create_date, end_create_date, balance_type, detail_amount_gte, detail_amount_lte, out_trade_no,
				trade_no, source_id, detail_desc, accountreqcode, trade_type);

		countRequest.query(searchSourceBuilder.query());

		CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);

		return countResponse.getCount();
	}
	
	@Override
	public String agg(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type) throws IOException {
		SearchRequest searchRequest = new SearchRequest(indexConfig.getIndexName());
		SearchSourceBuilder searchSourceBuilder = this.wrapSourceBuilder(account_detail_id, member_id,
				start_create_date, end_create_date, balance_type, detail_amount_gte, detail_amount_lte, out_trade_no,
				trade_no, source_id, detail_desc, accountreqcode, trade_type);
		
		AggregationBuilder aggBuilder = AggregationBuilders.sum("ammount_count").field("detail_amount");
		
		searchSourceBuilder.size(0);
		searchSourceBuilder.aggregation(aggBuilder);
		searchRequest.source(searchSourceBuilder);

		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
		
		
		return gson.toJson(response.getAggregations().getAsMap());
	}
	
	@Override
	public String createDocument(Paybill paybill) {
		IndexRequest indexRequest = new IndexRequest(indexConfig.getHotName());
		indexRequest.id(paybill.getSource_id());
		final String[] id = new String[] { null };
		indexRequest.source(gson.toJson(paybill), XContentType.JSON);

		client.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {

			@Override
			public void onResponse(IndexResponse indexResponse) {
				id[0] = indexResponse.getId();
				if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
					LOG.info("Document created with id [{}]", paybill.getSource_id());
				} else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {

				}
				ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
				if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
					LOG.debug("Document created.");
				}
				if (shardInfo.getFailed() > 0) {
					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						String reason = failure.reason();
						LOG.info("Shard get failed. {}", reason);
					}
				}
			}

			@Override
			public void onFailure(Exception e) {
				LOG.info("Document failed. {}", e.getMessage(), e);

				// TODO persisting failed data
			}

		});

		return id[0];
	}
	
	private SearchSourceBuilder wrapSourceBuilder(String account_detail_id, String member_id, String start_create_date,
			String end_create_date, String balance_type, Double detail_amount_gte, Double detail_amount_lte,
			String out_trade_no, String trade_no, String source_id, String detail_desc, String accountreqcode,
			String trade_type) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// TODO check parameters

		// build query DSL
		if (!StringUtils.isEmpty(account_detail_id)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("account_detail_id", account_detail_id));
		}

		if (!StringUtils.isEmpty(member_id)) {
			boolQueryBuilder.must().add(QueryBuilders.termQuery("member_id", member_id));
		}
		
		if (!StringUtils.isEmpty(start_create_date)) {
			boolQueryBuilder.filter().add(QueryBuilders.rangeQuery("detail_create_date").gte(start_create_date));
		}
		if (!StringUtils.isEmpty(end_create_date)) {
			boolQueryBuilder.filter().add(QueryBuilders.rangeQuery("detail_create_date").lte(end_create_date));
		}

		if (!StringUtils.isEmpty(balance_type)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("balance_type", balance_type));
		}

		if (detail_amount_gte != null) {
			boolQueryBuilder.filter().add(QueryBuilders.rangeQuery("detail_amount").gte(detail_amount_gte));
		}
		if (detail_amount_lte != null) {
			boolQueryBuilder.filter().add(QueryBuilders.rangeQuery("detail_amount").lte(detail_amount_lte));
		}
		
		if (!StringUtils.isEmpty(out_trade_no)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("out_trade_no", out_trade_no));
		}
		
		if (!StringUtils.isEmpty(trade_no)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("trade_no", trade_no));
		}
		if (!StringUtils.isEmpty(source_id)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("source_id", source_id));
		}
		if (!StringUtils.isEmpty(detail_desc)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("detail_desc", detail_desc));
		}
		if (!StringUtils.isEmpty(accountreqcode)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("accountreqcode", accountreqcode));
		}
		if (!StringUtils.isEmpty(trade_type)) {
			boolQueryBuilder.filter().add(QueryBuilders.matchQuery("trade_type", trade_type));
		}

		searchSourceBuilder.query(boolQueryBuilder);

		return searchSourceBuilder;
	}
	
	
	@Autowired
	private RestHighLevelClient client;
	@Autowired
	private IndexConfig indexConfig;

	
}
