/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.config.db.datatypes.VisObject;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.BillableAuthorOrderExport;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse für den Export von abrechnungsrelevanten Objektinformationen.
 * Die Klasse enthält die Spalten und Überschriften als {@link EtkDisplayField}s
 */
public class iPartsAuthorOrderExportFormatter implements iPartsConst {

    // Echte Feldnamen aus der DB
    private static final String[] WORK_ORDER_COLUMNS_NAMES = new String[]{ FIELD_DWO_BST_ID, FIELD_DWO_ORDER_NO, /*FIELD_DWO_SERIES,
                                                                           FIELD_DWO_BRANCH, FIELD_DWO_SUB_BRANCHES,*/ FIELD_DWO_COST_NEUTRAL,
                                                                           /*FIELD_DWO_INTERNAL_ORDER, FIELD_DWO_RELEASE_NO,*/ FIELD_DWO_TITLE,
                                                                           FIELD_DWO_DELIVERY_DATE_PLANNED, FIELD_DWO_START_OF_WORK, FIELD_DWO_SUPPLIER_NO/*,
                                                                           FIELD_DWO_SUPPLIER_SHORTNAME, FIELD_DWO_SUPPLIER_NAME*/ };
    private static final String[] WORK_TASK_COLUMNS_NAMES = new String[]{ /*FIELD_DWT_BST_ID, FIELD_DWT_LFDNR, FIELD_DWT_ACTIVITY_NAME,
                                                                          FIELD_DWT_ACTIVITY_TYPE, FIELD_DWT_AMOUNT*/ };
    private static final String[] AUTHOR_COLUMNS_NAMES = new String[]{ FIELD_DAO_GUID, FIELD_DAO_NAME, /*FIELD_DAO_DESC,*/
                                                                       FIELD_DAO_STATUS, /*FIELD_DAO_CREATION_DATE, FIELD_DAO_CREATION_USER_ID,*/
                                                                       FIELD_DAO_CHANGE_SET_ID/*, FIELD_DAO_CURRENT_USER_ID, FIELD_DAO_CREATOR_GRP_ID,
                                                                       FIELD_DAO_CURRENT_GRP_ID, FIELD_DAO_BST_ID, FIELD_DAO_BST_SUPPLIED,
                                                                       FIELD_DAO_BST_ERROR*/ };
    private static final String[] DIALOG_PARTLISTENTRY_COLUMNS_NAMES = new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM,
                                                                                     FIELD_DD_POSE, FIELD_DD_POSV, FIELD_DD_WW, FIELD_DD_ETKZ,
                                                                                     FIELD_DD_AA, FIELD_DD_SDATA };
    private static final String[] VARTABLE_COLUMNS_NAMES = new String[]{ FIELD_DCTC_TABLE_ID, FIELD_DCTC_POS, FIELD_DCTC_SDATA };

    private static final String[] PICORDER_COLUMNS_NAMES = new String[]{ FIELD_DA_PO_ORDER_ID_EXTERN, FIELD_DA_PO_ORDER_REVISION_EXTERN };

    private static final String[] EDS_PARTLISTENTRY_COLUMNS_NAMES = new String[]{ FIELD_K_VARI, FIELD_K_LFDNR };

    private static final String[] PICORDER_BTT_TEMPLATE_COLUMNS_NAMES = new String[]{ FIELD_DA_PO_AUTOMATION_LEVEL, FIELD_DA_PO_IS_TEMPLATE };

    // Zusätzliche Felder samt Fallback-Feldüberschriften
    private static final Map<String, String> AUTHOR_EXTRA_COLUMNS_FIELD_NAMES = new HashMap<>();
    private static final Map<String, String> PICORDER_EXTRA_COLUMNS_FIELD_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> EDS_PARTLISTENTRY_EXTRA_COLUMNS_FIELD_NAMES = new HashMap<>();

    // Einzelspalten für die man keine Map und kein Array braucht
    private static final SingleColumnData OBJECT_TYPE_FIELD = new SingleColumnData("DUMMY", "exportObjectType", "!!Objekttyp");
    private static final SingleColumnData AUTO_CREATED_FIELD = new SingleColumnData(TABLE_KATALOG, FIELD_K_AUTO_CREATED, null);
    private static final SingleColumnData STATUS_CHANGED_FIELD = new SingleColumnData("DUMMY", "statusChangedField", "!!Statusänderung");
    private static final SingleColumnData AUTO_STATE_FIELD = new SingleColumnData("DUMMY", "autoStateField", "!!Auto-Zustand");

    static {
        AUTHOR_EXTRA_COLUMNS_FIELD_NAMES.put(TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_COMMIT_DATE), "!!Freigabedatum");
        PICORDER_EXTRA_COLUMNS_FIELD_NAMES.put(TableAndFieldName.make(TABLE_DA_PICORDER, BillableAuthorOrderExport.FIELD_FOR_PIC_ORDER_TYPE), "!!Auftragstyp");
        PICORDER_EXTRA_COLUMNS_FIELD_NAMES.put(TableAndFieldName.make(TABLE_DA_PICORDER, BillableAuthorOrderExport.FIELD_FOR_PIC_ORDER_PRODUCT), "!!Produkt für Bildauftrag");
        EDS_PARTLISTENTRY_EXTRA_COLUMNS_FIELD_NAMES.put(TableAndFieldName.make(TABLE_KATALOG, BillableAuthorOrderExport.FIELD_FOR_EDS_PRODUCT), "!!Produkt für Truck");
    }

    public enum AUTO_STATE {
        MANUAL("!!Manuell erzeugt"),
        AUTO_NOT_MODIFIED("!!Automatisch erzeugt, nicht modifiziert"),
        AUTO_MODIFIED("!!Automatisch erzeugt, modifiziert");

        private String exportText;

        AUTO_STATE(String exportText) {
            this.exportText = exportText;
        }

        public String getExportText() {
            return exportText;
        }
    }

    private final EtkProject projectForCalculation;
    private List<EtkDisplayField> authorOrderHeaderFields; // EtkDisplayField Reihenfolge für Autorenaufträge (DA_AUTHOR_ORDER)
    private List<EtkDisplayField> workOrderHeaderFields; // EtkDisplayField Reihenfolge für Bearbeitungsaufträge (DA_WORKORDER)
    private List<EtkDisplayField> workTaskHeaderFields; // EtkDisplayField Reihenfolge für Tasks von Bearbeitungsaufträgen (DA_WORKORDER_TASKS)
    private List<EtkDisplayField> dialogPartListEntryHeaderFields; // EtkDisplayField Reihenfolge für die Bestandtteile des BCTE Schlüssel (DA_DIALOG)
    private List<EtkDisplayField> varTableHeaderFields; // EtkDisplayField Reihenfolge für Varianten zu Variantentabellen (DA_COLORTABLE_CONTENT)
    private List<EtkDisplayField> picOrderHeaderFields; // EtkDisplayField Reihenfolge für Bildaufträge (DA_PICORDER)
    private List<EtkDisplayField> edsPartListEntryHeaderFields; // EtkDisplayField Reihenfolge für die Bestandtteile des Truck-Schlüssels (KATALOG)
    private List<EtkDisplayField> picOrderBTTTemplateHeaderFields;  // EtkDisplayField Reihenfolge für die BTT-Template Felder der Bildaufträge (DA_PICORDER)
    private List<List<EtkDisplayField>> headerFieldsSequence; // EtkDisplayField Reihenfolge für Tasks von Bearbeitungsaufträgen (DA_WORKORDER_TASKS)
    private List<String> outputHeadValues;

    public iPartsAuthorOrderExportFormatter(EtkProject projectForCalculation) {
        this.projectForCalculation = projectForCalculation;
        this.outputHeadValues = null;
        initHeaderFields();
    }

    /**
     * Exportiert die abrechnungsrelevanten Objektinformationen im übergebenen {@link iPartsAuthorOrderExportObject}
     *
     * @param exportObject
     * @return
     */
    public List<List<String>> exportOneExportObject(iPartsAuthorOrderExportObject exportObject) {
        List<List<String>> result = new DwList<>();
        initOutputHeadValuesForAuthorOrder(exportObject);
        List<List<String>> contentList = createExportRows(exportObject);
        result.addAll(contentList);
        return result;
    }

    /**
     * Liefert die Spaltenüberschriften für die Exportdatei
     *
     * @return
     */
    public List<String> getHeader() {
        List<String> headerList = new DwList<>();
        String viewerLanguage = projectForCalculation.getViewerLanguage();
        for (List<EtkDisplayField> headerField : headerFieldsSequence) {
            headerList.addAll(getHeaderForElement(headerField, viewerLanguage));
        }
        return headerList;
    }

    /**
     * Erzeugt eine Zeile mit allen Inhalten für die übergebenen Datenobjekte {@link iPartsDialogBCTEPrimaryKey}
     * und {@link iPartsDataPicOrder}
     * <p>
     * Hier ist ganz wichtig, dass man die Reihenfolge der Felder beachtet:
     * - erst die fest definierten Werte aus den Listen
     * - danach die generierten Daten: Objekttyp und Status-Flag
     * - danach AutoCreate-Flag
     *
     * @param bcteKeyForExport
     * @param picOrderForExport
     * @param colorTableContent
     * @param partListEntryIdForExport
     * @param exportObject
     * @return
     */
    public List<String> createExportRow(iPartsDialogBCTEPrimaryKey bcteKeyForExport, iPartsDataPicOrder picOrderForExport,
                                        iPartsDataColorTableContent colorTableContent, PartListEntryId partListEntryIdForExport,
                                        iPartsAuthorOrderExportObject exportObject) {
        // Definierte Listen
        List<String> result = new DwList<>();
        result.addAll(outputHeadValues);
        result.addAll(setOutputValuesByBCTEKey(bcteKeyForExport, dialogPartListEntryHeaderFields));
        result.addAll(setOutputValuesByDataObject(colorTableContent, varTableHeaderFields));
        result.addAll(setOutputValuesByDataObject(picOrderForExport, picOrderHeaderFields));
        result.addAll(setOutputValuesByPartListEntryId(partListEntryIdForExport, edsPartListEntryHeaderFields));
        // Objekttyp
        addObjectType(result, bcteKeyForExport, picOrderForExport, colorTableContent, partListEntryIdForExport);
        // "Automatisch erzeugt" Flag setzen
        addAutoCreateFlag(result, bcteKeyForExport, exportObject);
        // Flag, ob es eine reine Statusänderung ist
        addStatusChangeFlag(result, bcteKeyForExport, colorTableContent, exportObject);
        // BTT-Template Felder der Bildaufträge
        result.addAll(setOutputValuesByDataObject(picOrderForExport, picOrderBTTTemplateHeaderFields));
        // DAIMLER-15362 AUTO_STATE-Werte setzen
        addAutoState(result, bcteKeyForExport, exportObject);

        return result;
    }

    /**
     * Setzt den Wert für die AutoCreate Spalte (Flag, ob der Datensatz automatisch erzeugt wurde)
     *
     * @param result
     * @param bcteKeyForExport
     * @param exportObject
     */
    private void addAutoCreateFlag(List<String> result, iPartsDialogBCTEPrimaryKey bcteKeyForExport, iPartsAuthorOrderExportObject exportObject) {
        boolean autoValue = (bcteKeyForExport != null) && exportObject.getBcteKeyToAutoCreatedMap().get(bcteKeyForExport);
        result.add(projectForCalculation.getVisObject().asText(TABLE_KATALOG, FIELD_K_AUTO_CREATED,
                                                               SQLStringConvert.booleanToPPString(autoValue),
                                                               projectForCalculation.getDBLanguage()));
    }

    private void addAutoState(List<String> result, iPartsDialogBCTEPrimaryKey bcteKeyForExport, iPartsAuthorOrderExportObject exportObject) {
        AUTO_STATE autoState = AUTO_STATE.MANUAL;
        if (bcteKeyForExport != null) {
            if ((exportObject.getBcteKeyForAutoState() != null) && exportObject.getBcteKeyForAutoState().get(bcteKeyForExport) != null) {
                autoState = exportObject.getBcteKeyForAutoState().get(bcteKeyForExport);
            }
        }
        result.add(TranslationHandler.translateForLanguage(autoState.getExportText(), projectForCalculation.getDBLanguage()));
    }

    /**
     * Setzt den Wert für die Statusänderung-Spalte (Flag, ob der Datensatz eine reine Statusänderung hat)
     *
     * @param result
     * @param bcteKeyForExport
     * @param colorTableContent
     * @param exportObject
     */
    private void addStatusChangeFlag(List<String> result, iPartsDialogBCTEPrimaryKey bcteKeyForExport,
                                     iPartsDataColorTableContent colorTableContent, iPartsAuthorOrderExportObject exportObject) {
        // Wenn es ein BCTE Schlüssel oder eine Farbtabelleninhalt-ID ist, dann muss geprüft werden, ob eine Statusänderung
        // der Grund für den Export war
        boolean statusValue = false;
        boolean isBCTEKeyData = bcteKeyForExport != null;
        boolean isColorContentData = colorTableContent != null;
        if ((isBCTEKeyData || isColorContentData) && !exportObject.getStatusChangedData().isEmpty()) {
            if (isBCTEKeyData) {
                statusValue = exportObject.getStatusChangedData().contains(bcteKeyForExport.toString());
            } else {
                statusValue = exportObject.getStatusChangedData().contains(colorTableContent.getAsId().toString(BillableAuthorOrderExport.CONTENT_ID_DELIMITER));
            }
        }
        // Status Wert setzen (Darstellung als boolean, wie das AutoCreate Flag)
        result.add(projectForCalculation.getVisObject().asText(TABLE_KATALOG, FIELD_K_AUTO_CREATED, SQLStringConvert.booleanToPPString(statusValue),
                                                               projectForCalculation.getDBLanguage()));
    }

    /**
     * Setzt den Wert für die Objekttyp-Spalte
     *
     * @param result
     * @param bcteKeyForExport
     * @param picOrderForExport
     * @param colorTableContent
     * @param partListEntryIdForExport
     */
    private void addObjectType(List<String> result, iPartsDialogBCTEPrimaryKey bcteKeyForExport,
                               iPartsDataPicOrder picOrderForExport, iPartsDataColorTableContent colorTableContent,
                               PartListEntryId partListEntryIdForExport) {
        if (bcteKeyForExport != null) {
            result.add("PKW");
        } else if (picOrderForExport != null) {
            result.add("PICTURE_ORDER");
        } else if (colorTableContent != null) {
            // Variantentabellenänderungen Momentan nur bei PKW
            result.add("COLOR_VARIANT");
        } else if (partListEntryIdForExport != null) {
            result.add("TRUCK");
        }
    }


    /**
     * Erzeugt pro abrechnungsrelevanten Objekt im übergebenen {@link iPartsAuthorOrderExportObject} eine Zeile für
     * den XML Export.
     *
     * @param exportObject
     * @return
     */
    public List<List<String>> createExportRows(iPartsAuthorOrderExportObject exportObject) {
        List<List<String>> result = new DwList<>();
        if (exportObject.hasBCTEKeys()) {
            for (Map.Entry<iPartsDialogBCTEPrimaryKey, Boolean> bcteKeyEntry : exportObject.getBcteKeyToAutoCreatedMap().entrySet()) {
                result.add(createExportRow(bcteKeyEntry.getKey(), null, null, null, exportObject));
            }
        }

        if (exportObject.hasPartListEntryIds()) {
            for (PartListEntryId partListEntryId : exportObject.getPartListEntryIds()) {
                result.add(createExportRow(null, null, null, partListEntryId, exportObject));
            }
        }

        if (exportObject.hasPicOrders()) {
            for (iPartsDataPicOrder picOrder : exportObject.getPicOrders()) {
                result.add(createExportRow(null, picOrder, null, null, exportObject));
            }
        }

        if (exportObject.hasColorTableContentIds()) {
            // Bei Farbtabellen haben wir nur die IDs. Um eine Konfigurationsänderung bezüglich der Displayfields abzufangen
            // (z.B. Reihenfolgenänderung oder Hinzufügen oder Entfernen von Displayfields) wird hier ein künstliches
            // EtkDataObject angelegt, dass bei jedem Durchlauf eine andere ID bekommt - weil wir ja eigentlich nur die
            // Schlüsselfelder ausgeben.
            iPartsDataColorTableContent fakeColorTableContent = new iPartsDataColorTableContent(projectForCalculation, new iPartsColorTableContentId("", "", ""));
            fakeColorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            for (iPartsColorTableContentId colorTableContentId : exportObject.getColorTableContentIds()) {
                fakeColorTableContent.setId(colorTableContentId, DBActionOrigin.FROM_EDIT);
                result.add(createExportRow(null, null, fakeColorTableContent, null, exportObject));
            }
        }
        return result;
    }

    private void initHeaderFields() {
        headerFieldsSequence = new DwList<>();
        String tableName = TABLE_DA_WORKORDER;
        workOrderHeaderFields = fillFields(tableName, WORK_ORDER_COLUMNS_NAMES);
        headerFieldsSequence.add(workOrderHeaderFields);

        tableName = TABLE_DA_WORKORDER_TASKS;
        workTaskHeaderFields = fillFields(tableName, WORK_TASK_COLUMNS_NAMES);
        headerFieldsSequence.add(workTaskHeaderFields);

        tableName = TABLE_DA_AUTHOR_ORDER;
        authorOrderHeaderFields = fillFields(tableName, AUTHOR_COLUMNS_NAMES, AUTHOR_EXTRA_COLUMNS_FIELD_NAMES);
        headerFieldsSequence.add(authorOrderHeaderFields);

        tableName = TABLE_DA_DIALOG;
        dialogPartListEntryHeaderFields = fillFields(tableName, DIALOG_PARTLISTENTRY_COLUMNS_NAMES);
        headerFieldsSequence.add(dialogPartListEntryHeaderFields);

        tableName = TABLE_DA_COLORTABLE_CONTENT;
        varTableHeaderFields = fillFields(tableName, VARTABLE_COLUMNS_NAMES);
        headerFieldsSequence.add(varTableHeaderFields);

        tableName = TABLE_DA_PICORDER;
        picOrderHeaderFields = fillFields(tableName, PICORDER_COLUMNS_NAMES, PICORDER_EXTRA_COLUMNS_FIELD_NAMES);
        headerFieldsSequence.add(picOrderHeaderFields);

        tableName = TABLE_KATALOG;
        edsPartListEntryHeaderFields = fillFields(tableName, null, EDS_PARTLISTENTRY_EXTRA_COLUMNS_FIELD_NAMES);
        edsPartListEntryHeaderFields.addAll(fillFields(tableName, EDS_PARTLISTENTRY_COLUMNS_NAMES));
        headerFieldsSequence.add(edsPartListEntryHeaderFields);

        // Objekttyp
        headerFieldsSequence.add(fillSingleColumn(OBJECT_TYPE_FIELD));
        // Flag, ob automatisch erzeugt
        headerFieldsSequence.add(fillSingleColumn(AUTO_CREATED_FIELD));
        // Flag, ob reine Statusänderung
        headerFieldsSequence.add(fillSingleColumn(STATUS_CHANGED_FIELD));

        // Felder für die BTT-Template im Bildauftrag
        tableName = TABLE_DA_PICORDER;
        picOrderBTTTemplateHeaderFields = fillFields(tableName, PICORDER_BTT_TEMPLATE_COLUMNS_NAMES, null);
        headerFieldsSequence.add(picOrderBTTTemplateHeaderFields);
        // Flag, mit dem Zustand nach Auto-Übernahme
        headerFieldsSequence.add(fillSingleColumn(AUTO_STATE_FIELD));
    }

    /**
     * Erzeugt eine Einzelspalte für die Daten im übergebenen {@link SingleColumnData}
     *
     * @param singleColumnData
     * @return
     */
    private List<EtkDisplayField> fillSingleColumn(SingleColumnData singleColumnData) {
        List<EtkDisplayField> headerFields = new DwList<>();
        EtkDatabaseTable dbTable = projectForCalculation.getConfig().getDBDescription().getTable(singleColumnData.getTableName());
        addDisplayFieldToHeaderFields(headerFields, singleColumnData, dbTable);
        return headerFields;
    }

    /**
     * Erzeugt zum übergebenen {@link SingleColumnData} Objekt ein {@link EtkDisplayField} Objekt und legt es in die
     * übergebene Liste
     *
     * @param headerFields
     * @param singleColumnData
     * @param dbTable
     */
    private void addDisplayFieldToHeaderFields(List<EtkDisplayField> headerFields, SingleColumnData singleColumnData,
                                               EtkDatabaseTable dbTable) {
        if ((projectForCalculation != null) && (headerFields != null) && (singleColumnData != null)) {
            EtkDisplayField displayField = createDisplayField(singleColumnData.getTableName(), singleColumnData.getFieldName(), dbTable,
                                                              projectForCalculation.getViewerLanguage(),
                                                              projectForCalculation.getDataBaseFallbackLanguages(),
                                                              singleColumnData.getFallbackTitle());
            headerFields.add(displayField);
        }

    }

    private List<EtkDisplayField> fillFields(String tableName, String[] firstColumnsNames) {
        return fillFields(tableName, firstColumnsNames, null);
    }

    /**
     * Sammelt eine {@link EtkDisplayField}s Spalten für die übergebene Tabelle auf. Zusätzliche Spalten können mit
     * <code>extraColumnNames</code> hinzugefügt werden.
     *
     * @param tableName
     * @param columnsNames
     * @param extraColumnNames
     * @return
     */
    private List<EtkDisplayField> fillFields(String tableName, String[] columnsNames, Map<String, String> extraColumnNames) {
        List<EtkDisplayField> headerFields = new DwList<>();
        String viewerLanguage = projectForCalculation.getViewerLanguage();
        List<String> dataBaseFallbackLanguages = projectForCalculation.getDataBaseFallbackLanguages();
        EtkDatabaseTable dbTable = projectForCalculation.getConfig().getDBDescription().getTable(tableName);
        if (columnsNames != null) {
            for (String fieldName : columnsNames) {
                EtkDisplayField displayField = createDisplayField(tableName, fieldName, dbTable,
                                                                  viewerLanguage, dataBaseFallbackLanguages, null);
                headerFields.add(displayField);
            }
        }
        if (extraColumnNames != null) {
            for (Map.Entry<String, String> tableAndFieldName : extraColumnNames.entrySet()) {
                String fieldName = TableAndFieldName.getFieldName(tableAndFieldName.getKey());
                dbTable = projectForCalculation.getConfig().getDBDescription().getTable(TableAndFieldName.getTableName(tableAndFieldName.getKey()));
                EtkDisplayField displayField = createDisplayField(TableAndFieldName.getTableName(tableAndFieldName.getKey()), fieldName, dbTable,
                                                                  viewerLanguage, dataBaseFallbackLanguages, tableAndFieldName.getValue());
                headerFields.add(displayField);
            }
        }
        return headerFields;
    }

    /**
     * Erzeugt ein {@link EtkDisplayField} samt Titel für die übergebene Tabelle und das übergebene Feld. Kann der Titel
     * für das Feld nicht aus der DB bestimmt werden, wird geprüft, ob ein "Fallback-Titel" übergeben wurde. Ist das
     * nicht der Fall, wird der Feldname als Titel gewählt.
     *
     * @param tableName
     * @param fieldName
     * @param dbTable
     * @param viewerLanguage
     * @param dataBaseFallbackLanguages
     * @param fallbackTitle
     * @return
     */
    private EtkDisplayField createDisplayField(String tableName, String fieldName, EtkDatabaseTable dbTable,
                                               String viewerLanguage, List<String> dataBaseFallbackLanguages,
                                               String fallbackTitle) {
        EtkDatabaseField dbField = null;
        if (dbTable != null) {
            dbField = dbTable.getField(fieldName);
        }
        EtkDisplayField displayField = new EtkDisplayField(tableName, fieldName, false, false);
        // Text noch setzen
        EtkMultiSprache multi = new EtkMultiSprache();
        if (dbField != null) {
            multi.setText(viewerLanguage, dbField.getDisplayText(viewerLanguage, dataBaseFallbackLanguages));
        } else if (StrUtils.isValid(fallbackTitle)) {
            multi.setText(viewerLanguage, TranslationHandler.translate(fallbackTitle));
        } else {
            multi.setText(viewerLanguage, fieldName);
        }
        displayField.setText(multi);
        displayField.setDefaultText(false);
        return displayField;
    }

    /**
     * Liefert die Spaltenüberschriften der übergebenen {@link EtkDisplayField}s für die übergebene Sprache
     *
     * @param headerFields
     * @param viewerLanguage
     * @return
     */
    private List<String> getHeaderForElement(List<EtkDisplayField> headerFields, String viewerLanguage) {
        List<String> result = new DwList<>();
        for (EtkDisplayField displayField : headerFields) {
            result.add(displayField.getText().getText(viewerLanguage));
        }
        return result;
    }

    /**
     * Initialisiert die Spalten samt Spaltentitel für den Export
     *
     * @param authorOrderExportObject
     */
    private void initOutputHeadValuesForAuthorOrder(iPartsAuthorOrderExportObject authorOrderExportObject) {
        outputHeadValues = new DwList<>();

        List<String> outputElemsWorkOrder = setOutputConstantValuesByDataObject(authorOrderExportObject.getDataWorkOrder(), authorOrderExportObject.getChangeSet(), workOrderHeaderFields);
        List<String> outputElemsWorkOrderTasks = setOutputConstantValuesByDataObject(authorOrderExportObject.getDataWorkOrderTask(), authorOrderExportObject.getChangeSet(), workTaskHeaderFields);
        List<String> outputElemsAuthorOrder = setOutputConstantValuesByDataObject(authorOrderExportObject.getAuthorOrder(), authorOrderExportObject.getChangeSet(), authorOrderHeaderFields);

        outputHeadValues.addAll(outputElemsWorkOrder);
        outputHeadValues.addAll(outputElemsWorkOrderTasks);
        outputHeadValues.addAll(outputElemsAuthorOrder);

    }

    /**
     * Bestimmt die Werte aus dem übergebenen {@link EtkDataObject} und {@link iPartsDataChangeSet} für die übergebenen
     * {@link EtkDisplayField}s.
     *
     * @param dataObject
     * @param changeSet
     * @param headerFields
     * @return
     */
    private List<String> setOutputConstantValuesByDataObject(EtkDataObject dataObject, iPartsDataChangeSet changeSet,
                                                             List<EtkDisplayField> headerFields) {
        String dbLanguage = projectForCalculation.getDBLanguage();
        VisObject visObject = projectForCalculation.getVisObject();

        List<String> outputElems = new DwList<>();
        for (EtkDisplayField displayField : headerFields) {
            String fieldName = displayField.getKey().getFieldName();
            // Bei CSV müssen alle Spalten immer vorhanden sein.
            String value = "";
            if (dataObject != null) {
                if (dataObject.getTableName().equals(displayField.getKey().getTableName())) {
                    value = dataObject.getFieldValue(fieldName);
                    if (StrUtils.isValid(value)) {
                        value = visObject.asText(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                                 value, dbLanguage);
                        if (value == null) {
                            value = "";
                        }
                    }
                } else {
                    switch (displayField.getKey().getTableName()) {
                        case TABLE_DA_CHANGE_SET:
                            if (changeSet != null) {
                                value = visObject.asText(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                                         changeSet.getFieldValue(fieldName), dbLanguage);
                            }
                            break;
                    }
                }
            }
            outputElems.add(value);
        }
        return outputElems;
    }

    private List<String> setOutputValuesByDataObject(EtkDataObject dataObject, List<EtkDisplayField> headerFields) {
        return setOutputConstantValuesByDataObject(dataObject, null, headerFields);
    }

    /**
     * Extrahiert alle Bestandteile des übergebenen BCTE Schlüssels und legt sie in der Reihenfolge ihrer {@link EtkDisplayField}s
     * ab.
     *
     * @param bcteKeyForExport
     * @param headerFields
     * @return
     */
    private List<String> setOutputValuesByBCTEKey(iPartsDialogBCTEPrimaryKey bcteKeyForExport, List<EtkDisplayField> headerFields) {
        String dbLanguage = projectForCalculation.getDBLanguage();
        VisObject visObject = projectForCalculation.getVisObject();

        List<String> outputElems = new DwList<>();
        for (EtkDisplayField displayField : headerFields) {
            String fieldName = displayField.getKey().getFieldName();
            // Bei CSV müssen alle Spalten immer vorhanden sein.
            String value = "";
            if (bcteKeyForExport != null) {
                switch (fieldName) {
                    case FIELD_DD_SERIES_NO:
                        value = bcteKeyForExport.seriesNo;
                        break;
                    case FIELD_DD_HM:
                        value = bcteKeyForExport.hm;
                        break;
                    case FIELD_DD_M:
                        value = bcteKeyForExport.m;
                        break;
                    case FIELD_DD_SM:
                        value = bcteKeyForExport.sm;
                        break;
                    case FIELD_DD_POSE:
                        value = bcteKeyForExport.posE;
                        break;
                    case FIELD_DD_POSV:
                        value = bcteKeyForExport.posV;
                        break;
                    case FIELD_DD_WW:
                        value = bcteKeyForExport.ww;
                        break;
                    case FIELD_DD_ETKZ:
                        value = bcteKeyForExport.et;
                        break;
                    case FIELD_DD_AA:
                        value = bcteKeyForExport.aa;
                        break;
                    case FIELD_DD_SDATA:
                        value = bcteKeyForExport.sData;
                        break;
                    default:
                        value = "";
                        break;
                }
            }
            if (StrUtils.isValid(value)) {
                value = visObject.asText(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                         value, dbLanguage);
            }
            outputElems.add(value);
        }

        return outputElems;
    }

    private List<String> setOutputValuesByPartListEntryId(PartListEntryId partListEntryId, List<EtkDisplayField> headerFields) {
        String dbLanguage = projectForCalculation.getDBLanguage();
        VisObject visObject = projectForCalculation.getVisObject();

        String productNo = "";
        if (partListEntryId != null) {
            AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(projectForCalculation, assemblyId);
            if (assembly instanceof iPartsDataAssembly) {
                // Product bestimmen
                iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    productNo = productId.getProductNumber();
                }
            }
        }

        List<String> outputElems = new DwList<>();
        for (EtkDisplayField displayField : headerFields) {
            String fieldName = displayField.getKey().getFieldName();
            // Bei CSV müssen alle Spalten immer vorhanden sein.
            String value = "";
            if (partListEntryId != null) {
                switch (fieldName) {
                    case FIELD_K_VARI:
                        value = partListEntryId.getKVari();
                        break;
                    case FIELD_K_LFDNR:
                        value = partListEntryId.getKLfdnr();
                        break;
                    case BillableAuthorOrderExport.FIELD_FOR_EDS_PRODUCT:
                        value = productNo;
                        break;
                    default:
                        value = "";
                        break;
                }
            }
            if (StrUtils.isValid(value)) {
                value = visObject.asText(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                         value, dbLanguage);
            }
            outputElems.add(value);
        }

        return outputElems;
    }

    /**
     * Hilfsklasse für die Erzeugung von Spalten
     */
    public static class SingleColumnData {

        private final String tableName;
        private final String fieldName;
        private final String fallbackTitle;

        public SingleColumnData(String tableName, String fieldName, String fallbackTitle) {
            this.tableName = tableName;
            this.fieldName = fieldName;
            this.fallbackTitle = fallbackTitle;
        }

        public String getTableName() {
            return tableName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFallbackTitle() {
            return fallbackTitle;
        }
    }
}
