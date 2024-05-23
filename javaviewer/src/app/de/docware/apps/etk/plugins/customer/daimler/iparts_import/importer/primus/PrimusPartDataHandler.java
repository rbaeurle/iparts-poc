/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindPRIMUSTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCacheType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.sql.TableAndFieldName;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Handler für den XML PRIMUS Teilestamm-Import
 */
public class PrimusPartDataHandler extends AbstractXMLPartHandler {

    private PrimusPartImportHelper primusPartImportHelper;
    private PrimusReplacementImportHelper primusReplacementImportHelper;
    private PrimusWWImportHelper primusWWImportHelper;
    private AbstractPrimusImportHelper currentlyActiveHelper;

    public PrimusPartDataHandler(EtkProject project, AbstractXMLPartImporter primusImporter) {
        super(project, primusImporter);
        DictImportTextIdHelper importTextIdHelper = new DictImportTextIdHelper(getProject());
        iPartsDictTextKindId txtKindId = importTextIdHelper.getDictTextKindIdForField(DictTextKindPRIMUSTypes.MAT_AFTER_SALES,
                                                                                      TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR));
        if (importTextIdHelper.hasWarnings()) {
            // Falls die Textart nicht definiert wäre, könnten nur verhaute Daten importiert werden.
            writeMessage(importTextIdHelper.getWarnings().get(0), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            writeMsg("!!Teilestamm-Import wird nicht ausgeführt.");
        } else {
            primusPartImportHelper = new PrimusPartImportHelper(project, txtKindId, primusImporter.getLogLanguage(), primusImporter.getMessageLog());
        }
        primusReplacementImportHelper = new PrimusReplacementImportHelper(project, primusImporter.getLogLanguage(), primusImporter.getMessageLog());
        primusWWImportHelper = new PrimusWWImportHelper(project, primusImporter.getLogLanguage(), primusImporter.getMessageLog());
    }

    @Override
    protected void onEndElement(String uri, String localName, String qName) {
        String tagContent = getCurrentTagData().getTextValue().toString();

        // Je nach Inhalt des MSG_ACTION Tags kommt ein anderer Helper zum Einsatz. Diesen jetzt bestimmen.
        if (getCurrentXMLPath().equals(AbstractPrimusImportHelper.MSG_ACTION)) {
            setHelper(tagContent, primusPartImportHelper, primusReplacementImportHelper, primusWWImportHelper);
        }

        if (currentlyActiveHelper != null) {
            currentlyActiveHelper.handleCurrentTag(getCurrentXMLPath(), tagContent, getTagCounter(), getCurrentRecord());
        }
        super.onEndElement(uri, localName, qName);
    }

    /**
     * Setzt den Helper für die aktuelle {@link PrimusAction}
     *
     * @param tagContent
     * @param helpers
     */
    private void setHelper(String tagContent, AbstractPrimusImportHelper... helpers) {
        List<AbstractPrimusImportHelper> validHelper = Arrays.stream(helpers).filter(helper -> (helper != null) && helper.isValidActionKey(tagContent)).collect(Collectors.toList());
        if (validHelper.size() != 1) {
            writeMsg("!!Tag %1: ungültige PRIMUS Aktion \"%2\". Wird übersprungen", String.valueOf(getTagCounter()), tagContent);
            currentlyActiveHelper = null;
        } else {
            writeMsg("!!Bearbeite PRIMUS Aktion %1...", tagContent);
            currentlyActiveHelper = validHelper.get(0);
        }
    }

    @Override
    protected void handleCurrentRecord() {
        // Record wird pro End-Tag immer weiter zusammengebaut. Jetzt ist das Ende des Msg-Tags erreicht -> Abspeichern
        if (currentlyActiveHelper != null) {
            GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = currentlyActiveHelper.handleRecord(getCurrentRecord());
            if (dataObjectsToBeSaved != null) {
                for (EtkDataObject deletedDataObject : dataObjectsToBeSaved.getDeletedList()) {
                    deletedDataObject.deleteFromDB(true);
                }
                // Den Importer zum Speichern verwenden, damit die Anzahl der importierten Datenobjekte richtig angezeigt wird.
                for (EtkDataObject etkDataObject : dataObjectsToBeSaved.getAsList()) {
                    getImporter().saveToDB(etkDataObject);
                }
            } else {
                writeMsg("!!Fehler beim Verarbeiten des Records. Record wird übersprungen.", String.valueOf(getTagCounter()));
            }
        }
        currentlyActiveHelper = null;
    }

    @Override
    public EnumSet<iPartsCacheType> getCacheTypesForClearCaches() {
        EnumSet<iPartsCacheType> cacheTypes = super.getCacheTypesForClearCaches();

        // Immer den PRIMUS-Ersetzungs-Cache löschen, auch wenn durch diesen konkreten Import evtl. keine PRIMUS-Ersetzungen
        // verändert wurden, weil dies durch vorherige MQ-Nachrichten der Fall sein kann und ansonsten der letzte Handler
        // darüber entscheiden würde, ob der PRIMUS-Ersetzungs-Cache gelöscht werden soll oder nicht, was im Worst Case
        // dazu führt, dass der Cache nicht gelöscht wird, obwohl vorherige MQ-Importe PRIMUS-Ersetzungen verändert haben.
        cacheTypes.add(iPartsCacheType.PRIMUS_REPLACEMENTS);

        return cacheTypes;
    }

    private void writeMsg(String key, String... placeHolderTexts) {
        String msg = TranslationHandler.translateForLanguage(key, getImporter().getLogLanguage(), placeHolderTexts);
        writeMessage(msg, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }
}
