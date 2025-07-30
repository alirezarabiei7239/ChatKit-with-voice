package com.stfalcon.chatkit.sample.features.demo.custom.media.holders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.utils.FormatUtils;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.stfalcon.chatkit.utils.VoicePlayer;

/*
 * Created by troy379 on 05.04.17.
 */
public class OutcomingVoiceMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message>
        implements VoicePlayer.VoicePlaybackListener {

    private TextView tvDuration;
    private TextView tvTime;
    private ImageButton playButton;
    private VoicePlayer voicePlayer;
    private boolean isPlaying = false;
    private String voiceFilePath;

    public OutcomingVoiceMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        tvDuration = itemView.findViewById(R.id.duration);
        tvTime = itemView.findViewById(R.id.time);
        playButton = itemView.findViewById(R.id.voicePlayButton);
        
        if (playButton == null) {
            // Fallback to default play button if custom layout doesn't have voicePlayButton
            playButton = itemView.findViewById(android.R.id.button1);
        }
        
        voicePlayer = new VoicePlayer();
        voicePlayer.setVoicePlaybackListener(this);
        
        if (playButton != null) {
            playButton.setOnClickListener(this::onPlayButtonClick);
        }
    }
    
    private void onPlayButtonClick(View view) {
        if (voiceFilePath != null) {
            if (isPlaying) {
                voicePlayer.stop();
            } else {
                voicePlayer.play(voiceFilePath);
            }
        }
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        
        if (message.getVoice() != null) {
            voiceFilePath = message.getVoice().getUrl();
            tvDuration.setText(
                    FormatUtils.getDurationString(
                            message.getVoice().getDuration()));
        }
        
        tvTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
        updatePlayButton();
    }
    
    private void updatePlayButton() {
        if (playButton != null) {
            if (isPlaying) {
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play);
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
    }
}
