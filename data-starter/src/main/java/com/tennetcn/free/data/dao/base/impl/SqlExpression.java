package com.tennetcn.free.data.dao.base.impl;

import com.tennetcn.free.core.util.StringHelper;
import com.tennetcn.free.data.dao.base.ISqlExpression;
import com.tennetcn.free.core.enums.OrderEnum;
import com.tennetcn.free.core.message.data.OrderByEnum;
import com.tennetcn.free.data.message.OrderInfo;
import com.tennetcn.free.data.message.SqlOperateMode;
import com.tennetcn.free.data.utils.ClassAnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author  chenghuan-home
 * @email   79763939@qq.com
 * @comment 
 */

@Component
public class SqlExpression implements ISqlExpression {

	private String sqlOperateMode = SqlOperateMode.select;

	private Map<String, Object> params = null;

	private String mainTableAlias = "";

	private String fromMainTableAlias = "";

	private StringBuffer wheres = new StringBuffer();

	private StringBuffer bodyBuffer = new StringBuffer();

	private StringBuffer orderBuffer = new StringBuffer();

	private StringBuffer groupBuffer = new StringBuffer();

	private StringBuffer setBuffer = new StringBuffer();

	private StringBuffer fromBuffer = new StringBuffer();

	public SqlExpression() {
		if (params == null) {
			params = new HashMap<String, Object>();
		}
	}


	/**
	 * 设置参数。
	 *
	 * @param param
	 * @param value
	 * @return
	 */
	@Override
	public SqlExpression setParam(String param, Object value) {
		params.put(param, value);
		return this;
	}


	public SqlExpression getSqlExpression() {
		return this;
	}

	@Override
	public ISqlExpression andWhere(String value) {
		wheres.append(" and (" + value + ") ");
		return this;
	}

	@Override
	public ISqlExpression andEq(String column, String value) {
		column = resolveColumnMainTable(column);
		String paramName = resolveColumn(column);

		this.andWhere(column + "=#{" + paramName + "}")
				.setParam(paramName, value);
		return this;
	}

	@Override
	public ISqlExpression andLike(String column, String value) {
		column = resolveColumnMainTable(column);
		String paramName = resolveColumn(column);

		this.andWhere(column + " like #{" + paramName + "}")
				.setParam(paramName, "%" + value + "%");
		return this;
	}

	@Override
	public ISqlExpression andRightLike(String column, String value) {
		column = resolveColumnMainTable(column);
		String paramName = resolveColumn(column);

		this.andWhere(column + " like #{" + paramName + "}")
				.setParam(paramName, value + "%");
		return this;
	}

	@Override
	public ISqlExpression andLikeNoEmpty(String column, String value) {
		if (!StringUtils.isEmpty(value)) {
			return andLike(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andRightLikeNoEmpty(String column, String value) {
		if (!StringUtils.isEmpty(value)) {
			return andRightLike(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andEqNoEmpty(String column, String value) {
		if (!StringUtils.isEmpty(value)) {
			return andEq(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andNotEq(String column, String value) {
		column = resolveColumnMainTable(column);
		String paramName = resolveColumn(column);

		this.andWhere(column + "!=#{" + paramName + "}")
				.setParam(paramName, value);
		return this;
	}

	@Override
	public ISqlExpression andNotLike(String column, String value) {
		column = resolveColumnMainTable(column);
		String paramName = resolveColumn(column);

		this.andWhere(column + " not like #{" + paramName + "}")
				.setParam(paramName, "%" + value + "%");
		return this;
	}

	@Override
	public ISqlExpression andNotLikeNoEmpty(String column, String value) {
		if (!StringUtils.isEmpty(value)) {
			return andNotLike(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andNotEqNoEmpty(String column, String value) {
		if (!StringUtils.isEmpty(value)) {
			return andNotEq(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andEq(String column, int value) {
		return andEq(column, String.valueOf(value));
	}

	@Override
	public ISqlExpression andEqNoEmpty(String column, int value) {
		if (value > -1) {
			return andEq(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andNotEq(String column, int value) {
		return andNotEq(column, String.valueOf(value));
	}

	@Override
	public ISqlExpression andNotEqNoEmpty(String column, int value) {
		if (value > -1) {
			return andNotEq(column, value);
		}
		return this;
	}

	@Override
	public ISqlExpression andMainTableWhere(String value) {
		wheres.append(" and (" + this.getMainTableAlias() + value + ") ");
		return this;
	}

	@Override
	public ISqlExpression orWhere(String value) {
		wheres.append(" or (" + value + ") ");
		return this;
	}

	@Override
	public ISqlExpression addOrders(OrderEnum order, String... columns) {
		for (String column : columns) {
			if (orderBuffer.length() == 0) {
				orderBuffer.append(" order by ");
				orderBuffer.append(" " + column + " ");
			} else {
				orderBuffer.append(" ," + column + " ");
			}
			orderBuffer.append(" " + order.name() + " ");
		}
		return this;
	}

	@Override
	public ISqlExpression addOrder(String column, OrderEnum order) {
		if (orderBuffer.length() == 0) {
			orderBuffer.append(" order by ");
			orderBuffer.append(" " + column + " ");
		} else {
			orderBuffer.append(" ," + column + " ");
		}
		orderBuffer.append(" " + order.name() + " ");

		return this;
	}

	@Override
	public ISqlExpression addBody(String body) {
		bodyBuffer.append(" " + body + " ");
		return this;
	}

	@Override
	public ISqlExpression addBody(String body, Class<?> tClass) {
		bodyBuffer.append(" " + body + " " + ClassAnnotationUtils.getTableName(tClass));
		return this;
	}

	public Map<String, Object> getParams() {
		return params;
	}


	@Override
	public String toSql() {
		String result = bodyBuffer.toString();
		if (sqlOperateMode == SqlOperateMode.select) {
			result += fromBuffer.toString();
			result += getWhereString();
			result += groupBuffer.toString();
			result += orderBuffer.toString();
		} else if (sqlOperateMode == SqlOperateMode.update) {
			result += fromBuffer.toString();
			result += setBuffer.toString();
			result += getWhereString();
		} else if (sqlOperateMode == SqlOperateMode.delete) {
			result += fromBuffer.toString();
			result += getWhereString();
		}

		return result;
	}

	private String getWhereString() {
		if (wheres.length() == 0) {
			return " ";
		}
		String defaultWhere = " where 1=1 ";
		//如果在第一个有了defaultWhere就不在添加了
		if (wheres.indexOf(defaultWhere) != 0) {
			wheres.insert(0, defaultWhere);
		}
		return wheres.toString();
	}


	@Override
	public ISqlExpression setParamAll(Map<String, Object> maps) {
		if (maps != null && maps.size() > 0) {
			params.putAll(maps);
		}
		return this;
	}


	@Override
	public ISqlExpression addGroup(String group) {
		if (groupBuffer.length() == 0) {
			groupBuffer.append(" group by ");
			groupBuffer.append(" " + group + " ");
		} else {
			groupBuffer.append(" ," + group + " ");
		}
		return this;
	}

	@Override
	public ISqlExpression addGroups(String... groups) {
		for (String group : groups) {
			if (groupBuffer.length() == 0) {
				groupBuffer.append(" group by ");
				groupBuffer.append(" " + group + " ");
			} else {
				groupBuffer.append(" ," + group + " ");
			}
		}
		return this;
	}

	@Override
	public ISqlExpression leftJoin(String body) {
		fromBuffer.append(" left join " + body + " ");
		return this;
	}

	@Override
	public ISqlExpression leftJoin(Class<?> tClass, String alias) {
		fromBuffer.append(" left join " + ClassAnnotationUtils.getTableName(tClass) + " " + alias + " ");
		return this;
	}

	@Override
	public ISqlExpression innerJoin(String body) {
		fromBuffer.append(" inner join " + body + " ");
		return this;
	}

	@Override
	public ISqlExpression innerJoin(Class<?> tClass, String alias) {
		fromBuffer.append(" inner join " + ClassAnnotationUtils.getTableName(tClass) + " " + alias + " ");
		return this;
	}

	@Override
	public ISqlExpression rightJoin(String body) {
		fromBuffer.append(" right join " + body + " ");
		return this;
	}

	@Override
	public ISqlExpression rightJoin(Class<?> tClass, String alias) {
		fromBuffer.append(" right join " + ClassAnnotationUtils.getTableName(tClass) + " " + alias + " ");
		return this;
	}

	@Override
	public ISqlExpression on(String body) {
		fromBuffer.append(" on (" + body + ") ");
		return this;
	}

	@Override
	public ISqlExpression on(String left, String right) {
		fromBuffer.append(" on (" + left + "=" + right + ") ");
		return this;
	}

	@Override
	public ISqlExpression selectAllFrom(Class<?> tClass) {
		return select(SqlHelper.getAllColumns(tClass)).from(tClass);

	}

	@Override
	public ISqlExpression selectAllFrom(Class<?> tClass, String alias) {
		// "aa,bb,cc"  replace ,->,xx.  ="aa,xx.bb,xx.cc"
		// and add start xx.
		String allColumns = alias+"."+SqlHelper.getAllColumns(tClass).replace(",",","+alias+".");

		return select(allColumns).from(tClass, alias);
	}

	@Override
	public ISqlExpression select(String body) {
		addBody("select " + body);
		sqlOperateMode = SqlOperateMode.select;
		return this;
	}

	@Override
	public ISqlExpression appendSelect(String body) {
		return addBody(","+body);
	}

	@Override
	public ISqlExpression select(String... bodys) {
		if (bodys != null && bodys.length > 0) {
			String body = StringHelper.join(bodys, ",");
			addBody("select " + body);
		}
		sqlOperateMode = SqlOperateMode.select;
		return this;
	}

	@Override
	public ISqlExpression appendSelect(String... bodys){
		if (bodys != null && bodys.length > 0) {
			String body = StringHelper.join(bodys, ",");
			addBody(","+body);
		}
		return this;
	}
	
	@Override
	public ISqlExpression selectCount(){
		select("count(1) as c ");
		return this;
	}
	
	@Override
	public ISqlExpression selectCount(String column){
		select("count("+column+") as c ");
		return this;
	}
	
	@Override
	public ISqlExpression update(String body){
		addBody("update "+body);
		sqlOperateMode=SqlOperateMode.update;
		return this;
	}
	
	@Override
	public ISqlExpression update(Class<?> tClass){
		addBody("update "+ClassAnnotationUtils.getTableName(tClass));
		sqlOperateMode=SqlOperateMode.update;
		return this;
	}
	
	@Override
	public ISqlExpression update(Class<?> tClass,String alias){
		addBody("update "+ClassAnnotationUtils.getTableName(tClass)+" "+alias);
		sqlOperateMode=SqlOperateMode.update;
		return this;
	}
	
	@Override
	public ISqlExpression set(String column,String columnKey){
		if(setBuffer.length()==0){
			setBuffer.append(" set ");
			setBuffer.append(column+"=#{"+columnKey+"}");
		}else{
			setBuffer.append(","+column+"=#{"+columnKey+"}");
		}
		return this;
	}
	
	@Override
	public ISqlExpression setColumn(String column,String value){
		this.set(column, column)
			.setParam(column, value);
		return this;
	}
	
	public ISqlExpression set(String... columnKeys){
		if(columnKeys!=null&&columnKeys.length>0){
			String sets=StringHelper.join(columnKeys,",");
			if(setBuffer.length()==0){
				setBuffer.append(" set ");
				setBuffer.append(sets);
			}else{
				setBuffer.append(","+sets);
			}
		}
		return this;
	}
	
	@Override
	public ISqlExpression delete(){
		sqlOperateMode=SqlOperateMode.delete;
		addBody("delete");
		return this;
	}
	
	@Override
	public ISqlExpression from(String body){
		fromBuffer.append("from "+body);
		return this;
	}

	@Override
	public ISqlExpression from(Class<?> tClass){
		fromBuffer.append("from "+ClassAnnotationUtils.getTableName(tClass));
		return this;
	}
	
	@Override
	public ISqlExpression from(Class<?> tClass,String alias){
		this.fromMainTableAlias = alias;
		fromBuffer.append("from "+ClassAnnotationUtils.getTableName(tClass)+" "+alias);
		return this;
	}
	

	public ISqlExpression setMainTableAlias(String mainTableAlias){
		this.mainTableAlias=mainTableAlias;
		return this;
	}
	
	public String getMainTableAlias(){
		// 如果设置的mainTableAlias为空，则取一次from的时候设置的mainTableAlias
		if(StringUtils.isEmpty(mainTableAlias)){
			mainTableAlias = this.fromMainTableAlias;
		}
		if(!StringUtils.isEmpty(mainTableAlias)){
			return mainTableAlias+".";
		}
		return mainTableAlias;
	}
	
	@Override
	public ISqlExpression addOrderInfoList(List<OrderInfo> orderInfos) {
		if(orderInfos!=null){
			for (OrderInfo orderInfo : orderInfos) {
				OrderEnum or=OrderEnum.asc;
				if(OrderByEnum.DESC.equals(orderInfo.getOrderBy().toUpperCase())){
					or=OrderEnum.desc;
				}
				this.addOrder(orderInfo.getProperty(),or);
			}
		}
		return this;
	}

	@Override
	public ISqlExpression andWhereIn(String column,ISqlExpression sqlExpression){
		StringBuilder builder=new StringBuilder();
		builder.append(column+" in (");
		builder.append(sqlExpression.toSql());
		builder.append(")");

		this.setParamAll(sqlExpression.getParams());

		andWhere(builder.toString());
		return this;
	}

	@Override
	public ISqlExpression andWhereIn(String column, List<Object> values) {
		if(values==null||values.size()<=0){
			return this;
		}
		column = resolveColumnMainTable(column);

		StringBuffer builder=new StringBuffer();
		builder.append(column+" in (");
		String columnName = "";
		for (int i = 0; i < values.size(); i++) {
			columnName = resolveColumn(column)+i;
			if(i==values.size()-1){
				builder.append("#{"+columnName+"}");
			}else{
				builder.append("#{"+columnName+"},");
			}
			this.setParam(columnName, values.get(i));
		}
		builder.append(")");
		
		andWhere(builder.toString());
		
		return this;
	}


	@Override
	public ISqlExpression andWhereInString(String column, List<String> values) {
		if(values==null||values.size()<=0){
			return this;
		}
		column = resolveColumnMainTable(column);
		StringBuffer builder=new StringBuffer();
		builder.append(column+" in (");
		String columnName = "";
		for (int i = 0; i < values.size(); i++) {

			columnName = resolveColumn(column)+i;
			if(i==values.size()-1){
				builder.append("#{"+columnName+"}");
			}else{
				builder.append("#{"+columnName+"},");
			}
			this.setParam(columnName, values.get(i));
		}
		builder.append(")");
		
		andWhere(builder.toString());
		
		return this;
	}

	@Override
	public ISqlExpression andWhereInString(String column, String ...values) {
		this.andWhereInString(column, Arrays.asList(values));
		return this;
	}

	@Override
	public ISqlExpression andWhereInString(List<String> values, String join,String... columns) {
		if(values==null||values.size()<=0){
			return this;
		}
		if(columns==null||columns.length<=0){
			return this;
		}
		StringBuffer sbWheres=new StringBuffer();
		String inWhereId="";
		Map<String,Object> inWhereMap=new HashMap<String, Object>();
		for (String column : columns) {
			column = resolveColumnMainTable(column);
			if(sbWheres.length()>0){
				sbWheres.append(" "+join+" ");
			}
			if(StringUtils.isEmpty(inWhereId)){

				inWhereId=resolveColumn(column);
			}
			sbWheres.append(column+" in (");
			for (int i = 0; i < values.size(); i++) {
				if(i==values.size()-1){
					sbWheres.append("#{"+inWhereId+i+"}");
				}else{
					sbWheres.append("#{"+inWhereId+i+"},");
				}
				
				if(inWhereMap.isEmpty()){
					this.setParam(resolveColumn(column)+i, values.get(i));
				}
			}
			
			sbWheres.append(")");
		}
		this.setParamAll(inWhereMap);
		
		andWhere(sbWheres.toString());
		
		return this;
	}
	
	private String resolveColumn(String column){

		return column.replaceAll("\\.", "_");
	}

	// 如果有maintable的别名设置，并且列没有指定表别名，则默认为主表别名
	private String resolveColumnMainTable(String column){
		if(!StringUtils.isEmpty(mainTableAlias)&&column.indexOf(".")<0){
			column = mainTableAlias+"."+column;
		}
		return column;
	}
}
