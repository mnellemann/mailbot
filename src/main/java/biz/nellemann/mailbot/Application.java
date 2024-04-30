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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import io.github.furstenheim.CopyDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(name = "mailbot",
        mixinStandardHelpOptions = true,
        versionProvider = biz.nellemann.mailbot.VersionProvider.class)
public class Application implements Callable<Integer>, MailReceivedListener {

    private final static Logger log = LoggerFactory.getLogger(Application.class);

    private final CopyDown markdownConverter = new CopyDown();

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
        MailHandler mailHandler = new MailHandler();
        mailHandler.addEventListener(this);

        // Build the embedded SMTP server
        SMTPServer smtpServer = SMTPServer.port(port)
            .messageHandler(mailHandler)
            .build();

        // Start SMTP server asynchronously
        smtpServer.start();

        // Start Telegram Bot
        bot = new TelegramBot(token);
        sendText(chatId, "*Mail Bot* _started_");

        while (keepRunning.get()) {
            Thread.sleep(1000);
        }

        smtpServer.stop();
        sendText(chatId, "*Mail Bot* _stopped_");
        bot.shutdown();
        return 0;
    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public void onEvent(MailEvent event) {

        String body;
        if(event.getMessage().hasText()) {
            body = event.getMessage().getText();
        } else if (event.getMessage().hasHtml()) {
            body = markdownConverter.convert(event.getMessage().getHtml());
        } else {
            body = "";
        }

        String bodyWithHeader = String.format("*Sender*: %s\n*Recipient*: %s\n*Subject*: %s\n\n%s",
            event.getMessage().getSender(),
            event.getMessage().getRecipient(),
            event.getMessage().getSubject(),
            body);

        if(event.getMessage().hasImage()) {
            SendPhoto photo = new SendPhoto(chatId, event.getMessage().getImage())
                .caption(bodyWithHeader)
                .parseMode(ParseMode.Markdown)
                .disableNotification(true);
            sendPhoto(chatId, photo);
        } else {
            SendMessage message = new SendMessage(chatId, bodyWithHeader)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true)
                .disableNotification(true);
            sendMessage(chatId, message);
        }
    }


    private void sendText(String chatId, String text) {
        log.info("sendText() - chatId: {}, text: {}", chatId, text);

        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(true)
            .disableNotification(true);

        SendResponse sendResponse = bot.execute(request);
        boolean ok = sendResponse.isOk();
        if(!ok) {
            log.warn("sendText() - text was not sent, error: {} - {}", sendResponse.errorCode(), sendResponse.description());
        }

    }


    private void sendMessage(String chatId,  SendMessage message) {
        log.info("sendMessage() - chatId: {}", chatId);

        SendResponse sendResponse = bot.execute(message);
        boolean ok = sendResponse.isOk();
        if(!ok) {
            log.warn("sendMessage() - message was not sent, error: {} - {}", sendResponse.errorCode(), sendResponse.description());
        }
    }


    private void sendPhoto(String chatId,  SendPhoto photo) {
        log.info("sendPhoto() - chatId: {}", chatId);

        SendResponse sendResponse = bot.execute(photo);
        boolean ok = sendResponse.isOk();
        if(!ok) {
            log.warn("sendPhoto() - photo was not sent, error: {} - {}", sendResponse.errorCode(), sendResponse.description());
        }

    }


}
