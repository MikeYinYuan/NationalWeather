<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@page import="org.jfree.data.category.DefaultCategoryDataset"%>
<%@page import="org.jfree.chart.JFreeChart"%>
<%@page import="org.jfree.chart.ChartFactory"%>
<%@page import="org.jfree.chart.plot.PlotOrientation"%>
<%@page import="org.jfree.chart.servlet.ServletUtilities"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
		<base href="<%=basePath%>">

		<title>My JSP 'index.jsp' starting page</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
	</head>
  
  <body>&nbsp; 
   <hr>
	气象数据：<br>
  	
  	国家：${requestScope.country }，城市：${requestScope.city2}<br>
  	
    <c:forEach var="temp" items="${requestScope.Temp_List} " >
    	${temp }<br>
    </c:forEach>
    <hr>
    拟合曲线参数值:<br>
    A=${requestScope.A }<br>
    B=${requestScope.B }<br>
    f=${requestScope.f }<br>
    <hr/>
    
    <img src="<%= path %>/servlet/SearchWeatherData"><br/><br/><br/>  
  
	<br/><br/><br/><br/><br/><br/><br/><br/>
	
  </body>
</html>
