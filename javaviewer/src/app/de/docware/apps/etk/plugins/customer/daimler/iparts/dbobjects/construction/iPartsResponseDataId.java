/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.HashSet;
import java.util.Set;

/**
 * Repräsentiert eine Rückmeldedaten-GUID aus der Tabelle TABLE_DA_RESPONSE_DATA im iParts Plug-in.
 */
public class iPartsResponseDataId extends IdWithType {

    public static final String TYPE = "DA_iPartsResponseDataId";
    public static final String DESCRIPTION = "!!Rückmeldedaten";

    protected enum INDEX {FACTORY, SERIES_NO, AA, BMAA, PEM, ADAT, IDENT, AS_DATA}

    /**
     * Für DIALOG Rückmeldedaten
     */
    public iPartsResponseDataId(String factory, String seriesNo, String ausfuehrungsArt, String bmaa, String pem, String adat,
                                String ident, boolean asData) {
        super(TYPE, new String[]{ factory, seriesNo, ausfuehrungsArt, bmaa, pem, adat, ident, SQLStringConvert.booleanToPPString(asData) });
    }

    /**
     * Für ELDAS Rückmeldedaten
     *
     * @param pseudoPEM
     * @param ident     aktuell ggf. mit inkl. Factory-Code an erster Stelle (weil das für DIALOG auch so gemacht ist); leer für Bis-Ident
     */
    public iPartsResponseDataId(String pseudoPEM, String ident) {
        this("", "", "", "", pseudoPEM, "", ident, false);
    }

    /**
     * Für Rückmeldedaten basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsResponseDataId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsResponseDataId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsResponseDataId() {
        this("", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getFactory() {
        return id[INDEX.FACTORY.ordinal()];
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String getAusfuehrungsArt() {
        return id[INDEX.AA.ordinal()];
    }

    public String getBmaa() {
        return id[INDEX.BMAA.ordinal()];
    }

    public String getIdent() {
        return id[INDEX.IDENT.ordinal()];
    }

    public String getPem() {
        return id[INDEX.PEM.ordinal()];
    }

    public boolean getAsData() {
        return SQLStringConvert.ppStringToBoolean(id[INDEX.AS_DATA.ordinal()]);
    }

    public String getAdatAttribute() {
        return id[INDEX.ADAT.ordinal()];
    }

    /**
     * Liefert alle relevanten {@link iPartsDialogBCTEPrimaryKey}s der Teilepositionen zurück, deren Werkseinsatzdaten
     * die PEM verwenden, auf Werk, Baureihe und Ausführungsart beschränkt. Wenn die Ausführungsart leer ist, wird nicht auf diese beschränkt
     *
     * @param project
     * @return
     */
    public Set<iPartsDialogBCTEPrimaryKey> getAllRelevantBCTEPrimaryKeys(EtkProject project) {
        Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys = new HashSet<>();
        String aa = getAusfuehrungsArt();
        if (!StrUtils.isValid(aa)) {
            aa = null;
        }
        iPartsDataFactoryDataList factoryDataList = iPartsDataFactoryDataList.loadFactoryDataForFactorySeriesAAandPEM(project, getFactory(),
                                                                                                                      getSeriesNo(), getPem(), aa);
        for (iPartsDataFactoryData factoryData : factoryDataList) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(factoryData.getAsId().getGuid());
            if (bctePrimaryKey != null) {
                relevantBCTEPrimaryKeys.add(bctePrimaryKey);
            }
        }
        return relevantBCTEPrimaryKeys;
    }
}