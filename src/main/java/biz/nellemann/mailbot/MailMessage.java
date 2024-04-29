package biz.nellemann.mailbot;


import com.sun.mail.util.BASE64DecoderStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MailMessage {

    private final static Logger log = LoggerFactory.getLogger(MailMessage.class);

    String sender;
    String recipient;
    String subject;
    List<BodyContent> contentList = new ArrayList<>();

    MailMessage() {}

    MailMessage(String sender, String recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void addContent(String type, Object data) {
        contentList.add(new BodyContent(type, data));
    }

    public String getSubject() {
        return subject;
    }


    public String getText() {
        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("text/plain"))
            .findFirst()
            .orElse(null);

        return (content != null ? (String) content.data : "");
        //return String.format("*From*: %s\n*To*: %s\n*Subject* %s\n\n%s", sender, recipient, subject, body);
    }


    public boolean hasText() {
        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("text/plain"))
            .findFirst()
            .orElse(null);

        return (content != null);
    }


    public String getHtml() {
        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("text/html"))
            .findFirst()
            .orElse(null);

        //return String.format("<b>From</b>: %s<br/><b>To</b>: %s<br/><b>Subject</b> %s<br/><br/>%s", sender, recipient, subject, body);
        return (content != null ? (String) content.data : "");
    }


    public boolean hasHtml() {
        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("text/html"))
            .findFirst()
            .orElse(null);

        return (content != null);
    }


    public boolean hasImage() {
        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("image/"))
            .findFirst()
            .orElse(null);

        return (content != null);
    }


    public byte[] getImage() {

        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("image/"))
            .findFirst()
            .orElse(null);

        if(content != null) {
            try {
                BASE64DecoderStream stream = (BASE64DecoderStream) content.data;
                byte[] buffer = null;
                buffer = new byte[stream.available()];
                stream.read(buffer, 0, buffer.length);
                return buffer;
            } catch (IOException e) {
                log.error("getImage() - error: {}", e.getMessage());
            }
        }

        return null;
    }


    public String getRecipient() {
        return this.recipient;
    }


    public String getSender() {
        return this.sender;
    }


    private static class BodyContent {

        String type;
        Object data;

        BodyContent(String type, Object data) {
            this.type = type;
            this.data = data;
        }

    }

    @Override
    public String toString() {
        return String.format("From: %; To: %s; Subject: %s", sender, recipient, subject);
    }

}
