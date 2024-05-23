/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert einen Platform-(Aufbau-)Ident (= Pritsche) mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public class PlatformIdent extends AggregateIdent {

    public static final String TYPE = "DA_PlatformIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.PLATFORM;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.PLATFORM;

    private enum INDEX {PLATFORMIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param platformIdent
     */
    public PlatformIdent(EtkProject project, String platformIdent) {
        super(project, TYPE, platformIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public PlatformIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getPlatformIdent() {
        return id[INDEX.PLATFORMIDENT.ordinal()];
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        String remainingIdent = extractRemainingIdent();
        result.add(new String[]{ "!!restlicher Ident", remainingIdent, setValueWithDefault(getShowText(IDENT_TYPE, remainingIdent)) });

        return result;
    }

    @Override
    public String toString() {
        return "(" + getPlatformIdent() + ") PLATFORMIDENT";
    }

    @Override
    public String getFormattedIdent() {
        return extractModelNumber() + " " + extractRemainingIdent();
    }

    @Override
    public boolean isValidId() {
        return isModelNumberValid(); // bei Pritschen wird nur das Baumuster verwendet, alles andere interessiert nicht
    }

    @Override
    public String extractSerialNumberString() {
        return ""; // bei Pritschen gibt es keine Endnurmmer
    }

    @Override
    public int getSerialNumberLength() {
        return 0;
    }

    public String extractRemainingIdent() {
        String platformIdent = getPlatformIdent();
        return StrUtils.copySubString(platformIdent, 6, platformIdent.length() - 6);
    }
}
