/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsWireHarnessId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.helper.DictImportConnectTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.helper.WireHarness;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.Map;

/**
 * Handler für den Leitungssatz-Importer, der den Inhalt des XML Tags <code>Contact</code> verarbeitet
 */
public class WireHarnessContactListHandler extends AbstractWireHarnessSubHandler {

    private static final String TRIGGER_ELEMENT = "Contact";

    protected static final String STECKER_NO = "Stecker-Nr.";
    protected static final String KONTAKTFORM = "Kontaktform";
    protected static final String KAMMER_NO = "Kammernummer";
    protected static final String POS_NO_KONTAKTGEHAEUSE = "PosNr-Kontaktgehaeuse";
    protected static final String SACHNUMMER_KONTAKTGEHAEUSE = "Sachnummer-Kontaktgehaeuse";
    protected static final String POS_NO_KONTAKT = "PosNr-Kontakt";
    protected static final String SACHNUMMER_KONTAKT = "Sachnummer-Kontakt";
    protected static final String POS_NO_ELA = "PosNr-Ela";
    protected static final String SACHNUMMER_ELA = "Sachnummer-Ela";
    protected static final String POS_NO_BLINDSTOPFEN = "PosNr-Blindstopfen";
    protected static final String SACHNUMMER_BLINDSTOPFEN = "Sachnummer-Blindstopfen";

    // Die nächsten Felder werden nur zum Speichern von Snr + Pos zum jeweiligen Typ in den Attributen verwendet.
    protected static final String SUB_SNR = "SUB_SNR";
    protected static final String SUB_SNR_POS = "SUB_SNR_POS";
    protected static final String SUB_SNR_TYPE = "SUB_SNR_TYPE";

    protected static String[] BASE_ATTRIB_NAMES = new String[]{ REF, STECKER_NO, KONTAKTFORM, KAMMER_NO,
                                                                BENENNUNG, LEITUNGSSATZ };

    private DictImportConnectTextIdHelper dictHelper; // Helfer um Texte im Lexikon zu suchen bzw. anzulegen

    public WireHarnessContactListHandler(EtkProject project, AbstractSAXPushHandlerImporter importer) {
        super(project, TRIGGER_ELEMENT, "ContactList", importer);
        this.dictHelper = new DictImportConnectTextIdHelper(this, getProject());
    }

    @Override
    public void doHandleCurrentRecord(Map<String, String> currentSubRecord) {
        // Hier kommt man heraus, wenn der EndTag von 'Contact' kommt

        // Hier den kompletten Inhalt eines "Contact" Elements (samt Unterelemente) verarbeiten
        if (currentSubRecord != null) {
            if (!checkImportPartNumbers(getTriggerElement(), currentSubRecord, LEITUNGSSATZ, SACHNUMMER_KONTAKTGEHAEUSE,
                                        SACHNUMMER_KONTAKT, SACHNUMMER_ELA, SACHNUMMER_BLINDSTOPFEN)) {
                return;
            }

            // Dieses Objekt wird mit den allgemeinen Werten befüllt und als Basis zum Kopieren verwendet.
            DBDataObjectAttributes baseAttributes = fillBaseAttributes(currentSubRecord);

            // ---------------------------------------------------------------------------------------------------------
            // Die allgemein gültigen Attribute übernehmen und durch die speziellen noch erweitern.
            // ---------------------------------------------------------------------------------------------------------
            // Für das "Kontaktgehäuse" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
            createIdFromPinHousing(currentSubRecord, baseAttributes);

            // Für den "Kontakt" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
            createIdFromPin(currentSubRecord, baseAttributes);

            // Für die "Einzeladerabdichtung" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
            createIdFromELA(currentSubRecord, baseAttributes);

            // Für den "Blindstopfen" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
            createIdFromFillerPlug(currentSubRecord, baseAttributes);

        }
    }

    @Override
    public void clearData() {
        clearResultMap();
        dictHelper.clearStoredEntries();
    }

    private void createIdFromFillerPlug(Map<String, String> currentSubRecord, DBDataObjectAttributes baseAttributes) {
        // Für den "Blindstopfen" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
        fillContactAttributes(WireHarness.FILLER_PLUG, currentSubRecord.get(SACHNUMMER_BLINDSTOPFEN),
                              currentSubRecord.get(POS_NO_BLINDSTOPFEN), baseAttributes);
    }

    private void createIdFromELA(Map<String, String> currentSubRecord, DBDataObjectAttributes baseAttributes) {
        // Für die "Einzeladerabdichtung" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
        fillContactAttributes(WireHarness.ELA, currentSubRecord.get(SACHNUMMER_ELA),
                              currentSubRecord.get(POS_NO_ELA), baseAttributes);
    }

    private void createIdFromPin(Map<String, String> currentSubRecord, DBDataObjectAttributes baseAttributes) {
        // Für die "Einzeladerabdichtung" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
        fillContactAttributes(WireHarness.PIN, currentSubRecord.get(SACHNUMMER_KONTAKT),
                              currentSubRecord.get(POS_NO_KONTAKT), baseAttributes);
    }

    private void createIdFromPinHousing(Map<String, String> currentSubRecord, DBDataObjectAttributes baseAttributes) {
        // Für das "Kontaktgehäuse" ggf. ein eigenes Element anlegen und einen eigenen Datensatz erzeugen.
        fillContactAttributes(WireHarness.PIN_HOUSING, currentSubRecord.get(SACHNUMMER_KONTAKTGEHAEUSE),
                              currentSubRecord.get(POS_NO_KONTAKTGEHAEUSE), baseAttributes);
    }

    private void fillContactAttributes(WireHarness wireHarnessType, String subSnr, String subSnrPos,
                                       DBDataObjectAttributes baseAttributes) {
        if (StrUtils.isValid(subSnr)) {
            // Die allgemein gültigen Attribute übernehmen und durch die speziellen noch erweitern.
            DBDataObjectAttributes attributes = new DBDataObjectAttributes();
            attributes.addFields(baseAttributes, DBActionOrigin.FROM_DB);

            attributes.addField(SUB_SNR, subSnr, DBActionOrigin.FROM_DB);
            attributes.addField(SUB_SNR_POS, (subSnrPos == null) ? "" : subSnrPos, DBActionOrigin.FROM_DB);
            attributes.addField(SUB_SNR_TYPE, wireHarnessType.getDbValue(), DBActionOrigin.FROM_DB);
            // Eine ID erzeugen
            iPartsWireHarnessId id = buildIdFromAttributes(attributes);
            // Und dieses Objekt in die Ergebnisliste einhängen.
            resultMap.put(id, attributes);
        }

    }

    private DBDataObjectAttributes fillBaseAttributes(Map<String, String> currentSubRecord) {
        // Dieses Objekt wird mit den allgemeinen Werten befüllt und als Basis zum Kopieren verwendet.
        DBDataObjectAttributes baseAttributes = createBaseAttributes(currentSubRecord, BASE_ATTRIB_NAMES);
        /**
         * Bezeichnung behandeln
         * Deutschen Text in Lexikon nachschlagen; wenn existent Texte übernehmen, sonst anlegen
         */
        String text = getValueFromSubRecord(currentSubRecord, BENENNUNG);
        EtkMultiSprache multiLang = dictHelper.searchConnectTextInDictionary(text);
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(BENENNUNG, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
        attribute.setValueAsMultiLanguage(multiLang, DBActionOrigin.FROM_DB);
        baseAttributes.addField(attribute, DBActionOrigin.FROM_DB);

        return baseAttributes;
    }

    private iPartsWireHarnessId buildIdFromAttributes(DBDataObjectAttributes attributes) {
        return new iPartsWireHarnessId(attributes.getFieldValue(LEITUNGSSATZ), attributes.getFieldValue(REF),
                                       attributes.getFieldValue(STECKER_NO), attributes.getFieldValue(SUB_SNR),
                                       attributes.getFieldValue(SUB_SNR_POS));
    }

    public static String getTriggerElement() {
        return TRIGGER_ELEMENT;
    }

}
