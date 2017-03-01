package com.example.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
	private static final int RECORDER_BPP = 16;
//private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder2";    //默认录音文件的存储位置
private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
private static int frequency = 48000;
private static int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;//单声道
private static int EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;    //音频数据格式：脉冲编码调制（PCM）每个样品16位
private AudioRecord audioRecord = null;
private int recBufSize = 0;
private Thread recordingThread = null;
private boolean isRecording = false;
private Button start1,stop1,play;
private TextView txt;
private String ChosenFile,filePath;
private MediaPlayer mp;
    private List<String> items =new ArrayList<String>();  ;
    ArrayAdapter<String> adapter;
@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start1=(Button)findViewById(R.id.start);
		stop1=(Button)findViewById(R.id.stop);
		txt=(TextView)findViewById(R.id.textView1);
        play = (Button)findViewById(R.id.play);
    mp = new MediaPlayer();



    ChosenFile = "none";
    txt.setText(ChosenFile);

    RuntimePermissionsManager runtimePermissionsManager= new RuntimePermissionsManager(MainActivity.this);
    runtimePermissionsManager.requestPermission("android.permission.READ_EXTERNAL_STORAGE");
    boolean permission = runtimePermissionsManager.checkPermission("android.permission.READ_EXTERNAL_STORAGE");
    if (permission) {
        Log.e("onCreate","has permission");

    }else {
        Log.e("onCreate","no permission");
    }

     filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    filePath = filePath+"/AudioRecorder2";

    Log.e("onCreate file path is",filePath);
    File folder = new File(filePath);
    String[] fileNames = folder.list();
    if(fileNames!= null){
        int count = fileNames.length;
        Log.e("there is file ",String.valueOf(count));
        for (int i =0;i<count;i++){
            if(fileNames[i]!=null)
                items.add(fileNames[i]);
        }
    }
    else {
        Log.e("???","files is null");

    }
    adapter = new ArrayAdapter<String>(
            MainActivity.this,android.R.layout.simple_list_item_1,items);
    final ListView listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(adapter);




    listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?>parent, View view, int position, long id){
            Toast.makeText(MainActivity.this,items.get(position), Toast.LENGTH_SHORT).show();
            ChosenFile = items.get(position);
            txt.setText(ChosenFile);
            try {

                Log.e("mp.setDataSource ",filePath+"/"+ChosenFile);
                mp.setDataSource(filePath+"/"+ChosenFile);
                Log.e("now prepare  ",filePath+"/"+ChosenFile);

            }catch (IOException e) {
                e.printStackTrace();
            }
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
    });



		start1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

                try{mp.prepare();}
                catch (IOException e) {
                    e.printStackTrace();
                }

				startRecord();
                mp.start();
				txt.setText("錄音中");
			}
		});		
		stop1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopRecord();
				txt.setText("結束了");
				mp.stop();
			}
		});
        play.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e("mp.start()","now start()");
                mp.start();
            }
        });
}


private String getFilename(){
      String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
      File file = new File(filepath,AUDIO_RECORDER_FOLDER);
      Log.e("getFilename file is",filepath);
      if(file.exists()){
          Log.e("getFilename","exist");
        file.delete();
      }
    Log.e("getFilename",file.getAbsolutePath() + "/speaker.wav");
      return (file.getAbsolutePath() + "/speaker.wav" );
}

private String getTempFilename(){
      String filepath = Environment.getExternalStorageDirectory().getPath();
    Log.e("getTempFilename",filepath);

      File file = new File(filepath,AUDIO_RECORDER_FOLDER);
      
      if(!file.exists()){
            Log.e("getTempFilename","not exist ,create");
              file.mkdirs();
      }
    else {
          Log.e("getTempFilename","exist");
      }
      
      File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
      
      if(tempFile.exists()){
              tempFile.delete();
          Log.e("getTempFilename","tempfile exist and delete it");
      }

      Log.e("getTempFilename",file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);

    return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
}

private void startRecord(){
      
createAudioRecord();
audioRecord.startRecording();
      
      isRecording = true;
      
      recordingThread = new Thread(new Runnable() {
              public void run() {
                      writeAudioDataToFile();
              }
      },"AudioRecorder Thread");
      
      recordingThread.start();
}

private void writeAudioDataToFile(){
      byte data[] = new byte[recBufSize];
      String filename = getTempFilename();
      FileOutputStream os = null;
      
      try {
              os = new FileOutputStream(filename);
      } catch (FileNotFoundException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
      }
      
      int read = 0;
      
      if(null != os){
              while(isRecording){
                      read = audioRecord.read(data, 0, recBufSize);
                      
                      if(AudioRecord.ERROR_INVALID_OPERATION != read){
                              try {
                                      os.write(data);
                              } catch (IOException e) {
                                      e.printStackTrace();
                              }
                      }
              }
              
              try {
                      os.close();
              } catch (IOException e) {
                      e.printStackTrace();
              }
      }
}

private void stopRecord(){
      if(null != audioRecord){
              isRecording = false;
              Log.e("stopRecord","now stop");
              audioRecord.stop();
              audioRecord.release();
              
              audioRecord = null;
              recordingThread = null;
      }
      Log.e("stopRecord","now copy");
      copyWaveFile(getTempFilename(),getFilename());
      deleteTempFile();
    File file = new File(getTempFilename());
    if(file.exists()){
        Log.e("stopRecord","temp is still here "+file.getAbsolutePath());

    }
    file = new File (getFilename());
    if(file.exists()){
        Log.e("stopRecord","file is still here "+file.getAbsolutePath());

    }
}

private void deleteTempFile() {
      File file = new File(getTempFilename());
      if(file.exists()){
          Log.e("deleteTempFile","the temp is real");
      }
      //file.delete();
}

private void copyWaveFile(String inFilename,String outFilename){
      FileInputStream in = null;
      FileOutputStream out = null;
      long totalAudioLen = 0;
      long totalDataLen = totalAudioLen + 36;
      long longSampleRate = frequency;
      int channels = 1;
      long byteRate = RECORDER_BPP * frequency * channels/8;
      
      byte[] data = new byte[recBufSize];
      
      try {
              in = new FileInputStream(inFilename);
              out = new FileOutputStream(outFilename);
              totalAudioLen = in.getChannel().size();
              totalDataLen = totalAudioLen + 36;
              
              //AppLog.logString("File size: " + totalDataLen);
              
              WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                              longSampleRate, channels, byteRate);
              
              while(in.read(data) != -1){
                      out.write(data);
              }
              
              in.close();
              out.close();
          Log.e("copyWaveFile","write over");
      } catch (FileNotFoundException e) {
              e.printStackTrace();
      } catch (IOException e) {
              e.printStackTrace();
      }
}

private void WriteWaveFileHeader(
              FileOutputStream out, long totalAudioLen,
              long totalDataLen, long longSampleRate, int channels,
              long byteRate) throws IOException {
      
      byte[] header = new byte[44];
      
      header[0] = 'R';  // RIFF/WAVE header
      header[1] = 'I';
      header[2] = 'F';
      header[3] = 'F';
      header[4] = (byte) (totalDataLen & 0xff);
      header[5] = (byte) ((totalDataLen >> 8) & 0xff);
      header[6] = (byte) ((totalDataLen >> 16) & 0xff);
      header[7] = (byte) ((totalDataLen >> 24) & 0xff);
      header[8] = 'W';
      header[9] = 'A';
      header[10] = 'V';
      header[11] = 'E';
      header[12] = 'f';  // 'fmt ' chunk
      header[13] = 'm';
      header[14] = 't';
      header[15] = ' ';
      header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
      header[17] = 0;
      header[18] = 0;
      header[19] = 0;
      header[20] = 1;  // format = 1
      header[21] = 0;
      header[22] = (byte) channels;
      header[23] = 0;
      header[24] = (byte) (longSampleRate & 0xff);
      header[25] = (byte) ((longSampleRate >> 8) & 0xff);
      header[26] = (byte) ((longSampleRate >> 16) & 0xff);
      header[27] = (byte) ((longSampleRate >> 24) & 0xff);
      header[28] = (byte) (byteRate & 0xff);
      header[29] = (byte) ((byteRate >> 8) & 0xff);
      header[30] = (byte) ((byteRate >> 16) & 0xff);
      header[31] = (byte) ((byteRate >> 24) & 0xff);
      header[32] = (byte) (1 * 16 / 8);  // block align
      header[33] = 0;
      header[34] = RECORDER_BPP;  // bits per sample
      header[35] = 0;
      header[36] = 'd';
      header[37] = 'a';
      header[38] = 't';
      header[39] = 'a';
      header[40] = (byte) (totalAudioLen & 0xff);
      header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
      header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
      header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
      out.write(header, 0, 44);
}
public void createAudioRecord(){
recBufSize = AudioRecord.getMinBufferSize(frequency,
channelConfiguration, EncodingBitRate);

audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
channelConfiguration, EncodingBitRate, recBufSize); 
System.out.println("AudioRecord成功");
}

    @Override
    protected void onDestroy() {
        if(mp != null)
            mp.release();
        super.onDestroy();
    }

}
