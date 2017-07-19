package com.miaoxiaoyan.web;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;


import com.alibaba.fastjson.JSON;
import com.miaoxiaoyan.domain.HeWeather_data_service;
import com.miaoxiaoyan.domain.Hourly_forecast;
import com.miaoxiaoyan.domain.JsonRootBean;

public class SearchWeatherData extends HttpServlet {
	
	private static final long serialVersionUID = -1799349055644118680L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//1,获取请求参数————城市名称
		String city = request.getParameter("city");
		String type = request.getParameter("type");
		
		//https://free-api.heweather.com/v5/hourly?city=yourcity&key=yourkey
		//2,组织调用接口的api参数
		String httpUrl = "http://apis.baidu.com/heweather/pro/weather";
		String httpArg = "city="+city;   //默认beijing
		String tempStr = request(httpUrl, httpArg);
		
		//将空格替换掉
		String newStr = "{\"HeWeather_data_service\"";
		String jsonResult = newStr+tempStr.substring(29);
		request.setAttribute("data", jsonResult);
		
		//组织JavaBean
		JsonRootBean root = JSON.parseObject(jsonResult, JsonRootBean.class);
		
		String country = root.getHeWeather_data_service().get(0).getBasic().getCnty();
		String city2 = root.getHeWeather_data_service().get(0).getBasic().getCity();
		request.setAttribute("country", country);
		request.setAttribute("city2", city2);
		
		
		List<String> show_Message = new ArrayList<String>();
		
		List<String> xLists = new ArrayList<String>();
		List<String> tmps = new ArrayList<String>();
		
		String show_str = "";
		for(Hourly_forecast h_temp : root.getHeWeather_data_service().get(0).getHourly_forecast()){
			show_str ="时间：" + h_temp.getDate()+"\t"+"温度值："+h_temp.getTmp()+"摄氏度";
			xLists.add(h_temp.getDate().substring(11, 13));
			tmps.add(h_temp.getTmp());
			show_Message.add(show_str);
			show_str = "";
		}
		
		request.setAttribute("Temp_List", show_Message);  //显示未来24小时温度值
		
		System.out.println(root.getHeWeather_data_service().size());
		System.out.println(root.getHeWeather_data_service().get(0).getHourly_forecast().get(0).getTmp());
		
		
		/////////////获取到了JSON代表的JAVABEAN对象////////////////
		List<Double> list_param = Calculate(root);
		double A = list_param.get(0);
		double B = list_param.get(1);
		double f = list_param.get(2);
		request.setAttribute("A", A);
		request.setAttribute("B", B);
		request.setAttribute("f", f);
		
		if("graph".equalsIgnoreCase(type)){
			//绘制折线图
			double[][] data = new double[][] { { Double.parseDouble(tmps.get(0)), Double.parseDouble(tmps.get(1)), Double.parseDouble(tmps.get(2)), Double.parseDouble(tmps.get(3)), Double.parseDouble(tmps.get(4)),Double.parseDouble(tmps.get(5)),Double.parseDouble(tmps.get(6)),Double.parseDouble(tmps.get(7)),Double.parseDouble(tmps.get(8)),Double.parseDouble(tmps.get(9)),Double.parseDouble(tmps.get(10)),Double.parseDouble(tmps.get(11)),Double.parseDouble(tmps.get(12)),Double.parseDouble(tmps.get(13)),Double.parseDouble(tmps.get(4)),Double.parseDouble(tmps.get(15)),Double.parseDouble(tmps.get(16)),Double.parseDouble(tmps.get(17)),Double.parseDouble(tmps.get(18)),Double.parseDouble(tmps.get(19)),Double.parseDouble(tmps.get(20)),Double.parseDouble(tmps.get(21)),Double.parseDouble(tmps.get(22)),Double.parseDouble(tmps.get(23))}
					 };
			String[] rowKeys = {"Temperature_Change"};
			String[] columnKeys = { xLists.get(0)+"时", xLists.get(1)+"时",xLists.get(2)+"时",xLists.get(3)+"时",xLists.get(4)+"时",xLists.get(5)+"时",xLists.get(6)+"时",xLists.get(7)+"时",xLists.get(8)+"时",xLists.get(9)+"时",xLists.get(10)+"时",xLists.get(11)+"时",xLists.get(12)+"时",xLists.get(13)+"时",xLists.get(14)+"时",xLists.get(15)+"时",xLists.get(16)+"时",xLists.get(17)+"时",xLists.get(18)+"时",xLists.get(19)+"时",xLists.get(20)+"时",xLists.get(21)+"时",xLists.get(22)+"时",xLists.get(23)+"时"};
			CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
			createTimeXYChar("气温折线图", "Hourly_Time", "Temprature", dataset, response);
		}else{
			request.getRequestDispatcher("/WEB-INF/page/show.jsp").forward(request, response);
			return;
		}
	}
	
	// 柱状图,折线图 数据集
	public CategoryDataset getBarData(double[][] data, String[] rowKeys,
			String[] columnKeys) {
		return DatasetUtilities
				.createCategoryDataset(rowKeys, columnKeys, data);

	}

	/**
	 * 折线图
	 * 
	 * @param chartTitle
	 * @param x
	 * @param y
	 * @param xyDataset
	 * @param charName
	 * @return
	 */
	public void createTimeXYChar(String chartTitle, String x, String y,
			CategoryDataset xyDataset, HttpServletResponse response) {

		JFreeChart chart = ChartFactory.createLineChart(chartTitle, x, y,
				xyDataset, PlotOrientation.VERTICAL, true, true, false);

		chart.setTextAntiAlias(false);
		chart.setBackgroundPaint(Color.WHITE);
		// 设置图标题的字体重新设置title
		Font font = new Font("隶书", Font.BOLD, 25);
		TextTitle title = new TextTitle(chartTitle);
		title.setFont(font);
		chart.setTitle(title);
		// 设置面板字体
		Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);

		chart.setBackgroundPaint(Color.WHITE);

		CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
		// x轴 // 分类轴网格是否可见
		categoryplot.setDomainGridlinesVisible(true);
		// y轴 //数据轴网格是否可见
		categoryplot.setRangeGridlinesVisible(true);

		categoryplot.setRangeGridlinePaint(Color.WHITE);// 虚线色彩

		categoryplot.setDomainGridlinePaint(Color.WHITE);// 虚线色彩

		categoryplot.setBackgroundPaint(Color.lightGray);

		
		// 设置轴和面板之间的距离
		// categoryplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));

		CategoryAxis domainAxis = categoryplot.getDomainAxis();

		domainAxis.setLabelFont(labelFont);// 轴标题
		domainAxis.setTickLabelFont(labelFont);// 轴数值

		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // 横轴上的
		// Lable
		// 45度倾斜
		// 设置距离图片左端距离
		domainAxis.setLowerMargin(0.0);
		// 设置距离图片右端距离
		domainAxis.setUpperMargin(0.0);

		NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		numberaxis.setAutoRangeIncludesZero(true);

		// 获得renderer 注意这里是下嗍造型到lineandshaperenderer！！
		LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
				.getRenderer();

		lineandshaperenderer.setBaseShapesVisible(true); // series 点（即数据点）可见
		lineandshaperenderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见

		// 显示折点数据
		// lineandshaperenderer.setBaseItemLabelGenerator(new
		// StandardCategoryItemLabelGenerator());
		// lineandshaperenderer.setBaseItemLabelsVisible(true);

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			response.setContentType("image/png");
			ChartUtilities.writeChartAsPNG(out, chart, 500, 510);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 计算拟合参数
	 */
	public List<Double> Calculate(JsonRootBean root){
		List<Double> result = new ArrayList<Double>();
		
		//求参数 A B f 
		double A;
		double B;
		double f;
		
		List<HeWeather_data_service> list = root.getHeWeather_data_service();
		if(list != null && list.size()>0){
			HeWeather_data_service h = list.get(0);
			List<Hourly_forecast> hour_list = h.getHourly_forecast();
			int size = hour_list.size();
			System.out.println("hour_list的个数为："+size);
			int i = 0;
			double sum_tmp = 0;
			double sum_tmp2 = 0;
			if(hour_list != null)
				for(Hourly_forecast t : hour_list){
					//计算参数A
					sum_tmp += Integer.parseInt(t.getTmp())*Math.cos(size*i);
					sum_tmp2 += Integer.parseInt(t.getTmp())*Math.sin(size*i);
					i++;
				}
			
			A = 2.0/24 * sum_tmp;
			B = 2.0/24 * sum_tmp2;
			f = 24.0/Math.PI;
			result.add(A);
			result.add(B);
			result.add(f);
			return result;
		}else{
			throw new RuntimeException("JSON没有获取到数据");
		}
	}
	
	
	/**
	 * @param urlAll
	 *            :请求接口
	 * @param httpArg
	 *            :参数
	 * @return 返回结果
	 */
	public static String request(String httpUrl, String httpArg) {
	    BufferedReader reader = null;
	    String result = null;
	    StringBuffer sbf = new StringBuffer();
	    httpUrl = httpUrl + "?" + httpArg;
	    
	    try {
	        URL url = new URL(httpUrl);
	        HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
	        connection.setRequestMethod("GET");
	        // 填入apikey到HTTP header
	        connection.setRequestProperty("apikey", "232b2336089a9f7bc7a06de48d8acf45");    //百度账户APIKEY
	        connection.connect();
	        InputStream is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	        String strRead = null;
	        while ((strRead = reader.readLine()) != null) {
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        reader.close();
	        result = sbf.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
