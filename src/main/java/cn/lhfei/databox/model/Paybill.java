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

package cn.lhfei.databox.model;

/**
 * @version 0.1
 *
 * @author Hefei Li
 *
 * @created Mar 25, 2020
 */

public class Paybill extends AbstractModel {
	private static final long serialVersionUID = -769936904996897743L;
	
	public String getAccount_detail_id() {
		return account_detail_id;
	}
	public void setAccount_detail_id(String account_detail_id) {
		this.account_detail_id = account_detail_id;
	}
	public String getMember_id() {
		return member_id;
	}
	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}
	public String getDetail_create_date() {
		return detail_create_date;
	}
	public void setDetail_create_date(String detail_create_date) {
		this.detail_create_date = detail_create_date;
	}
	public String getBalance_type() {
		return balance_type;
	}
	public void setBalance_type(String balance_type) {
		this.balance_type = balance_type;
	}
	public double getDetail_amount() {
		return detail_amount;
	}
	public void setDetail_amount(double detail_amount) {
		this.detail_amount = detail_amount;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getTrade_no() {
		return trade_no;
	}
	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}
	public String getSource_id() {
		return source_id;
	}
	public void setSource_id(String source_id) {
		this.source_id = source_id;
	}
	public String getDetail_desc() {
		return detail_desc;
	}
	public void setDetail_desc(String detail_desc) {
		this.detail_desc = detail_desc;
	}
	public String getAccountreqcode() {
		return accountreqcode;
	}
	public void setAccountreqcode(String accountreqcode) {
		this.accountreqcode = accountreqcode;
	}
	public String getTrade_type() {
		return trade_type;
	}
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	
	private String account_detail_id;
	private String member_id;
	private String detail_create_date;
	private String balance_type;
	private double detail_amount;
	private String out_trade_no;
	private String trade_no;
	private String source_id;
	private String detail_desc;
	private String accountreqcode;
	private String trade_type;
}
