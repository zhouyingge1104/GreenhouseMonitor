package com.yigo.greenhousemonitor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final String URL = "http://dc.comway.com.cn/webapi/senddata?userid=532348374@qq.com&pass=e10adc3949ba59abbe56e057f20f883e&dtuid=401415610803&msgdetail=01030001001015c6";
	static final int SHOW_RESPONSE = 0;
	
	boolean inTask = true;
	
	Handler h;
	int count;
	String respStatus;
	
	Timer timer;
	
	String temp = "00,010320011702B4029307B3001600050021001500320059000000000000010F02A000008673";
	
	/* UI */
	TextView tvRespTime, tvLocalTime, tvCount,
			 tvTempIs, tvHumiIs, tvCoIs, 
			 tvTempOs, tvHumiOs, tvCoOs;
	Button btnSwitch, btnExit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvRespTime 	= (TextView)findViewById(R.id.tv_resp_time);
		tvTempIs 	= (TextView)findViewById(R.id.tv_resp_temp_is);
		tvHumiIs 	= (TextView)findViewById(R.id.tv_resp_humi_is);
		tvCoIs 		= (TextView)findViewById(R.id.tv_resp_co_is);
		tvTempOs 	= (TextView)findViewById(R.id.tv_resp_temp_os);
		tvHumiOs 	= (TextView)findViewById(R.id.tv_resp_humi_os);
		tvCoOs 		= (TextView)findViewById(R.id.tv_resp_co_os);
		tvLocalTime = (TextView)findViewById(R.id.tv_local_time);
		tvCount 	= (TextView)findViewById(R.id.tv_count);
		
		btnSwitch = (Button)findViewById(R.id.btn_switch);
		btnExit = (Button)findViewById(R.id.btn_exit);
		
		btnSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(inTask){
					inTask = false;
					timer.cancel();
					btnSwitch.setText("开始");
				}else{
					inTask = true;
					timer = new Timer();  
					timer.schedule(new MyTask(), 0, 5*1000);
					btnSwitch.setText("停止");
					count = 0;
				}
			}
		});
		
		btnExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				timer.cancel();
				MainActivity.this.finish();
			}
		});
		
		h = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what){
				case SHOW_RESPONSE:
					String response = (String)msg.obj;
					
					handle(response);
					
				}
			}
		};
		
		//send();
		//handle(temp);
		
		timer = new Timer();  
        timer.schedule(new MyTask(), 0, 5*1000);
      
		
	}
	
	/** 处理返回内容 */
	private void handle(String response){
		count++;
		
		String code = response.split(",")[0]; //part1: status, success or fail
		if(getRespStatus(code)){
			
			int DATA_RIGHT_SIZE = 74;
		
			String content = response.split(",")[1]; //part2: content
			if(content.length() < DATA_RIGHT_SIZE){
				Toast.makeText(this, "数据不完整", Toast.LENGTH_LONG).show();
				return;
			}
						
			String tempIsStr = content.substring(6, 10);   double tempIs = Integer.parseInt(tempIsStr, 16)/10.0;
			String humiIsStr = content.substring(10, 14);  double humiIs = Integer.parseInt(humiIsStr, 16)/10.0;
			String coIsStr   = content.substring(14, 18);  double coIs 	= Integer.parseInt(coIsStr, 16);
			
			String yearStr   = content.substring(22, 26);  int year 	= Integer.parseInt(yearStr, 10);
			String monthStr  = content.substring(26, 30);  int month 	= Integer.parseInt(monthStr, 10);
			String dayStr    = content.substring(30, 34);  int day 	= Integer.parseInt(dayStr, 10);
			String hourStr   = content.substring(34, 38);  int hour 	= Integer.parseInt(hourStr, 10);
			String minStr    = content.substring(38, 42);  int min 	= Integer.parseInt(minStr, 10);
			String secStr    = content.substring(42, 46);  int sec 	= Integer.parseInt(secStr, 10);
			
			String tempOsStr = content.substring(58, 62);  double tempOs = Integer.parseInt(tempOsStr, 16)/10.0;
			String humiOsStr = content.substring(62, 66);  double humiOs = Integer.parseInt(humiOsStr, 16)/10.0;
			String coOsStr   = content.substring(66, 70);  double coOs   = Integer.parseInt(coOsStr, 16);
			
			String sRespTime = "20" + year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
			String sTempIs = tempIs + " ℃";
			String sHumiIs = humiIs + " %RH";
			String sCoIs = coIs + " ppm";
			String sTempOs = tempOs + " ℃";
			String sHumiOs = humiOs + " %RH";
			String sCoOs = coOs + " ppm";
			String sLocalTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String sCount = count + "";
		
			tvRespTime.setText(sRespTime);
			tvTempIs.setText(sTempIs);
			tvHumiIs.setText(sHumiIs);
			tvCoIs.setText(sCoIs);
			tvTempOs.setText(sTempOs);
			tvHumiOs.setText(sHumiOs);
			tvCoOs.setText(sCoOs);
			tvLocalTime.setText(sLocalTime);
			tvCount.setText(sCount);
			
		}else{
			
		}
	}
	
	//判断是否成功
		/*
		返回成功
			00  成功
		返回失败
			01  get参数不完整
			02  用户名密码不正确
			03  请求超时
			04  服务器超时
			05  调用失败
			06  用户未注册
			07  设备未绑定
			08  设备离线
		*/
	private boolean getRespStatus(String code){
		if(code.equals("00")){ respStatus = "成功"; 			  	 															return true;  }
		if(code.equals("01")){ respStatus = "get参数不完整"; 		Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("02")){ respStatus = "用户名密码不正确";		Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("03")){ respStatus = "请求超时";			Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("04")){ respStatus = "服务器超时"; 		Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("05")){ respStatus = "调用失败"; 			Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("06")){ respStatus = "用户未注册"; 		Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("07")){ respStatus = "设备未绑定"; 		Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
		if(code.equals("08")){ respStatus = "设备离线"; 			Toast.makeText(this, respStatus, Toast.LENGTH_LONG).show(); return false; }
			
		return false;
	}
	
	/** 发送请求 */
	private void send() {
		new Sender().start();
	}
	
	private class Sender extends Thread {
		
		public void run(){
			try{
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(URL);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if(httpResponse.getStatusLine().getStatusCode() == 200){
					HttpEntity entity = httpResponse.getEntity();
					String response = EntityUtils.toString(entity, "utf-8");
					Message message = new Message();
					message.what = SHOW_RESPONSE;
					message.obj = response;
					h.sendMessage(message);
				}
			}catch(Exception e){
				Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	class MyTask extends TimerTask{  
	     public void run() {  
	    	send();
	     }  
	}  
	
}
