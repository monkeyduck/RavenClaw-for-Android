package com.example.ravenclaw;

import user.definition.UserInput;
import utils.Const;
import utils.Utils;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUnderstander;
import com.iflytek.cloud.speech.SpeechUnderstanderListener;
import com.iflytek.cloud.speech.UnderstanderResult;

import dmcore.agents.coreagents.DMCore;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class OutputActivity extends Activity {
	// Tip
	private Toast mToast;
	//语义理解结果显示
	private EditText mResultText;
	// Sub thread
	private ClawnThread CoreThread = null;

	//语义理解对象
	private SpeechUnderstander speechUnderstander; 
	
	// Define a handler
	private Handler OutputHandler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
			case Const.CHANGEBACKGROUND:
				setContentView(R.layout.activity_output);
				//语义理解结果设置
				mResultText = (EditText) findViewById(R.id.txt_result);
				
				ImageButton btn = (ImageButton)findViewById(R.id.imagebutton_output_startspeak);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						showSpeechUnderstanderNoPop();
					}
				});
				break;
			case Const.FINISHDIALOG:
				finish();
			}
		}
	};
	// Define a new thread
	class ClawnThread extends Thread{
		public void run(){
			//Init the message looper queue
			Looper.prepare();
			
			//User login
			/*SpeechUser.getUser().login(MyOutput.getAppContext(), null, null
					, "appid=" + getString(R.string.app_id), listener);*/
			DMCore.DialogTaskOnBeginSession();
			Log.d("ThreadId",""+Thread.currentThread().getId());
			// Set DMCore.bForceExit to false
			DMCore.bForceExit = false;
			DMCore.pDMCore.Execute(OutputHandler);
			int i=0;
			while (i<100000){
				i++;
			}
			// Send the finish message to MainThread
			OutputHandler.sendEmptyMessage(Const.FINISHDIALOG);
			// 启动子线程消息循环队列
			Looper.loop();
		}
	}
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_background);
		// init the speechUnderstander object
		speechUnderstander = SpeechUnderstander.createUnderstander(this);
		
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		Log.d("ThreadId",""+Thread.currentThread().getId());
		
		if (CoreThread==null){
			CoreThread = new ClawnThread();
		}
		CoreThread.start();
		
		
	}
	/*//--------------------------------------------------------------------------------
	// AsyncTask
	//--------------------------------------------------------------------------------
	public class ExecuteDialogSystem extends AsyncTask{

		@Override
		protected Object doInBackground(Object... arg0) {
			// TODO Auto-generated method stub
			DMCore.DialogTaskOnBeginSession();
			Log.d("ThreadId",""+Thread.currentThread().getId());
			DMCore.pDMCore.Execute();
			return null;
		}
		
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.output, menu);
		return true;
	}
	
	@Override
	public void onStop(){
		super.onStop();
		Log.e("Activity","call onStop");
	}
	@Override
	public void onDestroy(){
		Log.e("Activity","call onDestroy");
		/*try {
			CoreThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		DMCore.bForceExit=true;
		super.onDestroy();
		//System.exit(0);
		
	}
	
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
		mResultText.setText(null);
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
			UserInput.JsonResult = result.getResultString();
			String sShow = Utils.GetValueGivenSlot(UserInput.JsonResult, "text");
			mResultText.append(sShow);
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
