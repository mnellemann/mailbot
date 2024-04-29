package biz.nellemann.mailbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.helper.BasicMessageListener;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MailHandler implements BasicMessageListener {

    private final static Logger log = LoggerFactory.getLogger(MailHandler.class);

    private final Session session;

    MailHandler() {
        Properties props = System.getProperties();
        session = Session.getInstance(props, null);
    }

    @Override
    public void messageArrived(MessageContext messageContext, String from, String to, byte[] data) throws RejectException {
        MailEvent event = new MailEvent(this, getMessage(from, to, data));
        sendEvent(event);
    }


    public MailMessage getMessage(String from, String to, byte[] data) {

        MailMessage mailMessage = new MailMessage(from, to);

        try {
            MimeMessage mimeMessage = new MimeMessage(session, new ByteArrayInputStream(data));
            mailMessage.setSubject(mimeMessage.getSubject());
            if (mimeMessage.getContent() instanceof String) {
                log.debug("getMessage() - String");
                mailMessage.addContent("text/plain", (String)mimeMessage.getContent());
            } else if (mimeMessage.getContent() instanceof MimeMultipart) {
                log.debug("getMessage() - MimeMultipart");
                MimeMultipart content = (MimeMultipart) mimeMessage.getContent();
                for(int i = 0; i < content.getCount(); i++) {
                    log.debug(content.getBodyPart(i).getContentType());
                    mailMessage.addContent(content.getBodyPart(i).getContentType(), content.getBodyPart(i).getContent());
                }
            } else {
                log.warn("getMessage() - Unknown type");
            }
        } catch (MessagingException | IOException e) {
            log.error("getMessage() - error: {}", e.getMessage());
        }

        return mailMessage;
    }


    /**
     * Event Listener Configuration
     */

    private final List<MailReceivedListener> eventListeners = new ArrayList<>();

    public synchronized void addEventListener(MailReceivedListener listener ) {
        eventListeners.add( listener );
    }

    public synchronized void addEventListener(List<MailReceivedListener> listeners ) {
        eventListeners.addAll(listeners);
    }

    public synchronized void removeEventListener( MailReceivedListener l ) {
        eventListeners.remove( l );
    }



    /*
      Message Handling
     */

    private synchronized void sendEvent(MailEvent email) {
        for (MailReceivedListener eventListener : eventListeners) {
            log.debug("sendEvent() - Sending event to: {}", eventListener);
            eventListener.onEvent(email);
        }
    }

 }

