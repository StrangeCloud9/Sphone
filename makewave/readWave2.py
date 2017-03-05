# -*- coding: utf-8 -*-
import wave
import pylab as pl
import numpy as np

# 打开WAV文档
f = wave.open(r"speaker_10hz.wav", "rb")

# 读取格式信息
# (nchannels, sampwidth, framerate, nframes, comptype, compname)
params = f.getparams()
nchannels, sampwidth, framerate, nframes = params[:4]
print (params[:4])
# 读取波形数据
str_data = f.readframes(nframes)
f.close()

#将波形数据转换为数组
wave_data = np.fromstring(str_data, dtype=np.short)
print (len(wave_data))
#wave_data = wave_data[60000:65000]

if(nchannels ==2):
	wave_data.shape = -1, 2
	print ("its double channels")
else :
	#wave_data.shape = -1,1
	print ("its single channels")
wave_data = wave_data.T
time = np.arange(0, nframes) * (1.0 / framerate)
# 绘制波形
if (nchannels ==2):
	pl.subplot(211) 
	pl.plot(time, wave_data[0])
	pl.subplot(212) 
	pl.plot(time, wave_data[1], c="g")
	pl.xlabel("time (seconds)")
	pl.show()
else :
	#pl.subplot(111) 
	pl.plot(time[80000:81000], wave_data[80000:81000])
	pl.xlabel("time (seconds)")
	pl.show()