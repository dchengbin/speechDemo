package com.cz.voicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.cz.speech.setting.UnderstanderSettings;
import com.cz.speech.util.ApkInstaller;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.voicedemo.R;


import java.io.IOException;

public class UnderstanderDemo extends Activity implements OnClickListener {
	private static String TAG = UnderstanderDemo.class.getSimpleName();
	// 语义理解对象（语音到语义）。
	private SpeechUnderstander mSpeechUnderstander;
	// 语义理解对象（文本到语义）。
	private TextUnderstander   mTextUnderstander;	
	private Toast mToast;	
	private EditText mUnderstanderText;
	private int mPercentForBuffering = 0;
	private SpeechSynthesizer mTts;
	ApkInstaller mInstaller ;
	private int mPercentForPlaying = 0;
	private SharedPreferences mSharedPreferences;

	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.understander);

		initLayout();
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
		mInstaller = new  ApkInstaller(this);
		/**
		 * 申请的appid时，我们为开发者开通了开放语义（语义理解）
		 * 由于语义理解的场景繁多，需开发自己去开放语义平台：http://www.xfyun.cn/services/osp
		 * 配置相应的语音场景，才能使用语义理解，否则文本理解将不能使用，语义理解将返回听写结果。
		 */
		// 初始化对象
		mSpeechUnderstander = SpeechUnderstander.createUnderstander(UnderstanderDemo.this, mSpeechUdrInitListener);
		mTextUnderstander = TextUnderstander.createTextUnderstander(UnderstanderDemo.this, mTextUdrInitListener);
		
		mToast = Toast.makeText(UnderstanderDemo.this, "", Toast.LENGTH_SHORT);
		mUnderstanderText.setText("");
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		Log.d(TAG, "current vol=" + current + "max vol=" + max);
		IntentFilter IntentFilter = new IntentFilter("com.auric.intell.xld.rbt.shake");
		IntentFilter.addAction("com.auric.intell.xld.rbt.stand");
		IntentFilter.addAction("com.auric.intell.xld.rbt.liedown");
		IntentFilter.addAction("com.auric.intell.xld.rbt.overturn");
		IntentFilter.addAction("com.auric.intell.xld.rbt.trip");
		IntentFilter.addAction("com.auric.intell.xld.rbt.takeup");
		IntentFilter.addAction("com.auric.intell.xld.rbt.paxia");
		IntentFilter.addAction("com.auric.intell.xld.rbt.cewo");
		IntentFilter.addAction("com.auric.intell.xld.rbt.unknow");
		IntentFilter.addAction("com.auric.intell.xld.rbt.face.head.down");





		IntentFilter.addAction("android.intent.action.cz.gsensorcali.success");

		Intent intent = new Intent("android.intent.action.cszj.gsensorcali");
		intent.putExtra("data", "hello");
		sendBroadcast(intent);

		registerReceiver(mBroadcastReceiver, IntentFilter);
		//test_tts_play("啊啊啊");
		//g_sensor_receiver();

		test_cz();
		//first_tts_play();



/*
		//注册广播接受者java代码
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		//创建广播接受者对象
		BatteryReceiver batteryReceiver = new BatteryReceiver();

		//注册receiver
		registerReceiver(batteryReceiver, intentFilter);
*/
	}



	class BatteryReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//判断它是否是为电量变化的Broadcast Action
			if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
				//获取当前电量
				int level = intent.getIntExtra("level", 0);
				//电量的总刻度
				int scale = intent.getIntExtra("scale", 100);
				//把它转成百分比
				//tv.setText("电池电量为"+((level*100)/scale)+"%");
				Log.d(TAG, "电池电量为="+(level*100)/scale);

			}
		}

	}

	void test_tts_play(String text)
	{


		if (!TextUtils.isEmpty(text)) {
			mUnderstanderText.setText(text);
			FlowerCollector.onEvent(UnderstanderDemo.this, "tts_play");


			setParam();
			int code = mTts.startSpeaking(text, mTtsListener);

			if (code != ErrorCode.SUCCESS) {
				if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
					//未安装则跳转到提示安装页面
					mInstaller.install();
				}else {
					showTip("语音合成失败,错误码: " + code);
				}
			}
		}


	}
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			final String action = intent.getAction();
			if("com.auric.intell.xld.rbt.shake".equals(action)){
				test_tts_play("摇一摇");
			}
			else if("com.auric.intell.xld.rbt.stand".equals(action)){
				test_tts_play("站立姿势");
			}
			else if("com.auric.intell.xld.rbt.liedown".equals(action)){
				test_tts_play("躺下");
			}
			else if("com.auric.intell.xld.rbt.overturn".equals(action)){
				test_tts_play("倒立");
			}
			else if("com.auric.intell.xld.rbt.trip".equals(action)){
				test_tts_play("摔倒");
			}
			else if("com.auric.intell.xld.rbt.takeup".equals(action)){
				test_tts_play("提起");
			}
			else if("com.auric.intell.xld.rbt.paxia".equals(action)){
				test_tts_play("趴下");
			}
			else if("com.auric.intell.xld.rbt.cewo".equals(action)){
				test_tts_play("侧卧");
			}
			else if("android.intent.action.cz.gsensorcali.success".equals(action)){
				test_tts_play("校准成功");
			}
			else if("com.auric.intell.xld.rbt.unknow".equals(action)){
				test_tts_play("其他");
			}
			else if("com.auric.intell.xld.rbt.face.head.down".equals(action))
			{
				test_cz();
			}

		}
	};
	private MediaPlayer mp=null;
	private String test_item;
	private int direct=0;
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
	{

		Log.d(TAG, "paramInt="+paramInt);
		if(paramInt == 4){//返回键
			/*
			AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			Log.d(TAG, "current vol=" + current+"max vol=" + max);

			if(current+1 >max)
			{
				direct=1;
			}
			else if(current-1 <=0)
			{
				direct=0;
			}
			if(direct==1)
			{
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			}
			else
			{
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			}
			current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			Log.d(TAG, "current vol=" + current);




*/
				//mTts.stopSpeaking();
				test_cz();
			Log.d(TAG, "playbeep");
				playbeep();
			//while(true)
			{
				direct=direct;
			}
				//return true;
			}

			return false;

	}
	private void playbeep() {
		AudioRecord ar;

		mp = new MediaPlayer();
		mp.reset();
		mp = MediaPlayer.create(this, R.raw.beep_once);//

		//MediaRecorder.AudioSource

		//MediaRecorder.AudioSource.VOICE_CALL
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();


			}

		});

		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mp.start();

	}
	Boolean is_exit=false;
	public void test_cz(){
		// 设置参数
		if(is_exit)
			return;

		setParam();

		if(mSpeechUnderstander.isUnderstanding()){// 开始前检查状态
			mSpeechUnderstander.stopUnderstanding();
			showTip("停止录音");
		}else {
			ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
			if(ret != 0){
				showTip("语义理解失败,错误码:"	+ ret);
			}else {
				showTip(getString(R.string.text_begin));
			}
		}

	}
	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
									 String info) {
			// 合成进度
			mPercentForBuffering = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				//showTip("播放完成");
				//mUnderstanderText.setText("");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
			playbeep();

			test_cz();
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码："+code);
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}
		}
	};
	/**
	 * 初始化Layout。
	 */
	private void initLayout(){
//		findViewById(R.id.text_understander).setOnClickListener(UnderstanderDemo.this);
		findViewById(R.id.start_understander).setOnClickListener(UnderstanderDemo.this);

		mUnderstanderText = (EditText)findViewById(R.id.understander_text);
		
//		findViewById(R.id.understander_stop).setOnClickListener(UnderstanderDemo.this);
//		findViewById(R.id.understander_cancel).setOnClickListener(UnderstanderDemo.this);
//		findViewById(R.id.image_understander_set).setOnClickListener(UnderstanderDemo.this);
		
		mSharedPreferences = getSharedPreferences(UnderstanderSettings.PREFER_NAME, Activity.MODE_PRIVATE);
	}
	
    /**
     * 初始化监听器（语音到语义）。
     */
    private InitListener mSpeechUdrInitListener = new InitListener() {
    	
		@Override
		public void onInit(int code) {
			Log.d(TAG, "speechUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("初始化失败,错误码："+code);
        	}			
		}
    };
    
    /**
     * 初始化监听器（文本到语义）。
     */
    private InitListener mTextUdrInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "textUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("初始化失败,错误码："+code);
        	}
		}
    };
	
    
	int ret = 0;// 函数调用返回值
	@Override
	public void onClick(View view) {				
		switch (view.getId()) {
		// 进入参数设置页面
			/*
		case R.id.image_understander_set:
			Intent intent = new Intent(UnderstanderDemo.this, UnderstanderSettings.class);
			startActivity(intent);
			break;
		// 开始文本理解

		case R.id.text_understander:
			mUnderstanderText.setText("");
			String text = "合肥明天的天气怎么样？";	
			showTip(text);
			
			if(mTextUnderstander.isUnderstanding()){
				mTextUnderstander.cancel();
				showTip("取消");
			}else {
				ret = mTextUnderstander.understandText(text, mTextUnderstanderListener);
				if(ret != 0)
				{
					showTip("语义理解失败,错误码:"+ ret);
				}
			}
			break;
*/
		// 开始语音理解
		case R.id.start_understander:
			mUnderstanderText.setText("");
			// 设置参数
			setParam();
	
			if(mSpeechUnderstander.isUnderstanding()){// 开始前检查状态
				mSpeechUnderstander.stopUnderstanding();
				showTip("停止录音");
			}else {
				ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
				if(ret != 0){
					showTip("语义理解失败,错误码:"	+ ret);
				}else {
					showTip(getString(R.string.text_begin));
				}
			}
			break;
		// 停止语音理解
/*
		case R.id.understander_stop:
			mSpeechUnderstander.stopUnderstanding();
			showTip("停止语义理解");
			break;
		// 取消语音理解
		case R.id.understander_cancel:
			mTts.stopSpeaking();
			mSpeechUnderstander.cancel();
			showTip("取消语义理解");
			break;
*/
		default:
			break;
		}
	}
	
	private TextUnderstanderListener mTextUnderstanderListener = new TextUnderstanderListener() {
		
		@Override
		public void onResult(final UnderstanderResult result) {
			if (null != result) {
				// 显示
				String text = result.getResultString();
				Log.d("onResult","text="+text);
				if (!TextUtils.isEmpty(text)) {
					mUnderstanderText.setText(text);
					FlowerCollector.onEvent(UnderstanderDemo.this, "tts_play");

					//String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
					// 设置参数
					setParam();
					int code = mTts.startSpeaking(text, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

					if (code != ErrorCode.SUCCESS) {
						if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
							//未安装则跳转到提示安装页面
							mInstaller.install();
						}else {
							showTip("语音合成失败,错误码: " + code);
						}
					}
				}
			} else {
				Log.d(TAG, "understander result:null");
				showTip("识别结果不正确。");
			}
		}
		
		@Override
		public void onError(SpeechError error) {
			// 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
			showTip("onError Code："	+ error.getErrorCode());
			
		}
	};
	
    /**
     * 语义理解回调。
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

		@Override
		public void onResult(final UnderstanderResult result) {
			if (null != result) {
				Log.d(TAG, result.getResultString());
				
				// 显示
				String text = result.getResultString();
				if (!TextUtils.isEmpty(text)) {
					//mUnderstanderText.setText(text);
					FlowerCollector.onEvent(UnderstanderDemo.this, "tts_play");

					//String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
					// 设置参数
					setParam();

					String [] strs = text.split("text");
					String [] strs1 = strs[1].split("type");
					//String jieguo = text.substring(text.indexOf("type")+1,text.indexOf("service"));
					mUnderstanderText.setText(strs1[0]);
					int code = mTts.startSpeaking(strs1[0], mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

					if (code != ErrorCode.SUCCESS) {
						if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
							//未安装则跳转到提示安装页面
							mInstaller.install();
						}else {
							showTip("语音合成失败,错误码: " + code);
						}
					}
				}
			} else {
				showTip("识别结果不正确。");
			}	
		}
    	
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        	showTip("当前正在说话，音量大小：" + volume);
        	Log.d(TAG, data.length+"");
        }
        
        @Override
        public void onEndOfSpeech() {
        	// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        	showTip("结束说话");

        }
        
        @Override
        public void onBeginOfSpeech() {
        	// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        	showTip("开始说话");
        }

		@Override
		public void onError(SpeechError error) {

			showTip(error.getPlainDescription(true));
			test_cz();
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
    };
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        // 退出时释放连接
		is_exit=true;
    	mSpeechUnderstander.cancel();
    	mSpeechUnderstander.destroy();
    	if(mTextUnderstander.isUnderstanding())
    		mTextUnderstander.cancel();
    	mTextUnderstander.destroy();    
    }
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	public void setParam(){

		String lang = mSharedPreferences.getString("understander_language_preference", "mandarin");
		if (lang.equals("en_us")) {
			// 设置语言
			mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "en_us");
		}else {
			// 设置语言
			mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, lang);
		}
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("understander_vadbos_preference", "4000"));
		
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("understander_vadeos_preference", "1000"));
		
		// 设置标点符号，默认：1（有标点）
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("understander_punc_preference", "1"));
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/sud.wav");

	}	
	
	@Override
	protected void onResume() {
		//移动数据统计分析
		FlowerCollector.onResume(UnderstanderDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		//移动数据统计分析
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(UnderstanderDemo.this);
		super.onPause();
	}




	float gsensor_x[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	float gsensor_y[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	float gsensor_z[]={10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};
	float gravity_ar[]={10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};
	int gsensor_static_state=0;
	int last_gsensor_static_state=0;
	int yaoyiyao_counts =0;
	float last_ax=0xff;
	float last_ay=0xff;
	float last_az=0xff;
	float last_gravity=0xff;

	int ax_Direct[]={0,0,0,0,0,0,0,0,0,0};
	int ay_Direct[]={0,0,0,0,0,0,0,0,0,0};
	int az_Direct[]={0,0,0,0,0,0,0,0,0,0};
	int gravity_Direct[]={0,0,0,0,0,0,0,0,0,0};

	float ax_Direct_value[]={0,0,0,0,0,0,0,0,0,0};
	float ay_Direct_value[]={0,0,0,0,0,0,0,0,0,0};
	float az_Direct_value[]={0,0,0,0,0,0,0,0,0,0};
	float gravity_Direct_value[]={0,0,0,0,0,0,0,0,0,0};
	int last_static_state =-1;
	int last_dynamic_state =-1;
	long last_dynamic_time =0;
	long lastshaketime[] ={0,0,0};
	private void g_sensor_receiver(){
		SensorManager sm = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sm.registerListener(new SensorEventListener() {

			public void onSensorChanged(SensorEvent event) {
				if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
					return;
				}

				float[] values = event.values;
				float ax = values[0];
				float ay = values[1];
				float az = values[2];
				float gravityNew = 0;

				gravityNew = (float) Math.sqrt(values[0] * values[0]
						+ values[1] * values[1] + values[2] * values[2]);
				//Log.d(TAG, "ax="+ax +" ay="+ay +" az="+az +"  gravityNew="+gravityNew);
				calc_gsensor_data(ax,ay,az,gravityNew);

			}
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		}, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);


	}
	public void calc_gsensor_data(float ax,float ay,float az,float gravity){



		for(int i=10-1;i>0;i--){
			gsensor_x[i]= gsensor_x[i-1];
			gsensor_y[i]= gsensor_y[i-1];
			gsensor_z[i]= gsensor_z[i-1];
			gravity_ar[i]= gravity_ar[i-1];
		}
		gsensor_x[0]=ax;
		gsensor_y[0]=ay;
		gsensor_z[0]=az;
		gravity_ar[0]=gravity;
		float average_x =0;
		float average_y =0;
		float average_z =0;
		float average_gravity =0;
		for(int i=0;i<10;i++){
			average_x+=gsensor_x[i];
			average_y+=gsensor_y[i];
			average_z+=gsensor_z[i];
			average_gravity+=gravity_ar[i];
		}
		average_x =average_x/10;
		average_y =average_y/10;
		average_z =average_z/10;
		average_gravity =average_gravity/10;
		Log.d(TAG, "average_x="+average_x +" average_y="+average_y +" average_z="+average_z +"  average_gravity="+average_gravity);



		boolean is_move_state =false;
		for(int i=1;i<10;i++){
			if(Math.abs(gsensor_x[0]-gsensor_x[i])>2) {
				is_move_state= true;
				break;
			}
			if(Math.abs(gsensor_y[0]-gsensor_y[i])>2) {
				is_move_state= true;
				break;
			}
			if(Math.abs(gsensor_z[0]-gsensor_z[i])>2) {
				is_move_state= true;
				break;
			}
		}

		calc_Direct(ax,ay,az,gravity);
		calc_bigshake(gravity);
		if(calc_is_yaoyiyao()){
			is_move_state= true;
		}else if(calc_is_pickup()){
			is_move_state= true;
		}

		//is_move_state =false;
		if(is_move_state){


		}
		else {
			if ((average_z < 9.8 + 3.5) && (average_z > 9.8 - 3.5)&&(Math.abs(average_y)<1) && (average_x<6)
					||( (average_x<8)&&(Math.abs(average_y)<8.3)&&(average_z>1.5)&&(average_z<14))
					||(average_x<4.7)&&(average_x>0)&&(average_y>0)&&(average_y<8)&&(average_z>6)&&(average_z<10)) {
				//if ((average_z < 9.8 + 3.5) && (average_z > 9.8 - 3.5)&&(Math.abs(average_y)<1) && (average_x<6)) {
				//test_tts_play("站立姿势");
				gsensor_static_state = 0;
			} else if ((average_x < 9.8 + 3.5) && (average_x > 9.8 - 2) && (Math.abs(average_y) < 4.5) && (average_z > 10)
					||(average_x < 9.8 + 3.5) && (average_x > 9.8 - 2)&&(average_y>0)&&(average_y<9)&&(average_z>8.7)&&(average_z<13)
					||(average_x < 9.8 + 3.5) && (average_x > 9.8 - 2)&&(average_y<0)&&(average_y>-9)&&(average_z>7.5)&&(average_z<13)
					||(average_x<15)&&(average_x>11)&&(average_y>-3)&&(average_y<-3)&&(average_z>8)&&(average_z<13)
					) {
				gsensor_static_state = 1;
				//test_tts_play("躺下姿势");
			}else if((Math.abs(average_z)<1.6)&&(average_x<2.2)&&(Math.abs(average_y)<8)
			||(average_x<3)&&(Math.abs(average_y)<7.05)&&(average_z>-3)&&(average_z<0.9)
					||((average_x>0.5)&&(average_x<8.6)&&(average_y<3.5)&&(average_y>-3.5)&&(average_z<-2)&&(average_z>-5.9))
					){
				gsensor_static_state = 3;
				//test_tts_play("趴下");
			}else if((Math.abs(average_y)<9.8+1)&&((Math.abs(average_y)>9.8-1))
					&&((average_x<11)&&(average_x>6.2-0.6))&&((average_z>3)&&(average_z<9.8))){
				gsensor_static_state = 4;

			} else if ((average_x >= 10) && ((Math.abs(average_y) + Math.abs(average_z)) < 10)
					||(average_x >= 10) && ((Math.abs(average_y) + Math.abs(average_z)) < 10)
					||(average_x  >= 10)&&(Math.abs(average_y)<0.5)
					||(average_x  >= 13.7)&&(Math.abs(average_y)<5)
					||(average_x  >= 3)&&(average_x<10)&&(average_y  >= -7)&&(average_y<7)&&(average_z  <= -2)&&(average_z>-5.5)
					||(average_x>8)&&(average_x<10)&&(average_z>-5.75)&&(average_z<-4)&&(Math.abs(average_y)<5)) {
				gsensor_static_state = 2;
				//test_tts_play("倒立姿势");
			}
			else {
				if((average_x<8)&&(Math.abs(average_y)<9.5)&&(average_z<1.5)&&(average_z>0.8))
					gsensor_static_state= 0;

							else
				gsensor_static_state = 5;

			}
			if (last_gsensor_static_state != gsensor_static_state) {
				last_gsensor_static_state = gsensor_static_state;
				yaoyiyao_counts=0;
				Log.d(TAG, "gsensor_static_state="+gsensor_static_state);
				switch (gsensor_static_state) {
					case 0:
						send_static_state(0);
						//test_tts_play("站立");
						//Log.d(TAG, "站立姿势");

//						Toast.makeText(getApplicationContext(), "站立姿势",
//								Toast.LENGTH_SHORT).show();
						break;
					case 1:
						send_static_state(1);
						//test_tts_play("睡觉");
						//Log.d(TAG, "睡觉姿势");
//						Toast.makeText(getApplicationContext(), "躺下姿势",
//								Toast.LENGTH_SHORT).show();
						checkisfalldown();
						break;
					case 2:
						send_static_state(2);
						//test_tts_play("倒立");
						//Log.d(TAG, "倒立姿势");
//						Toast.makeText(getApplicationContext(), "倒立姿势",
//								Toast.LENGTH_SHORT).show();
						break;
					case 3:
						send_static_state(3);
						//test_tts_play("趴下");
						//Log.d(TAG, "趴下");
//						Toast.makeText(getApplicationContext(), "趴下",
//								Toast.LENGTH_SHORT).show();
						checkisfalldown();
						break;
					case 4:
						send_static_state(4);
						//test_tts_play("侧卧");
						//Log.d(TAG, "侧卧");
//						Toast.makeText(getApplicationContext(), "侧卧",
//								Toast.LENGTH_SHORT).show();
						checkisfalldown();
						break;
					case 5:
						send_static_state(5);
						//test_tts_play("未知姿势");
						//Log.d(TAG, "未知姿势");
//						Toast.makeText(getApplicationContext(), "未知姿势",
//								Toast.LENGTH_SHORT).show();
						checkisfalldown();
						break;
				}
			}
		}




	}


	public void calc_Direct(float ax,float ay,float az,float gravity){
		int ax_Direct_once;
		int ay_Direct_once;
		int az_Direct_once;
		int gravity_Direct_once;

		if(last_ax==0xff)
		{
			last_ax =ax;
			last_ay =ay;
			last_az =az;
			last_gravity = gravity;
			return;
		}

		ax_Direct_once = (last_ax<ax)?1:0;
		ay_Direct_once = (last_ay<ay)?1:0;
		az_Direct_once = (last_az<az)?1:0;
		gravity_Direct_once =  (last_gravity<gravity)?1:0;
		if(Math.abs(last_ax-ax)<0.1)
		{
			ax_Direct_once=2;
		}
		if(Math.abs(last_ay-ay)<0.1)
		{
			ay_Direct_once=2;
		}
		if(Math.abs(last_az-az)<0.1)
		{
			az_Direct_once=2;
		}
		if(Math.abs(last_gravity-gravity)<0.1)
		{
			gravity_Direct_once=2;
		}


		for(int i=10-1;i>0;i--){
			ax_Direct[i]= ax_Direct[i-1];
			ay_Direct[i]= ay_Direct[i-1];
			az_Direct[i]= az_Direct[i-1];
			gravity_Direct[i]= gravity_Direct[i-1];
			ax_Direct_value[i]= ax_Direct_value[i-1];
			ay_Direct_value[i]= ay_Direct_value[i-1];
			az_Direct_value[i]= az_Direct_value[i-1];
			gravity_Direct_value[i]= gravity_Direct_value[i-1];

		}
		ax_Direct[0]=ax_Direct_once;
		ay_Direct[0]=ay_Direct_once;
		az_Direct[0]=az_Direct_once;
		gravity_Direct[0]=gravity_Direct_once;
		ax_Direct_value[0]=ax;
		ay_Direct_value[0]=ay;
		az_Direct_value[0]=az;
		gravity_Direct_value[0]=gravity;

		last_ax =ax;
		last_ay =ay;
		last_az =az;
		last_gravity =gravity;
		if(ax_Direct_value[1]<-3)
		{
			ax_Direct_value[1]=ax_Direct_value[1];
		}
	}

	public boolean calc_is_pickup(){
		int ping_counts=0;
		for(int i=5-1;i>2;i--){
			if((ax_Direct[i-1]==0)&&((ay_Direct[i-1]==1)||(az_Direct[i-1]==1))||(gravity_Direct[i-1]==1)
					&&(ax_Direct[i-2]==1)&&((gravity_Direct[i-2]==0)||(az_Direct[i-2]==0)||(ay_Direct[i-2]==0)))
			{
				if(((ax_Direct_value[i])<0.8)
						//&&((ax_Direct_value[i-2]-ax_Direct_value[i])>0.8)
						&&(ax_Direct_value[i-1]<-2.7)
					//&&(ax_Direct_value[i-3]-ax_Direct_value[i-2]<5)//防止跟摇一摇冲突
						)
				{
					for(int j=10-1;j>5-1;j--)
					{
						if(ax_Direct[j]==2||ay_Direct[j]==2||az_Direct[j]==2)
							ping_counts++;

					}
					if((ping_counts>=4)&&(gsensor_static_state==0)) {
						//test_tts_play("提气");
						send_dynamic_state(2);
						return true;
					}
				}
			}
		}
		return false;
	}
	public boolean calc_is_yaoyiyao(){
		int counts_z=0;
		int counts_y=0;
		for(int i=9;i>0;i--)
		{
			if(Math.abs(gsensor_z[i]-gsensor_z[i-1])>20)
			{
				counts_z++;
			}else
			if(i-2>0){
				if(Math.abs(gsensor_z[i]-gsensor_z[i-2])>20)
				{
					counts_z++;
				}
			}

			if(Math.abs(gsensor_y[i]-gsensor_y[i-1])>20)
			{
				counts_y++;
			}else
			if(i-2>0){
				if(Math.abs(gsensor_y[i]-gsensor_y[i-2])>20)
				{
					counts_y++;
				}
			}

			if((counts_z>=4)||(counts_y>=4))
			{
				//test_tts_play("药药");
				send_dynamic_state(0);
				return true;
			}

		}
		return false;
	}
	public void send_static_state(int state)
	{
		Intent intentBc =null;

		if(last_static_state!=state){

			last_static_state = state;
			Log.d(TAG, "send_static_state ="+state);
			switch(state){
				case 0:
					intentBc = new Intent("com.auric.intell.xld.rbt.stand");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 1:
					intentBc = new Intent("com.auric.intell.xld.rbt.liedown");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 2:
					intentBc = new Intent("com.auric.intell.xld.rbt.overturn");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 3:
					intentBc = new Intent("com.auric.intell.xld.rbt.paxia");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 4:
					intentBc = new Intent("com.auric.intell.xld.rbt.cewo");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 5:
					intentBc = new Intent("com.auric.intell.xld.rbt.unknow");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;


			}
		}

	}

	public void send_dynamic_state(int state)
	{
		Intent intentBc =null;

		if(last_dynamic_state == state)
		{
			long currtime = System.currentTimeMillis();
			if(currtime - last_dynamic_time>2000){
				last_dynamic_time = currtime;
				last_dynamic_state =-1;
			}
		}
		if(last_dynamic_state!=state){
			last_dynamic_state = state;
			Log.d(TAG, "send_dynamic_state ="+state);
			switch(state){
				case 0:
					intentBc = new Intent("com.auric.intell.xld.rbt.shake");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 1:
					intentBc = new Intent("com.auric.intell.xld.rbt.trip");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;
				case 2:
					intentBc = new Intent("com.auric.intell.xld.rbt.takeup");
					intentBc.putExtra("data", "hello");
					sendBroadcast(intentBc);
					break;


			}
		}

	}

	public void calc_bigshake(float gravity){
		float shake_value =16;
		//Log.d(TAG, "calc_bigshake =");
		//for(int i=9;i>0;i--) {
		int i =1;
		if((Math.abs(gsensor_x[i]-gsensor_x[i-1])>shake_value)||(Math.abs(gsensor_y[i]-gsensor_y[i-1])>shake_value)
				||(Math.abs(gsensor_z[i]-gsensor_z[i-1])>shake_value)||(Math.abs(gravity_ar[i]-gravity_ar[i-1])>shake_value))
		{
			for(int j=3-1;j>0;j--)
			{
				lastshaketime[j]=lastshaketime[j-1];
			}
			lastshaketime[0] = System.currentTimeMillis();

		}
		//}

	}
	public void checkisfalldown(){

		long currtime = System.currentTimeMillis();
		for(int i=0;i< 3;i++)
		{

			if(currtime-lastshaketime[i]<1000)
			{
				//test_tts_play("摔倒");
				send_dynamic_state(1);

			}

		}

	}
}
