package com.stfalcon.chatkit.sample.features.demo.custom.media;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.sample.R;

import android.util.Log;
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity;
import com.stfalcon.chatkit.sample.utils.AppUtils;
import com.stfalcon.chatkit.utils.VoiceRecorder;

import java.util.Date;

/**
 * Demo activity showing voice message functionality
 */
public class VoiceMessagesActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener,
        MessageInput.VoiceRecordingListener,
        VoiceRecorder.VoiceRecordingListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1001;
    private VoiceRecorder voiceRecorder;

    public static void open(Context context) {
        context.startActivity(new Intent(context, VoiceMessagesActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_messages);
        
        initAdapter();
        loadMessages();
        
        // Set up voice recording
        if (checkPermissions()) {
            setupVoiceRecording();
        } else {
            requestPermissions();
        }
    }

    private void initAdapter() {
        super.messagesAdapter = new VoiceMessagesAdapter(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        
        MessagesList messagesList = findViewById(R.id.messagesList);
        messagesList.setAdapter(super.messagesAdapter);
        
        // Set up input listeners
        MessageInput messageInput = findViewById(R.id.input);
        messageInput.setInputListener(this);
        messageInput.setTypingListener(this);
        messageInput.setAttachmentsListener(this);
    }

    protected void loadMessages() {
        for (Message message : MessagesFixtures.getVoiceMessages()) {
            super.messagesAdapter.addToStart(message, true);
        }
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(
                new Message("1", MessagesFixtures.getUser(), input.toString(), new Date()),
                true);
        return true;
    }

    @Override
    public void onAddAttachments() {
        AppUtils.showToast(this, "Attachments!", false);
    }

    @Override
    public void onStartTyping() {
        // Handle typing indicator if needed
    }

    @Override
    public void onStopTyping() {
        // Handle typing indicator if needed
    }

    // Voice recording callbacks from MessageInput
    @Override
    public void onStartRecording() {
        Log.d("VoiceMessagesActivity", "onStartRecording called from MessageInput");
        if (voiceRecorder != null) {
            voiceRecorder.startRecording();
            AppUtils.showToast(this, "Recording started...", false);
        } else {
            Log.e("VoiceMessagesActivity", "VoiceRecorder is null!");
        }
    }

    @Override
    public void onStopRecording() {
        Log.d("VoiceMessagesActivity", "onStopRecording called from MessageInput");
        if (voiceRecorder != null) {
            voiceRecorder.stopRecording();
        } else {
            Log.e("VoiceMessagesActivity", "VoiceRecorder is null!");
        }
    }

    // Voice recording callbacks from VoiceRecorder
    @Override
    public void onRecordingStarted() {
        // Visual feedback that recording started
    }

    @Override
    public void onRecordingFinished(String filePath, long duration) {
        // Create and send voice message
        Message voiceMessage = new Message("voice_" + System.currentTimeMillis(), 
                MessagesFixtures.getUser(), "", new Date());
        voiceMessage.setVoice(new Message.Voice(filePath, (int) (duration / 1000)));
        
        super.messagesAdapter.addToStart(voiceMessage, true);
        AppUtils.showToast(this, "Voice message sent!", false);
    }

    @Override
    public void onRecordingError(Exception error) {
        AppUtils.showToast(this, "Recording failed: " + error.getMessage(), false);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    private void setupVoiceRecording() {
        voiceRecorder = new VoiceRecorder(this);
        voiceRecorder.setVoiceRecordingListener(this);
        
        // Set up the voice recording listener on MessageInput
        MessageInput messageInput = findViewById(R.id.input);
        if (messageInput != null) {
            messageInput.setVoiceRecordingListener(this);
            Log.d("VoiceMessagesActivity", "Voice recording listener set on MessageInput");
        } else {
            Log.e("VoiceMessagesActivity", "MessageInput not found!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupVoiceRecording();
            } else {
                Toast.makeText(this, "Voice recording permission is required for voice messages", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceRecorder != null && voiceRecorder.isRecording()) {
            voiceRecorder.stopRecording();
        }
    }
}