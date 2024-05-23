/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindRSKTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsXMLPartImportTags;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

/**
 * Handler für den XML SRM Teilestamm-Import
 */
public class SrmPartDataHandler extends AbstractXMLPartHandler implements iPartsXMLPartImportTags {

    private iPartsDictTextKindId textKindId;

    public SrmPartDataHandler(EtkProject project, AbstractXMLPartImporter importer) {
        super(project, importer);
        textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getRSKTxtKindId(DictTextKindRSKTypes.MAT_AFTER_SALES);
    }

    @Override
    protected void handleCurrentRecord() {
        String partNo = getValueFromCurrentRecord(MSG_PTN);
        // Laut angehängter Doku sind alle numerischen Werte rechtsbündig und werden vorne mit "0" aufgefüllt
        String termId = StrUtils.removeLeadingCharsFromString(getValueFromCurrentRecord(MSG_TERMID), '0');

        // Beide Werte müssen gültig sein
        if (!StrUtils.isValid(partNo, termId)) {
            return;
        }
        // Für die Term-ID und die Textart "Teilebenennung After-Sales" (RSK) nach DictMeta Objekten suchen, weil dort die Verknüpfung von Term-ID (SRM) zu Text-ID (iParts) vorhanden ist
        iPartsDataDictMetaList dictMetasForForeignTermId = iPartsDataDictMetaList.loadMetaFromForeignTextIdAndTextKind(getProject(), termId, textKindId);
        if (dictMetasForForeignTermId.isEmpty()) {
            // Textart und Term-ID hat keine Treffer produziert
            writeMessage(getImporter().translateForLog("!!Für die Term-ID \"%1\" und Textart \"%2\"wurden " +
                                                       "keine iParts-Ids gefunden! Suche nach Einträge ohne vorgegebene " +
                                                       "Textart", termId, DictTextKindRSKTypes.MAT_AFTER_SALES.getTextKindName()),
                         MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            // Nach DictMetas ohne Textart suchen
            dictMetasForForeignTermId = iPartsDataDictMetaList.loadMetaFromForeignTextId(getProject(), termId);
            if (dictMetasForForeignTermId.isEmpty()) {
                writeMessage(getImporter().translateForLog("!!Für die Term-ID \"%1\" (ohne Textart) wurden keine iParts-Ids " +
                                                           "gefunden!", termId),
                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            } else {
                writeMessage(getImporter().translateForLog("!!Für die Term-ID \"%1\" (ohne Textart) wurden %2 iParts-Ids " +
                                                           "gefunden!", termId, String.valueOf(dictMetasForForeignTermId.size())),
                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }

        String iPartsTextid = null;
        // Alle Treffer durchlaufen und schauen, ob die iParts Text-IDs gleich bzw unterschiedlich sind
        for (iPartsDataDictMeta dictMeta : dictMetasForForeignTermId) {
            String dictMetaTextId = dictMeta.getTextId();
            if (iPartsTextid == null) {
                iPartsTextid = dictMetaTextId;
            } else {
                if (!dictMetaTextId.equals(iPartsTextid)) {
                    writeMessage(getImporter().translateForLog("!!Für die Term-ID \"%1\" existieren mehrere " +
                                                               "iParts-Ids! ID, die verwendet wird: \"%2\"; ID, die gefunden wurde: \"%3\"", termId, iPartsTextid, dictMetaTextId),
                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            }
        }
        // Nur weitermachen, wenn eine ID gefunden wurden konnte
        if (StrUtils.isValid(iPartsTextid)) {
            // Check, ob der Teilestamm existiert. Falls nicht, soll ein rudimentärer Teilestamm angelegt wurde
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), new iPartsPartId(partNo, ""));
            if (!part.existsInDB()) {
                part.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                writeMessage(getImporter().translateForLog("!!Für die Teilenummer \"%1\" existiert noch kein" +
                                                           " Teilestamm. Es wird ein rudimentärer Teilestamm mit der iParts " +
                                                           "Text-Id \"%2\" (SRM: \"%3\") angelegt", partNo, iPartsTextid, termId),
                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }

            String idFromPart = part.getFieldValue(FIELD_M_TEXTNR);
            if (!idFromPart.equals(iPartsTextid)) {
                // Laden der Textes um zu prüfen, ob der Text auch wirklich existiert. Falls er nicht existiert (geht das überhaupt?)
                // wird nur die ID an den Teilestamm geschrieben, ansonsten wird das EtkMultiSprach am Teilestamm gesetzt
                EtkMultiSprache existingText = getProject().getDbLayer().getLanguagesTextsByTextId(iPartsTextid);
                if ((existingText == null) || existingText.isEmpty()) {
                    writeMessage(getImporter().translateForLog("!!Für die Text-ID \"%1\" existieren noch keine" +
                                                               " Texte in der DB. Die Text-ID wird trotzdem mit dem Teilestamm verknüpft", iPartsTextid),
                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    part.setFieldValue(FIELD_M_TEXTNR, iPartsTextid, DBActionOrigin.FROM_EDIT);
                } else {
                    part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, existingText, DBActionOrigin.FROM_EDIT);
                }
            } else {
                writeMessage(getImporter().translateForLog("!!Die Teilestamm \"%1\" ist bereits mit der SRM" +
                                                           " Term-ID \"%2\" (iParts: \"%3\") verknüpft.", partNo, termId, iPartsTextid),
                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            // Jetzt hier die Quelle setzen. Teilestamm hat nun auf jeden Fall die Text-Id
            part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.SRM.getOrigin(), DBActionOrigin.FROM_EDIT);
            // M_BESTNR setzen
            part.setFieldValue(FIELD_M_BESTNR, partNo, DBActionOrigin.FROM_EDIT);
            getImporter().saveToDB(part);
        }
    }

}
