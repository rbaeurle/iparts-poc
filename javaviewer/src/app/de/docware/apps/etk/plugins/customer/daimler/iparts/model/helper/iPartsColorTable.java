/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Datenklasse für die Farbvariantentabellen und Farbvarianteninhalte aufbereitet für den Retail (Filterung und Webservices)
 * oder vollständig für die Konstruktion.
 */
public class iPartsColorTable {

    private Map<String, ColorTable> colorTablesMap = new TreeMap<>(); // Farbvarianteninhalte pro Farbvariantentabellen-ID

    public iPartsColorTable cloneMe() {
        iPartsColorTable clone = new iPartsColorTable();
        for (Map.Entry<String, ColorTable> colorTableEntry : colorTablesMap.entrySet()) {
            ColorTable colorTable = colorTableEntry.getValue();
            ColorTable colorTableClone = new ColorTable();
            colorTableClone.colorTableId = colorTable.colorTableId;
            colorTableClone.colorSign = colorTable.colorSign;

            // Farbvariantentabellen zum Teil klonen
            colorTableClone.colorTableToPartsMap = new LinkedHashMap<>(colorTable.colorTableToPartsMap.size());
            for (ColorTableToPart colorTableToPart : colorTable.colorTableToPartsMap.values()) {
                colorTableClone.addColorTableToPart(colorTableToPart.cloneMe(true, false));
            }

            // Farbvarianteninhalte klonen
            colorTableClone.colorTableContents = new DwList<>(colorTable.colorTableContents.size());
            for (ColorTableContent colorTableContent : colorTable.colorTableContents) {
                colorTableClone.colorTableContents.add(colorTableContent.cloneMe(true, false));
            }

            clone.colorTablesMap.put(colorTableEntry.getKey(), colorTableClone);
        }
        return clone;
    }

    /**
     * Liefert die unveränderliche Map von Farbvariantentabellen-ID auf Farbvariantentabelle inkl. Werkseinsatzdaten und
     * Inhalten zurück.
     *
     * @return
     */
    public Map<String, ColorTable> getColorTablesMap() {
        return Collections.unmodifiableMap(colorTablesMap);
    }

    /**
     * Liefert die Farbvariantentabelle für die übergebene Farbvariantentabellen-ID zurück.
     *
     * @param colorTableId
     * @return
     */
    public ColorTable getColorTable(String colorTableId) {
        return colorTablesMap.get(colorTableId);
    }

    /**
     * Fügt die übergebene Farbvariantentabelle hinzu.
     *
     * @param colorTable
     */
    public void addColorTable(ColorTable colorTable) {
        colorTablesMap.put(colorTable.colorTableId.getColorTableId(), colorTable);
    }

    /**
     * Entfernt die Farbvariantentabelle mit der übergebenen Farbvariantentabellen-ID.
     *
     * @param colorTableId
     */
    public void removeColorTable(String colorTableId) {
        colorTablesMap.remove(colorTableId);
    }

    /**
     * Liefert alle Farbvarianteninhalte für die übergebene Farbvariantentabellen-ID zurück.
     *
     * @param colorTableId
     * @return
     */
    public List<ColorTableContent> getColorTableContents(String colorTableId) {
        ColorTable colorTable = colorTablesMap.get(colorTableId);
        if (colorTable != null) {
            return colorTable.colorTableContents;
        }

        return null;
    }

    /**
     * Liefert den {@link ColorTableContent} für die übergebene {@link iPartsColorTableContentId} zur {@code colorTableId} zurück.
     *
     * @param colorTableId
     * @param colorTableContentId
     * @return {@code null} falls kein passender {@link ColorTableContent} gefunden werden konnte
     */
    public ColorTableContent getColorTableContent(String colorTableId, iPartsColorTableContentId colorTableContentId) {
        ColorTable colorTable = getColorTable(colorTableId);
        if (colorTable == null) {
            return null;
        }
        return colorTable.getColorTableContent(colorTableContentId);
    }


    /**
     * Eine konkrete Farbvariantentabelle inkl. Verknüpfungen zum Teil und Farbvarianteninhalten.
     */
    public static class ColorTable {

        public iPartsColorTableDataId colorTableId;
        public Map<iPartsColorTableToPartId, ColorTableToPart> colorTableToPartsMap = new TreeMap<>(); // Map von IDs auf Farbvariantentabellen zum Teil
        public List<ColorTableContent> colorTableContents = new DwList<>(); // Farbvarianteninhalte pro Farbvariantentabelle
        public String colorSign = ""; // Farbidentifikationskennzeichen (FIKZ)

        /**
         * Fügt die übergebene Farbvariantentabelle an einem Teil hinzu.
         *
         * @param colorTableToPart
         */
        public void addColorTableToPart(ColorTableToPart colorTableToPart) {
            colorTableToPartsMap.put(colorTableToPart.colorTableId, colorTableToPart);
        }

        /**
         * Sortiert die Farbvarianteninhalte nach Farbvariantentabellen-ID, Position, Farbvariante und SDATA.
         */
        public void sortColorTableContents() {
            Collections.sort(colorTableContents, new Comparator<ColorTableContent>() {
                @Override
                public int compare(ColorTableContent o1, ColorTableContent o2) {
                    String o1Key = StrUtils.stringArrayToString("|", o1.colorTableContentId.getColorTableId(), o1.colorTableContentId.getPosition(),
                                                                o1.colorNumber, o1.colorTableContentId.getSDATA());
                    String o2Key = StrUtils.stringArrayToString("|", o2.colorTableContentId.getColorTableId(), o2.colorTableContentId.getPosition(),
                                                                o2.colorNumber, o2.colorTableContentId.getSDATA());
                    return o1Key.compareTo(o2Key);
                }
            });
        }

        /**
         * Liefert den {@link ColorTableContent} für die übergebene {@link iPartsColorTableContentId} zurück.
         *
         * @param colorTableContentId {@code null} falls kein passender {@link ColorTableContent} gefunden werden konnte
         */
        public ColorTableContent getColorTableContent(iPartsColorTableContentId colorTableContentId) {
            for (ColorTableContent colorTableContent : colorTableContents) {
                if (colorTableContent.colorTableContentId.equals(colorTableContentId)) {
                    return colorTableContent;
                }
            }

            return null;
        }
    }


    /**
     * PEMs und Werksdaten für Farbinhalte oder Farben zu Teil
     */
    public static abstract class AbstractColortablePEMEvaluation {

        private iPartsColorFactoryDataForRetail factoryData;
        private boolean evalPemTo;
        private boolean evalPemFrom;

        protected void clonePemFlags(AbstractColortablePEMEvaluation pemEvaluationObject) {
            pemEvaluationObject.evalPemFrom = evalPemFrom;
            pemEvaluationObject.evalPemTo = evalPemTo;
        }

        public iPartsColorFactoryDataForRetail getFactoryData() {
            return factoryData;
        }

        public void setFactoryData(iPartsColorFactoryDataForRetail factoryData) {
            this.factoryData = factoryData;
        }

        public boolean isEvalPemTo() {
            return evalPemTo;
        }

        public void setEvalPemTo(boolean evalPemTo) {
            this.evalPemTo = evalPemTo;
        }

        public boolean isEvalPemFrom() {
            return evalPemFrom;
        }

        public void setEvalPemFrom(boolean evalPemFrom) {
            this.evalPemFrom = evalPemFrom;
        }

        public abstract String getColorTableId();
    }


    /**
     * Eine konkrete Farbvariantentabelle an einem Teil inkl. Werkseinsatzdaten.
     */
    public static class ColorTableToPart extends AbstractColortablePEMEvaluation {

        public iPartsColorTableToPartId colorTableId;
        public iPartsImportDataOrigin dataOrigin;
        private iPartsDataColorTableToPart dataColorTableToPart;
        private boolean dataColorTableToPartLoaded;

        @Override
        public String getColorTableId() {
            return colorTableId.getColorTableId();
        }

        /**
         * Klont diese Farbvariantentabelle an einem Teil mit oder ohne die Werkseinsatzdaten.
         *
         * @param cloneFactoryData
         * @param cloneDataColorTableToPart
         * @return
         */
        public ColorTableToPart cloneMe(boolean cloneFactoryData, boolean cloneDataColorTableToPart) {
            ColorTableToPart colorTableToPartClone = new ColorTableToPart();
            colorTableToPartClone.colorTableId = colorTableId;
            if (cloneFactoryData && (getFactoryData() != null)) {
                colorTableToPartClone.setFactoryData(getFactoryData().cloneMe());
            } else {
                colorTableToPartClone.setFactoryData(getFactoryData());
            }
            colorTableToPartClone.dataOrigin = dataOrigin;
            clonePemFlags(colorTableToPartClone);
            if (cloneDataColorTableToPart && (dataColorTableToPart != null)) {
                colorTableToPartClone.dataColorTableToPartLoaded = dataColorTableToPartLoaded;
                colorTableToPartClone.dataColorTableToPart = dataColorTableToPart.cloneMe(dataColorTableToPart.getEtkProject());
            }
            return colorTableToPartClone;
        }

        public iPartsDataColorTableToPart getDataColorTableToPart(boolean loadColorTableToPart, EtkProject project) {
            if ((dataColorTableToPart == null) || (loadColorTableToPart && !dataColorTableToPartLoaded)) {
                DBDataObjectAttributes existingAttributes = null;
                if (dataColorTableToPart != null) {
                    existingAttributes = dataColorTableToPart.getAttributes();
                }

                dataColorTableToPart = new iPartsDataColorTableToPart(project, colorTableId);
                if (loadColorTableToPart) {
                    dataColorTableToPartLoaded = true;
                    if (!dataColorTableToPart.existsInDB()) {
                        dataColorTableToPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }

                    // Vorher schon gesetzte Attribute (für den Filtergrund) hinzufügen
                    if (existingAttributes != null) {
                        DBDataObjectAttributes attributes = dataColorTableToPart.getAttributes();
                        for (Map.Entry<String, DBDataObjectAttribute> attributeEntry : existingAttributes.entrySet()) {
                            if (!attributes.fieldExists(attributeEntry.getKey())) {
                                attributes.addField(attributeEntry.getValue(), DBActionOrigin.FROM_DB);
                            }
                        }
                    }
                } else {
                    dataColorTableToPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                }
            }
            return dataColorTableToPart;
        }
    }

    /**
     * Farbvarianteninhalt für eine konkrete Farbvariantentabelle an einem Teil.
     */
    public static class ColorTableContent extends AbstractColortablePEMEvaluation {

        public iPartsColorTableContentId colorTableContentId;
        public long sdata; // theoretisch redundant, da in colorTableContentId ebenfalls enthalten (dort aber als String -> für die Filterung hier auch als long)
        public String colorNumber; // ES2
        public EtkMultiSprache colorName; // Benennung vom ES2
        public String productGroup;
        public String code;
        public String etkz;
        public String eventFrom;
        public String eventTo;
        public String eventFromAS;
        public String eventToAS;
        private iPartsDataColorTableContent dataColorTableContent;
        private boolean dataColorTableContentLoaded;

        @Override
        public String getColorTableId() {
            return colorTableContentId.getColorTableId();
        }

        /**
         * Klont diese Farbvarianteninhalte mit oder ohne die Werkseinsatzdaten.
         *
         * @param withFactoryData
         * @return
         */
        public ColorTableContent cloneMe(boolean withFactoryData, boolean cloneDataColorTableContent) {
            ColorTableContent colorTableContentClone = new ColorTableContent();
            colorTableContentClone.colorTableContentId = colorTableContentId;
            colorTableContentClone.sdata = sdata;
            colorTableContentClone.colorNumber = colorNumber;
            if (colorName != null) {
                colorTableContentClone.colorName = colorName.cloneMe();
            }
            colorTableContentClone.productGroup = productGroup;
            colorTableContentClone.code = code;
            colorTableContentClone.etkz = etkz;
            clonePemFlags(colorTableContentClone);
            colorTableContentClone.eventFrom = eventFrom;
            colorTableContentClone.eventTo = eventTo;
            colorTableContentClone.eventFromAS = eventFromAS;
            colorTableContentClone.eventToAS = eventToAS;
            if (withFactoryData && (getFactoryData() != null)) {
                colorTableContentClone.setFactoryData(getFactoryData().cloneMe());
            }
            if (cloneDataColorTableContent && (dataColorTableContent != null)) {
                colorTableContentClone.dataColorTableContentLoaded = dataColorTableContentLoaded;
                colorTableContentClone.dataColorTableContent = dataColorTableContent.cloneMe(dataColorTableContent.getEtkProject());
            }
            return colorTableContentClone;
        }

        /**
         * Liefert die effektive ID vom Ereignis-ab, welches für den Filter verwendet wird. Ein evtl. vorhandenes AS-Ereignis-ab
         * gewinnt vor einem Konstruktions-Ereignis-ab.
         *
         * @return
         */
        public String getFilterEventFromId() {
            boolean suppressConstEventFrom = iPartsEvent.isNotRelevantEventId(eventFromAS);
            return getFilterEventId(suppressConstEventFrom, eventFromAS, eventFrom);
        }

        /**
         * Liefert die effektive ID vom Ereignis-bis, welches für den Filter verwendet wird. Ein evtl. vorhandenes AS-Ereignis-bis
         * gewinnt vor einem Konstruktions-Ereignis-bis.
         *
         * @return
         */
        public String getFilterEventToId() {
            boolean suppressConstEventTo = iPartsEvent.isNotRelevantEventId(eventToAS);
            return getFilterEventId(suppressConstEventTo, eventToAS, eventTo);
        }

        /**
         * Liefert abhängig von <code>suppressConstEvent</code> den richtigen Event-Wert zurück
         *
         * @param suppressConstEvent Soll das Konstruktions-Ereignis unterdrückt werden, weil es nicht relevant ist?
         * @param eventAS            After-Sales-Ereignis
         * @param eventConst         Konstruktions-Ereignis
         * @return
         */
        private String getFilterEventId(boolean suppressConstEvent, String eventAS, String eventConst) {
            if (!suppressConstEvent) {
                if (StrUtils.isValid(eventAS)) {
                    return eventAS;
                } else {
                    return eventConst;
                }
            }
            return "";
        }

        public iPartsDataColorTableContent getDataColorTableContent(boolean loadColorTableContent, EtkProject project) {
            if ((dataColorTableContent == null) || (loadColorTableContent && !dataColorTableContentLoaded)) {
                DBDataObjectAttributes existingAttributes = null;
                if (dataColorTableContent != null) {
                    existingAttributes = dataColorTableContent.getAttributes();
                }

                dataColorTableContent = new iPartsDataColorTableContent(project, colorTableContentId);
                if (loadColorTableContent) {
                    dataColorTableContentLoaded = true;
                    if (!dataColorTableContent.existsInDB()) {
                        dataColorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }

                    // Vorher schon gesetzte Attribute (für den Filtergrund) hinzufügen
                    if (existingAttributes != null) {
                        DBDataObjectAttributes attributes = dataColorTableContent.getAttributes();
                        for (Map.Entry<String, DBDataObjectAttribute> attributeEntry : existingAttributes.entrySet()) {
                            if (!attributes.fieldExists(attributeEntry.getKey())) {
                                attributes.addField(attributeEntry.getValue(), DBActionOrigin.FROM_DB);
                            }
                        }
                    }
                } else {
                    dataColorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                }
            }
            return dataColorTableContent;
        }
    }
}