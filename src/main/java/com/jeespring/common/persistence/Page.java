/**
 * Copyright &copy; 2012-2020 <a href="https://github.com/gaowenhui/RocketSpringCloud">jeespring</a> All rights reserved.
 */
package com.jeespring.common.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeespring.common.config.Global;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeespring.common.utils.CookieUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 分页类
 * @author gaowh
 * @version 2013-7-2
 * @param <T>
 */
public class Page<T> implements Serializable {
	
	protected int pageNo = 1; // 当前页码
	protected int pageSize = Integer.valueOf(Global.getConfig("page.pageSize")); // 页面大小，设置为“-1”表示不进行分页（分页无效）
	
	protected long count;// 总记录数，设置为“-1”表示不查询总数
	
	protected int first;// 首页索引
	protected int last;// 尾页索引
	protected int prev;// 上一页索引
	protected int next;// 下一页索引
	
	private boolean firstPage;//是否是第一页
	private boolean lastPage;//是否是最后一页

	protected int length = 8;// 显示页面长度
	protected int slider = 1;// 前后显示页面长度
	
	private List<T> list = new ArrayList<T>();
	
	private String orderBy = ""; // 标准查询有效， 实例： updatedate desc, name asc

	protected String funcName = "page"; // 设置点击页码调用的js函数名称，默认为page，在一页有多个分页对象时使用。
	
	protected String funcParam = ""; // 函数的附加参数，第三个参数值。
	
	private String message = ""; // 设置提示消息，显示在“共n条”之后

	public Page() {
		this.pageSize = -1;
	}
	
	/**
	 * 构造方法
	 * @param request 传递 repage 参数，来记住页码
	 * @param response 用于设置 Cookie，记住页码
	 */
	public Page(HttpServletRequest request, HttpServletResponse response){
		this(request, response, -2);
	}

	public Page(int pageNo, int pageSize,String orderBy){
		this(pageNo, pageSize,0);
		this.orderBy=orderBy;
	}


	/**
	 * 构造方法
	 * @param request 传递 repage 参数，来记住页码
	 * @param response 用于设置 Cookie，记住页码
	 * @param defaultPageSize 默认分页大小，如果传递 -1 则为不分页，返回所有数据
	 */
	public Page(HttpServletRequest request, HttpServletResponse response, int defaultPageSize){
		// 设置页码参数（传递repage参数，来记住页码）
		String no = request.getParameter("pageNo");
		if (StringUtils.isNumeric(no)){
			CookieUtils.setCookie(response, "pageNo", no);
			this.setPageNo(Integer.parseInt(no));
		}else if (request.getParameter("repage")!=null){
			no = CookieUtils.getCookie(request, "pageNo");
			if (StringUtils.isNumeric(no)){
				this.setPageNo(Integer.parseInt(no));
			}
		}
		// 设置页面大小参数（传递repage参数，来记住页码大小）
		String size = request.getParameter("pageSize");
		if (StringUtils.isNumeric(size)){
			CookieUtils.setCookie(response, "pageSize", size);
			this.setPageSize(Integer.parseInt(size));
		}else if (request.getParameter("repage")!=null){
			no = CookieUtils.getCookie(request, "pageSize");
			if (StringUtils.isNumeric(size)){
				this.setPageSize(Integer.parseInt(size));
			}
		}else if (defaultPageSize != -2){
			this.pageSize = defaultPageSize;
		}
		// 设置排序参数
		String orderBy = request.getParameter("orderBy");
		if (StringUtils.isNotBlank(orderBy)){
			this.setOrderBy(orderBy);
		}
	}
	
	/**
	 * 构造方法
	 * @param pageNo 当前页码
	 * @param pageSize 分页大小
	 */
	public Page(int pageNo, int pageSize) {
		this(pageNo, pageSize, 0);
	}
	
	/**
	 * 构造方法
	 * @param pageNo 当前页码
	 * @param pageSize 分页大小
	 * @param count 数据条数
	 */
	public Page(int pageNo, int pageSize, long count) {
		this(pageNo, pageSize, count, new ArrayList<T>());
	}
	
	/**
	 * 构造方法
	 * @param pageNo 当前页码
	 * @param pageSize 分页大小
	 * @param count 数据条数
	 * @param list 本页数据对象列表
	 */
	public Page(int pageNo, int pageSize, long count, List<T> list) {
		this.setCount(count);
		this.setPageNo(pageNo);
		this.pageSize = pageSize;
		this.list = list;
	}
	
	/**
	 * 初始化参数
	 */
	public void initialize(){
				
		//1
		this.first = 1;
		
		this.last = (int)(count / (this.pageSize < 1 ? 20 : this.pageSize) + first - 1);
		
		if (this.count % this.pageSize != 0 || this.last == 0) {
			this.last++;
		}

		if (this.last < this.first) {
			this.last = this.first;
		}
		
		if (this.pageNo <= 1) {
			this.pageNo = this.first;
			this.firstPage=true;
		}

		if (this.pageNo >= this.last) {
			this.pageNo = this.last;
			this.lastPage=true;
		}

		if (this.pageNo < this.last - 1) {
			this.next = this.pageNo + 1;
		} else {
			this.next = this.last;
		}

		if (this.pageNo > 1) {
			this.prev = this.pageNo - 1;
		} else {
			this.prev = this.first;
		}
		
		//2
		if (this.pageNo < this.first) {// 如果当前页小于首页
			this.pageNo = this.first;
		}

		if (this.pageNo > this.last) {// 如果当前页大于尾页
			this.pageNo = this.last;
		}
		
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	/**
	 * 默认输出当前分页标签 
	 * <div class="page">${page}</div>
	 */
	public String toStringPage() {

		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"fixed-table-pagination\" style=\"display: block;\">");
//		sb.append("<div class=\"dataTables_info\">");
//		sb.append("<li class=\"disabled controls\"><a href=\"javascript:\">当前 ");
//		sb.append("<input type=\"text\" value=\""+pageNo+"\" onkeypress=\"var e=window.event||this;var c=e.keyCode||e.which;if(c==13)");
//		sb.append(funcName+"(this.value,"+pageSize+",'"+funcParam+"');\" onclick=\"this.select();\"/> / ");
//		sb.append("<input type=\"text\" value=\""+pageSize+"\" onkeypress=\"var e=window.event||this;var c=e.keyCode||e.which;if(c==13)");
//		sb.append(funcName+"("+pageNo+",this.value,'"+funcParam+"');\" onclick=\"this.select();\"/> 条，");
//		sb.append("共 " + count + " 条"+(message!=null?message:"")+"</a></li>\n");
//		sb.append("</div>");
		long startIndex = (pageNo-1)*pageSize + 1;
		long endIndex = pageNo*pageSize <=count? pageNo*pageSize:count;
		
		sb.append("<div class=\"pull-left pagination-detail\">");
		sb.append("<span class=\"pagination-info\">显示第 "+startIndex+" 到第 "+ endIndex +" 条记录，总共 "+count+" 条记录</span>");
		sb.append("<span class=\"page-list\">每页显示 <span class=\"btn-group dropup\">");
		sb.append("<button type=\"button\" class=\"btn btn-default  btn-outline dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">");
		sb.append("<span class=\"page-size\">"+pageSize+"</span> <span class=\"caret\"></span>");
		sb.append("</button>");
		sb.append("<ul class=\"dropdown-menu\" role=\"menu\">");
		sb.append("<li class=\""+getSelected(pageSize,10)+ "\"><a href=\"javascript:"+funcName+"("+pageNo+",10,'"+funcParam+"');\">10</a></li>");
		sb.append("<li class=\""+getSelected(pageSize,25)+ "\"><a href=\"javascript:"+funcName+"("+pageNo+",25,'"+funcParam+"');\">25</a></li>");
		sb.append("<li class=\""+getSelected(pageSize,50)+ "\"><a href=\"javascript:"+funcName+"("+pageNo+",50,'"+funcParam+"');\">50</a></li>");
		sb.append("<li class=\""+getSelected(pageSize,100)+ "\"><a href=\"javascript:"+funcName+"("+pageNo+",100,'"+funcParam+"');\">100</a></li>");
		sb.append("<li class=\""+getSelected(pageSize,1000)+ "\"><a href=\"javascript:"+funcName+"("+pageNo+",1000,'"+funcParam+"');\">1000</a></li>");
		sb.append("<li class=\""+getSelected(pageSize,100000000)+ "\"><a href=\"javascript:"+funcName+"("+pageNo+",100000000,'"+funcParam+"');\">全部</a></li>");
		sb.append("</ul>");
		sb.append("</span> 条记录</span>");
		sb.append("</div>");
//		sb.append("<p>每页 <select onChange=\""+funcName+"("+pageNo+",this.value,'"+funcParam+"');\"" +"style=\"display:display  !important;\" class=\"form-control m-b input-sm\">" +
//		        "<option value=\"10\" "+getSelected(pageSize,10)+ ">10</option>" +
//				"<option value=\"25\" "+getSelected(pageSize,25)+ ">25</option>" +
//				"<option value=\"50\" "+getSelected(pageSize,50)+ ">50</option>" +
//				"<option value=\"100\" "+getSelected(pageSize,100)+ ">100</option>" +
//				"</select> 条记录，显示 " +startIndex+ " 到 "+ endIndex +" 条，共 "+count+" 条</p>");
//		sb.append("</div>");
//		sb.append("</div>");
		
		
		
		
		sb.append("<div class=\"pull-right pagination-roll\">");
		sb.append("<ul class=\"pagination pagination-outline\">");
		if (pageNo == first) {// 如果是首页
			sb.append("<li class=\"paginate_button previous disabled\"><a href=\"javascript:\"><i class=\"fa fa-angle-double-left\"></i></a></li>\n");
			sb.append("<li class=\"paginate_button previous disabled\"><a href=\"javascript:\"><i class=\"fa fa-angle-left\"></i></a></li>\n");
		} else {
			sb.append("<li class=\"paginate_button previous\"><a href=\"javascript:\" onclick=\""+funcName+"("+first+","+pageSize+",'"+funcParam+"');\"><i class=\"fa fa-angle-double-left\"></i></a></li>\n");
			sb.append("<li class=\"paginate_button previous\"><a href=\"javascript:\" onclick=\""+funcName+"("+prev+","+pageSize+",'"+funcParam+"');\"><i class=\"fa fa-angle-left\"></i></a></li>\n");
		}

		int begin = pageNo - (length / 2);

		if (begin < first) {
			begin = first;
		}

		int end = begin + length - 1;

		if (end >= last) {
			end = last;
			begin = end - length + 1;
			if (begin < first) {
				begin = first;
			}
		}

		if (begin > first) {
			int i = 0;
			for (i = first; i < first + slider && i < begin; i++) {
				sb.append("<li class=\"paginate_button \"><a href=\"javascript:\" onclick=\""+funcName+"("+i+","+pageSize+",'"+funcParam+"');\">"
						+ (i + 1 - first) + "</a></li>\n");
			}
			if (i < begin) {
				sb.append("<li class=\"paginate_button disabled\"><a href=\"javascript:\">...</a></li>\n");
			}
		}

		for (int i = begin; i <= end; i++) {
			if (i == pageNo) {
				sb.append("<li class=\"paginate_button active\"><a href=\"javascript:\">" + (i + 1 - first)
						+ "</a></li>\n");
			} else {
				sb.append("<li class=\"paginate_button \"><a href=\"javascript:\" onclick=\""+funcName+"("+i+","+pageSize+",'"+funcParam+"');\">"
						+ (i + 1 - first) + "</a></li>\n");
			}
		}

		if (last - end > slider) {
			sb.append("<li class=\"paginate_button disabled\"><a href=\"javascript:\">...</a></li>\n");
			end = last - slider;
		}

		for (int i = end + 1; i <= last; i++) {
			sb.append("<li class=\"paginate_button \"><a href=\"javascript:\" onclick=\""+funcName+"("+i+","+pageSize+",'"+funcParam+"');\">"
					+ (i + 1 - first) + "</a></li>\n");
		}

		if (pageNo == last) {
			sb.append("<li class=\"paginate_button next disabled\"><a href=\"javascript:\"><i class=\"fa fa-angle-right\"></i></a></li>\n");
			sb.append("<li class=\"paginate_button next disabled\"><a href=\"javascript:\"><i class=\"fa fa-angle-double-right\"></i></a></li>\n");
		} else {
			sb.append("<li class=\"paginate_button next\"><a href=\"javascript:\" onclick=\""+funcName+"("+next+","+pageSize+",'"+funcParam+"');\">"
					+ "<i class=\"fa fa-angle-right\"></i></a></li>\n");
			sb.append("<li class=\"paginate_button next\"><a href=\"javascript:\" onclick=\""+funcName+"("+last+","+pageSize+",'"+funcParam+"');\">"
					+ "<i class=\"fa fa-angle-double-right\"></i></a></li>\n");
		}

		
        sb.append("</ul>");
        sb.append("</div>");
        sb.append("</div>");
//		sb.insert(0,"<ul>\n").append("</ul>\n");
		
//		sb.append("<div style=\"clear:both;\"></div>");

//		sb.insert(0,"<div class=\"page\">\n").append("</div>\n");
		
		return sb.toString();
	}
	
	protected String getSelected(int pageNo, int selectedPageNo){
		if(pageNo == selectedPageNo){
			//return "selected";
			return "active";
		}else{
			return "";
		}
		
	}
	/**
	 * 获取分页HTML代码
	 * @return
	 */
	//@JsonIgnore
	public String getHtml(){
		return toStringPage();
	}
	
//	public static void main(String[] args) {
//		Page<String> p = new Page<String>(3, 3);
//		System.out.println(p);
//		System.out.println("首页："+p.getFirst());
//		System.out.println("尾页："+p.getLast());
//		System.out.println("上页："+p.getPrev());
//		System.out.println("下页："+p.getNext());
//	}

	/**
	 * 获取设置总数
	 * @return
	 */
	public long getCount() {
		return count;
	}

	/**
	 * 设置数据总数
	 * @param count
	 */
	public void setCount(long count) {
		this.count = count;
		if (pageSize >= count){
			pageNo = 1;
		}
	}
	
	/**
	 * 获取当前页码
	 * @return
	 */
	public int getPageNo() {
		return pageNo;
	}
	
	/**
	 * 设置当前页码
	 * @param pageNo
	 */
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
	/**
	 * 获取页面大小
	 * @return
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 设置页面大小（最大500）
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize <= 0 ? 10 : pageSize;// > 500 ? 500 : pageSize;
	}

	/**
	 * 首页索引
	 * @return
	 */
	@JsonIgnore
	public int getFirst() {
		return first;
	}

	/**
	 * 尾页索引
	 * @return
	 */
	@JsonIgnore
	public int getLast() {
		return last;
	}
	
	/**
	 * 获取页面总数
	 * @return getLast();
	 */
	@JsonIgnore
	public int getTotalPage() {
		return getLast();
	}

	/**
	 * 是否为第一页
	 * @return
	 */
	@JsonIgnore
	public boolean isFirstPage() {
		return firstPage;
	}

	/**
	 * 是否为最后一页
	 * @return
	 */
	@JsonIgnore
	public boolean isLastPage() {
		return lastPage;
	}
	
	/**
	 * 上一页索引值
	 * @return
	 */
	@JsonIgnore
	public int getPrev() {
		if (isFirstPage()) {
			return pageNo;
		} else {
			return pageNo - 1;
		}
	}

	/**
	 * 下一页索引值
	 * @return
	 */
	@JsonIgnore
	public int getNext() {
		if (isLastPage()) {
			return pageNo;
		} else {
			return pageNo + 1;
		}
	}
	
	/**
	 * 获取本页数据对象列表
	 * @return List<T>
	 */
	public List<T> getList() {
		return list;
	}

	/**
	 * 设置本页数据对象列表
	 * @param list
	 */
	public Page<T> setList(List<T> list) {
		this.list = list;
		initialize();
		return this;
	}

	/**
	 * 获取查询排序字符串
	 * @return
	 */
	@JsonIgnore
	public String getOrderBy() {
		// SQL过滤，防止注入 
		String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
					+ "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
		Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		if (sqlPattern.matcher(orderBy).find()) {
			return "";
		}
		return orderBy;
	}

	/**
	 * 设置查询排序，标准查询有效， 实例： updatedate desc, name asc
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * 获取点击页码调用的js函数名称
	 * function ${page.funcName}(pageNo){location="${ctx}/list-${category.id}${urlSuffix}?pageNo="+i;}
	 * @return
	 */
	@JsonIgnore
	public String getFuncName() {
		return funcName;
	}

	/**
	 * 设置点击页码调用的js函数名称，默认为page，在一页有多个分页对象时使用。
	 * @param funcName 默认为page
	 */
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	/**
	 * 获取分页函数的附加参数
	 * @return
	 */
	@JsonIgnore
	public String getFuncParam() {
		return funcParam;
	}

	/**
	 * 设置分页函数的附加参数
	 * @return
	 */
	public void setFuncParam(String funcParam) {
		this.funcParam = funcParam;
	}

	/**
	 * 设置提示消息，显示在“共n条”之后
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * 分页是否有效
	 * @return this.pageSize==-1
	 */
	@JsonIgnore
	public boolean isDisabled() {
		return this.pageSize==-1;
	}
	
	/**
	 * 是否进行总数统计
	 * @return this.count==-1
	 */
	@JsonIgnore
	public boolean isNotCount() {
		return this.count==-1;
	}
	
	/**
	 * 获取 Hibernate FirstResult
	 */
	public int getFirstResult(){
		int firstResult = (getPageNo() - 1) * getPageSize();
		if (firstResult >= getCount()) {
			firstResult = 0;
		}
		return firstResult;
	}
	/**
	 * 获取 Hibernate MaxResults
	 */
	public int getMaxResults(){
		return getPageSize();
	}

//	/**
//	 * 获取 Spring data JPA 分页对象
//	 */
//	public Pageable getSpringPage(){
//		List<Order> orders = new ArrayList<Order>();
//		if (orderBy!=null){
//			for (String order : StringUtils.split(orderBy, ",")){
//				String[] o = StringUtils.split(order, " ");
//				if (o.length==1){
//					orders.add(new Order(Direction.ASC, o[0]));
//				}else if (o.length==2){
//					if ("DESC".equals(o[1].toUpperCase())){
//						orders.add(new Order(Direction.DESC, o[0]));
//					}else{
//						orders.add(new Order(Direction.ASC, o[0]));
//					}
//				}
//			}
//		}
//		return new PageRequest(this.pageNo - 1, this.pageSize, new Sort(orders));
//	}
//	
//	/**
//	 * 设置 Spring data JPA 分页对象，转换为本系统分页对象
//	 */
//	public void setSpringPage(org.springframework.data.domain.Page<T> page){
//		this.pageNo = page.getNumber();
//		this.pageSize = page.getSize();
//		this.count = page.getTotalElements();
//		this.list = page.getContent();
//	}
	
	
}
