package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.config.common.Language;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Helper-Klasse für die Verwaltung der DAIMLER-Sprachen (Reihenfolge etc)
 */
public class iPartsLanguage {

    private static final String LANG_DELIMITER = ",";

    private static List<Language> primaryDaimlerLanguages = null;
    private static List<Language> availableDaimlerLanguages = null;
    private static Set<String> isoDaimlerCountryCodes = null;
    private static EnumSet<Language> DAIMLER_PRIMARY_LANGUAGES = EnumSet.of(Language.DE, Language.EN, Language.FR,
                                                                            Language.IT, Language.ES, Language.PT,
                                                                            Language.JA, Language.ZH, Language.TR,
                                                                            Language.RU);
    private static EnumSet<Language> ASPLM_PRIMARY_LANGUAGES = EnumSet.of(Language.DE, Language.EN, Language.PT,
                                                                          Language.ES);

    private static EnumSet<Language> EPC_LANGUAGES = EnumSet.of(Language.DE, Language.EN, Language.FR,
                                                                Language.IT, Language.ES, Language.PT,
                                                                Language.JA, Language.ZH, Language.TR,
                                                                Language.RU, Language.PL);

    /**
     * Daimler Primär-Sprachen (mit Abgleich aus den vorhandenen Sprachen "Available Languages" der DWK)
     *
     * @param project
     * @return
     */
    public static List<Language> getDaimlerPrimaryLanguages(EtkProject project) {
        if (primaryDaimlerLanguages == null) {
            primaryDaimlerLanguages = new DwList<>();
            List<String> availLangs = project.getAvailLanguages();
            for (Language lang : DAIMLER_PRIMARY_LANGUAGES) {
                if (availLangs.contains(lang.getCode())) {
                    primaryDaimlerLanguages.add(lang);
                }
            }
        }
        return primaryDaimlerLanguages;
    }

    /**
     * Daimler Primär-Sprachen (aus Reihenfolge EnumSet)
     *
     * @return
     */
    public static List<Language> getDaimlerPrimaryLanguages() {
        return getLanguagesAsList(DAIMLER_PRIMARY_LANGUAGES);
    }

    /**
     * Gibt das übergebene EnumSet als List zurück.
     *
     * @param languagesEnumSet
     * @return
     */
    private static List<Language> getLanguagesAsList(EnumSet<Language> languagesEnumSet) {
        List<Language> result = new DwList<>();
        if ((languagesEnumSet != null) && !languagesEnumSet.isEmpty()) {
            result.addAll(languagesEnumSet);
        }
        return result;
    }

    /**
     * Alle in der DWK vorhandenen Sprachen (Available Languages), sortiert nach DAIMLER Manier
     *
     * @param project
     * @return
     */
    public static List<Language> getAvailDaimlerLanguages(EtkProject project) {
        if (availableDaimlerLanguages == null) {
            availableDaimlerLanguages = new DwList<>();
            List<String> availLangs = project.getAvailLanguages();
            for (Language lang : DAIMLER_PRIMARY_LANGUAGES) {
                if (availLangs.contains(lang.getCode())) {
                    availableDaimlerLanguages.add(lang);
                }
                availLangs.remove(lang.getCode());
            }
            availableDaimlerLanguages.addAll(convertToLanguageList(availLangs));
        }
        return availableDaimlerLanguages;
    }

    /**
     * AS/PLM Primär-Sprachen (aus Reihenfolge EnumSet)
     *
     * @return
     */
    public static List<Language> getASPLMPrimaryLanguages() {
        return getLanguagesAsList(ASPLM_PRIMARY_LANGUAGES);
    }

    /**
     * Abfrage, ob es sich um eine Daimler-Primär-Sprache handelt
     *
     * @param lang
     * @return
     */
    public static boolean isDaimlerPrimaryLanguage(Language lang) {
        return DAIMLER_PRIMARY_LANGUAGES.contains(lang);
    }

    /**
     * Abfrage, ob es sich um eine Daimler-Primär-Sprache handelt
     *
     * @param lang
     * @return
     */
    public static boolean isDaimlerPrimaryLanguage(String lang) {
        return isDaimlerPrimaryLanguage(Language.getFromCode(lang));
    }

    /**
     * Abfrage, ob es sich um eine AS/PLM Primär-Sprache handelt
     *
     * @param lang
     * @return
     */
    public static boolean isASPLMPrimaryLanguage(Language lang) {
        return ASPLM_PRIMARY_LANGUAGES.contains(lang);
    }

    /**
     * Abfrage, ob es sich um eine AS/PLM Primär-Sprache handelt
     *
     * @param lang
     * @return
     */
    public static boolean isASPLMPrimaryLanguage(String lang) {
        return isASPLMPrimaryLanguage(Language.getFromCode(lang));
    }

    /**
     * Liste von {@link }Language] nach List<String> (getCode()) konvertieren
     *
     * @param Languages
     * @return
     */
    public static List<String> convertToStringList(List<Language> Languages) {
        List<String> result = new DwList<>();
        for (Language lang : Languages) {
            result.add(lang.getCode());
        }
        return result;
    }

    /**
     * Liste von List<String> mit Sprachen nach {@link }Language] konvertieren
     *
     * @param langs
     * @return
     */
    public static List<Language> convertToLanguageList(List<String> langs) {
        List<Language> result = new DwList<>();
        for (String lang : langs) {
            Language language = Language.getFromCode(lang);
            if (language != null) {
                result.add(language);
            }
        }
        return result;
    }

    /**
     * Komma-separierten String der Sprachen erzeugen
     *
     * @param Languages
     * @return
     */
    public static String convertToDBStringLangs(List<Language> Languages) {
        return convertToDBString(convertToStringList(Languages));
    }

    /**
     * Komma-separierten String der Sprachen erzeugen
     *
     * @param langs
     * @return
     */
    public static String convertToDBString(List<String> langs) {
        return StrUtils.stringListToString(langs, LANG_DELIMITER);
    }

    /**
     * Komma-separierten String der Sprachen in eine Liste<Language> konvertieren
     *
     * @param dbValue
     * @return
     */
    public static List<Language> getLangListFromDBValue(String dbValue) {
        return convertToLanguageList(StrUtils.toStringList(dbValue, LANG_DELIMITER, false));
    }

    /**
     * erzwingt ein Neuladen der CountryCodes
     */
    public static synchronized void resetDaimlerIsoCountryCodesLoaded() {
        isoDaimlerCountryCodes = null;
    }

    /**
     * Liefert die Enum-Tokens (CountryCodes) aus dem enum "CountryISO3166"
     *
     * @param project
     * @return
     */
    public static synchronized Set<String> getDaimlerIsoCountryCodes(EtkProject project) {
        if (isoDaimlerCountryCodes == null) {
            EnumValue isoDaimlerCountryCodeEnums = project.getEtkDbs().getEnumValue(iPartsConst.DAIMLER_ISO_COUNTRY_CODE_ENUM_NAME);
            isoDaimlerCountryCodes = new TreeSet<>();
            if (isoDaimlerCountryCodeEnums != null) {
                for (EnumEntry entry : isoDaimlerCountryCodeEnums.values()) {
                    isoDaimlerCountryCodes.add(entry.getToken().toUpperCase());
                }
            }
        }
        return Collections.unmodifiableSet(isoDaimlerCountryCodes);
    }

    /**
     * Überprüft die Gültigkeit eines CountryCodes
     *
     * @param project
     * @param countryCode
     * @return
     */
    public static boolean isValidDaimlerIsoCountryCode(EtkProject project, String countryCode) {
        return getDaimlerIsoCountryCodes(project).contains(countryCode.toUpperCase());
    }

    /**
     * Gibt die Liste der EPC-Sprachen als Language-Liste zurück
     *
     * @return
     */
    public static List<Language> getEPCLanguages() {
        return getLanguagesAsList(EPC_LANGUAGES);
    }

    /**
     * Gibt die Liste der EPC-Sprachen als String-Liste zurück
     *
     * @return
     */
    public static List<String> getEPCLanguageList() {
        return convertToStringList(getEPCLanguages());
    }

    /**
     * Liefert die Liste der Daimler-Sprachen (24)
     * Sind in der WB-Konfiguration hinterlegt
     *
     * @param project
     * @return
     */
    public static List<String> getCompleteDaimlerLanguageList(EtkProject project) {
        return project.getConfig().getDatabaseLanguages();
    }
}