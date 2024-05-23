/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.util.StrUtils;

/**
 * iPartsMatrixEditField Erweiterung des {@link EtkEditField}s für die Matrix-Dartstellung
 */
public class iPartsMatrixEditField extends EtkEditField {

    private static final int DEFAULT_ROWNO = 0;
    private static final int DEFAULT_COLNO = 0;
    private static final int DEFAULT_LABEL_WIDTH = -1;
    private static final int DEFAULT_GRID_WIDTH = 1;
    private static final String CONFIG_VALUE_SEPARATOR = " ";
    private static final String CONFIG_VALUE_ROW_NO = "y";
    private static final String CONFIG_VALUE_COL_NO = "x";
    private static final String CONFIG_VALUE_LABEL_WIDTH = "t";
    private static final String CONFIG_VALUE_GRID_WIDTH = "w";
    private static final String CONFIG_VALUE_WIDTH = "f";

    protected int colNo;
    protected int rowNo;
    protected int labelWidth;
    protected int gridwidth;  // über mehrere Spalten

    public iPartsMatrixEditField() {
        super();
    }

    public iPartsMatrixEditField(String name, boolean mehrSprachig) {
        super(name, mehrSprachig);
    }


    public iPartsMatrixEditField(String tablename, String fieldname, boolean mehrSprachig) {
        super(tablename, fieldname, mehrSprachig);
    }

    public iPartsMatrixEditField(EtkEditField editField) {
        this();
        super.assign(editField);
    }

    @Override
    public void load(ConfigBase configBase, String fieldKey, String rootKey, int saveVersion) {
        super.load(configBase, fieldKey, rootKey, saveVersion);
    }

    @Override
    protected void loadIntern(ConfigBase configBase, String fieldKey, int saveVersion) {
        super.loadIntern(configBase, fieldKey, saveVersion);
        String layoutSettings = configBase.getString(fieldKey + "/EditLayoutSettings", "");
        parseLayoutSettings(layoutSettings);
    }

    /**
     * Parst den String für die Layoutsettings und befüllt damit die colno, rowno, gridwidth, labelwidth und width
     * Der String sieht z.B. so aus: "x=3 y=2 f=30 t=10 w=2"
     *
     * @param layoutSettings
     */
    private void parseLayoutSettings(String layoutSettings) {
        String[] tags = StrUtils.toStringArray(layoutSettings, CONFIG_VALUE_SEPARATOR, false);

        for (String s : tags) {
            String[] keyValue = StrUtils.toStringArray(s, "=", false);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().toLowerCase();
                String value = keyValue[1].trim().toLowerCase();
                if (StrUtils.isInteger(value)) {
                    if (key.equals(CONFIG_VALUE_COL_NO)) {
                        colNo = Integer.parseInt(value) - 1;
                    }
                    if (key.equals(CONFIG_VALUE_ROW_NO)) {
                        rowNo = Integer.parseInt(value) - 1;
                    }
                    if (key.equals(CONFIG_VALUE_GRID_WIDTH)) {
                        gridwidth = Integer.parseInt(value);
                    }
                    if (key.equals(CONFIG_VALUE_LABEL_WIDTH)) {
                        labelWidth = Integer.parseInt(value);
                    }
                    if (key.equals(CONFIG_VALUE_WIDTH)) {
                        width = Integer.parseInt(value);
                    }
                }
            }

        }
    }

    public void assign(iPartsMatrixEditField source) {
        super.assign(source);
        colNo = source.getColNo();
        rowNo = source.getRowNo();
        labelWidth = source.getLabelWidth();
        gridwidth = source.getGridwidth();
    }

    @Override
    public void assign(EtkEditField source) {
        if (source instanceof iPartsMatrixEditField) {
            assign((iPartsMatrixEditField)source);
        } else {
            super.assign(source);
        }
    }


    @Override
    public boolean existsInConfig(EtkConfig config) {
        // todo muss anders gelöst werden
        return super.existsInConfig(config);
//        EtkDatabaseTable table = config.getDBDescription().findTable(getKey().getTableName());
//        if (table != null) {
//            return table.fieldExists(getKey().getFieldName());
//        }
//        return false;
    }

    public int getColNo() {
        return colNo;
    }

    public void setColNo(int colNo) {
        this.colNo = colNo;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public int getGridwidth() {
        return gridwidth;
    }

    public void setGridwidth(int gridwidth) {
        this.gridwidth = gridwidth;
    }

    @Override
    protected void init() {
        super.init();
        colNo = DEFAULT_COLNO;
        rowNo = DEFAULT_ROWNO;
        labelWidth = DEFAULT_LABEL_WIDTH;
        gridwidth = DEFAULT_GRID_WIDTH;
    }
}