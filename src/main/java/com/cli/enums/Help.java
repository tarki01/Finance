/**
 * Help descriptions for application features.
 */
package com.cli.enums;

/**
 * Enumeration of help descriptions for different features.
 */
public enum Help {
    /**
     * Transaction operation help.
     */
    TRANSACTION_OPERATION("Управление транзакциями — позволяет добавлять, "
            + "изменять и удалять записи о доходах и расходах."),

    /**
     * Transaction add help.
     */
    TRANSACTION_ADD("Добавить доход — создаёт новую транзакцию. Необходимо "
            + "указать категорию и сумму."),

    /**
     * Transaction remove help.
     */
    TRANSACTION_REMOVE("Удалить транзакцию — позволяет удалить выбранную "
            + "транзакцию из списка."),

    /**
     * Transaction update help.
     */
    TRANSACTION_UPDATE("Изменить транзакцию — позволяет изменить категорию, "
            + "сумму или тип (доход/расход)."),

    /**
     * Budget operation help.
     */
    BUDGET_OPERATION("Управление бюджетом — позволяет устанавливать и "
            + "удалять бюджеты по категориям."),

    /**
     * Budget add help.
     */
    BUDGET_ADD("Добавить бюджет — задаёт лимит расходов по выбранной "
            + "категории."),

    /**
     * Budget remove help.
     */
    BUDGET_REMOVE("Удалить бюджет — удаляет ранее установленный бюджет."),

    /**
     * Statistics operation help.
     */
    STATISTICS_OPERATION("Статистика — отображает финансовые показатели и "
            + "анализ по категориям."),

    /**
     * Statistics all help.
     */
    STATISTICS_ALL("Показать полную статистику — выводит общие суммы "
            + "доходов, расходов и состояние бюджетов."),

    /**
     * Statistics by category help.
     */
    STATISTICS_BY_CATEGORY("Показать статистику по категориям и времени — "
            + "позволяет выбрать период и категории для анализа."),

    /**
     * Transaction operation send to user help.
     */
    TRANSACTION_OPERATION_SEND_TO_USER("Перевести деньги другому "
            + "пользователю — позволяет перевести средства"
            + " между пользователями."),

    /**
     * JSON operation help.
     */
    JSON_OPERATION("Управление пользователем — загрузка, сохранение и "
            + "удаление данных пользователя."),

    /**
     * JSON upload help.
     */
    JSON_UPLOAD("Загрузить данные пользователя — импортирует данные из "
            + "файла."),

    /**
     * JSON unload help.
     */
    JSON_UNLOAD("Выгрузить данные пользователя — экспортирует данные "
            + "пользователя в файл."),

    /**
     * Delete user help.
     */
    DELETE_USER("Удалить пользователя — полностью удаляет профиль и "
            + "связанные данные.");

    /**
     * Description string.
     */
    private final String description;

    /**
     * Constructor for Help enum.
     *
     * @param descriptionText the description text
     */
    Help(final String descriptionText) {
        this.description = descriptionText;
    }

    /**
     * Gets the description.
     *
     * @return the description string
     */
    public String getDescription() {
        return description;
    }
}
