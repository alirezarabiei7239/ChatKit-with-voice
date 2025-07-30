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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.stfalcon.chatkit.R;

import java.lang.reflect.Field;

/**
 * Component for input outcoming messages
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MessageInput extends RelativeLayout
        implements View.OnClickListener, TextWatcher, View.OnFocusChangeListener, View.OnTouchListener {

    protected EditText messageInput;
    protected ImageButton messageSendButton;
    protected ImageButton voiceRecordButton;
    protected ImageButton attachmentButton;
    protected Space sendButtonSpace, attachmentButtonSpace;

    private CharSequence input;
    private InputListener inputListener;
    private AttachmentsListener attachmentsListener;
    private VoiceRecordingListener voiceRecordingListener;
    
    // Animation, sound effects, and haptic feedback
    private ScaleAnimation pulseAnimation;
    private ToneGenerator toneGenerator;
    private Vibrator vibrator;
    private MediaPlayer beepPlayer;
    
    // Recording duration tracking
    private long recordingStartTime;
    private static final int MIN_RECORDING_DURATION_MS = 1000; // 1 second minimum
    private boolean isTyping;
    private TypingListener typingListener;
    private int delayTypingStatusMillis;
    private boolean isRecordingVoice = false;
    private Runnable typingTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTyping) {
                isTyping = false;
                if (typingListener != null) typingListener.onStopTyping();
            }
        }
    };
    private boolean lastFocus;

    public MessageInput(Context context) {
        super(context);
        init(context);
    }

    public MessageInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanup();
    }

    public MessageInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Sets callback for 'submit' button.
     *
     * @param inputListener input callback
     */
    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    /**
     * Sets callback for 'add' button.
     *
     * @param attachmentsListener input callback
     */
    public void setAttachmentsListener(AttachmentsListener attachmentsListener) {
        this.attachmentsListener = attachmentsListener;
    }

    /**
     * Sets callback for voice recording functionality.
     *
     * @param voiceRecordingListener voice recording callback
     */
    public void setVoiceRecordingListener(VoiceRecordingListener voiceRecordingListener) {
        this.voiceRecordingListener = voiceRecordingListener;
    }

    /**
     * Returns EditText for messages input
     *
     * @return EditText
     */
    public EditText getInputEditText() {
        return messageInput;
    }

    /**
     * Returns `submit` button
     *
     * @return ImageButton
     */
    public ImageButton getButton() {
        return messageSendButton;
    }

    /**
     * Returns voice record button
     *
     * @return ImageButton
     */
    public ImageButton getVoiceButton() {
        return voiceRecordButton;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.messageSendButton) {
            boolean isSubmitted = onSubmit();
            if (isSubmitted) {
                messageInput.setText("");
            }
            removeCallbacks(typingTimerRunnable);
            post(typingTimerRunnable);
        } else if (id == R.id.attachmentButton) {
            onAddAttachments();
        }
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start have just replaced old text that had length before
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        input = s;
        messageSendButton.setEnabled(input.length() > 0);
        
        // Switch between voice and send button based on text input
        if (s.length() > 0) {
            // Show send button when there's text
            showSendButton();
            if (!isTyping) {
                isTyping = true;
                if (typingListener != null) typingListener.onStartTyping();
            }
            removeCallbacks(typingTimerRunnable);
            postDelayed(typingTimerRunnable, delayTypingStatusMillis);
        } else {
            // Show voice button when there's no text
            showVoiceButton();
        }
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start are about to be replaced by new text with length after.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //do nothing
    }

    /**
     * This method is called to notify you that, somewhere within s, the text has been changed.
     */
    @Override
    public void afterTextChanged(Editable editable) {
        //do nothing
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (lastFocus && !hasFocus && typingListener != null) {
            typingListener.onStopTyping();
        }
        lastFocus = hasFocus;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("MessageInput", "onTouch called for view: " + v.getId());
        if (v.getId() == R.id.voiceRecordButton) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("MessageInput", "ACTION_DOWN - Starting voice recording");
                    // Start voice recording
                    startVoiceRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("MessageInput", "ACTION_UP/CANCEL - Stopping voice recording");
                    // Stop voice recording and send
                    stopVoiceRecording();
                    return true;
            }
        }
        return false;
    }

    private boolean onSubmit() {
        return inputListener != null && inputListener.onSubmit(input);
    }

    private void onAddAttachments() {
        if (attachmentsListener != null) attachmentsListener.onAddAttachments();
    }

    private void startVoiceRecording() {
        Log.d("MessageInput", "startVoiceRecording called. Listener: " + (voiceRecordingListener != null) + ", isRecording: " + isRecordingVoice);
        if (voiceRecordingListener != null && !isRecordingVoice) {
            isRecordingVoice = true;
            
            // Record start time for duration validation
            recordingStartTime = System.currentTimeMillis();
            Log.d("MessageInput", "Recording start time: " + recordingStartTime);
            
            // Play start beep sound + haptic feedback
            playBeepSound();
            performHapticFeedback();
            
            // Start pulsing animation
            startRecordingAnimation();
            
            voiceRecordingListener.onStartRecording();
            Log.d("MessageInput", "Voice recording started with beep and vibration");
        } else {
            Log.w("MessageInput", "Cannot start recording - listener: " + (voiceRecordingListener != null) + ", already recording: " + isRecordingVoice);
        }
    }

    private void stopVoiceRecording() {
        Log.d("MessageInput", "stopVoiceRecording called. Listener: " + (voiceRecordingListener != null) + ", isRecording: " + isRecordingVoice);
        if (voiceRecordingListener != null && isRecordingVoice) {
            isRecordingVoice = false;
            
            // Calculate recording duration
            long recordingEndTime = System.currentTimeMillis();
            long recordingDuration = recordingEndTime - recordingStartTime;
            Log.d("MessageInput", "Recording duration: " + recordingDuration + "ms");
            
            // Stop pulsing animation
            stopRecordingAnimation();
            
            // Play stop beep sound + haptic feedback
            playBeepSound();
            performHapticFeedback();
            
            // Check if recording is long enough
            if (recordingDuration < MIN_RECORDING_DURATION_MS) {
                // Recording too short - show toast and don't send
                Log.w("MessageInput", "Recording too short (" + recordingDuration + "ms), not sending");
                showRecordingTooShortToast();
                // Don't call onStopRecording() - this prevents sending the message
            } else {
                // Recording long enough - proceed with sending
                Log.d("MessageInput", "Recording duration OK (" + recordingDuration + "ms), sending message");
                voiceRecordingListener.onStopRecording();
            }
            
            Log.d("MessageInput", "Voice recording stopped with beep and vibration");
        }
    }
    
    private void showRecordingTooShortToast() {
        Toast.makeText(getContext(), 
                "Voice message too short! Hold for at least 1 second to record.", 
                Toast.LENGTH_SHORT).show();
    }

    private void showSendButton() {
        messageSendButton.setVisibility(VISIBLE);
        voiceRecordButton.setVisibility(GONE);
        // Update space reference to point to send button
        updateSpaceReferences(true);
    }

    private void showVoiceButton() {
        messageSendButton.setVisibility(GONE);
        voiceRecordButton.setVisibility(VISIBLE);
        // Update space reference to point to voice button
        updateSpaceReferences(false);
    }
    
    private void updateSpaceReferences(boolean showingSendButton) {
        // With the new FrameLayout approach, we don't need complex space management
        // Just ensure the layout is refreshed
        if (messageInput != null) {
            messageInput.requestLayout();
        }
        requestLayout();
    }
    
    private void initializeSoundAndAnimation() {
        try {
            // Initialize vibrator for haptic feedback
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            Log.d("MessageInput", "Vibrator initialized: " + (vibrator != null));
            
            // Initialize tone generator for beep sounds
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                Log.d("MessageInput", "ToneGenerator initialized successfully");
            } catch (RuntimeException e) {
                Log.w("MessageInput", "ToneGenerator initialization failed, will try alternatives during playback", e);
                toneGenerator = null;
            }
            
            // Create pulsing animation - bigger scale so it's visible around finger
            pulseAnimation = new ScaleAnimation(
                1.0f, 1.5f,  // X scale from 100% to 150% (bigger for visibility)
                1.0f, 1.5f,  // Y scale from 100% to 150% (bigger for visibility)
                Animation.RELATIVE_TO_SELF, 0.5f,  // pivot X at center
                Animation.RELATIVE_TO_SELF, 0.5f   // pivot Y at center
            );
            pulseAnimation.setDuration(600);  // Faster pulse (600ms instead of 800ms)
            pulseAnimation.setRepeatCount(Animation.INFINITE);  // Repeat infinitely
            pulseAnimation.setRepeatMode(Animation.REVERSE);  // Scale back and forth
            
        } catch (Exception e) {
            Log.w("MessageInput", "Failed to initialize sound/animation", e);
        }
    }
    
    private void initializeBeepPlayer() {
        // We'll create a programmatic beep using ToneGenerator instead
        // This is a fallback method, but we'll keep ToneGenerator as primary
        Log.d("MessageInput", "BeepPlayer fallback - will use ToneGenerator with different parameters");
    }
    
    private void playBeepSound() {
        Log.d("MessageInput", "playBeepSound called");
        try {
            if (toneGenerator != null) {
                // Try different tones for better audibility
                Log.d("MessageInput", "Playing beep with ToneGenerator");
                boolean success = toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
                if (!success) {
                    // Try alternative tone
                    success = toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
                    Log.d("MessageInput", "Alternative tone result: " + success);
                }
                Log.d("MessageInput", "ToneGenerator beep result: " + success);
            } else {
                // Create a new ToneGenerator if the original failed
                Log.d("MessageInput", "Creating temporary ToneGenerator for beep");
                try {
                    ToneGenerator tempTone = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
                    tempTone.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
                    // Release after a short delay
                    new android.os.Handler().postDelayed(() -> {
                        try {
                            tempTone.release();
                        } catch (Exception ignored) {}
                    }, 200);
                } catch (Exception e) {
                    Log.w("MessageInput", "Temporary ToneGenerator failed", e);
                }
            }
        } catch (Exception e) {
            Log.w("MessageInput", "Failed to play beep sound", e);
        }
    }
    
    private void performHapticFeedback() {
        Log.d("MessageInput", "performHapticFeedback called");
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Modern vibration with VibrationEffect
                    VibrationEffect effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
                    vibrator.vibrate(effect);
                    Log.d("MessageInput", "Performed modern haptic feedback");
                } else {
                    // Legacy vibration
                    vibrator.vibrate(50);
                    Log.d("MessageInput", "Performed legacy haptic feedback");
                }
            } else {
                Log.w("MessageInput", "Vibrator not available or no vibrator capability");
            }
        } catch (Exception e) {
            Log.w("MessageInput", "Failed to perform haptic feedback", e);
        }
    }
    
    private void startRecordingAnimation() {
        if (voiceRecordButton != null && pulseAnimation != null) {
            voiceRecordButton.startAnimation(pulseAnimation);
            Log.d("MessageInput", "Started recording animation");
        }
    }
    
    private void stopRecordingAnimation() {
        if (voiceRecordButton != null) {
            voiceRecordButton.clearAnimation();
            Log.d("MessageInput", "Stopped recording animation");
        }
    }
    
    private void cleanup() {
        // Clean up resources
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        if (beepPlayer != null) {
            beepPlayer.release();
            beepPlayer = null;
        }
        if (voiceRecordButton != null) {
            voiceRecordButton.clearAnimation();
        }
        vibrator = null; // Don't need to release vibrator, just clear reference
    }

    private void init(Context context, AttributeSet attrs) {
        init(context);
        MessageInputStyle style = MessageInputStyle.parse(context, attrs);

        this.messageInput.setMaxLines(style.getInputMaxLines());
        this.messageInput.setHint(style.getInputHint());
        this.messageInput.setText(style.getInputText());
        this.messageInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getInputTextSize());
        this.messageInput.setTextColor(style.getInputTextColor());
        this.messageInput.setHintTextColor(style.getInputHintColor());
        ViewCompat.setBackground(this.messageInput, style.getInputBackground());
        setCursor(style.getInputCursorDrawable());

        this.attachmentButton.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButton.setImageDrawable(style.getAttachmentButtonIcon());
        this.attachmentButton.getLayoutParams().width = style.getAttachmentButtonWidth();
        this.attachmentButton.getLayoutParams().height = style.getAttachmentButtonHeight();
        ViewCompat.setBackground(this.attachmentButton, style.getAttachmentButtonBackground());

        this.attachmentButtonSpace.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButtonSpace.getLayoutParams().width = style.getAttachmentButtonMargin();

        this.messageSendButton.setImageDrawable(style.getInputButtonIcon());
        this.messageSendButton.getLayoutParams().width = style.getInputButtonWidth();
        this.messageSendButton.getLayoutParams().height = style.getInputButtonHeight();
        ViewCompat.setBackground(messageSendButton, style.getInputButtonBackground());
        this.sendButtonSpace.getLayoutParams().width = style.getInputButtonMargin();

        if (getPaddingLeft() == 0
                && getPaddingRight() == 0
                && getPaddingTop() == 0
                && getPaddingBottom() == 0) {
            setPadding(
                    style.getInputDefaultPaddingLeft(),
                    style.getInputDefaultPaddingTop(),
                    style.getInputDefaultPaddingRight(),
                    style.getInputDefaultPaddingBottom()
            );
        }
        this.delayTypingStatusMillis = style.getDelayTypingStatus();
    }

    private void init(Context context) {
        inflate(context, R.layout.view_message_input, this);

        messageInput = findViewById(R.id.messageInput);
        messageSendButton = findViewById(R.id.messageSendButton);
        voiceRecordButton = findViewById(R.id.voiceRecordButton);
        attachmentButton = findViewById(R.id.attachmentButton);
        sendButtonSpace = findViewById(R.id.sendButtonSpace);
        attachmentButtonSpace = findViewById(R.id.attachmentButtonSpace);

        messageSendButton.setOnClickListener(this);
        voiceRecordButton.setOnTouchListener(this);
        attachmentButton.setOnClickListener(this);
        messageInput.addTextChangedListener(this);
        messageInput.setText("");
        messageInput.setOnFocusChangeListener(this);
        
        // Set default microphone icon
        voiceRecordButton.setImageResource(R.drawable.ic_mic);
        
        // Initialize sound and animation effects
        initializeSoundAndAnimation();
    }

    private void setCursor(Drawable drawable) {
        if (drawable == null) return;

        try {
            final Field drawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            drawableResField.setAccessible(true);

            final Object drawableFieldOwner;
            final Class<?> drawableFieldClass;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                drawableFieldOwner = this.messageInput;
                drawableFieldClass = TextView.class;
            } else {
                final Field editorField = TextView.class.getDeclaredField("mEditor");
                editorField.setAccessible(true);
                drawableFieldOwner = editorField.get(this.messageInput);
                drawableFieldClass = drawableFieldOwner.getClass();
            }
            final Field drawableField = drawableFieldClass.getDeclaredField("mCursorDrawable");
            drawableField.setAccessible(true);
            drawableField.set(drawableFieldOwner, new Drawable[]{drawable, drawable});
        } catch (Exception ignored) {
        }
    }

    public void setTypingListener(TypingListener typingListener) {
        this.typingListener = typingListener;
    }

    /**
     * Interface definition for a callback to be invoked when user pressed 'submit' button
     */
    public interface InputListener {

        /**
         * Fires when user presses 'send' button.
         *
         * @param input input entered by user
         * @return if input text is valid, you must return {@code true} and input will be cleared, otherwise return false.
         */
        boolean onSubmit(CharSequence input);
    }

    /**
     * Interface definition for a callback to be invoked when user presses 'add' button
     */
    public interface AttachmentsListener {

        /**
         * Fires when user presses 'add' button.
         */
        void onAddAttachments();
    }

    /**
     * Interface definition for a callback to be invoked when user typing
     */
    public interface TypingListener {

        /**
         * Fires when user presses start typing
         */
        void onStartTyping();

        /**
         * Fires when user presses stop typing
         */
        void onStopTyping();

    }

    /**
     * Interface definition for a callback to be invoked when user records voice
     */
    public interface VoiceRecordingListener {

        /**
         * Fires when user starts recording voice
         */
        void onStartRecording();

        /**
         * Fires when user stops recording voice
         */
        void onStopRecording();

    }
}
