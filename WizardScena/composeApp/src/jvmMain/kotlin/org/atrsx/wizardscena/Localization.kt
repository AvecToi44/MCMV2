package org.atrsx.wizardscena

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import java.util.prefs.Preferences

enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en")
}

object AppI18n {
    private const val PREF_NODE = "org.atrsx.wizardscena"
    private const val PREF_KEY_LANG = "ui_language"
    private val prefs: Preferences = Preferences.userRoot().node(PREF_NODE)

    val languageState = mutableStateOf(loadLanguage())

    private val ru = mapOf(
        "app_title" to "WizardScena",
        "tab_main" to "Основной сценарий",
        "tab_pressures" to "Давления",
        "tab_currents" to "Токи",
        "tab_freq_params" to "Параметры 0x68",
        "btn_back_to_start" to "Назад на стартовый экран",
        "start_open" to "Открыть сценарий",
        "start_create" to "Создать новый сценарий",
        "start_recent" to "Последние сценарии",
        "start_recent_empty" to "Нет недавно открытых сценариев",
        "start_open_failed" to "Не удалось открыть сценарий",
        "choose_excel_dialog" to "Открыть Excel сценарий",
        "last_save" to "Сохранение (%s) было крайний раз в %s",
        "standard_not_selected" to "Эталонный chart .txt не выбран",
        "choose_standard_dialog" to "Выбрать эталонный chart (.txt)",
        "btn_standard" to "Выбрать эталон .txt",
        "btn_save_excel" to "Сохранить Excel",
        "btn_open_excel" to "Открыть Excel",
        "btn_settings" to "Настройки",
        "settings_language" to "Язык",
        "language_ru" to "Русский",
        "language_en" to "Английский",
        "default_scenario_name" to "Новый сценарий",
        "main_add_step" to "Добавить шаг",
        "main_delete" to "Удалить",
        "main_copy_selected" to "Копировать выбранные",
        "main_paste_before" to "Вставить до",
        "main_paste_after" to "Вставить после",
        "main_select_all" to "Выбрать все",
        "main_deselect" to "Снять выбор",
        "main_new_step_name" to "Новый шаг",
        "main_label_text" to "текст",
        "main_label_duration" to "длительность",
        "main_label_gradient" to "время перехода",
        "main_label_analog1" to "аналог 1",
        "main_label_analog2" to "аналог 2",
        "press_channel_params" to "Параметры канала",
        "press_display_name" to "Отображаемое имя",
        "press_min_value" to "Минимум",
        "press_max_value" to "Максимум",
        "press_tolerance" to "Допуск",
        "press_unit" to "Единица",
        "press_pick_right" to "Выберите канал справа, чтобы редактировать параметры.",
        "press_title" to "Давления",
        "sol_title_panel" to "Соленоиды - глобальные и канальные параметры",
        "sol_main_freq" to "Главная частота (целое)",
        "sol_display_name" to "Отображаемое имя",
        "sol_max_value_255" to "Макс. значение [0 - 255]",
        "sol_value_div" to "Цена деления",
        "sol_dither_amp" to "Амплитуда дитера",
        "sol_dither_freq" to "Частота дитера",
        "sol_min_current" to "Мин. ток",
        "sol_max_current" to "Макс. ток",
        "sol_pick_right" to "Выберите соленоид справа, чтобы редактировать параметры.",
        "sol_list_title" to "Соленоиды",
        "sol_range" to "Диапазон",
        "freq_params_title" to "Параметры пакета 0x68 (D14..M14)",
        "freq_param_label" to "Параметр %d",
        "freq_params_hint" to "Допустимые значения: 0..255"
    )

    private val en = mapOf(
        "app_title" to "WizardScena",
        "tab_main" to "Main Scenario",
        "tab_pressures" to "Pressures",
        "tab_currents" to "Currents",
        "tab_freq_params" to "0x68 Parameters",
        "btn_back_to_start" to "Back to Start Screen",
        "start_open" to "Open Scenario",
        "start_create" to "Create New Scenario",
        "start_recent" to "Recent Scenarios",
        "start_recent_empty" to "No recent scenarios",
        "start_open_failed" to "Failed to open scenario",
        "choose_excel_dialog" to "Open Excel scenario",
        "last_save" to "Last save (%s) at %s",
        "standard_not_selected" to "Standard chart .txt is not selected",
        "choose_standard_dialog" to "Choose standard chart (.txt)",
        "btn_standard" to "Choose reference .txt",
        "btn_save_excel" to "Save Excel",
        "btn_open_excel" to "Open Excel",
        "btn_settings" to "Settings",
        "settings_language" to "Language",
        "language_ru" to "Russian",
        "language_en" to "English",
        "default_scenario_name" to "New Scenario",
        "main_add_step" to "Add Step",
        "main_delete" to "Delete",
        "main_copy_selected" to "Copy Selected",
        "main_paste_before" to "Paste Before",
        "main_paste_after" to "Paste After",
        "main_select_all" to "Select All",
        "main_deselect" to "Deselect",
        "main_new_step_name" to "New Step",
        "main_label_text" to "text",
        "main_label_duration" to "duration",
        "main_label_gradient" to "gradient time",
        "main_label_analog1" to "analog 1",
        "main_label_analog2" to "analog 2",
        "press_channel_params" to "Channel Parameters",
        "press_display_name" to "Display Name",
        "press_min_value" to "Min Value",
        "press_max_value" to "Max Value",
        "press_tolerance" to "Tolerance",
        "press_unit" to "Unit",
        "press_pick_right" to "Select a channel on the right to edit parameters.",
        "press_title" to "Pressures",
        "sol_title_panel" to "Solenoids - Global & Channel Params",
        "sol_main_freq" to "Main Frequency (Integer)",
        "sol_display_name" to "Display Name",
        "sol_max_value_255" to "Max Value [0 - 255]",
        "sol_value_div" to "Value of division",
        "sol_dither_amp" to "Dither Amplitude",
        "sol_dither_freq" to "Dither Frequency",
        "sol_min_current" to "Min Current Value",
        "sol_max_current" to "Max Current Value",
        "sol_pick_right" to "Select a solenoid on the right to edit parameters.",
        "sol_list_title" to "Solenoids",
        "sol_range" to "Range",
        "freq_params_title" to "0x68 packet parameters (D14..M14)",
        "freq_param_label" to "Parameter %d",
        "freq_params_hint" to "Allowed values: 0..255"
    )

    private fun loadLanguage(): AppLanguage {
        return when (prefs.get(PREF_KEY_LANG, AppLanguage.RU.code)) {
            AppLanguage.EN.code -> AppLanguage.EN
            else -> AppLanguage.RU
        }
    }

    fun setLanguage(language: AppLanguage) {
        languageState.value = language
        prefs.put(PREF_KEY_LANG, language.code)
    }

    fun text(key: String, language: AppLanguage = languageState.value): String {
        val dict = if (language == AppLanguage.RU) ru else en
        return dict[key] ?: key
    }

    fun textf(key: String, vararg args: Any, language: AppLanguage = languageState.value): String {
        return String.format(text(key, language), *args)
    }
}

@Composable
fun tr(key: String): String {
    val language by AppI18n.languageState
    return AppI18n.text(key, language)
}

@Composable
fun trf(key: String, vararg args: Any): String {
    val language by AppI18n.languageState
    return AppI18n.textf(key, *args, language = language)
}
