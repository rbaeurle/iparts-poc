/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.util.misc.id.IdWithType;

public class iPartsKgTuPredictionId extends IdWithType {

    // Für die Serialisierung (unterdrückt die Warning beim Kompilieren):
    private static final long serialVersionUID = 0xC7A3F0FF;

    public static final String TYPE = "DA_iPartsKgTuPredictionId";

    private enum INDEX {DIALOG_ID}

    private iPartsDialogBCTEPrimaryKey bctePrimaryKey = null;
    private String hmmsmWithSeries = "";

    /**
     * Der normale Konstruktor
     *
     * @param dialogId
     */
    public iPartsKgTuPredictionId(String dialogId) {
        super(TYPE, new String[]{ dialogId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsKgTuPredictionId() {
        this("");
    }

    public String getDialogId() {
        return id[INDEX.DIALOG_ID.ordinal()];
    }

    /**
     * Macht aus einer DIALOG-ID 'C205|02|12|20|0210|0001|||FA|20130102155943' einen BCTE-Schlüssel
     *
     * @return
     */
    public iPartsDialogBCTEPrimaryKey getAsBCTEPrimaryKey() {
        // Nur bei Bedarf und nur einmalig berechnen.
        if (bctePrimaryKey == null) {
            bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(getDialogId());
        }
        return bctePrimaryKey;
    }

    /**
     * Macht aus dem BCTE-Schlüssel den Suchstring für DHK_BR_HMMSM.
     *
     * @return z.B. 'C205&02&12&20'
     */
    public String getHmMSmWithSeries() {
        // Nur bei Bedarf und nur einmalig berechnen.
        if (hmmsmWithSeries.isEmpty()) {
            iPartsDialogBCTEPrimaryKey bcteKey = getAsBCTEPrimaryKey();
            if (bcteKey != null) {
                hmmsmWithSeries = bcteKey.getHmMSmId().toString(iPartsConst.K_SOURCE_CONTEXT_DELIMITER, true);
            }
        }
        return hmmsmWithSeries;
    }
}
