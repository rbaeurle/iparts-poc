/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiTimeIntervalEditPanel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsTimeInterval;
import de.docware.framework.combimodules.config_gui.UniversalConfigCustomOptionWithControl;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;

/**
 * {@link de.docware.framework.combimodules.config_gui.UniversalConfigCustomOptionWithControl} für
 * die Darstellung des auswählbaren Zeitintervalls in den Adminoptionen. Darf keine Anonyme-Klasse sein, da bei verschiedenen
 * Umgebungsvarianten die Klasse geclont wird.
 */
public class iPartsTimeIntervalCustomOptionWithControl extends UniversalConfigCustomOptionWithControl {

    /**
     * Erzeugt eine neue benutzerdefinierte Konfigurationsoption inkl. Name und {@link AbstractGuiControl}.
     *
     * @param customOption Benutzerdefinierte Konfigurationsoption
     * @param name         Name für diese Konfigurationsoption, der links von dem {@link AbstractGuiControl} zum Editieren des
     *                     Wertes angezeigt werden
     * @param isMandatory
     * @param control      {@link AbstractGuiControl} zum Editieren des Wertes für diese Konfigurationsoption
     */
    public iPartsTimeIntervalCustomOptionWithControl(UniversalConfigCustomOption customOption, String name, boolean isMandatory, AbstractGuiControl control) {
        super(customOption, name, isMandatory, control);
    }

    public iPartsTimeIntervalCustomOptionWithControl(UniversalConfigCustomOption customOption, String name, boolean isMandatory) {
        this(customOption, name, isMandatory, getEmtpyTimeIntervalPanel());
    }

    @Override
    public void configToScreen(ConfigBase config, String pathWithKey) {
        iPartsTimeInterval timeInterval = (iPartsTimeInterval)getConfigValue(config, pathWithKey);
        if (timeInterval != null) {
            String startTime = timeInterval.getStartTime();
            String endTime = timeInterval.getEndTime();
            getControlValue().setStartTime(startTime == null ? "" : startTime);
            getControlValue().setEndTime(endTime == null ? "" : endTime);
        }
    }

    @Override
    public void screenToConfig(ConfigBase config, String pathWithKey) {
        iPartsTimeInterval timeInterval = new iPartsTimeInterval(getStartTime(), getEndTime());
        config.setString(pathWithKey, timeInterval.toString());
    }

    @Override
    public iPartsGuiTimeIntervalEditPanel getControlValue() {
        return (iPartsGuiTimeIntervalEditPanel)getControl();
    }

    public static UniversalConfigOption.UniversalConfigCustomOption getTimeIntervalCustomOption() {
        return new UniversalConfigOption.UniversalConfigCustomOption() {
            @Override
            public Object getConfigValue(ConfigBase config, String pathWithKey) {
                String configValue = config.getString(pathWithKey, (String)getDefaultValue());
                if (configValue != null) {
                    return new iPartsTimeInterval(configValue);
                }
                return null;
            }
        };
    }

    private static iPartsGuiTimeIntervalEditPanel getEmtpyTimeIntervalPanel() {
        iPartsGuiTimeIntervalEditPanel timeIntervalEditPanel = new iPartsGuiTimeIntervalEditPanel();
        timeIntervalEditPanel.alignLeft();
        return timeIntervalEditPanel;
    }

    public String getStartTime() {
        return getControlValue().getStartTimeAsRawString();
    }

    public void setStartTime(String startTime) {
        getControlValue().setStartTime(startTime);
    }

    public String getEndTime() {
        return getControlValue().getEndTimeAsRawString();
    }

    public void setEndTime(String endTime) {
        getControlValue().setEndTime(endTime);
    }
}
