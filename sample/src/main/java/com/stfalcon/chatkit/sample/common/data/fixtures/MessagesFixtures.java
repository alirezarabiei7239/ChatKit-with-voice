package com.stfalcon.chatkit.sample.common.data.fixtures;

import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.common.data.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by troy379 on 12.12.16.
 */
public final class MessagesFixtures extends FixturesData {
    private MessagesFixtures() {
        throw new AssertionError();
    }

    public static Message getImageMessage() {
        Message message = new Message(getRandomId(), getUser(), null);
        message.setImage(new Message.Image(getRandomImage()));
        return message;
    }

    public static Message getVoiceMessage() {
        Message message = new Message(getRandomId(), getUser(), null);
        message.setVoice(new Message.Voice("http://example.com", rnd.nextInt(200) + 30));
        return message;
    }

    public static Message getTextMessage() {
        return getTextMessage(getRandomMessage());
    }

    public static Message getTextMessage(String text) {
        return new Message(getRandomId(), getUser(), text);
    }

    public static ArrayList<Message> getMessages(Date startDate) {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10/*days count*/; i++) {
            int countPerDay = rnd.nextInt(5) + 1;

            for (int j = 0; j < countPerDay; j++) {
                Message message;
                if (i % 2 == 0 && j % 3 == 0) {
                    message = getImageMessage();
                } else {
                    message = getTextMessage();
                }

                Calendar calendar = Calendar.getInstance();
                if (startDate != null) calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, -(i * i + 1));

                message.setCreatedAt(calendar.getTime());
                messages.add(message);
            }
        }
        return messages;
    }

    public static ArrayList<Message> getVoiceMessages() {
        ArrayList<Message> messages = new ArrayList<>();
        
        // Create some users for the demo
        User user1 = new User("0", names.get(0), avatars.get(0), true);
        User user2 = new User("1", names.get(1), avatars.get(1), true);
        
        // Add some regular text messages
        messages.add(new Message(getRandomId(), user2, "Hey! How are you?"));
        messages.add(new Message(getRandomId(), user1, "I'm good, thanks! How about you?"));
        
        // Add voice messages with different durations
        Message voiceMsg1 = new Message(getRandomId(), user2, "");
        voiceMsg1.setVoice(new Message.Voice("sample_voice_1.3gp", 15));
        messages.add(voiceMsg1);
        
        messages.add(new Message(getRandomId(), user1, "Nice voice message!"));
        
        Message voiceMsg2 = new Message(getRandomId(), user1, "");
        voiceMsg2.setVoice(new Message.Voice("sample_voice_2.3gp", 8));
        messages.add(voiceMsg2);
        
        Message voiceMsg3 = new Message(getRandomId(), user2, "");
        voiceMsg3.setVoice(new Message.Voice("sample_voice_3.3gp", 23));
        messages.add(voiceMsg3);
        
        messages.add(new Message(getRandomId(), user1, "Great! This voice feature is awesome 🎤"));
        
        return messages;
    }

    public static User getUser() {
        boolean even = rnd.nextBoolean();
        return new User(
                even ? "0" : "1",
                even ? names.get(0) : names.get(1),
                even ? avatars.get(0) : avatars.get(1),
                true);
    }
}
