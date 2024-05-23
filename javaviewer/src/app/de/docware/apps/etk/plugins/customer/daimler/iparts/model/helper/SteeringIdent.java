/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert einen Steering-(Lenkung-)Ident mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public class SteeringIdent extends AggregateIdent {

    public static final String TYPE = "DA_SteeringIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.STEERING;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.STEERING;

    private enum INDEX {STEERINGIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param steeringIdent
     */
    public SteeringIdent(EtkProject project, String steeringIdent) {
        super(project, TYPE, steeringIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public SteeringIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getSteeringIdent() {
        return id[INDEX.STEERINGIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getSteeringIdent() + ") STEERINGIDENT";
    }

    @Override
    public String getFormattedIdent() {
        if (isSmallIdent()) {
            return extractModelNumber() + " " + extractSerialNumberString();
        } else {
            return extractModelNumber() + " " + extractExtraInfo1() + extractExtraInfo2() + " " + extractSerialNumberString() + " " + extractRemainder();
        }
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        if (!isSmallIdent()) {
            String extraInfo1 = extractExtraInfo1();
            result.add(new String[]{ "!!Zusatzinformation 1", extraInfo1, setValueWithDefault(extraInfo1) });

            String extraInfo2 = extractExtraInfo2();
            result.add(new String[]{ "!!Zusatzinformation 2", extraInfo2, setValueWithDefault(extraInfo2) });
        }

        addDisplayGridValuesForSerialNumber(result, "!!Zählnummer", true);

        String extra = extractRemainder();
        if (!extra.isEmpty()) {
            result.add(new String[]{ "!!Zusatz", extra, extra });
        }
        return result;
    }

    public boolean isExtraInfo1Valid() {
        String extraExtraInfo1 = extractExtraInfo1();
        if (StrUtils.isValid(extraExtraInfo1)) {
            // mögliche Werte testen
            return true;
        }
        return false;
    }

    public boolean isExtraInfo2Valid() {
        String extraInfo2 = extractExtraInfo1();
        if (StrUtils.isValid(extraInfo2)) {
            // mögliche Werte testen
            return true;
        }
        return false;
    }

    public String getExtraInfo1() {
        if (isExtraInfo1Valid()) {
            return extractExtraInfo1();
        }
        return "";
    }

    public String getExtraInfo2() {
        if (isExtraInfo2Valid()) {
            return extractExtraInfo2();
        }
        return "";
    }

    @Override
    public boolean isValidId() {
        if (isSmallIdent()) {
            return isModelNumberValid() && isSerialNumberValid();
        } else {
            return isModelNumberValid() && isExtraInfo1Valid() && isExtraInfo2Valid() && isSerialNumberValid();
        }
    }


    private String extractExtraInfo1() {
        return StrUtils.copySubString(getSteeringIdent(), 6, 1);
    }

    private String extractExtraInfo2() {
        return StrUtils.copySubString(getSteeringIdent(), 7, 1);
    }

    @Override
    public String extractSerialNumberString() {
        if (isSmallIdent()) {
            return StrUtils.copySubString(getSteeringIdent(), 6, getSerialNumberLength());
        } else {
            return StrUtils.copySubString(getSteeringIdent(), 8, getSerialNumberLength());
        }
    }

    @Override
    public int getSerialNumberLength() {
        return 6;
    }

    private String extractRemainder() {
        String ident = getSteeringIdent();
        if (ident.length() > 14) {
            return getSteeringIdent().substring(14);
        }
        return "";
    }

    private boolean isSmallIdent() {
        return getIdent().length() <= 12;
    }
}
