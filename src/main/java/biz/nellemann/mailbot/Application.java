/*
   Copyright 2024 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package biz.nellemann.mailbot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.mail.MessagingException;

@Command(name = "mailbot",
        mixinStandardHelpOptions = true,
        versionProvider = biz.nellemann.mailbot.VersionProvider.class)
public class Application implements Callable<Integer>, MailReceivedListener {

    private final static Logger log = LoggerFactory.getLogger(Application.class);


    @CommandLine.Option(names = {"-p", "--port"}, description = "SMTP Port [default: 25].", defaultValue = "25", paramLabel = "<port>", required = true)
    private int port;

    @CommandLine.Option(names = { "-t", "--token"}, description = "Telegram Token.", required = true)
    private String token;

    @CommandLine.Option(names = { "-i", "--chat-id"}, description = "Telegram Chat ID.", required = true)
    private String chatId;

    AtomicBoolean keepRunning = new AtomicBoolean(true);
    TelegramBot bot;

    @Override
    public Integer call() throws InterruptedException {

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> keepRunning.set(false)));

        // Setup mail event listener
        MailListener mailListener = new MailListener();
        mailListener.addEventListener(this);

        // Build the embedded SMTP server
        SMTPServer smtpServer = SMTPServer.port(port)
            .simpleMessageListener(mailListener)
            .build();

        // Start SMTP server asynchronously
        smtpServer.start();

        // Start Telegram Bot
        bot = new TelegramBot(token);
        sendBotMessage(chatId, "*Mail Bot* _started_");

        while (keepRunning.get()) {
            Thread.sleep(1000);
        }

        smtpServer.stop();
        sendBotMessage(chatId, "*Mail Bot* _stopped_");
        bot.shutdown();
        return 0;
    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public void onEvent(MailEvent event) {
        //String content = new String(email.getMessage().getData());
        try {
            String content = (String) event.getMessage().getMimeMessage().getContent();
            String message = String.format("*From*: %s\n*To*: %s\n*Subject*: %s\n\n```\n%s\n```",
                event.getMessage().envelopeSender,
                event.getMessage().envelopeReceiver,
                event.getMessage().getMimeMessage().getSubject(),
                content);
            sendBotMessage(chatId, message);
        } catch (MessagingException | IOException e) {
            log.error("onEvent() - error: {}", e.getMessage());
        }
    }


    private void sendBotMessage(String chatId, String content) {
        log.debug("sendBotMessage() - chatId: {}, content: {}", chatId, content);
        SendMessage request = new SendMessage(chatId, content)
            .parseMode(ParseMode.MarkdownV2)
            .disableWebPagePreview(true)
            .disableNotification(true)
            .replyToMessageId(0);

        SendResponse sendResponse = bot.execute(request);
        boolean ok = sendResponse.isOk();
        if(!ok) {
            Message message = sendResponse.message();
            log.warn("sendBotMessage() - message was no sent.");
            log.info(message.text());
        }

    }


}
