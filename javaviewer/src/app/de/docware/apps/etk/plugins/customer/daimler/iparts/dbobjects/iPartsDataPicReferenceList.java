/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicReferenceState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.ArrayUtil;

import java.util.List;
import java.util.Set;

/**
 * DataObjectList für {@link iPartsDataPicReference}
 */
public class iPartsDataPicReferenceList extends EtkDataObjectList<iPartsDataPicReference> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicReference}s für die angegebene Bildreferenz ohne Referenzdatum
     *
     * @param project
     * @param partsPicReferenceId
     * @return
     */
    public static iPartsDataPicReferenceList loadPicReferencesWithoutDate(EtkProject project, iPartsPicReferenceId partsPicReferenceId) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicReferencesForNumber(project, partsPicReferenceId.getPicReferenceNumber(), DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicReference}s für die angegebene Bildreferenz inkl. Referenzdatum
     *
     * @param project
     * @param picReferenceId
     * @return
     */
    public static iPartsDataPicReferenceList loadPicReferencesWithDate(EtkProject project, iPartsPicReferenceId picReferenceId) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicReferencesForNumberAndDate(project, picReferenceId.getPicReferenceNumber(), picReferenceId.getPicReferenceDate(), DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicReference}s für die angegebene Bildreferenz sortiert nach
     * der Revision
     *
     * @param project
     * @param image
     * @return
     */
    public static iPartsDataPicReferenceList loadPicRefWithSortedContainerIdForVariantId(EtkProject project, EtkDataImage image) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicRefForVariantIdSorted(project, image, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadPicRefForVariantIdSorted(EtkProject project, EtkDataImage image, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_VAR_ID }, new String[]{ image.getFieldValue(FIELD_I_IMAGES) },
                          new String[]{ FIELD_DPR_VAR_REV_ID }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataPicReference}s aus der DB für die angegebene Bildnummer und Referenzdatum
     *
     * @param project
     * @param picReferenceNumber
     * @param picReferenceDate
     * @param origin
     */
    private void loadPicReferencesForNumberAndDate(EtkProject project, String picReferenceNumber, String picReferenceDate, DBActionOrigin origin) {
        clear(origin);

        // zunächst nur Suche im Referenzdatum
        String[] whereFields = new String[]{ FIELD_DPR_REF_ID, FIELD_DPR_REF_DATE };
        String[] whereValues = new String[]{ picReferenceNumber, picReferenceDate };
        searchAndFill(project, TABLE_DA_PIC_REFERENCE, whereFields, whereValues, LoadType.COMPLETE, origin);

        if (isEmpty()) { // falls es keine Treffer im Referenzdatum gibt, jetzt noch mit Wildcards in den vorherigen Datumswerten suchen
            whereFields = new String[]{ FIELD_DPR_REF_ID, FIELD_DPR_PREVIOUS_DATES };
            whereValues = new String[]{ picReferenceNumber, "*" + picReferenceDate + "*" };
            searchWithWildCardsSortAndFill(project, whereFields, whereValues, null, LoadType.COMPLETE, origin);
        }
    }

    /**
     * Lädt alle {@link iPartsDataPicReference}s aus der DB für die angegebene Bildnummer
     *
     * @param project
     * @param picReferenceNumber
     * @param origin
     */
    private void loadPicReferencesForNumber(EtkProject project, String picReferenceNumber, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_REF_ID }, new String[]{ picReferenceNumber },
                      LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataPicReference getNewDataObject(EtkProject project) {
        return new iPartsDataPicReference(project, null);
    }

    /**
     * Lädt alle {@link iPartsDataPicReference}s, die bereit sind für die AS-PLM Verarbeitung (Suche nach Bildnummer)
     *
     * @param project
     * @return
     */
    public static iPartsDataPicReferenceList loadNewBatchForProcessing(EtkProject project) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadNextBatch(project, iPartsPicReferenceState.NEW, DBActionOrigin.FROM_DB);
        return list;

    }

    private void loadNextBatch(EtkProject project, iPartsPicReferenceState state, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_STATUS }, new String[]{ state.getDbValue() },
                          new String[]{ FIELD_DPR_LAST_MODIFIED }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataPicReference}s mit dem übergebenen Status und der übergebenen Bildnummer
     *
     * @param project
     * @param picRefId
     * @param state
     * @return
     */
    public static iPartsDataPicReferenceList loadPicRefForState(EtkProject project, String picRefId, iPartsPicReferenceState state) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicRefWithState(project, picRefId, state, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadPicRefWithState(EtkProject project, String picRefId, iPartsPicReferenceState state, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPR_REF_ID, FIELD_DPR_STATUS };
        String[] whereValues = new String[]{ picRefId, state.getDbValue() };
        searchAndFill(project, TABLE_DA_PIC_REFERENCE, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataPicReference}s für den übergebenen {@link iPartsXMLMediaContainer}
     *
     * @param project
     * @param mContainer
     * @return
     */
    public static iPartsDataPicReferenceList loadPicRefsWithMcIdsAndEmptyVarIds(EtkProject project, iPartsXMLMediaContainer mContainer) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicRefWithASPLMIds(project, mContainer.getMcItemId(), mContainer.getMcItemRevId(), "", "", DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataPicReference}s für die übergebene {@link iPartsXMLMediaVariant} und {@link iPartsXMLMediaContainer}
     *
     * @param project
     * @param variant
     * @param mContainer
     * @return
     */
    public static iPartsDataPicReferenceList loadPicRefsWithBothIds(EtkProject project, iPartsXMLMediaVariant variant, iPartsXMLMediaContainer mContainer) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicRefWithASPLMIds(project, mContainer.getMcItemId(), mContainer.getMcItemRevId(), variant.getItemId(), variant.getItemRevId(), DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadPicRefWithASPLMIds(EtkProject project, String mcItemI, String mcItemRevId, String varId, String varRevId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPR_MC_ID, FIELD_DPR_MC_REV_ID, FIELD_DPR_VAR_ID, FIELD_DPR_VAR_REV_ID };
        String[] whereValues = new String[]{ mcItemI, mcItemRevId, varId, varRevId };
        searchAndFill(project, TABLE_DA_PIC_REFERENCE, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle Datensätze mit der übergebenen Nachrichten-ID (ohne MediaContentPrefix)
     *
     * @param project
     * @param messageGUID
     * @return
     */
    public static iPartsDataPicReferenceList loadPicReferencesWithMessageGUID(EtkProject project, String messageGUID) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicRefWithMessageGUID(project, messageGUID, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadPicRefWithMessageGUID(EtkProject project, String messageGUID, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPR_GUID };
        String[] whereValues = new String[]{ messageGUID };
        searchAndFill(project, TABLE_DA_PIC_REFERENCE, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    public static iPartsDataPicReferenceList loadWithPicRefNumberAndStates(EtkProject project, String pictureId, String pictureVer, Set<iPartsPicReferenceState> searchStates) {
        iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
        list.loadPicsWithRefNumberAndVer(project, pictureId, pictureVer, DBActionOrigin.FROM_DB, searchStates);
        return list;
    }

    private void loadPicsWithRefNumberAndVer(EtkProject project, String pictureId, String pictureVer, DBActionOrigin origin,
                                             Set<iPartsPicReferenceState> searchStates) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPR_VAR_ID, FIELD_DPR_VAR_REV_ID };
        String[] whereValues = new String[]{ pictureId, pictureVer };

        List<String> fields = project.getConfig().getDBDescription().getTable(TABLE_DA_PIC_REFERENCE).getFieldListAsStringList(false);
        fillListWithOrQuery(project, fields, whereFields, whereValues, searchStates, origin);

        // Ist die übergebene Referenznummer eine DASTi Referenznummer, muss in einer anderen Spalte gesucht werden
        if (isEmpty()) {
            whereFields = new String[]{ FIELD_DPR_REF_ID };
            whereValues = new String[]{ pictureId };
            fillListWithOrQuery(project, fields, whereFields, whereValues, searchStates, origin);
        }
    }

    private void fillListWithOrQuery(EtkProject project, List<String> fields, String[] whereFields, String[] whereValues,
                                     Set<iPartsPicReferenceState> searchStates, DBActionOrigin origin) {
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_PIC_REFERENCE,
                                                                                           ArrayUtil.toStringArray(fields),
                                                                                           whereFields, whereValues);

        if ((searchStates == null) || searchStates.isEmpty()) {
            fillAndAddDataObjectsFromAttributesList(project, attributesList, LoadType.COMPLETE, origin);
        } else {
            for (DBDataObjectAttributes attributes : attributesList) {
                iPartsPicReferenceState currentState = iPartsPicReferenceState.getFromDBValue(attributes.getFieldValue(FIELD_DPR_STATUS));
                if (searchStates.contains(currentState)) {
                    fillAndAddDataObjectFromAttributes(project, attributes, LoadType.COMPLETE, true, origin);
                }
            }
        }
    }
}
