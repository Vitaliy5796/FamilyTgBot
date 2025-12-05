package ru.sidorov.familytgbot.component;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {

    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/start", "запустить бота"),
            new BotCommand("/help", "информация о боте")/*,
            new BotCommand("/setprompt", "добавить тему промпта")*/
    );

    String HELP_TEXT = "Этого бота ты можешь добавить бота в группу и в определенное время он будет отправлять сообщение в группу.";

}
