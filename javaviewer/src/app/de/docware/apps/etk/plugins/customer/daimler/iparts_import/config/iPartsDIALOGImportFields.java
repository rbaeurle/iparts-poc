/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Enthält alle Must Have und Must Exist Felder für den DIALOG Import
 */
public class iPartsDIALOGImportFields {

    public static final String TABLENAME_PREFIX = "T10R";
    public static iPartsDIALOGImportFields instance;

    public static Map<String, String[]> tablenameAndMustExist;
    public static Map<String, String[]> tablenameAndMustHave;

    public static iPartsDIALOGImportFields getInstance() {
        if (instance == null) {
            instance = new iPartsDIALOGImportFields();
        }
        return instance;
    }

    public iPartsDIALOGImportFields() {

    }

    public void addMustExistFieldsForTable(String tablename, String[] mustExistfields) {
        if (tablenameAndMustExist == null) {
            tablenameAndMustExist = new HashMap<String, String[]>();
        }
        tablenameAndMustExist.put(tablename, mustExistfields);
    }

    public void addMustHaveDataFieldsForTable(String tablename, String[] mustHaveDataFields) {
        if (tablenameAndMustHave == null) {
            tablenameAndMustHave = new HashMap<String, String[]>();
        }
        tablenameAndMustHave.put(tablename, mustHaveDataFields);
    }

    public void addMustHaveDataAndMustExistFieldsForTable(String tablename, String[] mustExistfields, String[] mustHaveDataFields) {
        addMustExistFieldsForTable(tablename, mustExistfields);
        addMustHaveDataFieldsForTable(tablename, mustHaveDataFields);
    }

    public String[] getMustExistForTable(String tablename) {
        if (tablenameAndMustExist == null) {
            tablenameAndMustExist = new HashMap<String, String[]>();
        }
        String[] result = null;
        if (tablenameAndMustExist.containsKey(tablename)) {
            result = tablenameAndMustExist.get(tablename);
        }
        return result;
    }

    public String[] getMustHaveForTable(String tablename) {
        if (tablenameAndMustHave == null) {
            tablenameAndMustHave = new HashMap<String, String[]>();
        }
        String[] result = null;
        if (tablenameAndMustHave.containsKey(tablename)) {
            result = tablenameAndMustHave.get(tablename);
        }
        return result;
    }


}
