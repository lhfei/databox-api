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

package cn.lhfei.databox.config;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 0.1
 *
 * @author Hefei Li
 *
 * @created Mar 25, 2020
 */
@Configuration
@ConfigurationProperties(prefix = "hbase")
public class HBaseConfig {

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getZookeeperQuorum() {
		return zookeeperQuorum;
	}

	public void setZookeeperQuorum(String zookeeperQuorum) {
		this.zookeeperQuorum = zookeeperQuorum;
	}

	public String getZookeeperZnodeParent() {
		return zookeeperZnodeParent;
	}

	public void setZookeeperZnodeParent(String zookeeperZnodeParent) {
		this.zookeeperZnodeParent = zookeeperZnodeParent;
	}

	public Map<String, String> getColumnsWithFamily() {
		return columnsWithFamily;
	}

	public void setColumnsWithFamily(Map<String, String> columnsWithFamily) {
		this.columnsWithFamily = columnsWithFamily;
	}

	@Bean
	public Connection getConncection() throws IOException {
		org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum", zookeeperQuorum);
		config.set("zookeeper.znode.parent", zookeeperZnodeParent);

		Connection conn = ConnectionFactory.createConnection(config);

		return conn;
	}

	private String tableName;
	private String zookeeperQuorum;
	private String zookeeperZnodeParent;
	private Map<String, String> columnsWithFamily;
}
