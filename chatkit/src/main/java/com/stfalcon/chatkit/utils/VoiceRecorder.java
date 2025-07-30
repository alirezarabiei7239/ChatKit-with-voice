/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for recording voice messages
 */
public class VoiceRecorder {
    
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private long startTime;
    private boolean isRecording = false;
    private VoiceRecordingListener listener;
    
    public interface VoiceRecordingListener {
        void onRecordingStarted();
        void onRecordingFinished(String filePath, long duration);
        void onRecordingError(Exception error);
    }
    
    public VoiceRecorder(Context context) {
        // Create audio file path
        File audioDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voice_messages");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        audioFilePath = new File(audioDir, "voice_" + System.currentTimeMillis() + ".3gp").getAbsolutePath();
    }
    
    public void setVoiceRecordingListener(VoiceRecordingListener listener) {
        this.listener = listener;
    }
    
    public void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            startTime = System.currentTimeMillis();
            isRecording = true;
            
            if (listener != null) {
                listener.onRecordingStarted();
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onRecordingError(e);
            }
        }
    }
    
    public void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                
                long duration = System.currentTimeMillis() - startTime;
                isRecording = false;
                
                if (listener != null) {
                    listener.onRecordingFinished(audioFilePath, duration);
                }
            } catch (RuntimeException e) {
                // Recording was too short or other issue
                if (listener != null) {
                    listener.onRecordingError(e);
                }
            }
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public String getAudioFilePath() {
        return audioFilePath;
    }
    
    public long getCurrentDuration() {
        if (isRecording) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
}