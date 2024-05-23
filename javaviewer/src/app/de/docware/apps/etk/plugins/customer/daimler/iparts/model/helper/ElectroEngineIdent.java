package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

public class ElectroEngineIdent extends AggregateIdent {

    public static final String TYPE = "DA_ElectroEngineIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.ELECTRO_ENGINE;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.ELECTRO_ENGINE;

    private enum INDEX {ELECTROENGINEIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param electroEngineIdent
     */
    public ElectroEngineIdent(EtkProject project, String electroEngineIdent) {
        super(project, TYPE, electroEngineIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public ElectroEngineIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getElectroEngineIdent() {
        return id[ElectroEngineIdent.INDEX.ELECTROENGINEIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getElectroEngineIdent() + ") ELECTROENGINEIDENT";
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

        addDisplayGridValuesForSerialNumber(result, "!!Elektromotor-Endnummer", true);
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
        return StrUtils.copySubString(getElectroEngineIdent(), 6, 1);
    }

    @Override
    public String extractSerialNumberString() {
        return StrUtils.copySubString(getElectroEngineIdent(), 7, getSerialNumberLength());
    }

    @Override
    public int getSerialNumberLength() {
        return 7;
    }

    /**
     * Handelt es sich um einen Austauschmotor?
     *
     * @return
     */
    @Override
    public boolean isExchangeAggregate() {
        // mit DAIMLER-6024: beide Idents sind an der Stelle 8 als Austauschmotor gekennzeichnet
        String exchangeChar = StrUtils.copySubString(getElectroEngineIdent(), 7, 1);
        return exchangeChar.equals("7") || exchangeChar.equals("8") || exchangeChar.equals("9");
    }
}
