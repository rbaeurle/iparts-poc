/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;

/**
 * Konstanten für iParts Edit für Konfigurationen aus der DWK
 */
public class iPartsEditConfigConst {

    public static final String iPARTS_EDIT_CONFIG_KEY = EtkConfigConst.PLUGIN + "/iPartsEdit";
    public static final String iPARTS_EDIT_MASTER_DATA_KEY = iPARTS_EDIT_CONFIG_KEY + "/MasterData";
    public static final String iPARTS_EDIT_PICTURE_ORDER_KEY = iPARTS_EDIT_CONFIG_KEY + "/PictureOrders";
    public static final String iPARTS_EDIT_PICTURE_REFERENCE_KEY = iPARTS_EDIT_CONFIG_KEY + "/PictureReference";
    public static final String iPARTS_EDIT_AUTHOR_DATA_KEY = iPARTS_EDIT_CONFIG_KEY + "/AuthorData";
    public static final String iPARTS_EDIT_MAILBOX_KEY = iPARTS_EDIT_CONFIG_KEY + "/Mailbox";
    public static final String iPARTS_EDIT_DICT_TRANSDATA_KEY = iPARTS_EDIT_CONFIG_KEY + "/TransData";

    // für die Stammdaten-Editoren
    public static final String iPARTS_EDIT_MASTER_PRODUCT_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_PRODUCT;
    public static final String iPARTS_EDIT_MASTER_SERIES_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_SERIES;
    public static final String iPARTS_EDIT_MASTER_BAD_CODE_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_BAD_CODE;
    public static final String iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_MODEL;
    public static final String iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_MBS_KEY = iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY;
    public static final String iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_CTT_KEY = iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY;
    public static final String iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_MODEL_PROPERTIES;
    public static final String iPARTS_EDIT_MASTER_SAA_MODEL_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_EDS_SAA_MODELS;
    public static final String iPARTS_EDIT_MASTER_PRODUCT_MODEL_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_PRODUCT_MODELS;
    public static final String iPARTS_EDIT_MASTER_MODULE_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_MODULE;
    public static final String iPARTS_EDIT_MASTER_FREE_SA_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_SA;
    public static final String iPARTS_EDIT_MASTER_SERIES_EVENTS_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_SERIES_EVENTS;
    public static final String iPARTS_EDIT_MASTER_VARIANTS_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_COLORTABLE_PART;
    public static final String iPARTS_EDIT_MASTER_PEM_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + iPartsConst.TABLE_DA_PEM_MASTERDATA;
    public static final String iPARTS_EDIT_MASTER_SAA_BK_USAGE = iPARTS_EDIT_MASTER_DATA_KEY + "/SAA_BK_USAGE";
    public static final String iPARTS_EDIT_MASTER_MAT_KEY = iPARTS_EDIT_MASTER_DATA_KEY + "/" + EtkDbConst.TABLE_MAT;

    public static final String iPARTS_EDIT_DICT_TRANSJOB_KEY = iPARTS_EDIT_DICT_TRANSDATA_KEY + "/" + iPartsConst.TABLE_DA_DICT_TRANS_JOB;
    public static final String iPARTS_EDIT_TEXT_USAGE_LOCATION_KEY = iPARTS_EDIT_DICT_TRANSDATA_KEY + "/" + "TextUsageLocation";

    public static final String iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_ASSIGNMENT_KEY = iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + "_" + "ASSIGNMENT";
    public static final String iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_ASSIGNMENT_KEY = iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_KEY + "_" + "ASSIGNMENT";
    public static final String iPARTS_EDIT_SERIES_CODE_KEY = iPARTS_EDIT_CONFIG_KEY + "/" + "SeriesCodes";

    public static final String iPARTS_EDIT_CONFIG_PRODUCT_FACTORIES_KEY = iPARTS_EDIT_CONFIG_KEY + "/" + "ProductFactoriesData";

    public static final String iPARTS_EDIT_AUTHOR_KEY = iPARTS_EDIT_AUTHOR_DATA_KEY + "/" + iPartsConst.TABLE_DA_AUTHOR_ORDER;
    public static final String iPARTS_EDIT_CONFIRM_CHANGES_KEY = iPARTS_EDIT_AUTHOR_DATA_KEY + "/" + iPartsConst.TABLE_DA_CONFIRM_CHANGES;

    public static final String REL_EDIT_MASTER_SEARCHFIELDS = "/SearchFields";
    public static final String REL_EDIT_MASTER_DISPLAYFIELDS = "/DisplayFields";
    public static final String REL_EDIT_MASTER_EDITFIELDS = "/EditFields";


}
