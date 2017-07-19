<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  </head>
  
  <body>
  	<center>气象信息查询页面&mdash;&mdash;支持全球气象信息查询<hr></center>
  	<form action="${pageContext.request.contextPath }/servlet/SearchWeatherData" method="POST">
  		城市名称：<input type="text" name="city"><br>
  		查看数据类型：<input type="radio" name="type" value="graph" checked="checked">图表 <input type="radio" name="type" value="data">原始数据
  		<input type="submit" value="查询"/>
  	</form>
  </body>
</html>
