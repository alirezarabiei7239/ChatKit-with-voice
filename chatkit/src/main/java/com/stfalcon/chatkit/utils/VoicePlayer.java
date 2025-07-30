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

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for playing voice messages
 */
public class VoicePlayer {
    private static final String TAG = "VoicePlayer";
    
    private MediaPlayer mediaPlayer;
    private VoicePlaybackListener listener;
    
    public interface VoicePlaybackListener {
        void onPlaybackStarted();
        void onPlaybackCompleted();
        void onPlaybackError(Exception error);
    }
    
    public VoicePlayer() {
        // Constructor
    }
    
    public void setVoicePlaybackListener(VoicePlaybackListener listener) {
        this.listener = listener;
    }
    
    /**
     * Play voice message from file path
     */
    public void play(String filePath) {
        Log.d(TAG, "play() called with filePath: " + filePath);
        
        if (filePath == null || filePath.isEmpty() || !new File(filePath).exists()) {
            Log.e(TAG, "Invalid or non-existent file path: " + filePath);
            if (listener != null) {
                listener.onPlaybackError(new IllegalArgumentException("Invalid file path"));
            }
            return;
        }
        
        // Stop any existing playback
        stop();
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared, starting playback");
                mp.start();
                if (listener != null) {
                    listener.onPlaybackStarted();
                }
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                if (listener != null) {
                    listener.onPlaybackCompleted();
                }
                stop();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                if (listener != null) {
                    listener.onPlaybackError(new Exception("MediaPlayer error: " + what));
                }
                stop();
                return true;
            });
            
            mediaPlayer.prepareAsync();
            
        } catch (IOException e) {
            Log.e(TAG, "IOException while setting data source", e);
            if (listener != null) {
                listener.onPlaybackError(e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during playback", e);
            if (listener != null) {
                listener.onPlaybackError(e);
            }
        }
    }
    
    /**
     * Stop playback
     */
    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping MediaPlayer", e);
            }
            mediaPlayer = null;
        }
    }
    
    /**
     * Pause playback
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    
    /**
     * Resume playback
     */
    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
    
    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    /**
     * Get current playback position
     */
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    
    /**
     * Get total duration
     */
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
    
    /**
     * Release resources
     */
    public void release() {
        stop();
    }
}