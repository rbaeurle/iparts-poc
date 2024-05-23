package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

public class HighVoltageBatIdent extends AggregateIdent {

    public static final String TYPE = "DA_HighVoltageBatIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.HIGH_VOLTAGE_BATTERY;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.HIGH_VOLTAGE_BATTERY;

    private enum INDEX {HIGHVOLTAGEBATIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param highVoltageBatIdent
     */
    public HighVoltageBatIdent(EtkProject project, String highVoltageBatIdent) {
        super(project, TYPE, highVoltageBatIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public HighVoltageBatIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getHighVoltageBatIdent() {
        return id[INDEX.HIGHVOLTAGEBATIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getHighVoltageBatIdent() + ") HIGHVOLTAGEBATIDENT";
    }

    @Override
    public String getFormattedIdent() {
        String result = extractModelNumber() + " " + extractFactorySign() + " " + extractSerialNumberString();
        if (isExchangeAggregate()) {
            result = result + "  (" + TranslationHandler.translate(DCAggregateTypes.EXCHANGE_ENGINE) + ")";
        }
        return result;
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        String factorySign = extractFactorySign();
        String factoryKeyDescription = setValueWithDefault(getShowText(IDENT_TYPE, factorySign));
        result.add(new String[]{ "!!Montagewerk", factorySign, factoryKeyDescription });

        addDisplayGridValuesForSerialNumber(result, "!!Hochvoltbatterie-Endnummer", true);
        return result;
    }

    public boolean isValidId() {
        return isModelNumberValid() && isFactorySignValid() && isSerialNumberValid();
    }

    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        fillFactoryCombobox(comboboxFactory, IDENT_TYPE);
    }

    @Override
    public String extractFactorySign() {
        return StrUtils.copySubString(getHighVoltageBatIdent(), 6, 2);
    }

    @Override
    public String extractSerialNumberString() {
        return StrUtils.copySubString(getHighVoltageBatIdent(), 8, getSerialNumberLength());
    }

    @Override
    public int getSerialNumberLength() {
        return 6;
    }

    @Override
    public boolean isExchangeAggregate() {
        return false; // Aktuell gibt es keine Austausch-Hochvoltbatterien
    }
}
