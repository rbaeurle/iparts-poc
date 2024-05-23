/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert einen Cab- (Fahrerhaus-) Ident mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public class CabIdent extends AggregateIdent {

    public static final String TYPE = "DA_CabIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.CAB;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.CAB;

    private enum INDEX {CABIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param cabIdent
     */
    public CabIdent(EtkProject project, String cabIdent) {
        super(project, TYPE, cabIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public CabIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getCabIdent() {
        return id[INDEX.CABIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getCabIdent() + ") CABIDENT";
    }

    @Override
    public String getFormattedIdent() {
        return extractModelNumber() + " " + extractFactorySign() + " " + extractSteeringKey() + " " + extractSerialNumberString();
    }

    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        String factorySign = extractFactorySign();
        String factoryDescription = setValueWithDefault(getShowText(IDENT_TYPE, factorySign));
        result.add(new String[]{ "!!Montage-/Produktionswerke", factorySign, factoryDescription });

        String steeringKey = extractSteeringKey();
        String steeringDescription = setValueWithDefault(SteeringIdentKeys.getSteeringIdentKeyByValue(steeringKey).getDescription());
        result.add(new String[]{ "!!Lenkung/Zerlegungsgrad", steeringKey, steeringDescription });

        addDisplayGridValuesForSerialNumber(result, "!!Zählnummer", true);
        return result;
    }

    public boolean isSteeringKeyValid() {
        return SteeringIdentKeys.isValid(extractSteeringKey(), getCabIdent(), true);
    }

    public String getSteeringKey() {
        if (isSteeringKeyValid()) {
            return extractSteeringKey();
        }
        return SteeringIdentKeys.STEERING_INDEPENDENT;
    }

    @Override
    public boolean isValidId() {
        return isModelNumberValid() && isSteeringKeyValid() && isFactorySignValid() && isSerialNumberValid();
    }

    /**
     * Befüllt die Werke Combobox
     */
    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        fillFactoryCombobox(comboboxFactory, DatacardIdentOrderTypes.CAB);
        //fillFactoryCombobox(comboboxFactory, ENUM_KEY);
    }

    private String extractSteeringKey() {
        return StrUtils.copySubString(getCabIdent(), 7, 1);
    }

    @Override
    public String extractFactorySign() {
        return StrUtils.copySubString(getCabIdent(), 6, 1);
    }

    @Override
    public String extractSerialNumberString() {
        return StrUtils.copySubString(getCabIdent(), 8, getSerialNumberLength());
    }

    @Override
    public int getSerialNumberLength() {
        return 6;
    }
}
