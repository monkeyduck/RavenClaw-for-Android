package com.example.ravenclaw;

import java.util.HashMap;
import java.util.List;

import utils.Const;
import utils.Utils;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUnderstander;
import com.iflytek.cloud.speech.SpeechUnderstanderListener;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.UnderstanderResult;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	// Tip
	private Toast mToast;
	//语义理解对象
	private SpeechUnderstander speechUnderstander; 
	//合成对象.
	private SpeechSynthesizer mSpeechSynthesizer;
	// Store installed app info <appName,packageName>
	private HashMap<String,String> InstalledAppMap = new HashMap<String, String>();
	
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Set listener on button
		findViewById(R.id.DialogSystemButton).setOnClickListener(this);
		findViewById(R.id.StartCalendarButton).setOnClickListener(this);
		findViewById(R.id.imagebutton_startspeak).setOnClickListener(this);
		
		// User login
		SpeechUser.getUser().login(this, null, null
				, "appid=" + getString(R.string.app_id), listener);
		// init the speechUnderstander object
		speechUnderstander = SpeechUnderstander.createUnderstander(this);
		//初始化合成对象.
		mSpeechSynthesizer=SpeechSynthesizer.createSynthesizer(this);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		for (PackageInfo pi : packages){
			InstalledAppMap.put(pi.applicationInfo.loadLabel(
					getPackageManager()).toString().toLowerCase(),pi.packageName);
			Log.v("app","<"+pi.applicationInfo.loadLabel(getPackageManager()).toString()
					+","+pi.packageName+">");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = null;
		switch(v.getId()){
		case R.id.DialogSystemButton:
			intent = new Intent(this,OutputActivity.class);
			startActivity(intent);
			break;
		case R.id.StartCalendarButton:
			intent = new Intent();
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN,null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			String packageName ="com.android.calculator2";
			resolveIntent.setPackage(packageName);
			List<ResolveInfo> apps = 
					getPackageManager().queryIntentActivities(resolveIntent, 0);
			ResolveInfo ri = apps.iterator().next();
			if (ri!=null){
				String className = ri.activityInfo.name;
				intent.setComponent(new ComponentName(packageName,className));
			}
			startActivity(intent);
			break;
		case R.id.imagebutton_startspeak:
			showSpeechUnderstanderNoPop();
		}
		
	}
		
		/**
		 * 用户登录回调监听器.
		 */
		private SpeechListener listener = new SpeechListener()
		{

			@Override
			public void onData(byte[] arg0) {
			}

			@Override
			public void onCompleted(SpeechError error) {
				if(error != null) {
					Toast.makeText(MainActivity.this, getString(R.string.text_login_fail)
							, Toast.LENGTH_SHORT).show();
					
				}			
			}
			@Override
			public void onEvent(int arg0, Bundle arg1) {
			}		
		};
		/**
		 * 语义理解
		 */
		public void showSpeechUnderstanderNoPop()
		{
			//清空Grammar_ID，防止语义理解后进行语义理解时Grammar_ID的干扰
			speechUnderstander.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
			//设置语义理解的引擎
			speechUnderstander.setParameter(SpeechConstant.DOMAIN, "iat");
			//设置采样率参数，支持8K和16K 
			speechUnderstander.setParameter(SpeechConstant.SAMPLE_RATE, "16000");
			speechUnderstander.startUnderstanding(understanderListener);
			showTip(getString(R.string.text_iat_begin));
		}
		/**
		 * 语义理解回调监听器
		 */
		SpeechUnderstanderListener understanderListener = new SpeechUnderstanderListener()
		{
			@Override
			public void onBeginOfSpeech() {
				showTip("开始说话");
			}

			@Override
			public void onError(SpeechError error) {
				showTip(error.getPlainDescription(true));
			}

			@Override
			public void onEndOfSpeech() {
				//showTip("结束说话");
			}

			@Override
			public void onEvent(int eventType, int arg1, int arg2, String msg) {
				
			}

			@Override
			public void onResult(UnderstanderResult result) {
				String sResult = result.getResultString();
				/*String tmpString="{\"semantic\": {\"slots\": {\"name\": \"计算器\"" +
						"}},\"rc\": 0,\"operation\": \"LAUNCH\",\"service\": \"app\","+ 
					  "\"text\": \"打开计算器\"}";*/
				String AppName = Utils.GetValueGivenSlot(sResult, "name");
				if (AppName.equals("")){
					showTip("未识别出应用名");
					Log.e(Const.LAUNCHAPP_TAG,"App name can not be recongnized");
				}else{
					Intent Newintent = new Intent();
					Intent resolveIntent = new Intent(Intent.ACTION_MAIN,null);
					resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
					String packageName = InstalledAppMap.get(AppName);
					if (packageName.equals("")){
						showTip("未安装此应用");
						Log.e(Const.LAUNCHAPP_TAG,"App not installed");
					}else{
						resolveIntent.setPackage(packageName);
						List<ResolveInfo> apps = 
								getPackageManager().queryIntentActivities(resolveIntent, 0);
						ResolveInfo ri = apps.iterator().next();
						if (ri!=null){
							String className = ri.activityInfo.name;
							Newintent.setComponent(new ComponentName(packageName,className));
						}
						startActivity(Newintent);
					}
				}
			}

			@Override
			public void onVolumeChanged(int volume) {
				showTip("当前正在说话，音量值为:" + volume);
				
			}
			
		};
		private void showTip(String str)
		{
			if(!TextUtils.isEmpty(str))
			{
				mToast.setText(str);
				mToast.show();
			}
		}
		
	
	

}
