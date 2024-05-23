package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.framework.modules.gui.controls.misc.DWDatePattern;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.csscreator.CssCreator;
import de.docware.framework.modules.gui.output.j2ee.jscreator.JavascriptCreator;
import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Komponente zur Eingabe / Anzeige von Datumswerten mit Pattern für Filter
 */
public class GuiExtDateEditTextField extends AbstractGuiExtDateTimeEditTextField {

    public static String TYPE = "editextdatetextfield";
    public static String LOG_TYPE = "date";

    public static void getCssAndJavascript(CssCreator cssCreator, JavascriptCreator jsCreator) {
    }

    public GuiExtDateEditTextField() {
        super(TYPE);
    }

    public GuiExtDateEditTextField(Date date) {
        this();
        setDateTime(date);
    }

    /**
     * Liefert den Date-Format-String für die SQL-Date Darstellung
     *
     * @return
     */
    protected String getDateFormatForSQL() {
        return DateUtils.simpleDateFormatyyyyMMdd;
    }

    @Override
    protected String __internal_getDateSeparator() {
        if (dateSeparator == null) {
            // Suche nach Zeichengruppierungen die weder 'd', 'M' noch 'y' enthalten - wenn eine Gruppierung existiert, wähle die erste
            Matcher matcher = Pattern.compile("([^dMy]+)").matcher(getPattern());
            if (matcher.find()) {
                dateSeparator = matcher.group();
                dateSeparator = dateSeparator.replace("'", "");
            } else {
                Logger.getLogger().throwRuntimeException("Failed to determine date separator for pattern " + getPattern());
            }
        }
        return dateSeparator;
    }

    @Override
    protected Pattern __internal_getHtmlMatcherPattern() {
        if (datePattern == null) {
            List<String> patternGroups = StrUtils.toStringList(getPattern(), __internal_getDateSeparator(), false);
            String dateSeparator = StrUtils.replaceSubstring(__internal_getDateSeparator(), "'", "");
            StringBuilder patternRegex = new StringBuilder();
            for (int i = 0; i < patternGroups.size(); i++) {
                String patternGroup = patternGroups.get(i);
                if (i > 0) {
                    patternRegex.append(StrUtils.escapeStringForRegex(dateSeparator));
                }
                patternRegex.append("[");
                patternRegex.append(StrUtils.escapeStringForRegex(String.valueOf(placeHolderCharacter)));
                patternRegex.append("0-9]{");
                patternRegex.append("1,");
                patternRegex.append(patternGroup.length() + 1);
//                patternRegex.append(Math.max(2, patternGroup.length()));
                patternRegex.append("}");
            }
            datePattern = Pattern.compile(patternRegex.toString());
        }
        return datePattern;
    }

    @Override
    protected String __internal_getDateSearchMask() {
        String pattern = getModifiedPattern();
        String mask = pattern.replaceAll("[dMy]", "#"); // dd.MM.yyyy -> ##.##.####
        return mask;
    }

    @Override
    protected boolean isAmPM() {
        return false;
    }


    @Override
    protected String getLogDateTimeType() {
        return LOG_TYPE;
    }

    @Override
    protected String getTextDimensionDummyPattern() {
        return "##-##-####";
    }

    @Override
    protected String getDefaultPattern() {
        return DWDatePattern.PATTERN_DEFAULT_VALUE;
    }

    @Override
    protected String toDateTimeDefaultStringFromDate(Date dateTime) {
        return DateUtils.toyyyyMMdd_Date(dateTime);
    }

    @Override
    protected java.util.Date toSqlDateTimeFromString(String dateTime) throws ParseException, DateException {
        return DateUtils.toSqlDate_yyyyMMdd(dateTime);
    }

    @Override
    protected boolean isValidDateTime(String dateTime) {
        return DateUtils.isValidDate_yyyyMMdd(dateTime);
    }

    @Override
    protected String buildHumanReadablePattern(String pattern) {
        return DWDatePattern.buildHumanReadablePattern(pattern, __internal_getUiLanguage());
    }

    @Override
    protected String __internal_getOsPattern(String language) {
        return DWDatePattern.getInstance().getDatePattern(language, DWDatePattern.PATTERN_DEFAULT_VALUE);
    }

    @Override
    protected boolean dateTimeEqualsDateTime(Date dateTime1, Date dateTime2) {
        return DateUtils.dateIsEqualDate(dateTime1, dateTime2);
    }

    @Override
    protected String getDateTimeInput() {
        try {
            return DateUtils.toyyyyMMdd_Date(__internal_getDateTimeFormat().parse(htmlDateTimeString));
        } catch (ParseException e) {
            return "";
        }
    }
}
