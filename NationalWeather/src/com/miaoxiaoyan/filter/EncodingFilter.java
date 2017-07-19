package com.miaoxiaoyan.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class EncodingFilter implements Filter {
	private FilterConfig config = null;
	private String encode = null;
	public void destroy() {
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		response.setContentType("text/html;charset="+encode); //--解决响应乱码
		chain.doFilter(new MyHttpServletRequest((HttpServletRequest) request), response);//--包装改造request中和获取请求参数相关的方法解决请求参数乱码
	}
	
	public void init(FilterConfig filterConfig) throws ServletException {
		this.config = filterConfig;
		encode = config.getInitParameter("encode") == null
							?"utf-8"
									:config.getInitParameter("encode");
	}
	class MyHttpServletRequest extends HttpServletRequestWrapper{
		private HttpServletRequest request = null;
		private boolean isNotEncode = true;
		public MyHttpServletRequest(HttpServletRequest request) {
			super(request);
			this.request = request;
		}
		
		@Override
		public Map getParameterMap() {
			try{
				if(request.getMethod().equalsIgnoreCase("POST")){//--如果是post提交,一行代码解决post提交请求参数乱码
					request.setCharacterEncoding(encode);
					return request.getParameterMap();
				}else if(request.getMethod().equalsIgnoreCase("GET")){//--如果是get提交,则应该手动编解码解决乱码
					Map<String,String[]> map = request.getParameterMap();//获取有乱码的map
					if(isNotEncode){//只能在第一次解决乱码
						for(Map.Entry<String, String[]> entry : map.entrySet()){//遍历map,解决所有值的乱码
							String [] vs = entry.getValue();
							for(int i=0;i<vs.length;i++){
								vs[i] = new String(vs[i].getBytes("iso8859-1"),encode);
							}
						}
						isNotEncode = false;//设置为false,第二次就不会再进这个代码块了
					}
					return map;
				}else{
					return request.getParameterMap();
				}
			}catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public String[] getParameterValues(String name) {
			return (String[]) getParameterMap().get(name);
		}
		
		@Override
		public String getParameter(String name) {
			return getParameterValues(name) == null ? null : getParameterValues(name)[0];
		}
		
		
	}
}

