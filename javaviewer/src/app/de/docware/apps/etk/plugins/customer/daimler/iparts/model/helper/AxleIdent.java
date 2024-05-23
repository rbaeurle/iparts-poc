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
 * Repräsentiert einen Axle- (Achs-) Ident mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public class AxleIdent extends AggregateIdent {

    public static final String TYPE = "DA_AxleIdent";
    public static final String ENUM_KEY_OLD = "AxleCounterKey";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.AXLE_FRONT_NEW;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.AXLE;

    private enum INDEX {AXLEIDENT}

//    private boolean isNewAxleIdent;

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param axelIdent
     */
    public AxleIdent(EtkProject project, String axelIdent) {
        super(project, TYPE, axelIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public AxleIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getAxleIdent() {
        return id[INDEX.AXLEIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getAxleIdent() + ") AXLEIDENT";
    }

//    public void setNewAxleIdent(boolean newAxleIdent) {
//        isNewAxleIdent = newAxleIdent;
//    }

    @Override
    public String getFormattedIdent() {
        if (isNewIdentSpecification()) {
            return extractModelNumber() + " " + extractFactorySign() + " " + extractSerialNumberString();
        } else {
            return extractModelNumber() + " " + extractFactorySign() + " " + extractCounterKey() + " " + extractSerialNumberString();
        }
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        if (isNewIdentSpecification()) {
            String factorySign = extractFactorySign();
            String factoryKeyDescription = setValueWithDefault(getShowText(IDENT_TYPE, factorySign));
            result.add(new String[]{ "!!Zusatzinformation", factorySign, factoryKeyDescription });

            addDisplayGridValuesForSerialNumber(result, "!!Endnummer", true);
        } else {
            String placeHolder = extractFactorySign();
            if (placeHolder.equals(" ")) {
                result.add(new String[]{ "!!Platzhalter", "<leer>", "" });
            } else {
                result.add(new String[]{ "!!Platzhalter", placeHolder, UNKNOWN_VALUE });
            }
            String counterKey = extractCounterKey();
            String counterKeyDescription = setValueWithDefault(getEnumText(ENUM_KEY_OLD, counterKey));
            result.add(new String[]{ "!!Zähler-Kennzeichen", counterKey, counterKeyDescription });

            addDisplayGridValuesForSerialNumber(result, "!!Fortlaufende Nummer", true);
        }
        return result;
    }

    @Override
    public boolean isFactorySignValid() {
        String factoryKey = extractFactorySign();
        if (StrUtils.isValid(factoryKey)) {
            if (isNewIdentSpecification()) {
                // mögliche Werte testen
                return containsToken(IDENT_TYPE, factoryKey);
                //return containsEnumToken(ENUM_KEY, factoryKey);
            } else {
                return factoryKey.equals(" ");
            }
        }
        return false;
    }

    public boolean isCounterKeyValid() {
        if (isOldIdentSpecification()) {
            String counterKey = extractCounterKey();
            if (StrUtils.isValid(counterKey)) {
                // mögliche Werte testen
                return containsEnumToken(ENUM_KEY_OLD, counterKey);
            }
        }
        return false;
    }

    @Override
    public String getFactorySign() {
        if (isNewIdentSpecification()) {
            return super.getFactorySign();
        } else {
            return getCounterKey();
        }
    }

    public String getCounterKey() {
        if (isOldIdentSpecification()) {
            return extractCounterKey();
        }
        return "";
    }

    @Override
    public boolean isValidId() {
        if (isNewIdentSpecification()) {
            return isModelNumberValid() && isFactorySignValid() && isSerialNumberValid();
        } else {
            return isModelNumberValid() && isFactorySignValid() && isCounterKeyValid() && isSerialNumberValid();
        }
    }

    /**
     * Befüllt die Werke Combobox
     */
    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        if (isNewIdentSpecification()) {
            fillFactoryCombobox(comboboxFactory, DatacardIdentOrderTypes.AXLE_FRONT_NEW);
            //fillFactoryCombobox(comboboxFactory, ENUM_KEY);
        } else {
            // für AXLE_FRONT_OLD sind in der WMI-Tabelle keine Werte vorhanden
            //fillFactoryCombobox(comboboxFactory, DatacardIdentOrderTypes.AXLE_FRONT_OLD);
            fillFactoryCombobox(comboboxFactory, ENUM_KEY_OLD);
        }
    }


    @Override
    public String extractFactorySign() {
        return StrUtils.copySubString(getAxleIdent(), 6, 1);
    }

    private String extractCounterKey() {
        if (isOldIdentSpecification()) {
            return StrUtils.copySubString(getAxleIdent(), 7, 1);
        }
        return "";
    }

    public String extractSerialNumberString() {
        if (isNewIdentSpecification()) {
            return StrUtils.copySubString(getAxleIdent(), 7, getSerialNumberLength());
        } else {
            return StrUtils.copySubString(getAxleIdent(), 8, getSerialNumberLength());
        }
    }

    @Override
    public int getSerialNumberLength() {
        if (isNewIdentSpecification()) {
            return 7;
        } else {
            return 6;
        }
    }

}