package ru.sidorov.familytgbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sidorov.familytgbot.component.BotCommands;
import ru.sidorov.familytgbot.moduls.group.entity.TargetGroup;
import ru.sidorov.familytgbot.moduls.group.repository.TargetGroupRepository;
import ru.sidorov.familytgbot.moduls.prompt.dto.PromptDto;
import ru.sidorov.familytgbot.moduls.prompt.service.abstracts.PromptService;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TgBot extends TelegramLongPollingBot implements BotCommands {

    public static final String PROMPT = "Сгенерируй забавный факт о %s на сегодня, уложись в одно предложение";
    public static final String FORMAT_HTML = "HTML";

    private final Map<Long, Boolean> awaitingTopicResponse = new HashMap<>();
    private final Map<Long, Boolean> awaitingPasswordResponse = new HashMap<>();
    private Map<Long, Integer> requestMessageId = new HashMap<>();

    private final BotConfig botConfig;
    private final PromptService promptService;
    private final TargetGroupRepository groupRepository;

    public TgBot(BotConfig botConfig, PromptService promptService, TargetGroupRepository groupRepository) {
        this.botConfig = botConfig;
        this.promptService = promptService;
        this.groupRepository = groupRepository;

        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            for (BotCommand list : LIST_OF_COMMANDS) {
                log.info(String.valueOf(list));
            }
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        int messageId;
        String username;
        String receivedMessage;

        checkChatMember(update);

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            if (groupRepository.existsById(chatId)) {
                log.info("Игнорируем сообщения в группе с id: {}", chatId);
                return;
            }
            username = update.getMessage().getChat().getFirstName();
            messageId = update.getMessage().getMessageId();
            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                checkCommand(receivedMessage, chatId, username, messageId);
            }
        }
    }

    private void checkChatMember(Update update) {
        long chatId;
        if (update.hasMyChatMember()) {
            var myChatMember = update.getMyChatMember();
            var chat = myChatMember.getChat();
            var newStatus = myChatMember.getNewChatMember().getStatus();

            if ("member".equals(newStatus) || "administrator".equals(newStatus)) {
                // Бот стал участником группы
                chatId = chat.getId();
                String title = chat.getTitle() != null ? chat.getTitle() : "Без названия";

                TargetGroup group = groupRepository.findById(chatId)
                        .orElse(TargetGroup.builder()
                                .chatId(chatId)
                                .title(title)
                                .isActive(true)
                                .build());

                group.setTitle(title);
                group.setActive(true);
                groupRepository.save(group);

                log.info("Бот добавлен в группу: {} (ID: {})", title, chatId);
                sendMessage(chatId, "Привет! Я сохранён и теперь буду тут жить! \uD83D\uDE0B");
            } else if ("kicked".equals(newStatus) || "left".equals(newStatus)) {
                groupRepository.findById(chat.getId()).ifPresent(group -> {
                    group.setActive(false);
                    groupRepository.save(group);
                });
            }
        }
    }

    private void checkCommand(String receivedMessage, long chatId, String username, int messageId) {
        try {
            if (awaitingPasswordResponse.containsKey(chatId) && awaitingPasswordResponse.get(chatId)) {
                if (!receivedMessage.equals("Qweasdzxc123")) {
                    deleteMessage(chatId, requestMessageId.get(chatId));
                    int oldMessageId = sendMessage(chatId, "Неверный пароль!");
                    requestMessageId.put(chatId, oldMessageId);
                    deleteMessage(chatId, messageId);
                } else {
                    deleteMessage(chatId, requestMessageId.get(chatId));
                    deleteMessage(chatId, messageId);
                    sendPrompt(chatId);
                    awaitingPasswordResponse.put(chatId, false);
                }
            } else if (awaitingTopicResponse.containsKey(chatId) && awaitingTopicResponse.get(chatId)) {
                deleteMessage(chatId, messageId);
                deleteMessage(chatId, requestMessageId.get(chatId));
                promptService.save(PromptDto.builder()
                        .title(receivedMessage)
                        .prompt(String.format(PROMPT, receivedMessage))
                        .build());
                sendMessage(chatId, "Промпт сохранен");
                awaitingTopicResponse.put(chatId, false);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            sendMessage(chatId, "Ошибка: " + e.getMessage());
        }

        switch (receivedMessage) {
            case "/start" -> startCommandReceived(chatId, username);
            case "/help" -> sendMessage(chatId, HELP_TEXT);
//            case "/setprompt" -> checkPassword(chatId);
        }
    }

    private void startCommandReceived(Long chatId, String username) {
        String answer = "Привет, " + username + " я могу отправлять запланированные сообщения. \n " +
                "Так же могу отправлять прогноз погоды по городам.";

        sendMessage(chatId, answer);
    }

    private void checkPassword(Long chatId) {
        String text = "Введите пароль для задания темы промпта";
        int messageId = sendMessage(chatId, text);
        awaitingPasswordResponse.put(chatId, true);
        requestMessageId.put(chatId, messageId);
    }

    private void sendPrompt(Long chatId) {
        String text = "Отправте тему новости";
        int messageId = sendMessage(chatId, text);
        awaitingTopicResponse.put(chatId, true);
        requestMessageId.put(chatId, messageId);
    }

    private int sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            Message sentMessage = execute(sendMessage);
            log.info("Reply sent in sendMessage");
            return sentMessage.getMessageId();
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            return -1;
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(Long.toString(chatId), messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public int sendMessageWithFormat(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setParseMode(FORMAT_HTML);
        try {
            Message sentMessage = execute(sendMessage);
            log.info("Reply sent in sendMessageWithStatistics");
            return sentMessage.getMessageId();
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            return -1;
        }
    }

    // Метод для отправки сообщения в группу (вызывается из scheduler)
    public void sendDailyMessage(String text, String imageUrl, Long groupChatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(groupChatId);
        message.setText(text);

        if (imageUrl != null) {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(groupChatId);
            photo.setPhoto(new InputFile(imageUrl));  // Или base64 если из API
            photo.setCaption(text);
            execute(photo);
        } else {
            execute(message);
        }
    }
}
