package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.misc.DWTimePattern;
import de.docware.framework.modules.gui.misc.guiapps.guidesigner.controls.GUIDesignerProperty;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.csscreator.CssCreator;
import de.docware.framework.modules.gui.output.j2ee.jscreator.JavascriptCreator;
import de.docware.framework.modules.gui.reader.XmlGuiParser;
import de.docware.framework.modules.gui.writer.JavaGuiWriter;
import de.docware.framework.modules.gui.writer.XmlGuiWriter;
import de.docware.framework.utils.HTMLUtils;
import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Komponente zur Eingabe / Anzeige von Uhrzeiten mit Pattern für Filter
 */
public class GuiExtTimeEditTextField extends AbstractGuiExtDateTimeEditTextField {

    public static String TYPE = "edittimetextfield";
    public static String LOG_TYPE = "time";
    private static boolean showSecondsDefaultValue = false;

    private Boolean showSeconds;

    public static void getCssAndJavascript(CssCreator cssCreator, JavascriptCreator jsCreator) {
    }

    public GuiExtTimeEditTextField() {
        super(TYPE);
    }

    public GuiExtTimeEditTextField(Date date) {
        this();
        setDateTime(date);
    }

    @Override
    protected String getDateFormatForSQL() {
        return DateUtils.simpleTimeFormatHHmmss;
    }


    @Override
    protected void cloneProperties(AbstractGuiControl control) {
        if (control.isOfType(getType())) {
            super.cloneProperties(control);
            GuiExtTimeEditTextField field = (GuiExtTimeEditTextField)control;
            field.showSeconds = showSeconds;
        }
    }

    @Override
    protected String __internal_getDateSeparator() {
        if (dateSeparator == null) {
            // Suche nach Zeichengruppierungen die weder 'd', 'M' noch 'y' enthalten - wenn eine Gruppierung existiert, wähle die erste
            Matcher matcher = Pattern.compile("([^hHms]+)").matcher(getPattern());
            if (matcher.find()) {
                dateSeparator = matcher.group();
            } else {
                Logger.getLogger().throwRuntimeException("Failed to determine date separator for pattern " + getPattern());
            }
        }
        return dateSeparator;
    }

    @Override
    protected Pattern __internal_getHtmlMatcherPattern() {
        if (datePattern == null) {
            String pattern = getPattern();
            String dateSeparator = StrUtils.replaceSubstring(__internal_getDateSeparator(), "'", "");
            pattern = StrUtils.replaceSubstring(pattern, "'", "");
            pattern = StrUtils.replaceSubstring(pattern, " ", dateSeparator);
            List<String> patternGroups = StrUtils.toStringList(pattern, dateSeparator, true);
            StringBuilder patternRegex = new StringBuilder();
            for (int i = 0; i < patternGroups.size(); i++) {
                String patternGroup = patternGroups.get(i);
                if ((i > 0) && !patternGroup.contains("a")) {
                    patternRegex.append(StrUtils.escapeStringForRegex(dateSeparator));
                }
                if (patternGroup.equals("h")) {
                    patternGroup += "h";
                }
                if (patternGroup.contains("a")) {
                    patternRegex.append('\\');
                    patternRegex.append('s');
                    patternRegex.append("*");
                    patternRegex.append("[AP]M");
                } else {
                    patternRegex.append("[");
                    patternRegex.append(StrUtils.escapeStringForRegex(String.valueOf(placeHolderCharacter)));
                    patternRegex.append("0-9]{");
                    patternRegex.append("1,");
                    patternRegex.append(patternGroup.length() + 1);
//                patternRegex.append(Math.max(2, patternGroup.length()));
                    patternRegex.append("}");
                }
            }
            datePattern = Pattern.compile(patternRegex.toString());
        }
        return datePattern;
    }

    @Override
    protected String __internal_getDateSearchMask() {
        String pattern = getModifiedPattern();
        String mask = pattern.replaceAll("[hHms]", "#"); // HH:mm:ss -> ##:##:##
        return mask;
    }

    @Override
    protected boolean isAmPM() {
        // AM/PM enthalten?
        return getPattern().contains("a");
    }

    @Override
    protected String getLogDateTimeType() {
        return LOG_TYPE;
    }

    @Override
    protected String getTextDimensionDummyPattern() {
        // absichtlich - als Separator verwendet, weil es mit : zu schmal wird
        String dummyPattern;
        if (isShowSeconds()) {
            dummyPattern = "##-##-##";
        } else {
            dummyPattern = "##-##";
        }

        // AM/PM enthalten?
        if (getPattern().contains("a")) {
            dummyPattern += " ##";
        }

        return dummyPattern;
    }

    @Override
    protected String getDefaultPattern() {
        return DWTimePattern.PATTERN_DEFAULT_VALUE;
    }

    @Override
    protected String toDateTimeDefaultStringFromDate(Date dateTime) {
        return DateUtils.toHHmmss_Time(dateTime);
    }

    @Override
    protected Date toSqlDateTimeFromString(String dateTime) throws ParseException, DateException {
        return DateUtils.toSqlTime_HHmmss(dateTime);
    }

    @Override
    protected boolean isValidDateTime(String dateTime) {
        return DateUtils.isValidTime_HHmmss(dateTime);
    }

    @Override
    protected String buildHumanReadablePattern(String pattern) {
        return DWTimePattern.buildHumanReadablePattern(pattern, __internal_getUiLanguage());
    }

    @Override
    protected String __internal_getOsPattern(String language) {
        return DWTimePattern.getInstance().getTimePattern(isShowSeconds(), language, DWTimePattern.PATTERN_DEFAULT_VALUE);
    }

    @Override
    protected boolean dateTimeEqualsDateTime(Date dateTime1, Date dateTime2) {
        return DateUtils.timeIsEqualTime(dateTime1, dateTime2);
    }

    @Override
    protected String getDateTimeInput() {
        try {
            return DateUtils.toHHmmss_Time(__internal_getDateTimeFormat().parse(htmlDateTimeString));
        } catch (ParseException e) {
            return "";
        }
    }

    @GUIDesignerProperty
    public boolean isShowSeconds() {
        if (showSeconds == null) {
            showSeconds = showSecondsDefaultValue;
        }
        return showSeconds;
    }

    public void setShowSeconds(boolean showSeconds) {
        if (this.showSeconds == showSeconds) {
            return;
        }

        boolean updateMinimumWidth = getMinimumWidth() == HTMLUtils.getTextDimension(font, getTextDimensionDummyPattern()).getWidth();
        this.showSeconds = showSeconds;
        if (updateMinimumWidth) {
            setMinimumWidth(HTMLUtils.getTextDimension(font, getTextDimensionDummyPattern()).getWidth());
        }
        __internal_resetAttributes();
        __internal_updateComponent();
    }

    @Override
    protected void readComponentSpecific(XmlGuiParser parser, Element element) {
        if (element.hasAttribute("showSeconds")) {
            this.setShowSeconds(parser.getBooleanAttribute(element, "showSeconds"));
        }

        super.readComponentSpecific(parser, element);
    }

    @Override
    protected void writeComponentSpecific(XmlGuiWriter writer, Document xmlDocument, Element element) {
        // Attribute
        writer.writeBooleanAttribute(element, "showSeconds", showSeconds, showSecondsDefaultValue);

        super.writeComponentSpecific(writer, xmlDocument, element);
    }

    @Override
    protected void writeComponentSpecific(JavaGuiWriter writer) {
        // Attribute
        writer.appendBooleanAttributeCode(this, "setShowSeconds", showSeconds, showSecondsDefaultValue);

        super.writeComponentSpecific(writer);
    }
}
