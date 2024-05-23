/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert einen ATS- (After treatment system/Abgasnachbehandlung) Ident mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 * relevant ist dabei nur die neue Darstellung
 */
public class ATSIdent extends AggregateIdent {

    private static final int IDENT_NO_LENGTH = 7;

    public static final String TYPE = "DA_ATSIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.AFTER_TREATMENT;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.AFTER_TREATMENT_SYSTEM;

    private enum INDEX {ATSIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param atsIdent
     */
    public ATSIdent(EtkProject project, String atsIdent) {
        super(project, TYPE, atsIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public ATSIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getATSIdent() {
        return id[INDEX.ATSIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getATSIdent() + ") ATSIDENT";
    }

    @Override
    public String getFormattedIdent() {
        return extractModelNumber() + " " + extractFactorySign() + " " + extractSerialNumberString();
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        String factorySign = extractFactorySign();
        String factoryDescription = setValueWithDefault(getShowText(IDENT_TYPE, factorySign));
        result.add(new String[]{ "!!Zusatzinformation", factorySign, factoryDescription });

        addDisplayGridValuesForSerialNumber(result, "!!ATS-Zählnummer", true);
        return result;
    }

    @Override
    public boolean isValidId() {
        return isModelNumberValid() && isFactorySignValid() && isSerialNumberValid();
    }

    /**
     * Befüllt die Werke Combobox
     */
    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        fillFactoryCombobox(comboboxFactory, IDENT_TYPE);
        //fillFactoryCombobox(comboboxFactory, ENUM_KEY);

    }

    @Override
    public String extractFactorySign() {
        return StrUtils.copySubString(getATSIdent(), 6, 1);
    }

    @Override
    public int getSerialNumberLength() {
        return IDENT_NO_LENGTH;
    }

    @Override
    public String extractSerialNumberString() {
        return StrUtils.copySubString(getATSIdent(), 7, getSerialNumberLength());
    }

}
