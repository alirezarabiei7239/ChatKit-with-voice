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
import android.widget.TextView;

import com.stfalcon.chatkit.R;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.utils.DateFormatter;

/**
 * View holder for outcoming voice messages
 */
public class OutcomingVoiceMessageViewHolder<MESSAGE extends IMessage> 
        extends VoiceMessageViewHolder<MESSAGE> {
    
    protected TextView messageTime;
    
    public OutcomingVoiceMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        messageTime = itemView.findViewById(R.id.messageTime);
    }
    
    @Override
    public void onBind(MESSAGE message) {
        if (messageTime != null) {
            messageTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
        }
        
        // Extract voice data from message
        setVoiceData(getVoiceUrl(message), getVoiceDuration(message));
    }
    
    // Extract voice data using reflection or interface
    protected String getVoiceUrl(MESSAGE message) {
        try {
            // Try to get voice data from message using reflection
            java.lang.reflect.Method getVoiceMethod = message.getClass().getMethod("getVoice");
            Object voice = getVoiceMethod.invoke(message);
            if (voice != null) {
                java.lang.reflect.Method getUrlMethod = voice.getClass().getMethod("getUrl");
                return (String) getUrlMethod.invoke(voice);
            }
        } catch (Exception e) {
            Log.d("OutcomingVoiceViewHolder", "Could not extract voice URL: " + e.getMessage());
        }
        return null;
    }
    
    protected int getVoiceDuration(MESSAGE message) {
        try {
            // Try to get voice data from message using reflection
            java.lang.reflect.Method getVoiceMethod = message.getClass().getMethod("getVoice");
            Object voice = getVoiceMethod.invoke(message);
            if (voice != null) {
                java.lang.reflect.Method getDurationMethod = voice.getClass().getMethod("getDuration");
                return (Integer) getDurationMethod.invoke(voice);
            }
        } catch (Exception e) {
            Log.d("OutcomingVoiceViewHolder", "Could not extract voice duration: " + e.getMessage());
        }
        return 0;
    }
}