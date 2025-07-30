package com.stfalcon.chatkit.sample.features.demo.custom.media;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.features.demo.custom.media.holders.IncomingVoiceMessageViewHolder;
import com.stfalcon.chatkit.sample.features.demo.custom.media.holders.OutcomingVoiceMessageViewHolder;

/**
 * Custom adapter for handling voice messages
 */
public class VoiceMessagesAdapter extends MessagesListAdapter<Message> {

    public VoiceMessagesAdapter(String senderId, ImageLoader imageLoader) {
        super(senderId, getMessageHolders(), imageLoader);
    }

    private static MessageHolders getMessageHolders() {
        return new MessageHolders()
                .registerContentType(
                        (byte) 1,
                        IncomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_incoming_voice_message,
                        OutcomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_outcoming_voice_message,
                        (message, senderId) -> ((Message) message).getVoice() != null);
    }


}