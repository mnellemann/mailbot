package biz.nellemann.mailbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;

import javax.mail.Session;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MailListener implements SimpleMessageListener {

    private final static Logger log = LoggerFactory.getLogger(MailListener.class);


    /**
     * Event Listener Configuration
     */

    protected final List<MailReceivedListener> eventListeners = new ArrayList<>();

    public synchronized void addEventListener(MailReceivedListener listener ) {
        eventListeners.add( listener );
    }

    public synchronized void addEventListener(List<MailReceivedListener> listeners ) {
        eventListeners.addAll(listeners);
    }

    public synchronized void removeEventListener( MailReceivedListener l ) {
        eventListeners.remove( l );
    }




    /**
     * Do some business logic here
     */

    @Override
    public boolean accept(String from, String recipient) {
        //return recipient != null && recipient.endsWith(MARKETING_DOMAIN);
        return true;
    }

    /** Cache the messages in memory */
    @Override
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
    {
        if (log.isDebugEnabled())
            log.debug("Delivering mail from {} to {}", from, recipient);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        data = new BufferedInputStream(data);

        // read the data from the stream
        int current;
        while ((current = data.read()) >= 0)
        {
            out.write(current);
        }

        byte[] bytes = out.toByteArray();

        if (log.isDebugEnabled())
            log.debug("Creating message from data with {} bytes", bytes.length);

        MailMessage message = new MailMessage(this, from, recipient, bytes);
        MailEvent mail = new MailEvent(this, message);
        sendEvent(mail);
    }


    /**
     * Creates the JavaMail Session object for use in WiserMessage
     */
    protected Session getSession()
    {
        return Session.getDefaultInstance(new Properties());
    }


    /*
      Message Handling
     */

    private synchronized void sendEvent(MailEvent email) {
        for (MailReceivedListener eventListener : eventListeners) {
            log.info("sendEvent() - Sending event to: {}", eventListener);
            eventListener.onEvent(email);
        }
    }

}
