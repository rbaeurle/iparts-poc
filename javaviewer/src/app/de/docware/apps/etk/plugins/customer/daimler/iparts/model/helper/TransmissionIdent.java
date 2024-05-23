/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert einen Transmission- (Getriebe-) Ident mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public class TransmissionIdent extends AggregateIdent {

    public static final String TYPE = "DA_TransmissionIdent";
    public static final String ENUM_KEY = "TransmissionFactoryKey";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.TRANSMISSION;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.TRANSMISSION;

    private enum INDEX {TRANSMISSIONIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param transmissionIdent
     */
    public TransmissionIdent(EtkProject project, String transmissionIdent) {
        super(project, TYPE, transmissionIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public TransmissionIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getTransmissionIdent() {
        return id[INDEX.TRANSMISSIONIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getTransmissionIdent() + ") TRANSMISSIONIDENT";
    }

    @Override
    public String getFormattedIdent() {
        String result = extractModelNumber() + " " + extractFactorySign() + " " + extractSerialNumberString();
        if (isExchangeAggregate()) {
            result = result + "  (" + TranslationHandler.translate(DCAggregateTypes.EXCHANGE_TRANSMISSION) + ")";
        }
        return result;
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        String factorySign = extractFactorySign();
        String factoryKeyDescription = setValueWithDefault(getShowText(IDENT_TYPE, factorySign));
        //String factoryKeyDescription = setValueWithDefault(getEnumText(ENUM_KEY, factoryKey));
        result.add(new String[]{ "!!Montage-Werkskennung", factorySign, factoryKeyDescription });

        addDisplayGridValuesForSerialNumber(result, "!!Endnummer", true);
        return result;
    }

    /**
     * Befüllt die Werke Combobox
     */
    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        fillFactoryCombobox(comboboxFactory, DatacardIdentOrderTypes.TRANSMISSION);
        //fillFactoryCombobox(comboboxFactory, ENUM_KEY);
    }


    @Override
    public boolean isValidId() {
        return isModelNumberValid() && isFactorySignValid() && isSerialNumberValid();
    }


    @Override
    public String extractFactorySign() {
        return StrUtils.copySubString(getTransmissionIdent(), 6, 1);
    }

    @Override
    public String extractSerialNumberString() {
        return StrUtils.copySubString(getTransmissionIdent(), getSerialNumberLength(), getSerialNumberLength());
    }

    @Override
    public int getSerialNumberLength() {
        return 7;
    }

    /**
     * Handelt es sich um ein Austauschgetriebe?
     *
     * @return
     */
    public boolean isExchangeAggregate() {
        String exchangeChar = StrUtils.copySubString(getTransmissionIdent(), 7, 1);
        return exchangeChar.equals("9") || exchangeChar.toUpperCase().equals("T") || exchangeChar.toUpperCase().equals("R");
    }
}
