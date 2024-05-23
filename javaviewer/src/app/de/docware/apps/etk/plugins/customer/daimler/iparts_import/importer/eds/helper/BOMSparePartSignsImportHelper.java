/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.json.SparePartSignAndMarketJSONObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.json.SparePartSignsJSONObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDbDataObjectAsJSON;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helfer für das Importieren von marktspezifischen ET-Kenner (BOM-DB)
 */
public class BOMSparePartSignsImportHelper extends EDSImportHelper {

    public BOMSparePartSignsImportHelper(EtkProject project, String tableName) {
        super(project, null, tableName);
    }

    /**
     * Befüllt das übergebene {@link iPartsDataPart} Objekt mit den BOM-DB spezifischen Ersatzteilkennzeichen.
     * Ist der Importer nicht NULL, dann kann eine Warnung in den passenden Log-Kanal geschrieben werden.
     * Falls nicht einfach überschrieben werden soll, ist die Logik wie folgt:
     * Falls Markt bereits vorhanden ist -> ETKZ Zeichen überschreiben
     * Falls Markt noch nicht vorhanden  ist -> an Teil hinzufügen
     *
     * @param importer
     * @param dataPart
     * @param signsAndMarkets
     * @param isOverwrite
     */
    private void fillDataPartWithSignsAndMarkets(AbstractDataImporter importer, iPartsDataPart dataPart, String signsAndMarkets,
                                                 boolean isOverwrite) {
        if (dataPart != null) {
            String jsonString = "";
            String currentValueJSON = dataPart.getFieldValueAsStringFromZippedBlob(FIELD_M_MARKET_ETKZ);
            if (StrUtils.isValid(signsAndMarkets)) {
                SparePartSignsJSONObject newSparePartSignsJSONObject = new SparePartSignsJSONObject(signsAndMarkets);
                SerializedDbDataObjectAsJSON jsonHelper = new SerializedDbDataObjectAsJSON(true);
                // Daten überschreiben oder es sind noch keine Ersatzteilkenner vorhanden
                if (isOverwrite || currentValueJSON.isEmpty()) {
                    newSparePartSignsJSONObject.getSparePartSignsAndMarket().sort(Comparator.comparing(SparePartSignAndMarketJSONObject::getMarket));
                    jsonString = jsonHelper.getAsJSON(newSparePartSignsJSONObject);
                } else {
                    SparePartSignsJSONObject currentValueAsObj = jsonHelper.getFromJSON(currentValueJSON, SparePartSignsJSONObject.class);
                    List<SparePartSignAndMarketJSONObject> currentSparePartSignsAndMarket = currentValueAsObj.getSparePartSignsAndMarket();
                    List<SparePartSignAndMarketJSONObject> newSparePartSignAndMarket = new ArrayList<>();
                    // Unterscheidung, ob es ein JSON Objekt mit Kenner oder ohne gibt (ohne ist eher unwahrscheinlich, aber möglich)
                    if (currentSparePartSignsAndMarket != null) {
                        Map<String, SparePartSignAndMarketJSONObject> currentMarketToObject = currentSparePartSignsAndMarket.stream()
                                .collect(Collectors.toMap(SparePartSignAndMarketJSONObject::getMarket, value -> value, (oldValue, newValue) -> newValue));
                        for (SparePartSignAndMarketJSONObject sparePartSignAndMarketJSONObject : newSparePartSignsJSONObject.getSparePartSignsAndMarket()) {
                            String newValueMarket = sparePartSignAndMarketJSONObject.getMarket();
                            SparePartSignAndMarketJSONObject existingMarketAndSignObject = currentMarketToObject.remove(newValueMarket);
                            // Markt bereits vorhanden. ETKZ wird überschrieben
                            if (existingMarketAndSignObject != null) {
                                existingMarketAndSignObject.setSparePartSign(sparePartSignAndMarketJSONObject.getSparePartSign());
                                newSparePartSignAndMarket.add(existingMarketAndSignObject);
                            } else {
                                // Markt noch nicht vorhanden. Markt und ETKZ müssen an die schon bestehenden Daten hinzugefügt werden
                                SparePartSignAndMarketJSONObject newMarketWithSpareSign = new SparePartSignAndMarketJSONObject();
                                newMarketWithSpareSign.setMarket(newValueMarket);
                                newMarketWithSpareSign.setSparePartSign(sparePartSignAndMarketJSONObject.getSparePartSign());
                                newSparePartSignAndMarket.add(newMarketWithSpareSign);
                            }
                        }
                        // Bestehende marktspezifische ET-Sichten behalten
                        if (!currentMarketToObject.isEmpty()) {
                            newSparePartSignAndMarket.addAll(currentMarketToObject.values());
                        }
                    } else {
                        // Es gibt ein JSON Eintrag am Teilestamm, der aber keine Ersatzteilkennzeichen hat -> die neuen
                        // Kennzeichen hinzufügen
                        newSparePartSignAndMarket.addAll(newSparePartSignsJSONObject.getSparePartSignsAndMarket());
                    }
                    newSparePartSignAndMarket.sort(Comparator.comparing(SparePartSignAndMarketJSONObject::getMarket));
                    currentValueAsObj.setSparePartSignsAndMarket(newSparePartSignAndMarket);
                    jsonString = jsonHelper.getAsJSON(currentValueAsObj);
                }
            } else {
                if (importer != null) {
                    importer.getMessageLog().fireMessage(importer.translateForLog("!!Teilenummer \"%1\" enthält keine Ersatzteilkennzeichnung",
                                                                                  dataPart.getAsId().getMatNr()), MessageLogType.tmlWarning,
                                                         MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                }
            }
            // Nur setzen, wenn der neue JSON String nicht gleich dem aktuellen String ist
            if (!jsonString.equals(currentValueJSON)) {
                dataPart.setFieldValueAsZippedBlobFromString(FIELD_M_MARKET_ETKZ, jsonString, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Importiert die marktspezifischen ET-Kenner für EDS/BCS
     *
     * @param importer
     * @param partNo
     * @param signsAndMarkets
     */
    public void handleSparePartsImport(AbstractDataImporter importer, String partNo, String signsAndMarkets) {
        importer.saveToDB(handleSpareParts(importer, partNo, signsAndMarkets, true));
    }

    public EtkDataPart handleSpareParts(AbstractDataImporter importer, String partNo, String signsAndMarkets,
                                        boolean isOverwrite) {
        iPartsPartId partId = new iPartsPartId(partNo, "");
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId);
        if (part instanceof iPartsDataPart) {
            iPartsDataPart dataPart = (iPartsDataPart)part;
            // Hier muss explizit die loadMissingAttributesFromDB Methode aufgerufen werden, weil wir den Blob auch gleich
            // laden möchten
            if (!dataPart.loadMissingAttributesFromDB(null, true, false, true)) {
                dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // Bestellnummer setzen
                dataPart.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
            }
            // Quelle setzen
            dataPart.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
            // Die Ersatzteilkenner pro Markt setzen.
            fillDataPartWithSignsAndMarkets(importer, dataPart, signsAndMarkets, isOverwrite);
        }
        return part;
    }
}
