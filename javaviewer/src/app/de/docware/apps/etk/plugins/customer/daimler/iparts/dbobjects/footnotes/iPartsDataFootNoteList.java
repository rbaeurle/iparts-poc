/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.Set;

public class iPartsDataFootNoteList extends EtkDataObjectList<iPartsDataFootNote> implements iPartsConst {

    /**
     * lädt die kompletten Standard-Fußnoten incl der Texte
     * (die DataObjects enthalten alle Felder aus TABLE_DA_FN und TABLE_DA_FN_CONTENT)
     *
     * @param project
     * @return
     */
    public static iPartsDataFootNoteList loadStandardFootNoteListWithContent(EtkProject project) {
        iPartsDataFootNoteList list = new iPartsDataFootNoteList();
        list.setSearchWithoutActiveChangeSets(true); // Standardfußnoten sind unabhängig von ChangeSets
        list.loadCompleteStandardFootNoteListFromDB(project);
        return list;
    }

    /**
     * Lädt ein Set mit allen Standard-Fußnotennummern
     *
     * @param project
     * @return
     */
    public static Set<String> loadStandardFootnotesAsSet(EtkProject project) {
        iPartsDataFootNoteList dataFootNoteList = new iPartsDataFootNoteList();
        dataFootNoteList.loadFootNoteListFromDB(project, true);
        HashSet<String> standardFootNoteNumbers = new HashSet<>(dataFootNoteList.size());
        for (iPartsDataFootNote dataFootNote : dataFootNoteList) {
            standardFootNoteNumbers.add(dataFootNote.getAsId().getFootNoteId());
        }
        return standardFootNoteNumbers;
    }

    /**
     * Lädt eine komplette Liste aller {@link iPartsDataFootNote}s der Tabelle.
     *
     * @param project
     * @return
     */
    public static iPartsDataFootNoteList loadFootNoteOverviewList(EtkProject project) {
        iPartsDataFootNoteList dataFootNoteList = new iPartsDataFootNoteList();
        dataFootNoteList.loadFootNoteOverviewListFromDB(project);
        return dataFootNoteList;
    }

    /**
     * Lädt eine komplette Liste aller {@link iPartsDataFootNote}s der Tabelle.
     *
     * @param project
     * @return
     */
    private void loadFootNoteOverviewListFromDB(EtkProject project) {
        clear(DBActionOrigin.FROM_DB);
        searchSortAndFill(project, TABLE_DA_FN, null, null, new String[]{ FIELD_DFN_NAME }, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataFootNote} Standardfußnoten oder Nicht-Standardfußnoten, je nachdem.
     *
     * @param project
     * @param standardFootNote Standard oder nicht Standard, das ist die Frage.
     * @return
     */
    public void loadFootNoteListFromDB(EtkProject project, boolean standardFootNote) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DFN_STANDARD };
        String[] whereValues = new String[]{ SQLStringConvert.booleanToPPString(standardFootNote) };
        String[] sortFields = new String[]{ FIELD_DFN_ID };

        searchSortAndFill(project, TABLE_DA_FN, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt eine komplette Liste aller aus den Einsatzdaten erzeugten {@link iPartsDataFootNote}s zum angegebenen
     * Prefix (EPC oder ELDAS)und Produkt oder SA.
     *
     * @param project
     * @param productOrSANumber
     * @return
     */
    public void loadSpecialCreatedFootNoteListForProductOrSAFromDB(EtkProject project, String productOrSANumber, String prefix) {
        clear(DBActionOrigin.FROM_DB);
        String footNoteIdPattern = prefix + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER
                                   + productOrSANumber + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + "*";
        searchWithWildCardsSortAndFill(project, new String[]{ FIELD_DFN_ID }, new String[]{ footNoteIdPattern }, null,
                                       LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt eine komplette Liste aller ELDAS-{@link iPartsDataFootNote}s zum angegebenen Produkt oder SA.
     *
     * @param project
     * @param productOrSANumber
     * @return
     */
    public void loadELDASFootNoteListForProductOrSAFromDB(EtkProject project, String productOrSANumber) {
        loadSpecialCreatedFootNoteListForProductOrSAFromDB(project, productOrSANumber, iPartsDataFootNote.FOOTNOTE_PREFIX_ELDAS);
    }

    /**
     * Lädt eine komplette Liste aller EPC-{@link iPartsDataFootNote}s zum angegebenen Produkt oder SA.
     *
     * @param project
     * @param productOrSANumber
     * @return
     */
    public void loadEPCFootNoteListForProductOrSAFromDB(EtkProject project, String productOrSANumber) {
        loadSpecialCreatedFootNoteListForProductOrSAFromDB(project, productOrSANumber, iPartsDataFootNote.FOOTNOTE_PREFIX_EPC);
    }

    /**
     * lädt die kompletten Standard-Fußnoten inklusive der Texte in allen Sprachen.
     *
     * @param project
     */
    private void loadCompleteStandardFootNoteListFromDB(EtkProject project) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_FN, FIELD_DFN_STANDARD) };
        String[] whereValues = new String[]{ SQLStringConvert.booleanToPPString(true) };
        String[] sortFields = new String[]{ FIELD_DFN_ID };
        EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_FN);
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_FN_CONTENT));
        searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, selectFields, TableAndFieldName.make(TABLE_DA_FN_CONTENT,
                                                                                                                FIELD_DFNC_TEXT),
                                                                  whereFields, whereValues, false, sortFields, false, false,
                                                                  new JoinData(TABLE_DA_FN_CONTENT,
                                                                               new String[]{ FIELD_DFN_ID },
                                                                               new String[]{ FIELD_DFNC_FNID },
                                                                               false, false));
    }

    @Override
    protected iPartsDataFootNote getNewDataObject(EtkProject project) {
        return new iPartsDataFootNote(project, null);
    }
}
