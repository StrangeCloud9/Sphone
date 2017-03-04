# Sphone
## 两周内目标

### 第二周进行声波实验
**初步目标，制造模拟信号，进行采样，zigbee和OFDM的实验，保证声波信号理论可行**
**实验结果不是很好，10HZ的信号都不是很稳，可能是没有用滤波？**
### 第一周内完成android两个麦克风同时接收不同音频的功能

  **3.1** 完成同时音频验证，确实是两个不同的音频，接下来进行初步的二维定位
  
  ![Markdown](http://p1.bqimg.com/586990/18eec68594bacdd8.png)

  
  **2.27** 初步完成app， 实现了在播放超声波的同时，两个麦克风同时接收。接下来需要对音频进行验证。
  
  **2.25** 简单app运行成功，需要注意华为手机必须得用手机助手才能看见里面的音频文件！！一直以来以为是代码问题！！
  
  **2.24** 添加了简单的app代码 
  
  new!
    今天以为下了一个代码就可以了，但是关于双声道操作的话，其他现成的代码可能使用的录制方式不一样，还是要自己写一个。明天写。
    
  new! 
    不需要使用两个AudioRecord对象，只要一个，用双声道录音即可。
    private int sampleRateInHz = 48000;

    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;

    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;

    recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
        channelConfig, encodingBitrate);

    playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
        channelConfig, encodingBitrate);

    audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
        sampleRateInHz, channelConfig, encodingBitrate, recBufSize);

读取数据的时候每个Sample会有两个样本点，分别对应两个声道。具体确定是那两个MIC的数据需要root手机，修改手机配置文件中mixer_paths.xml（或类似文件）中的音频DSP混音信息。在某些机型上，调用返回的两个声道的声音可能是完全一样的。这时，有可能需要利用系统自带的录音机录一次双声道的音（三星自己的interview模式），这时就有可能通过调用audio record获取两个不同麦克风的数据。另外，三星有部分手机型号，如S3是很难录到高频的超声信号的。

