/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsCustomProperty;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsSPKMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSAdditionalPartInformation;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSWiringHarness;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.ArrayList;
import java.util.List;

public class iPartsWSWireHarnessHelper implements iPartsConst {

    public static final EtkDisplayFields WIRING_HARNESS_DISPLAY_FIELDS = new EtkDisplayFields();

    static {
        // Alle benötigten Felder aus DA_WIRE_HARNESS
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_REF, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONNECTOR_NO, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SUB_SNR, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_POS, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR_TYPE, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_ADD_TEXT, true, false));

        // Die benötigten Felder aus MAT
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_ETKZ, false, false)); // Für den Filter
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_SECURITYSIGN_REPAIR, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_IMAGE_AVAILABLE, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_ADDTEXT, true, false));

        // Gewicht, Länge, Breite, Höhe und Volumen
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_WEIGHT, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_LENGTH, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_WIDTH, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_HEIGHT, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_VOLUME, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_HAZARDOUS_GOODS_INDICATOR, false, false));
    }

    /**
     * Methode zum Befüllen des {@link iPartsWSWiringHarness}-Objekts.
     * Übergeben wird eine Liste von Leitungsbausatzkasten-Datensätzen als Liste aus der DB.
     * Diese ist bereits sortiert und enthält Dank des Joins mit der Tabelle MAT alle relevanten Daten.
     * Gibt eine Liste von aufbereiteten, befüllten {@link iPartsWSWiringHarness}-Objekten für den Webservice zurück.
     *
     * @param project
     * @param dataWireHarnessList
     * @param includeTextIds
     * @param hmMSmId
     * @param spkMappingCache
     * @param steeringValue
     * @return
     */
    public static List<iPartsWSWiringHarness> fillWiringHarnessKit(EtkProject project, List<iPartsDataWireHarness> dataWireHarnessList,
                                                                   boolean includeTextIds, HmMSmId hmMSmId,
                                                                   iPartsSPKMappingCache spkMappingCache, String steeringValue) {
        List<iPartsWSWiringHarness> iPartsWSWiringHarnessList = new ArrayList<>();
        String dbLanguage = project.getDBLanguage();
        List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
        iPartsCustomProperty iPartsCustomPropertyCache = iPartsCustomProperty.getInstance(project);

        for (iPartsDataWireHarness dataWireHarness : dataWireHarnessList) {
            iPartsWSWiringHarness wiringHarness = new iPartsWSWiringHarness();
            String partNumber = dataWireHarness.getFieldValue(FIELD_DWH_SUB_SNR);
            wiringHarness.setPartNumber(partNumber); // (Untere) Teile-/Sachnummer
            wiringHarness.setPartNumberFormatted(iPartsNumberHelper.formatPartNo(project, partNumber)); // (Untere) Teile-/Sachnummer formatiert
            wiringHarness.setPartNumberType(dataWireHarness.getFieldValue(FIELD_DWH_SNR_TYPE)); // Sachnummerntyp
            EtkMultiSprache name = dataWireHarness.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
            if (name != null) {
                wiringHarness.setName(WSHelper.getNullForEmptyString(name.getTextByNearestLanguage(dbLanguage, dbFallbackLanguages))); // Benennung
                if (includeTextIds) {
                    wiringHarness.setNameRef(WSHelper.getNullForEmptyString(name.getTextId())); // Text-Id der Benennung
                }
            }
            wiringHarness.setEs1Key(WSHelper.getNullForEmptyString(dataWireHarness.getFieldValue(FIELD_M_AS_ES_1))); // ES1 Schlüssel
            wiringHarness.setEs2Key(WSHelper.getNullForEmptyString(dataWireHarness.getFieldValue(FIELD_M_AS_ES_2))); // ES2 Schlüssel
            wiringHarness.setDsr(dataWireHarness.getFieldValueAsBoolean(FIELD_M_SECURITYSIGN_REPAIR)); // DSR Kenner
            wiringHarness.setPictureAvailable(dataWireHarness.getFieldValueAsBoolean(FIELD_M_IMAGE_AVAILABLE)); // Bildverfügbarkeit

            String connectorNo = dataWireHarness.getFieldValue(FIELD_DWH_CONNECTOR_NO);
            wiringHarness.setConnectorNumber(WSHelper.getNullForEmptyString(connectorNo)); // Steckernummer

            String dwhRef = dataWireHarness.getFieldValue(FIELD_DWH_REF);
            iPartsSPKMappingCache.SPKEntries spkEntries = null;
            if (spkMappingCache != null) {
                spkEntries = spkMappingCache.getTextEntriesForMapping(hmMSmId, dwhRef, connectorNo, steeringValue);
            }
            if ((spkEntries != null) && (spkEntries.getShortText() != null)) {
                wiringHarness.setReferenceNumber(WSHelper.getNullForEmptyString(spkEntries.getShortText())); // Referenznummer ersetzt durch SPKM_KURZ_AS
            } else {
                wiringHarness.setReferenceNumber(WSHelper.getNullForEmptyString(dwhRef)); // Referenznummer
            }

            EtkMultiSprache contactAdditionalText;
            if ((spkEntries != null) && (spkEntries.getLongText() != null)) {
                contactAdditionalText = spkEntries.getLongText();
            } else {
                contactAdditionalText = dataWireHarness.getFieldValueAsMultiLanguage(FIELD_DWH_CONTACT_ADD_TEXT);
            }
            if (contactAdditionalText != null) {
                // Ergänzungstext vom Kontakt
                wiringHarness.setContactAdditionalText(WSHelper.getNullForEmptyString(contactAdditionalText.getTextByNearestLanguage(dbLanguage,
                                                                                                                                     dbFallbackLanguages)));
                if (includeTextIds) {
                    // Text-Id des Ergänzungstexts vom Kontakt
                    wiringHarness.setContactAdditionalTextRef(WSHelper.getNullForEmptyString(contactAdditionalText.getTextId()));
                }
            }
            EtkMultiSprache materialDesc = dataWireHarness.getFieldValueAsMultiLanguage(FIELD_M_ADDTEXT);
            if (materialDesc != null) {
                // Sprachneutraler Text
                wiringHarness.setMaterialDesc(WSHelper.getNullForEmptyString(materialDesc.getTextByNearestLanguage(dbLanguage,
                                                                                                                   dbFallbackLanguages)));
                if (includeTextIds) {
                    // Text-Id des Sprachneutralen Texts
                    wiringHarness.setMaterialDescRef(WSHelper.getNullForEmptyString(materialDesc.getTextId()));
                }
            }

            // Additional Part Information & Custom Properties
            List<iPartsWSAdditionalPartInformation> additionalPartInformationList = iPartsWSAdditionalPartInformationHelper.fillAdditionalPartInformation(project, dataWireHarness,
                                                                                                                                                          partNumber, dbLanguage,
                                                                                                                                                          dbFallbackLanguages, iPartsCustomPropertyCache);
            if (!additionalPartInformationList.isEmpty()) {
                wiringHarness.setAdditionalPartInformation(additionalPartInformationList);
            }

            iPartsWSWiringHarnessList.add(wiringHarness);
        }
        return iPartsWSWiringHarnessList;
    }
}
