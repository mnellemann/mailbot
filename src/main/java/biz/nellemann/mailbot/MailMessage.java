package biz.nellemann.mailbot;


import java.util.ArrayList;
import java.util.List;

public class MailMessage {

    String envelopeSender;
    String envelopeReceiver;
    String subject;
    List<BodyContent> contentList = new ArrayList<>();

    MailMessage() {}

    MailMessage(String envelopeSender, String envelopeReceiver) {
        this.envelopeSender = envelopeSender;
        this.envelopeReceiver = envelopeReceiver;
    }


    public void setEnvelopeSender(String envelopeSender) {
        this.envelopeSender = envelopeSender;
    }

    public void setEnvelopeReceiver(String envelopeReceiver) {
        this.envelopeReceiver = envelopeReceiver;
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

        String body = (content != null ? (String) content.data : "");
        String response = String.format("*From*: %s\n*To*: %s\n*Subject* %s\n\n%s", envelopeSender, envelopeReceiver, subject, body);
        return response;
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

        String body = (content != null ? (String) content.data : "");
        String response = String.format("<b>From</b>: %s<br/><b>To</b>: %s<br/><b>Subject</b> %s<br/><br/>%s", envelopeSender, envelopeReceiver, subject, body);
        return response;
    }

    public boolean hasHtml() {
        BodyContent content = contentList.stream()
            .filter(e -> e.type.startsWith("text/html"))
            .findFirst()
            .orElse(null);

        return (content != null);
    }

    public String getEnvelopeReceiver() {
        return this.envelopeReceiver;
    }


    public String getEnvelopeSender() {
        return this.envelopeSender;
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
        return String.format("From: %; To: %s; Subject: %s", envelopeSender, envelopeReceiver, subject);
    }

}
