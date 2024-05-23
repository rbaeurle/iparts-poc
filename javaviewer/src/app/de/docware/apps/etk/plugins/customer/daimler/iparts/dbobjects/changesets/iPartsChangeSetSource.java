/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelsAggsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSAAModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketEDSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketMBSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketSaaStatesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;

import java.util.EnumSet;

/**
 * Quelle für ein Änderungsset {@link iPartsDataChangeSet}.
 */
public enum iPartsChangeSetSource {
    AUTHOR_ORDER(iPartsAuthorOrderId.TYPE),
    IMPORTER(""), // Hier gibt es mehrere Möglichkeiten - Welche ist richtig?
    PRODUCT(iPartsProductId.TYPE),
    SERIES(iPartsSeriesId.TYPE),
    SAA_MODEL(iPartsSAAModelsId.TYPE),
    MODEL_AS(iPartsModelId.TYPE),
    MODEL_AGGS(iPartsModelsAggsId.TYPE),
    SA(iPartsSaId.TYPE),
    SAA(iPartsSaaId.TYPE),
    DICTIONARY(iPartsDictMetaId.TYPE),
    UNITTESTS(""),
    AUTO_RELEASE(""), // Hier gibt es mehrere Möglichkeiten (Werkseinsatzdaten für Stücklisteneinträge und für Farbvarianten)!
    PEM(iPartsPemId.TYPE),
    COPY_TU(AssemblyId.TYPE),
    UPDATE_PSK_TU(AssemblyId.TYPE),
    DELETE_EMPTY_TU(AssemblyId.TYPE),
    SAA_WORKBASKET(iPartsWorkBasketSaaStatesId.TYPE),
    KEM_WORKBASKET(iPartsKEMWorkBasketEDSId.TYPE),
    KEM_WORKBASKET_MBS(iPartsKEMWorkBasketMBSId.TYPE),
    WORK_BASKET_MISSING(iPartsNutzDokSAAId.TYPE),
    GENERAL_WORKBASKET(iPartsDataInternalTextId.TYPE),
    LOCK_ENTRIES(PartListEntryId.TYPE);

    private String masterDataObjectType;

    /**
     * Erzeugt einen neuen Änderungsset-Enum-Wert
     *
     * @param masterDataObjectType Typ vom Master-DataObject dieses Änderungssets
     */
    iPartsChangeSetSource(String masterDataObjectType) {
        this.masterDataObjectType = masterDataObjectType;
    }

    /**
     * Typ vom Master-DataObject dieses Änderungssets
     *
     * @return
     */
    public String getMasterDataObjectType() {
        return masterDataObjectType;
    }

    // Das EnumSet specialChangeSetSources enthält alle ChangeSet-Quellen, wo beim Commit des ChangeSets die SerializedDBDateObjects
    // explizit NICHT in die DB geschrieben werden sollen, weil dies anderweitig gemacht wird (z.B. über iPartsRevisionChangeSet.saveDataObjectWithChangeSet())
    private static EnumSet<iPartsChangeSetSource> specialChangeSetSources = EnumSet.of(IMPORTER, PRODUCT, SERIES, SAA_MODEL, MODEL_AS, MODEL_AGGS,
                                                                                       SA, SAA, DICTIONARY, PEM, KEM_WORKBASKET,
                                                                                       KEM_WORKBASKET_MBS, WORK_BASKET_MISSING,
                                                                                       GENERAL_WORKBASKET);

    public static iPartsChangeSetSource getSourceByDbValue(String dbValue) {
        try {
            return iPartsChangeSetSource.valueOf(dbValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isSpecialChangeSetSource(iPartsChangeSetSource source) {
        return specialChangeSetSources.contains(source);
    }
}