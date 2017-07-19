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
		//1,��ȡ�����������������������
		String city = request.getParameter("city");
		String type = request.getParameter("type");
		
		//https://free-api.heweather.com/v5/hourly?city=yourcity&key=yourkey
		//2,��֯���ýӿڵ�api����
		String httpUrl = "http://apis.baidu.com/heweather/pro/weather";
		String httpArg = "city="+city;   //Ĭ��beijing
		String tempStr = request(httpUrl, httpArg);
		
		//���ո��滻��
		String newStr = "{\"HeWeather_data_service\"";
		String jsonResult = newStr+tempStr.substring(29);
		request.setAttribute("data", jsonResult);
		
		//��֯JavaBean
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
			show_str ="ʱ�䣺" + h_temp.getDate()+"\t"+"�¶�ֵ��"+h_temp.getTmp()+"���϶�";
			xLists.add(h_temp.getDate().substring(11, 13));
			tmps.add(h_temp.getTmp());
			show_Message.add(show_str);
			show_str = "";
		}
		
		request.setAttribute("Temp_List", show_Message);  //��ʾδ��24Сʱ�¶�ֵ
		
		System.out.println(root.getHeWeather_data_service().size());
		System.out.println(root.getHeWeather_data_service().get(0).getHourly_forecast().get(0).getTmp());
		
		
		/////////////��ȡ����JSON�����JAVABEAN����////////////////
		List<Double> list_param = Calculate(root);
		double A = list_param.get(0);
		double B = list_param.get(1);
		double f = list_param.get(2);
		request.setAttribute("A", A);
		request.setAttribute("B", B);
		request.setAttribute("f", f);
		
		if("graph".equalsIgnoreCase(type)){
			//��������ͼ
			double[][] data = new double[][] { { Double.parseDouble(tmps.get(0)), Double.parseDouble(tmps.get(1)), Double.parseDouble(tmps.get(2)), Double.parseDouble(tmps.get(3)), Double.parseDouble(tmps.get(4)),Double.parseDouble(tmps.get(5)),Double.parseDouble(tmps.get(6)),Double.parseDouble(tmps.get(7)),Double.parseDouble(tmps.get(8)),Double.parseDouble(tmps.get(9)),Double.parseDouble(tmps.get(10)),Double.parseDouble(tmps.get(11)),Double.parseDouble(tmps.get(12)),Double.parseDouble(tmps.get(13)),Double.parseDouble(tmps.get(4)),Double.parseDouble(tmps.get(15)),Double.parseDouble(tmps.get(16)),Double.parseDouble(tmps.get(17)),Double.parseDouble(tmps.get(18)),Double.parseDouble(tmps.get(19)),Double.parseDouble(tmps.get(20)),Double.parseDouble(tmps.get(21)),Double.parseDouble(tmps.get(22)),Double.parseDouble(tmps.get(23))}
					 };
			String[] rowKeys = {"Temperature_Change"};
			String[] columnKeys = { xLists.get(0)+"ʱ", xLists.get(1)+"ʱ",xLists.get(2)+"ʱ",xLists.get(3)+"ʱ",xLists.get(4)+"ʱ",xLists.get(5)+"ʱ",xLists.get(6)+"ʱ",xLists.get(7)+"ʱ",xLists.get(8)+"ʱ",xLists.get(9)+"ʱ",xLists.get(10)+"ʱ",xLists.get(11)+"ʱ",xLists.get(12)+"ʱ",xLists.get(13)+"ʱ",xLists.get(14)+"ʱ",xLists.get(15)+"ʱ",xLists.get(16)+"ʱ",xLists.get(17)+"ʱ",xLists.get(18)+"ʱ",xLists.get(19)+"ʱ",xLists.get(20)+"ʱ",xLists.get(21)+"ʱ",xLists.get(22)+"ʱ",xLists.get(23)+"ʱ"};
			CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
			createTimeXYChar("��������ͼ", "Hourly_Time", "Temprature", dataset, response);
		}else{
			request.getRequestDispatcher("/WEB-INF/page/show.jsp").forward(request, response);
			return;
		}
	}
	
	// ��״ͼ,����ͼ ���ݼ�
	public CategoryDataset getBarData(double[][] data, String[] rowKeys,
			String[] columnKeys) {
		return DatasetUtilities
				.createCategoryDataset(rowKeys, columnKeys, data);

	}

	/**
	 * ����ͼ
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
		// ����ͼ�����������������title
		Font font = new Font("����", Font.BOLD, 25);
		TextTitle title = new TextTitle(chartTitle);
		title.setFont(font);
		chart.setTitle(title);
		// �����������
		Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);

		chart.setBackgroundPaint(Color.WHITE);

		CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
		// x�� // �����������Ƿ�ɼ�
		categoryplot.setDomainGridlinesVisible(true);
		// y�� //�����������Ƿ�ɼ�
		categoryplot.setRangeGridlinesVisible(true);

		categoryplot.setRangeGridlinePaint(Color.WHITE);// ����ɫ��

		categoryplot.setDomainGridlinePaint(Color.WHITE);// ����ɫ��

		categoryplot.setBackgroundPaint(Color.lightGray);

		
		// ����������֮��ľ���
		// categoryplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));

		CategoryAxis domainAxis = categoryplot.getDomainAxis();

		domainAxis.setLabelFont(labelFont);// �����
		domainAxis.setTickLabelFont(labelFont);// ����ֵ

		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // �����ϵ�
		// Lable
		// 45����б
		// ���þ���ͼƬ��˾���
		domainAxis.setLowerMargin(0.0);
		// ���þ���ͼƬ�Ҷ˾���
		domainAxis.setUpperMargin(0.0);

		NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		numberaxis.setAutoRangeIncludesZero(true);

		// ���renderer ע���������������͵�lineandshaperenderer����
		LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
				.getRenderer();

		lineandshaperenderer.setBaseShapesVisible(true); // series �㣨�����ݵ㣩�ɼ�
		lineandshaperenderer.setBaseLinesVisible(true); // series �㣨�����ݵ㣩�������߿ɼ�

		// ��ʾ�۵�����
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
	 * ������ϲ���
	 */
	public List<Double> Calculate(JsonRootBean root){
		List<Double> result = new ArrayList<Double>();
		
		//����� A B f 
		double A;
		double B;
		double f;
		
		List<HeWeather_data_service> list = root.getHeWeather_data_service();
		if(list != null && list.size()>0){
			HeWeather_data_service h = list.get(0);
			List<Hourly_forecast> hour_list = h.getHourly_forecast();
			int size = hour_list.size();
			System.out.println("hour_list�ĸ���Ϊ��"+size);
			int i = 0;
			double sum_tmp = 0;
			double sum_tmp2 = 0;
			if(hour_list != null)
				for(Hourly_forecast t : hour_list){
					//�������A
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
			throw new RuntimeException("JSONû�л�ȡ������");
		}
	}
	
	
	/**
	 * @param urlAll
	 *            :����ӿ�
	 * @param httpArg
	 *            :����
	 * @return ���ؽ��
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
	        // ����apikey��HTTP header
	        connection.setRequestProperty("apikey", "232b2336089a9f7bc7a06de48d8acf45");    //�ٶ��˻�APIKEY
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
