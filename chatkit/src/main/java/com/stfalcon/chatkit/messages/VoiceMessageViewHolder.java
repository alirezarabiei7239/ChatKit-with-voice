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

package com.stfalcon.chatkit.messages;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stfalcon.chatkit.R;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.utils.VoicePlayer;

/**
 * Base class for voice message view holders
 */
public abstract class VoiceMessageViewHolder<MESSAGE extends IMessage> 
        extends MessageHolders.BaseMessageViewHolder<MESSAGE>
        implements VoicePlayer.VoicePlaybackListener {
    
    protected ImageButton playButton;
    protected TextView durationText;
    protected VoicePlayer voicePlayer;
    protected String voiceFilePath;
    protected boolean isPlaying = false;
    
    public VoiceMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        playButton = itemView.findViewById(R.id.voicePlayButton);
        durationText = itemView.findViewById(R.id.voiceDuration);
        
        // Initialize voice player
        voicePlayer = new VoicePlayer();
        voicePlayer.setVoicePlaybackListener(this);
        
        if (playButton != null) {
            playButton.setOnClickListener(this::onPlayButtonClick);
        }
    }

    private void onPlayButtonClick(View view) {
        Log.d("VoiceMessageViewHolder", "Play button clicked. Currently playing: " + isPlaying + ", file path: " + voiceFilePath);
        
        if (voiceFilePath == null || voiceFilePath.isEmpty()) {
            Log.w("VoiceMessageViewHolder", "No voice file path available");
            return;
        }
        
        if (!isPlaying) {
            Log.d("VoiceMessageViewHolder", "Starting playback");
            startPlayback();
        } else {
            Log.d("VoiceMessageViewHolder", "Stopping playback");
            stopPlayback();
        }
    }
    

    
    protected void startPlayback() {
        Log.d("VoiceMessageViewHolder", "startPlayback called with file: " + voiceFilePath);
        if (voiceFilePath != null && !voiceFilePath.isEmpty()) {
            voicePlayer.play(voiceFilePath);
        } else {
            Log.e("VoiceMessageViewHolder", "Cannot start playback - invalid file path");
        }
    }
    
    protected void stopPlayback() {
        voicePlayer.stop();
    }
    
    protected void setVoiceData(String filePath, int duration) {
        this.voiceFilePath = filePath;
        updateDurationText(duration);
        updatePlayButton();
    }
    
    protected void updateDurationText(int durationSeconds) {
        if (durationText != null) {
            int minutes = durationSeconds / 60;
            int seconds = durationSeconds % 60;
            durationText.setText(String.format("%d:%02d", minutes, seconds));
        }
    }
    
    protected void updatePlayButton() {
        if (playButton != null) {
            if (isPlaying) {
                playButton.setImageResource(R.drawable.ic_pause);
            } else {
                playButton.setImageResource(R.drawable.ic_play);
            }
        }
    }
    
    @Override
    public void onPlaybackStarted() {
        isPlaying = true;
        updatePlayButton();
    }
    
    @Override
    public void onPlaybackCompleted() {
        isPlaying = false;
        updatePlayButton();
    }
    
    @Override
    public void onPlaybackError(Exception error) {
        isPlaying = false;
        updatePlayButton();
        // Handle error - maybe show a toast or log
    }
    
    // Clean up when view holder is recycled
    public void onViewRecycled() {
        if (voicePlayer != null) {
            voicePlayer.stop();
        }
        isPlaying = false;
    }
}