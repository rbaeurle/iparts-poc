package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;

/**
 * Hilfsklasse für BCTE-Key ohne AA
 * Wird mit dem normalen BCTE-Key oder {@link EtkDataPartListEntry} erzeugt und liefert den modifizierten BCTE-Key und den
 * Such-String für SQL-Abfragen zurück.
 */
public class PartListEntryReferenceKeyByAA {

    private iPartsDialogBCTEPrimaryKey bcteKeyWithoutAA;
    private String searchSQLBcteKeyWithoutAA;
    private String originalAA;

    public PartListEntryReferenceKeyByAA(iPartsDialogBCTEPrimaryKey bcteKey) {
        init(bcteKey);
    }

    public PartListEntryReferenceKeyByAA(EtkDataPartListEntry partListEntry) {
        init(iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry));
    }

    private void init(iPartsDialogBCTEPrimaryKey bcteKey) {
        if (bcteKey != null) {
            originalAA = bcteKey.aa;
            bcteKeyWithoutAA = bcteKey.cloneMe();
            bcteKeyWithoutAA.aa = "*";
            searchSQLBcteKeyWithoutAA = bcteKeyWithoutAA.createDialogGUID();
            bcteKeyWithoutAA.aa = "";
        } else {
            bcteKeyWithoutAA = null;
            searchSQLBcteKeyWithoutAA = "";
        }
    }

    public boolean isValid() {
        return bcteKeyWithoutAA != null;
    }

    public iPartsDialogBCTEPrimaryKey getBcteKeyWithoutAA() {
        return bcteKeyWithoutAA;
    }

    public String getSearchSQLBcteKeyWithoutAA() {
        return searchSQLBcteKeyWithoutAA;
    }

    public String getOriginalAA() {
        return originalAA;
    }
}
