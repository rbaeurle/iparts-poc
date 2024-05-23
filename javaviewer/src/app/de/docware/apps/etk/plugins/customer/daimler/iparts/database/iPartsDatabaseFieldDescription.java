/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.database;

import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.base.useradmin.EtkUserAdminDbActions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.combimodules.useradmin.db.*;
import de.docware.framework.modules.config.db.*;
import de.docware.util.sql.dbobjects.DbInternalDbObject;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static de.docware.apps.etk.base.config.db.EtkDatabaseDescription.addFieldDefinition;
import static de.docware.framework.modules.config.db.DBDatabaseDescription.getFieldDefinitionOrDummy;

/**
 * Felder, die für iParts implementiert sind
 */
public class iPartsDatabaseFieldDescription implements iPartsConst {

    private static final Set<String> TABLES_TO_SKIP_DURING_DB_VALIDATION = new HashSet<>();

    static {
        TABLES_TO_SKIP_DURING_DB_VALIDATION.add("hints");
        TABLES_TO_SKIP_DURING_DB_VALIDATION.add("flyway_schema_history");
    }

    public static void modifyFieldDefinitions(DBDatabaseDescription databaseDescription) {
        int defaultBoolLen = EtkFieldLengthType.flBool.getDefaultLen();
        // TODO: Differences to the PROD DB that need to be removed in a future sprint
        // Javaviewer fields
        for (int x = 1; x <= 20; x++) {
            addFieldDefinition(TABLE_BESTELL, FIELD_B_FELD + Long.toString(x), 300, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "Bestellung Spalte " + Long.toString(x), databaseDescription);
        }
        addFieldDefinition(TABLE_ICONS, FIELD_I_INETDATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Zusatzgrafik (IPP)", databaseDescription);
        addFieldDefinition(TABLE_IMAGES, "I_KATALOG", 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Katalog", databaseDescription);
        addFieldDefinition(TABLE_POOL, "P_LASTDATE", 15, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "Last date", databaseDescription);
        addFieldDefinition(TABLE_POOL, FIELD_P_RATIO, EtkFieldLengthType.flInteger.getDefaultLen(), EtkFieldLengthType.flInteger, EtkFieldType.feInteger, false, false, "!!Seitenverhältnis", databaseDescription);
        addFieldDefinition(TABLE_POOL, "P_ISDRAFT", defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Is Draft", databaseDescription);
        addFieldDefinition(TABLE_STRUKT, "S_STATINPROGRESS", defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Stat in progress", databaseDescription);
        addFieldDefinition(TABLE_S_ITEMS, "S_ADDPART", 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "Add part", databaseDescription);
        addFieldDefinition(TABLE_S_ITEMS, "S_CONSTRUCTIONKITS", 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "Construction kits", databaseDescription);
        addFieldDefinition(TABLE_S_ITEMS, "S_ADDTEXT", 50, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "Additional text", databaseDescription);
        addFieldDefinition(TABLE_S_ITEMS, "S_ADDTEXT2", 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "Additional text 2", databaseDescription);
        addFieldDefinition(TABLE_S_SET, "S_TITLE", 50, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "Title", databaseDescription);
        addFieldDefinition(TABLE_S_SET, "S_ISMODUL", defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "Is modul", databaseDescription);

        // iParts fields
        addFieldDefinition(TABLE_DA_MODEL_OIL, "DMO_QUANTITY", 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ölwechselmenge mit Filter", databaseDescription);
        // different lengths
        addFieldDefinition(TABLE_DA_AO_HISTORY, FIELD_DAH_CHANGE_DATE, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_RELEASE_FROM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin ab", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_RELEASE_TO, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin bis", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET, FIELD_DCS_COMMIT_DATE, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Freigabedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_CORTEX_IMPORT_DATA, FIELD_DCI_CREATION_TS, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_SDATA, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_SDATB, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_SDATA, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CREATION_DATE, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CHANGE_DATE, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORDERDATE, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bestellzeitstempel", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_TARGETDATE, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erwartete Lieferung", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_CREATEDATE, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erstellt", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_SDATA, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_SDATA, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_SDATB, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_SDATA, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Datum von", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_SDATB, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        // Javaviewer Fields (created in EtkDatabaseDescription)
        databaseDescription.getFieldByName(TABLE_DOKULINK, FIELD_D_SEQNR).setFieldSize(10);
        databaseDescription.getFieldByName(TABLE_DOKULINK, FIELD_D_SEQNR).setLengthType(EtkFieldLengthType.flInteger);
        databaseDescription.getFieldByName(DbInternalDbObject.TABLE, DbInternalDbObject.FIELD_SCHEMA).setFieldSize(100);
        databaseDescription.getFieldByName(DbInternalDbObject.TABLE, DbInternalDbObject.FIELD_KEY).setFieldSize(100);
        databaseDescription.getFieldByName(TABLE_KAPITEL, FIELD_K_SEQNR).setFieldSize(10);
        databaseDescription.getFieldByName(TABLE_KAPITEL, FIELD_K_SEQNR).setLengthType(EtkFieldLengthType.flInteger);
        databaseDescription.getFieldByName(TABLE_PREISE, FIELD_P_WKZ).setFieldSize(10);
        databaseDescription.getFieldByName(TABLE_PREISE, FIELD_P_WKZ).setLengthType(EtkFieldLengthType.flTextKurz);
        databaseDescription.getFieldByName(TABLE_PREISE, FIELD_P_EORDERNO).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_SBADR, FIELD_A_ZIP).setFieldSize(50);
        databaseDescription.getFieldByName(TABLE_STRUKT, FIELD_S_SEQNR).setFieldSize(10);
        databaseDescription.getFieldByName(TABLE_STRUKT, FIELD_S_SEQNR).setLengthType(EtkFieldLengthType.flInteger);
        databaseDescription.getFieldByName(TABLE_ESTRUCT, FIELD_ES_DESTSCHEMAENTRY).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ESCHEMAENTRY, FIELD_EH_SCHEMAENTRY).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ESCHEMAENTRY, FIELD_EH_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ESCHEMAHEAD, FIELD_EH_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ETREE, FIELD_ET_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ETREE, FIELD_ET_KEY).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ETREE, FIELD_ET_DESTKEY).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ETREE, FIELD_ET_DESTSHEET).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ESHEET, FIELD_ES_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ESHEET, FIELD_ES_SHEET).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EHOTSPOT, FIELD_EH_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EHOTSPOT, FIELD_EH_SHEET).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EHOTSPOT, FIELD_EH_ID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EHOTSPOT, FIELD_EH_ITEMID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EHOTSPOT, FIELD_EH_DESTSHEET).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMS, FIELD_EI_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMS, FIELD_EI_ITEMID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMS, FIELD_EI_SORT).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMS, FIELD_EI_PARENTID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMS, FIELD_EI_MAINSHEET).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMS, FIELD_EI_MAINID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMDATA, FIELD_ED_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EITEMDATA, FIELD_ED_ITEMID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ELINKS, FIELD_EL_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ELINKS, FIELD_EL_ITEMID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ELINKS, FIELD_EL_DESTSHEET).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ELINKS, FIELD_EL_DESTID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ECONNECTIONS, FIELD_EC_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ECONNECTIONS, FIELD_EC_ITEMID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ECONNECTIONS, FIELD_EC_DESTTYPE).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ECONNECTIONS, FIELD_EC_DESTITEM).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTLINK, FIELD_EP_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTLINK, FIELD_EP_ITEMID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTLINK, FIELD_EP_PARTNO).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTS, FIELD_EP_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTS, FIELD_EP_PARTNO).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTS, FIELD_EP_VISIBLEPARTNO).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTDATA, FIELD_ED_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EPARTDATA, FIELD_ED_PARTNO).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_EMECHLINK, FIELD_EM_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_ETRANS, FIELD_ET_SCHEMA).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(TABLE_NOTIZ, FIELD_N_USERID).setLengthType(EtkFieldLengthType.flUserDefined);
        databaseDescription.getFieldByName(TABLE_NOTIZ, FIELD_N_TEXT).setFieldSize(50);
        databaseDescription.getFieldByName(TABLE_NOTIZ, FIELD_N_ATTACH).setFieldSize(50);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + AppDbObject.TABLE_NAME, FIELD_A_ID).setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsDbObject.TABLE_NAME, "N_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsDbObject.TABLE_NAME, "N_PUBLISHER_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsDbObject.TABLE_NAME, "N_PUBLISHER_ORG_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsDbObject.TABLE_NAME, "N_PUBLISHER_APP_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsDbObject.TABLE_NAME, "N_PUBLISHER_SCOPE_CONSTRAINTS").setFieldSize(10);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsDbObject.TABLE_NAME, "N_CREATION_TS").setType(EtkFieldType.feString);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsFeedbackDbObject.TABLE_NAME, "NF_NEWS_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsFeedbackDbObject.TABLE_NAME, "NF_USER_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsTextsDbObject.TABLE_NAME, "NT_NEWS_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + NewsTextsDbObject.TABLE_NAME, "NT_CONTENT").setFieldSize(10);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationDbObject.TABLE_NAME, "O_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationDbObject.TABLE_NAME, "O_PARENT_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationAppsDbObject.TABLE_NAME, "OA_ORGANISATION_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationAppsDbObject.TABLE_NAME, "OA_APP_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationPropertiesDbObject.TABLE_NAME, "OP_ORGANISATION_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationPropertiesDbObject.TABLE_NAME, "OP_APP_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationPropertiesDbObject.TABLE_NAME, "OP_BLOB").setFieldSize(10);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationRolesDbObject.TABLE_NAME, "OR_ORGANISATION_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationRolesDbObject.TABLE_NAME, "OR_ROLE_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + RightDbObject.TABLE_NAME, "R_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + RightDbObject.TABLE_NAME, "R_APP_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + RoleDbObject.TABLE_NAME, "R_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + RoleRightsDbObject.TABLE_NAME, "RR_ROLE_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + RoleRightsDbObject.TABLE_NAME, "RR_RIGHT_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserDbObject.TABLE_NAME, "U_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserAdminHistoryDbObject.TABLE_NAME, "UAH_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserAdminHistoryDbObject.TABLE_NAME, "UAH_USER_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserAdminHistoryDbObject.TABLE_NAME, "UAH_OLD_VALUE").setFieldSize(400);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserAdminHistoryDbObject.TABLE_NAME, "UAH_NEW_VALUE").setFieldSize(400);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserOrganisationsDbObject.TABLE_NAME, "UO_USER_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserOrganisationsDbObject.TABLE_NAME, "UO_ORGANISATION_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesDbObject.TABLE_NAME, "UP_USER_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesDbObject.TABLE_NAME, "UP_APP_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesDbObject.TABLE_NAME, "UP_ORG_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesDbObject.TABLE_NAME, "UP_BLOB").setFieldSize(10);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesTemplateDbObject.TABLE_NAME, "UPT_APP_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesTemplateDbObject.TABLE_NAME, "UPT_BLOB").setFieldSize(10);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserRolesDbObject.TABLE_NAME, "UR_USER_ID").setLengthType(EtkFieldLengthType.flMatNr);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserRolesDbObject.TABLE_NAME, "UR_ROLE_ID").setLengthType(EtkFieldLengthType.flMatNr);
        EtkDatabaseField field_SI_ST_VER = databaseDescription.getFieldByName(TABLE_S_ITEMS, FIELD_SI_ST_VER);
        field_SI_ST_VER.setLengthType(EtkFieldLengthType.flInteger);
        field_SI_ST_VER.setFieldSize(10);
        databaseDescription.getFieldByName(TABLE_S_ITEMS, FIELD_SI_SEQNR).setFieldSize(10);
        databaseDescription.getFieldByName(TABLE_S_ITEMS, FIELD_SI_SEQNR).setLengthType(EtkFieldLengthType.flInteger);
        databaseDescription.getFieldByName("UA_NEWS", "N_CREATION_TS").setFieldSize(28);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationPropertiesDbObject.TABLE_NAME, OrganisationPropertiesDbObject.OP_VALUE).setFieldSize(4000);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + OrganisationPropertiesDbObject.TABLE_NAME, OrganisationPropertiesDbObject.OP_VALUE).setType(EtkFieldType.feString);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesDbObject.TABLE_NAME, UserPropertiesDbObject.UP_VALUE).setFieldSize(4000);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesDbObject.TABLE_NAME, UserPropertiesDbObject.UP_VALUE).setType(EtkFieldType.feString);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesTemplateDbObject.TABLE_NAME, UserPropertiesTemplateDbObject.UPT_VALUE).setFieldSize(4000);
        databaseDescription.getFieldByName(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX + UserPropertiesTemplateDbObject.TABLE_NAME, UserPropertiesTemplateDbObject.UPT_VALUE).setType(EtkFieldType.feString);


        databaseDescription.deleteTable(TABLE_DOKUREFS);
        databaseDescription.addTable(TABLE_DOKUREFS, TranslationKeys.DOKU_REFS, false, EnumSet.of(DBTableProperties.teNameFix, DBTableProperties.teEditing), EnumSet.noneOf(EtkFieldType.class));
        databaseDescription.addField(TABLE_DOKUREFS, FIELD_DR_DOKUMD5, "!!Datei-MD5-Hash", EtkFieldType.feString, EnumSet.of(EtkFieldOption.NameFix, EtkFieldOption.TypFix, EtkFieldOption.SystemFeld, EtkFieldOption.LaengeFix), EtkFieldLengthType.flMD5, false, DBConst.DEFAULT_MD5_LEN);
        databaseDescription.addField(TABLE_DOKUREFS, FIELD_DR_FILE, "!!Datei", EtkFieldType.feString, EnumSet.of(EtkFieldOption.NameFix, EtkFieldOption.SystemFeld), EtkFieldLengthType.flFileName, false, DEFAULT_FILENAME_LEN);
        databaseDescription.addField(TABLE_DOKUREFS, FIELD_DR_REFFILENAME, "!!Refernenzdateiname", EtkFieldType.feString, EnumSet.of(EtkFieldOption.NameFix, EtkFieldOption.SystemFeld), EtkFieldLengthType.flFileName, false, DEFAULT_FILENAME_LEN);
        databaseDescription.addField(TABLE_DOKUREFS, DBConst.FIELD_STAMP, DBDatabaseDescription.STAMP_FIELD_TEXT, EtkFieldType.feString, EnumSet.of(EtkFieldOption.NameFix, EtkFieldOption.TypFix, EtkFieldOption.LaengeFix, EtkFieldOption.SystemFeld), EtkFieldLengthType.flUserDefined, false, DBConst.DEFAULT_STAMP_LEN);

        databaseDescription.addIndex(TABLE_DOKUREFS, new String[]{ FIELD_DR_DOKUMD5, FIELD_DR_FILE, FIELD_DR_REFFILENAME }, true, false, false);
        databaseDescription.addIndex(TABLE_DOKUREFS, new String[]{ FIELD_DR_REFFILENAME }, false, false, false);

    }

    protected static void addFieldDefinitions(DBDatabaseDescription databaseDescription) {
        int defaultBenennungLen = EtkFieldLengthType.flTextKurz.getDefaultLen();
        int defaultBoolLen = EtkFieldLengthType.flBool.getDefaultLen();
        int defaultDateLen = 8;
        int defaultDateTimeLen = EtkFieldLengthType.flDateTimeLen.getDefaultLen();
        int defaultFileLen = EtkFieldLengthType.flFileName.getDefaultLen();
        int defaultGUIDLen = EtkFieldLengthType.flGUID.getDefaultLen();
        int defaultIntegerLen = EtkFieldLengthType.flInteger.getDefaultLen();
        int defaultLfdNrLen = EtkFieldLengthType.flLfdnr.getDefaultLen();
        int defaultMatNrLen = EtkFieldLengthType.flMatNr.getDefaultLen();
        int defaultStampLen = DEFAULT_STAMP_LEN;
        int defaultTextKurzLen = EtkFieldLengthType.flTextKurz.getDefaultLen();
        int defaultVersionLen = EtkFieldLengthType.flVer.getDefaultLen();

        // Änderungen an Systemfeldern
        EtkDatabaseTable table = databaseDescription.getTable(TABLE_BEST_H);
        if (table != null) {
            for (EtkDatabaseField field : table.getFieldList()) {
                // In der Tabelle BEST_H alle Felder mit Textlänge flTextLang korrigieren, damit diese nur noch flMatNr
                // verwenden, weil die summierten Feldlängen ansonsten zu lang sind für PostgreSQL
                if (field.getLengthType() == EtkFieldLengthType.flTextLang) {
                    field.setLengthType(EtkFieldLengthType.flMatNr);
                }
            }
        }

        // TODO: Mit MB abklären
//        getFieldDefinitionOrDummy(TABLE_DOKUREFS, FIELD_DR_REFFILENAME, databaseDescription).setCaseInsensitive(true);
//
//        getFieldDefinitionOrDummy(TABLE_DWARRAY, FIELD_DWA_TOKEN, databaseDescription).setCaseInsensitive(true);

        EtkDatabaseField field_E_TOKEN = getFieldDefinitionOrDummy(TABLE_ENUM, FIELD_E_TOKEN, databaseDescription);
        field_E_TOKEN.setLengthType(EtkFieldLengthType.flUserDefined);
        field_E_TOKEN.setFieldSize(40);

        EtkDatabaseField field_F_USER_ID = getFieldDefinitionOrDummy(TABLE_FAVORITES, FIELD_FAV_USERID, databaseDescription);
        field_F_USER_ID.setLengthType(EtkFieldLengthType.flUserDefined);
        field_F_USER_ID.setFieldSize(38);

        getFieldDefinitionOrDummy(TABLE_KATALOG, FIELD_K_SEQNR, databaseDescription).setFieldSize(300);

        EtkDatabaseField field_L_TEXT = getFieldDefinitionOrDummy(TABLE_LINKS, FIELD_L_TEXT, databaseDescription);
        field_L_TEXT.setLengthType(EtkFieldLengthType.flUserDefined);
        field_L_TEXT.setFieldSize(15);

        // TODO: Mit MB abklären
//        getFieldDefinitionOrDummy(TABLE_MAT, FIELD_M_MATNR, databaseDescription).setCaseInsensitive(true);

        EtkDatabaseField field_N_KVARI = getFieldDefinitionOrDummy(TABLE_NOTIZ, FIELD_N_KVARI, databaseDescription);
        field_N_KVARI.setLengthType(EtkFieldLengthType.flUserDefined);
        field_N_KVARI.setFieldSize(200);

        EtkDatabaseField field_US_USERID = getFieldDefinitionOrDummy(TABLE_USERSETTINGS, FIELD_US_USERID, databaseDescription);
        field_US_USERID.setLengthType(EtkFieldLengthType.flUserDefined);
        field_US_USERID.setFieldSize(38);


        // Felder
        addFieldDefinition(TABLE_SERNO, FIELD_U_SERNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!FIN", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_MODNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baugruppe", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_MODVER, 5, EtkFieldLengthType.flVer, EtkFieldType.feString, false, false, "!!Baugruppe Version", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_TYPE, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Art", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_DATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Konfiguration", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_BDATE, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Baudatum", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_ORDERNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Auftragsnummer", databaseDescription);
        addFieldDefinition(TABLE_SERNO, FIELD_U_VIN, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!VIN", databaseDescription);

        //Konstruktionsdaten, additionale Felder zur bestehenden Standardtabelle [MAT]
        addFieldDefinition(TABLE_MAT, FIELD_M_ASSEMBLYSIGN, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Zusammenbaukennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_CONST_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Konstruktionsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_QUANTUNIT, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Mengeneinheit", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_IMAGESTATE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Zeichnungsgeometriestand", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_IMAGEDATE, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Zeichnungsdatum", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_REFSER, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Hinweiszeichnung", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_SECURITYSIGN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Sicherheitskennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_VEDOCSIGN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Vedoc-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_WEIGHTCALC, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Gewicht berechnet", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_WEIGHTREAL, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Gewicht gewogen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_WEIGHTPROG, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Gewicht geschätzt", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_MATERIALFINITESTATE, 20, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Werkstoff-Endzustand", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_ETKZ, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!ET-Kennzeichen Gesamt", databaseDescription);
        // DAIMLER-9152, Zusätzliche Informationen be Quittierung von ETKZ
        addFieldDefinition(TABLE_MAT, FIELD_M_ETKZ_OLD, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!ET-Kennzeichen alt", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_NOTEONE, 20, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bemerkung 1", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_NOTETWO, 20, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Bemerkung 2", databaseDescription);
        // [MAT] Zusätzliche Felder für: [Daimler-510], DIALOG-Urladung Teilestammdaten übernehmen
        addFieldDefinition(TABLE_MAT, FIELD_M_RELEASESTATE, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Freigabestand", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_RELATEDPIC, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bezugszeichnung", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_CHANGE_DESC, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Änderungstexte", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_AS_ES_1, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ergänzungsschlüssel 1", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_ADDTEXT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Sprachneutraler Text", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_LAYOUT_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Baukastenaufteilung", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_THEFTREL, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Diebstahlrelevant", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_CERTREL, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Zertifizierungsrelevant", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_DOCREQ, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DokumentationspFLicht", databaseDescription);
        // PRIMUS-Felder
        addFieldDefinition(TABLE_MAT, FIELD_M_BRAND, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Marke", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_STATE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_REMAN_IND, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Reman-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_CHINA_IND, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!China-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_NATO_NO, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Nato-Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_SVHC_IND, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SVHC-Kenner", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_ESD_IND, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!ESD-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_ARMORED_IND, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Sonderschutz-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_SOURCE, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_IS_DELETED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Löschkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_THEFTRELINFO, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Diebstahlrelevanzinformation", databaseDescription);
        // PRIMUS, DAIMLER-3220, Verwaltung von ES1-Grundlagen
        addFieldDefinition(TABLE_MAT, FIELD_M_AS_ES_2, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ergänzungsschlüssel 2", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_BASE_MATNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Grundsachnummer", databaseDescription);
        // DAIMLER-3696 Haltbarkeit in Teilestamm
        addFieldDefinition(TABLE_MAT, FIELD_M_SHELF_LIFE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Haltbarkeit in Monaten", databaseDescription);
        // DAIMLER-4306, Erweiterung um "Sicherheitsrelevantes Teil Repair"
        addFieldDefinition(TABLE_MAT, FIELD_M_SECURITYSIGN_REPAIR, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Sicherheitskenner für Reparatur", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_VARIANT_SIGN, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Variantenkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_VERKSNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!E/E-Kategorie", databaseDescription);
        // DAIMLLER-594, EDS/BCP Urladung, Teilestammdaten
        addFieldDefinition(TABLE_MAT, FIELD_M_FACTORY_IDS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werkskennungen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_LAST_MODIFIED, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Letzte Änderung", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_MARKET_ETKZ, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Markt-Ersatzteilkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_INTERNAL_TEXT, 300, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Interner Text", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_BASKET_SIGN, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Warenkorb-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_ASSEMBLY, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Baugruppe", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_ETKZ_MBS, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!ET-Kennzeichen MBS", databaseDescription);
        // DAIMLER-10143, Sprachneutralen Text am Teilestamm pFLegbar machen
        addFieldDefinition(TABLE_MAT, FIELD_M_ADDTEXT_EDITED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Sprachneutraler Text bearbeitet", databaseDescription);
        // DAIMLER-10798, SAP.CTT: Anlage von Teilepositionen im TU aus CTT-Importdatei
        addFieldDefinition(TABLE_MAT, FIELD_M_ETKZ_CTT, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!ET-Kennzeichen CTT", databaseDescription);
        // DAIMLER-11476, Import von Sachnummern für Einzelteilbilder
        addFieldDefinition(TABLE_MAT, FIELD_M_IMAGE_AVAILABLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Einzelteilbild vorhanden", databaseDescription);
        // DAIMLER-11616, PSK: neue Teilestammtattribute
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_MATERIAL, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!PSK Material", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_SUPPLIER_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK Herstellercode Lieferant", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_MANUFACTURER_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK Herstellercode Hersteller", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_SUPPLIER_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK SNR-Lieferant", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_MANUFACTURER_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK SNR-Hersteller", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_IMAGE_NO_EXTERN, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK Zeichnungsnummer extern", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PSK_REMARK, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!PSK Bemerkung", databaseDescription);

        // DAIMLER-12220, Neue Teilestammattribute aus PRIMUS
        addFieldDefinition(TABLE_MAT, FIELD_M_WEIGHT, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gewicht", databaseDescription); // kg
        addFieldDefinition(TABLE_MAT, FIELD_M_LENGTH, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Länge", databaseDescription);   // m
        addFieldDefinition(TABLE_MAT, FIELD_M_WIDTH, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Breite", databaseDescription);  // m
        addFieldDefinition(TABLE_MAT, FIELD_M_HEIGHT, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Höhe", databaseDescription);    // m
        addFieldDefinition(TABLE_MAT, FIELD_M_VOLUME, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Volumen", databaseDescription); // m³

        // DAIMLER-12460, Focus: Import Teilemapping zur AuFLösung von Gleichteilen
        // TODO: Mit MB abklären
//        addFieldDefinition(TABLE_MAT, FIELD_M_MATNR_MBAG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, true, "!!MBAG Teilenummer", databaseDescription); // Abweichende Teilenummer (Mercedes Benz)
//        addFieldDefinition(TABLE_MAT, FIELD_M_MATNR_DTAG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, true, "!!DTAG Teilenummer", databaseDescription); // Abweichende Teilenummer (Daimler Truck)
        addFieldDefinition(TABLE_MAT, FIELD_M_MATNR_MBAG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!MBAG Teilenummer", databaseDescription); // Abweichende Teilenummer (Mercedes Benz)
        addFieldDefinition(TABLE_MAT, FIELD_M_MATNR_DTAG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!DTAG Teilenummer", databaseDescription); // Abweichende Teilenummer (Daimler Truck)

        // DAIMLER-14339, Import Gefahrgutkenner aus PRIMUS
        addFieldDefinition(TABLE_MAT, FIELD_M_HAZARDOUS_GOODS_INDICATOR, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gefahrgutkennzeichen", databaseDescription);

        // DAIMLER-15555	Import von Reman (Austauschmotor) Varianten zur ZB Sachnummer
        addFieldDefinition(TABLE_MAT, FIELD_M_PARTNO_BASIC, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer Basismotor", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PARTNO_SHORTBLOCK, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer Shortblock", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PARTNO_LONGBLOCK, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer Longblock", databaseDescription);
        addFieldDefinition(TABLE_MAT, FIELD_M_PARTNO_LONGBLOCK_PLUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer LongblockPlus", databaseDescription);


        // [KATALOG]
        addFieldDefinition(TABLE_KATALOG, FIELD_K_DATEFROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_DATETO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Gültigkeit bei Code", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_MINUSPARTS, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Entfallteile +- Dokumentation", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_HIERARCHY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Einrückung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_STEERING, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SOURCE_TYPE, 2, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Typ", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SOURCE_CONTEXT, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Context", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SOURCE_REF1, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Context Zusatz 1", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SOURCE_REF2, 8, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Context Zusatz 2", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SOURCE_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Datensatz GUID", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_ETKZ, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ersatzteilkennzeichen", databaseDescription);
        // Zusätzliche Felder für: [DAIMLER-1840]
        addFieldDefinition(TABLE_KATALOG, FIELD_K_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        // Zusätzliche Felder für DAIMLER-1853
        addFieldDefinition(TABLE_KATALOG, FIELD_K_GEARBOX_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Getriebeart", databaseDescription);
        // Gültigkeiten ArrayFelder
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SA_VALIDITY, 12, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SA/BK Gültigkeit", true, databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY, 12, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumuster Gültigkeit", true, databaseDescription);
        // Zusätzliche Felder: ETZ, sprachneutraler Text, Ergänzungstext
        addFieldDefinition(TABLE_KATALOG, FIELD_K_ETZ, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!ET-Zähler", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_VIRTUAL_MAT_TYPE, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Virtueller Materialtyp", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_WW_EXTRA_PARTS, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Extra Wahlweise-Teile", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_FAIL_LOCLIST, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Fehlerorte", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_AS_CODE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!AS-Code entfernt", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_ACC_CODE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Zubehör-Code entfernt", databaseDescription);
        // Bezieht sich auf das Enum [ASProductClasses]
        addFieldDefinition(TABLE_KATALOG, FIELD_K_PCLASSES_VALIDITY, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Sortimentsklassengültigkeit", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_EVAL_PEM_FROM, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Auswertung PEM ab", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_EVAL_PEM_TO, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Auswertung PEM bis", databaseDescription);
        // DAIMLER- 4825
        addFieldDefinition(TABLE_KATALOG, FIELD_K_CODES_CONST, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Coderegel Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_HIERARCHY_CONST, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Strukturstufe Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_MENGE_CONST, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_OMIT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ausgabe unterdrücken", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_ONLY_MODEL_FILTER, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nur Baumuster-Filter", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_EVENT_FROM_CONST, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_EVENT_TO_CONST, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_CODES_REDUCED, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Code gekürzt", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_MIN_KEM_DATE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Minimales KEM Datum ab", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_MAX_KEM_DATE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Maximales KEM Datum bis", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_USE_PRIMUS_SUCCESSOR, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!PRIMUS-Nachfolger verwenden", databaseDescription);
        // DAIMLER-11433, PSK: Erweiterung Kopierfunktion in PSK-TU
        addFieldDefinition(TABLE_KATALOG, FIELD_K_COPY_VARI, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kopie-Ursprung Baugruppe", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_COPY_LFDNR, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Kopie-Ursprung laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_COPY_DATE, defaultStampLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Kopie-Ursprung Zeitstempel", databaseDescription);
        // DAIMLER-11614, PSK: Neue Teileposition anlegen und Varianten an Teileposition pFLegen können
        addFieldDefinition(TABLE_KATALOG, FIELD_K_PSK_VARIANT_VALIDITY, 12, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK Variantengültigkeit", true, databaseDescription);
        // DAIMLER-12466, DIALOG - automatische Übernahme offener konstr. Teilepositionen
        addFieldDefinition(TABLE_KATALOG, FIELD_K_AUTO_CREATED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Automatisch erzeugt", databaseDescription);
        // DAIMLER-12860, Bestimmte Teilepos. von der Bearbeitung sperren
        addFieldDefinition(TABLE_KATALOG, FIELD_K_ENTRY_LOCKED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Stücklistenposition gesperrt", databaseDescription);
        // DAIMLER-13401, Motoröle: Import Stückliste inkl. neue Gültigkeiten
        addFieldDefinition(TABLE_KATALOG, FIELD_K_COUNTRY_VALIDITY, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ländergültigkeit", databaseDescription);
        addFieldDefinition(TABLE_KATALOG, FIELD_K_SPEC_VALIDITY, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Spezifikationen", databaseDescription);
        // DAIMLER-15168	Auswertemöglichkeit zur Identifikation von Autorenaufträgen an automatisch erzeugten Teilepos, Tabellenerweiterung
        addFieldDefinition(TABLE_KATALOG, FIELD_K_WAS_AUTO_CREATED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ursprünglich automatisch erzeugt", databaseDescription);


        // Zusätzliche Felder für die Systemtabelle [POOL/Zeichnungspool]
        addFieldDefinition(TABLE_POOL, FIELD_P_IMPORTDATE, 12, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Importdatum", databaseDescription);
        addFieldDefinition(TABLE_POOL, FIELD_P_PREVIEW_DATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Vorschaubild", databaseDescription);
        addFieldDefinition(TABLE_POOL, FIELD_P_PREVIEW_IMGTYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Vorschaubild Zeichnungstyp", databaseDescription);
        // DAIMLER-12365, Focus: Übernahme der AS Produktklassen für Bilder
        addFieldDefinition(TABLE_POOL, FIELD_P_VALIDITY_SCOPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Gültigkeitsbereich", databaseDescription);


        // Zusätzliche Felder für [DAIMLER-2083]
        addFieldDefinition(TABLE_IMAGES, FIELD_I_IMAGEDATE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Zeichnungsdatum", databaseDescription);
        // DAIMLER-5519, Gültigkeiten für Zeichnungsreferenz pFLegen
        addFieldDefinition(TABLE_IMAGES, FIELD_I_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Code-Gültigkeit", databaseDescription);
        addFieldDefinition(TABLE_IMAGES, FIELD_I_MODEL_VALIDITY, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumustergültigkeit", true, databaseDescription);
        addFieldDefinition(TABLE_IMAGES, FIELD_I_SAA_CONSTKIT_VALIDITY, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA-/Baukastengültigkeit", true, databaseDescription);
        addFieldDefinition(TABLE_IMAGES, FIELD_I_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_IMAGES, FIELD_I_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        // DAIMLER-11624, PSK: Gültigkeit bei Bildtafeln um Varianten erweitern
        addFieldDefinition(TABLE_IMAGES, FIELD_I_PSK_VARIANT_VALIDITY, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PSK Variantengültigkeit", true, databaseDescription);
        // DAIMLER-14099, Neue Gültigkeit "Nur bei FIN ausgeben" an Bildtafel
        addFieldDefinition(TABLE_IMAGES, FIELD_I_ONLY_FIN_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nur bei FIN ausgeben", databaseDescription);
        // DAIMLER-15273, Fahrzeugnavigation: Typisierung der Fahrzeugperspektiven und Ausgabe in den visualNav
        addFieldDefinition(TABLE_IMAGES, FIELD_I_NAVIGATION_PERSPECTIVE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Navigationsperspektive", databaseDescription);


        // Felder der Tabelle [DA_MODULE]
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_MODULE_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_DOCUTYPE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Dokumentationstyp", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_SPRING_FILTER, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Relevant für Feder-Filter", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_VARIANTS_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Variantendaten anzeigen", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_USE_COLOR_TABLEFN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Farbtabellenfußnoten verwenden", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_MODULE_HIDDEN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Modul ausblenden", databaseDescription);
        // DAIMLER-13581, Motoröle: Datenkarte und Filterung: Ermittlung der Spezifikationen
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_SPEC, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Spezifikation", databaseDescription);
        // DAIMLER-14738, Neuer Kenner am TU-Stamm zum Deaktivieren der "Prüfung Stückliste enthält Einträge ohne Positionsnummer"
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_POS_PIC_CHECK_INACTIVE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Prüfung Stückliste enthält Einträge ohne Positionsnummer deaktivieren", databaseDescription);
        // DAIMLER-15340, Aufnahme Ursprungs-TU in Historie bei "TU kopieren"
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_SOURCE_TU, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Quell-TU", databaseDescription);
        // DAIMLER-15342, Hinweismeldung in Quali-Prüfung auf Fehler ändern, Schalter um "Prüfung Teileposition hat Hotspot ohne Bild" zu deaktivieren
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_HOTSPOT_PIC_CHECK_INACTIVE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Prüfung Teileposition hat Hotspot ohne Bild deaktivieren", databaseDescription);
        // DAIMLER-15403, Spezialfilter für ZB-Sachnummer
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_ZB_PART_NO_AGG_TYPE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!ZB-Sachnummer Aggregate-Typ", databaseDescription);
        // DAIMLER-15728, Bestimmte TUs nur für zertifizierte Retail-User anzeigen
        addFieldDefinition(TABLE_DA_MODULE, FIELD_DM_SPECIAL_TU, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Spezial TU", databaseDescription);


        // Felder der Tabelle [DA_PRODUCT_MODULES]
        addFieldDefinition(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_MODULE_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);


        // Tabelle: [DA_SERIES]
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_TYPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Baureihentyp", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_NAME, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Baureihenbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_COMPONENT_FLAG, 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Sachnummernkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_SPARE_PART, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ersatzteilkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_IMPORT_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Versorgungsrelevant", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_EVENT_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ereignisgesteuert", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_ALTERNATIVE_CALC, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Neues Berechnungsmodell", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_HIERARCHY, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Strukturstufen-Berechnung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_MERGE_PRODUCTS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Migrierte Produkte zusammenführen", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_AUTO_CALCULATION, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Automatische Berechnung offener Stände", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_AA_WO_FACTORY_DATA, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Berechnung ohne Werksdaten", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES, FIELD_DS_V_POSITION_CHECK, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!V-Position Doku-Relevanz Prüfung und Kopplung", databaseDescription);


        // Tabelle: [DA_MODEL]
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_NAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Baumusterbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Baumusterbildende Codes", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_HORSEPOWER, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!PS", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_KILOWATTS, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!KW", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Verkaufsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_DEVELOPMENT_TITLE, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Entwicklungsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_DRIVE_SYSTEM, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Antriebsart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_ENGINE_CONCEPT, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Motorkonzept", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_CYLINDER_COUNT, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Anzahl Zylinder", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_ENGINE_KIND, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Motorart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_AA, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_DATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_DATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_MODEL_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Baumusterart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_MODEL_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Baumuster anzeigen", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_AS_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_AS_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_MODEL_INVALID, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Baumuster ungültig", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_COMMENT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Kommentar", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_TECHDATA, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Technische Daten", databaseDescription);
        // DAIMLER-5102, PFLege eingeschränkter Attribute am AS-Baumuster-Stamm
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_VALID_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Gültig ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_VALID_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Gültig bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_ADD_TEXT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Zusatztext", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_MANUAL_CHANGE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Manuelle Änderung erfolgt", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_CONST_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumuster Konstruktion", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_FILTER_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Relevant für Baumuster-Filter", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_NOT_DOCU_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nicht Doku-relevant", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL, FIELD_DM_MODEL_SUFFIX, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Suffix", databaseDescription);


        // DAIMLER-9274, weitere Liste mit bm-bildende Codes
        // ctDA_MODEL_BUILDING_CODE, "DA_MODEL_BUILDING_CODE", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_MODEL_BUILDING_CODE, FIELD_DMBC_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_BUILDING_CODE, FIELD_DMBC_AA, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_BUILDING_CODE, FIELD_DMBC_CODE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Code ID", databaseDescription);


        // Tabelle: [DA_MODEL_DATA], DAIMLER-1356, * DIALOG-Baumusterstammdaten werden in die falsche Tabelle importiert
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_NAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Baumusterbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_HORSEPOWER, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!PS", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_KILOWATTS, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!KW", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_DEVELOPMENT_TITLE, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Entwicklungsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_MODEL_INVALID, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Baumuster ungültig", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_DRIVE_SYSTEM, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Antriebsart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_ENGINE_CONCEPT, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Motorkonzept", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_CYLINDER_COUNT, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Anzahl Zylinder", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_ENGINE_KIND, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Motorart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_DATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_DATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_DATA, FIELD_DMD_SALES_TITLE, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Verkaufsbezeichnung", databaseDescription);


        // Tabelle: [DA_MODEL_PROPERTIES]
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AA, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Baumusterbildende Codes", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AS_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Relevant für After-Sales", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);


        // Tabelle:
        addFieldDefinition(TABLE_DA_SERIES_AGGS, FIELD_DSA_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_AGGS, FIELD_DSA_AGGSERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Aggregatebaureihe", databaseDescription);

        // Tabelle:
        addFieldDefinition(TABLE_DA_MODELS_AGGS, FIELD_DMA_MODEL_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODELS_AGGS, FIELD_DMA_AGGREGATE_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Aggregatebaumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODELS_AGGS, FIELD_DMA_SOURCE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);

        // Tabelle:
        addFieldDefinition(TABLE_DA_EINPAS, FIELD_EP_HG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPAS, FIELD_EP_G, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPAS, FIELD_EP_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);

        // Tabelle:
        addFieldDefinition(TABLE_DA_EINPASDSC, FIELD_EP_HG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASDSC, FIELD_EP_G, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASDSC, FIELD_EP_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASDSC, FIELD_EP_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASDSC, FIELD_EP_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);

        // Tabelle:
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_MODELTYPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Gültig für Baumustertyp", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_KG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TU", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_LFDNR, 10, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_HGDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_GDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASKGTU, FIELD_EP_TUDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);

        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_SERIES, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!gültig für Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "HM", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "M", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "SM", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_LFDNR, 10, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_HGDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_GDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASHMMSM, FIELD_EP_TUDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);

        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_GROUP, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_SCOPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_SAAPREFIX, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Gültig für SAA-Prefix", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_LFDNR, 10, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_HGDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_GDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_EINPASOPS, FIELD_EP_TUDEST, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);


        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODULE_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_LFDNR, 10, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_HG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_G, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Source-KG", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Source-TU", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_HM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Source-HM", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_M, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Source-M", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_SM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Source-SM", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SORT, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sortierung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_SAA_VALIDITY, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!SAA/BK Gültigkeiten aller Stücklisteneinträge", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_CODE_VALIDITY, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Gültigkeit bei Code", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODEL_VALIDITY, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumuster Gültigkeit", true, databaseDescription);       // String Array


        //Produktstruktur [DA_STRUCTURE]
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_PARENT, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Struktur-Vaterelement", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_CHILD, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Struktur-Kindelement", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_TITLE, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_SORT, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sortierung", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_CONSTRUCTION, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Konstruktionskenner", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_MODEL_TYPE_PREFIX, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Typkennzahl-Präfix", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_ASPRODUCT_CLASSES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Aftersales Produktklassen", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_AGGREGATE_TYPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Aggregatetyp", databaseDescription);


        //Produktstruktur [DA_PRODUCT] NEU:
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_STRUCTURING_TYPE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Strukturtyp", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_TITLE, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Produktbild", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_AGGREGATE_TYPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Aggregatetyp", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_ASSORTMENT_CLASSES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Sortimentsklassen", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_DOCU_METHOD, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!DOK-Methode", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Produkt anzeigen", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_KZ_DELTA, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Kennzeichen Deltaversorgung", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_MIGRATION, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Migriert", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_MIGRATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Migrationsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_DATASET_DATE, defaultDateLen, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Datenstand", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_ASPRODUCT_CLASSES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Aftersales Produktklassen", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_COMMENT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Kommentar", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_SERIES_REF, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Referenzierte Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_IS_SPECIAL_CAT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Spezialkatalog", databaseDescription);
        // DAIMLER-3547, TAL47S, Auto-Kat-Select Informationen
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_APS_REMARK, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!APS Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_APS_CODE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!APS Codebedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_APS_FROM_IDENTS, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!APS Idents ab", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_APS_TO_IDENTS, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!APS Idents bis", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_IDENT_CLASS_OLD, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ident nach alter Systematik", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_EPC_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!EPC relevant", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_VALID_COUNTRIES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Nur gültig in Ländern", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_INVALID_COUNTRIES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Nicht gültig in Ländern", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_BRAND, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Markenbezeichnungen", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_SECOND_PARTS_ENABLED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!2nd-Parts verwenden", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_TTZ_FILTER, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!TTZ für Endnummernfilter verwenden", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_SCORING_WITH_MCODES, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!BM-Code für Scoring verwenden", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_DISABLED_FILTERS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Deaktivierte Filter", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_MODIFICATION_TIMESTAMP, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungszeitstempel", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_SHOW_SAS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Freie SAs immer anzeigen", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_CAB_FALLBACK, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Werkseitig verbaute Aggregate nutzen", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_NO_PRIMUS_HINTS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Keine PRIMUS-Hinweise ausgeben", databaseDescription);
        // DAIMLER-11423 PSK: Neue Stücklistentypen "PSK PKW/VAN" "PSK Truck"
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PSK, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!PSK", databaseDescription);
        // DAIMLER-12412, SVG: Am Produkt steuern, ob SVGs ausgegeben werden sollen
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_USE_SVGS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!SVGs beim Export verwenden", databaseDescription);
        // DAIMLER-13501, Option "SVG bevorzugen im Autorenprozess" pro Produkt festlegen
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PREFER_SVG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!SVG bevorzugen im Autorenprozess", databaseDescription);
        // DAIMLER-13553, Endnummernfilter mit mehreren Idents aus untersch. Werken auf eine PEM filtern
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_IDENT_FACTORY_FILTERING, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Idents zu Montagewerk steuern", databaseDescription);
        // DAIMLER-13859, Sprachen für elasticExport am Produkt definieren, es sind alle 24 Sprachen für dieses Produkt vorhanden
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_FULL_LANGUAGE_SUPPORT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Alle Sprachen", databaseDescription);
        // DAIMLER-13971, Delta-Updates für ElasticSearch, Zeitstempel letzter Export
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_ES_EXPORT_TIMESTAMP, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!ElasticSearch Exportzeitstempel", databaseDescription);
        // DAIMLER-14025, Prüfung "Ungültige DIALOG-Teileposition" über Produkt steuern
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_DIALOG_POS_CHECK, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Prüfung ungültige DIALOG-Teileposition", databaseDescription);
        // DAIMLER-14617	Truck: Zuordnung Produkt zu Supplier, Tabellenerweiterung
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_SUPPLIER_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Lieferantennummer", databaseDescription);
        // DAIMLER-14934, Anlage eines Moduls für eine "Fahrzeugperspektive"
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_CAR_PERSPECTIVE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Navigationsansicht", databaseDescription);
        // DAIMLER-15482, Bestimmung des Werks bei zugekauften Elektromotoren optional über das Fahrzeugprodukt bestimmen
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_USE_FACTORY, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Werk von Fahrzeug-Datenkarte benutzen", databaseDescription);
        // DAIMLER-15731, Ausgabe der Connect-Leitungssätze am Produkt steuern
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_CONNECT_DATA_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Connect-Daten anzeigen", databaseDescription);
        // DAIMLER-15880, Fahrzeugidents am Produkt hinterlegen
        addFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_FINS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fahrzeugidents", true, databaseDescription);

        // Zuordnung Baumuster zum Produkt [DA_PRODUCT_MODELS]
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_TEXTNR, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Zusatztext", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_VALID_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Gültig ab", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_VALID_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Gültig bis", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Baumuster veröffentlichen", databaseDescription);


        // DIALOG-Urladung Modulstruktur HM/M/SM
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_HIDDEN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Zur Bearbeitung nicht benötigt", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_NO_CALCULATION, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nicht berechnungsrelevant", databaseDescription);
        // DAIMLER-11485, Kennzeichnung an HM/M/SM dass "ANR" zu "Offen" bei Wegfall-SNR wird
        addFieldDefinition(TABLE_DA_HMMSM, FIELD_DH_SPECIAL_CALC_OMITTED_PARTS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Neues Berechnungsmodell Wegfall-SNR", databaseDescription);


        // Die Daten zur DIALOG-Urladung Modulstruktur HM/M/SM
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_DATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_DATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_FACTORIES, 40, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werke", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_GHM, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Geheimhaltungsmerkmal", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_GHS, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Geheimhaltungsstufe", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_KGU, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!KGU", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_PRI, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_SALES_KZ, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!V-Kenner", databaseDescription);


        // DIALOG Stücklistenmapping für Erstdokumentation
        // ctDA_HMMSM_KGTU, DA_HMMSM_KGTU
        // C213|10|16|04|1510|0010|||FS|20170417085822
        addFieldDefinition(TABLE_DA_HMMSM_KGTU, FIELD_DHK_BCTE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DIALOG-Referenz", databaseDescription);
        // Baureihe+HM+M+SM = "C205&02&12&20"
        addFieldDefinition(TABLE_DA_HMMSM_KGTU, FIELD_DHK_BR_HMMSM, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe&HM&M&SM", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM_KGTU, FIELD_DHK_KG_PREDICTION, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Konstruktionsgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_HMMSM_KGTU, FIELD_DHK_TU_PREDICTION, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Technischer Umfang", databaseDescription);


        // Tabelle für die Entwicklerdaten zur Positionsvariante [DA_DIALOG] [BCTE ]
        // Entwicklungsstückliste
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datensatzkenner", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_ETKZ, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ersatzteilkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_POSE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Dialogposition", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_POSV, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Dialogpositionsvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_HIERARCHY, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Strukturstufe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PARTNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_ETZ, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ersatzteilzähler", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Gültigkeit bei Code", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STEERING, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_QUANTITY_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Mengenkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_RFG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Reifegrad", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_KEMA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kem ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_KEMB, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kem bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum von", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        // Neue Felder hinzu DAIMLER-1246 DG
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STEUA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Steuercode ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STEUB, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Steuercode bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SESI, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Strukturerzeugende Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_POSP, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsnummer Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_FED, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Federführende Konstruktions Freigabe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_RFMEA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!RFME ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_RFMEN, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!RFME bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_BZA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Plan- Bezugsart Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PTE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!PTE", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_KGUM, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Konstruktionsgruppe als Merkmal", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_DISTR, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Verteiler", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_ZFLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_VARG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Variantengenerierung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_VARM, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!VARM", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_GES, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Attribute", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PROJ, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Verlagerungs-KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_CODE_LEN, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Längenangabe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_BZAE_NEU, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Plan-Bezugsart Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_DOCU_RELEVANT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!DIALOG-Doku-relevant", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_LINKED_FACTORY_DATA_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verknüpfte Werkseinsatzdaten GUID", databaseDescription);


        // [ctDA_DIALOG_ADD_DATA], EtkFieldType.felder für AS-Zusatzinformationen zur Entwicklerstückliste
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datensatzkenner", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_POSE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DIALOG Position", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_POSV, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DIALOG Positionsvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise-Kenner", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_ETZ, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!ET-Zähler", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_ADD_TEXT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Ergänzungstext", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_TEXT_NEUTRAL, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Sprachneutraler Text", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_HIERARCHY, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Strukturstufe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Codebedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_INTERNAL_TEXT, 300, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Interner Text", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);

        // TabellenFelder für die Tabelle [ctDA_DIALOG_PARTLIST_TEXT] (=BCTX) DAIMLER-1246 DG
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_BR, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_POSE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsnummer Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_POSV, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_ETZ, 10, EtkFieldLengthType.flInteger, EtkFieldType.feString, false, false, "!!Ersatzteilzähler", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_TEXTKIND, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Textart", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_PG, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_FED, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Federführung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_AATAB, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_STR, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Strukturstufe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_TEXT, 50, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_PARTLIST_TEXT, FIELD_DD_PLT_RFG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Reifegrad", databaseDescription);

        // DA_KGTU_TEMPLATE
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_KG, 10, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Konstruktionsgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_TU, 10, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Technischer Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_AGGREGATE_TYPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Baumusterart", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_AS_PRODUCT_CLASS, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Aftersales Produktklasse", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_TEMPLATE, FIELD_DA_DKT_TU_OPTIONS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!TU-Optionen", databaseDescription);


        // Definition für DA_SAA_HISTORY
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_SAA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_REV_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_KEM_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_KEM_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA_HISTORY, FIELD_DSH_FACTORY_IDS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werkskennungen", databaseDescription);


        // Definition für DA_SA
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_SA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_EDAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_CODES, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Codes", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_NOT_DOCU_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nicht Doku-relevant", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_CONST_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Konstruktionsbenennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SA, FIELD_DS_CONST_SA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SA Nummer (Konstruktion)", databaseDescription);

        // Definition für DA_SAA
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_SAA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_CONST_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Konstruktionsbenennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_DESC_EXTENDED, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Erweiterte Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_REMARK, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_EDAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_CONNECTED_SAS, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verbindungs-SAs", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_KG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_SAA_REF, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Referenz-SAA", databaseDescription);
        addFieldDefinition(TABLE_DA_SAA, FIELD_DS_CONST_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer (Konstruktion)", databaseDescription);


        // Die nummerierten Felder aus EDS_SAA_MASTERDATA werden nun in den beiden folgenden Tabellen abgebildet.
        // Die Tabelle für die Felder EDS_SAA_NOTE_.. 1-10 aus EDS_SAA_MASTERDATA
        // ctDA_EDS_SAA_REMARKS    = ", iPartsDataBaseDescriptionDA_EDS_SAA_REMARKS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_SAA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_REMARK_NO, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkungsziffer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_REMARK, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_TEXT, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Text", databaseDescription);


        // Die Tabelle für die Felder EDS_SAA_WWKB_.. 1-26 aus EDS_SAA_MASTERDATA
        // ctDA_EDS_SAA_WW_FLAGS   = ", iPartsDataBaseDescriptionDA_EDS_SAA_WW_FLAGS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_EDS_SAA_WW_FLAGS, FIELD_DESW_SAA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_WW_FLAGS, FIELD_DESW_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_WW_FLAGS, FIELD_DESW_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_WW_FLAGS, FIELD_DESW_TEXT, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Beschreibung", databaseDescription);


        // Tabelle ctDA_EDS_MODEL: EDS Baumusterinhalt // DAIMLER-1662 DG
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_MODELNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Modellnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_GROUP, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_SCOPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Position", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_STEERING, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!EDS Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_AA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!EDS Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_REVFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_REVTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_KEMFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS KEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_KEMTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!EDS Freigabetermin ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!EDS Freigabetermin bis", databaseDescription);
        // UPPERCASE Feld für Indizierung:
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_MSAAKEY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, true, "!!EDS SAA-Kenner", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_RFG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!EDS Reifegrad", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_QUANTITY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EDS Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_PGKZ, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!EDS Produktgruppenkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!EDS Code", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_FACTORIES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!EDS Werke", databaseDescription);


        // EDS Baukasten (Construction Kit)
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Obere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_PARTPOS, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REVFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand von", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REVTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM von", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RELEASE_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Freigabetermin von", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RELEASE_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Freigabetermin bis", databaseDescription);
        // Folgende DCK_GIUD war in der DB MANUELL verlängert worden. Auch beim Daimler so vorhanden, wirde bemerkt und mit korrigiert.
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datensatzkenner", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SUB_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Untere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_NOTE_ID, 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkungsziffer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_WWKB, 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_QUANTITY, 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Menge", databaseDescription); // Format: [1234.123]
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_QUANTITY_FLAG, 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Mengenkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RFG, 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Reifegrad", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_FACTORY_IDS, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werkskennungen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REPLENISHMENT_KIND, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bezugsart", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_TRANSMISSION_KIT, 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Leitungsbaukasten", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_WWZM, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise zusammen mit", databaseDescription);


        // EDS Verwendungsstellentexte für Baukasten (Construction Kit Properties)
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Obere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_PARTPOS, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_BTX_FLAG, 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!BTX Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_REVFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand von", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_REVTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_KEMFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM von", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_KEMTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_RELEASE_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Freigabetermin von", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_RELEASE_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Freigabetermin bis", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_CONST_PROPS, FIELD_DCP_TEXT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Verwendungsstellentext", databaseDescription);


        // Tabelle DA_EDS_SAA_MODELS: Migration ELDAS, SAA-Gültigkeit zu Baumuster, DAIMLER-1938
        addFieldDefinition(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA/BK Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);


        // Tabellen für den Bildauftrag
        // DA_PICORDER
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bildauftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_ID_EXTERN, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bildauftragsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_REVISION_EXTERN, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Revisionsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_PROPOSED_NAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_PICTURE_TYPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Bildauftragstyp", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_USER_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_USER_GROUP_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzergruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_LAST_ERROR_CODE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fehlernummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_LAST_ERROR_TEXT, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Fehlertext", databaseDescription);
//        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORDERDATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bestellzeitstempel", databaseDescription);
//        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_TARGETDATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erwartete Lieferung", databaseDescription);
//        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_CREATEDATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erstellt", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_DESCRIPTION, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_JOB_USER, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auftragnehmer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_JOB_GROUP, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auftragnehmergruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_JOB_ROLE, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auftragnehmerrolle", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_EVENTNAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Eventname", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_HAS_ATTACHMENTS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Hat Anhänge", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_CHANGE_REASON, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Änderungsgrund", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORIGINAL_PICORDER, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Original Bildauftrag", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Code-Gültigkeit", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_INVALID, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ungültig", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_PO_STATUS_CHANGE_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum Statusänderung", databaseDescription);
        // DAIMLER-13096, AS-PLM Mednienservice: Erweiterung der GetMediaContents für BTT-Templates
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_IS_TEMPLATE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!TemplateFLag", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_DA_PO_AUTOMATION_LEVEL, defaultIntegerLen, EtkFieldLengthType.flInteger, EtkFieldType.feString, false, false, "!!Automatisierungsgrad", databaseDescription);
        // DAIMLER-14117, Erzeugung Bildkopie mit neuer MC-ID und gleichem Inhalt, Tabellenerweiterung
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_PO_IS_COPY, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Kopie", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_PO_ORIGINAL_ORDER_FOR_COPY, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Kopie von Bildauftrag", databaseDescription);
        // DAIMLER-14283, "Bildtafeln nur bei FIN ausgeben" direkt am Bildauftrag setzen
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_PO_ONLY_FIN_VISIBLE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nur bei FIN ausgeben", databaseDescription);
        // DAIMLER-14354, AS-PLM Mendienservice: Unbekannte SVG Elemente abfangen
        addFieldDefinition(TABLE_DA_PICORDER, FIELD_PO_INVALID_IMAGE_DATA, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ungültige Bilddaten", databaseDescription);


        // DA_PICORDER_MODULES
        addFieldDefinition(TABLE_DA_PICORDER_MODULES, FIELD_DA_POM_ORDER_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bildauftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_MODULES, FIELD_DA_POM_MODULE_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);


        //DA_PICORDER_USAGE
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_ORDER_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bildauftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_EINPAS_HG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Einpas HG", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_EINPAS_G, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Einpas G", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_EINPAS_TU, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Einpas TU", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_KG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_USAGE, FIELD_DA_POU_TU, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TU", databaseDescription);


        // DA_PICORDER_PICTURES
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_ORDER_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bildauftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bildnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMREVID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bildrevision", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_USED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Wird verwendet", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_DESIGNER, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ersteller des Bildes", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_VAR_TYPE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Variantentyp", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_LAST_MODIFIED, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Letzte Änderung", databaseDescription);


        // DA_PICORDER_PARTS Teilezuordnung zum Bildauftrag // DAIMLER-1679 DG
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_ORDER_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bildauftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_VARI, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baugruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_VER, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baugruppe Version", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_LFDNR, defaultLfdNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_POS, defaultTextKurzLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!iParts Position (Hotspot)", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_SACH, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sachmummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_SRC_KEY, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Quellschlüssel", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_ZGS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!ZGS der Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_RELDATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_CONTEXT, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Kontextinformation Bild", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_SENT, defaultBoolLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feBoolean, false, false, "!!FLag gesendet", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_PARTLIST_ENTRY_DATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Stücklisteneintrag", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_PIC_POSITION_MARKER, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Bildpositionskenner", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_PARTS, FIELD_DA_PPA_SEQ_NO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Sequenzzähler", databaseDescription);


        // Tabelle DA_PICORDER_ATTACHMENTS: Attachments für den Bildauftrag an AS-PLM
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Datensatzkenner", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_NAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_DESC, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_SIZE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Originäre Dateigröße", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_SIZE_BASE64, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Kodierte Dateigröße", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_CONTENT, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Dateninhalt", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_FILETYPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Dateityp", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_ERRORTEXT, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Fehlertext", databaseDescription);
        addFieldDefinition(TABLE_DA_PICORDER_ATTACHMENTS, FIELD_DPA_ERRORCODE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fehlernummer", databaseDescription);


        // [ctDA_PIC_REFERENCE, DA_PIC_REFERENCE], Tabelle für (DASTI-) Bildreferenzen
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_REF_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bildreferenznummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_REF_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Referenzdatum des Bildes", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_MC_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Mediencontainer ID", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_MC_REV_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Mediencontainer Revisionsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_VAR_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Varianten ID", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_VAR_REV_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Varianten Revisionsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_ERROR_CODE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fehlernummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_ERROR_TEXT, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Fehlertext", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_LAST_MODIFIED, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_PREVIOUS_DATES, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Referenzzeitstempel", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_REFERENCE, FIELD_DPR_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Nachrichten-ID", databaseDescription);


        // [ctDA_PIC_TO_ATTACHMENT, DA_PIC_TO_ATTACHMENT], Verwaltungstabelle zur Änderung eines Bildauftrags
        addFieldDefinition(TABLE_DA_PIC_TO_ATTACHMENT, FIELD_DA_PTA_PICORDER, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bildauftrag", databaseDescription);
        addFieldDefinition(TABLE_DA_PIC_TO_ATTACHMENT, FIELD_DA_PTA_ATTACHMENT, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Anhang", databaseDescription);


        // Tabelle für die Verknüpfung: Fahrzeugbaureihe zur Aggregatebaureihe (gleiche Ebene wie Baumuster) aus [X2E]
        // [DA_VS2US_RELATION] = "Vehicle Series to Unit Series Relation"
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_VEHICLE_SERIES, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fahrzeugbaureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_VS_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsbezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_VS_POSV, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_UNIT_SERIES, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Aggregatsbaureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_DATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_DATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_GROUP, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_RFG, 1, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Reifegrad", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_QUANTITY, 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_DISTR, 150, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verteiler", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_FED, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Federführende KF", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Codebedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_VS2US_RELATION, FIELD_VUR_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);


        // Tabelle für die Positionstexte aus [POSX] DAIMLER-856 DG
        // [ctDA_DIALOG_POS_TEXT] = POSX Texte DAIMLER-1246 DG
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_BR, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fahrzeugbaureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_POS, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsnummer Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_SESI, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_POS_TEXT, FIELD_DD_POS_TEXTNR, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Positionsbenennung", databaseDescription);

        // Tabelle für die Werkseinsatzdaten aus [PBCTP] DAIMLER-1151 DG
        // [DA_FACTORY_DATA] = Werkeinstzdaten
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis DIALOG Datensatz GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werk", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_SPKZ, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Splittkz 1.Coderegel", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_DATA_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Datensatz Zugehörigkeit", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_SEQ_NO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!ELDAS Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_PRODUCT_GRP, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktgruppen-KZ", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_POSE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsnummer Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_POSV, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Variante", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_ET, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ersatzteilzähler", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM-ab", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMB, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM-bis", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!PEM Termin ab", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!PEM Termin bis", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_CRN, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Gültigkeit bei Code", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_STCA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Steuercode ab", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_STCB, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Steuercode bis", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_SOURCE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_FN_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_DATA, FIELD_DFD_LINKED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Verknüpfte Werkseinsatzdaten", databaseDescription);

        // Felder der Tabelle der verschiedenen Werke [DA_FACTORIES]
        addFieldDefinition(TABLE_DA_FACTORIES, FIELD_DF_LETTER_CODE, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Werkskennbuchstaben", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORIES, FIELD_DF_FACTORY_NO, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Werksnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORIES, FIELD_DF_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Bezeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORIES, FIELD_DF_PEM_LETTER_CODE, 10, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM-Kennbuchstaben", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORIES, FIELD_DF_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORIES, FIELD_DF_FILTER_NOT_REL, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Nicht filterrelevant", databaseDescription);

        // Tabelle zur Zuordnung Produkt zu Werke [DA_PRODUCT_FACTORIES]
        addFieldDefinition(TABLE_DA_PRODUCT_FACTORIES, FIELD_DPF_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_FACTORIES, FIELD_DPF_FACTORY_NO, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Werksnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_FACTORIES, FIELD_DPF_EDAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_FACTORIES, FIELD_DPF_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);

        // TabellenFelder für die Tabelle [ctDA_COLORTABLE_DATA] (=FTS)
        addFieldDefinition(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_TABLE_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Variantentabellennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_DESC, 80, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_BEM, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_FIKZ, 10, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Farbidentifikationskennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_VALID_SERIES, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Gültige Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        // TabellenFelder für die Tabelle [ctDA_COLORTABLE_PART] (=X10E)
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Variantentabellennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_PART, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_ETKZ, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Ersatzteilkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_POS_SOURCE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position (Datenquelle)", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_EVAL_PEM_FROM, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Auswertung PEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_EVAL_PEM_TO, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Auswertung PEM bis", databaseDescription);

        // TabellenFelder für die Tabelle [ctDA_COLORTABLE_CONTENT] (=X9E)
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_TABLE_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Variantentabellennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_COLOR_VAR, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Farbvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_PGRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Codebedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_ETKZ, 20, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Ersatzteilkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE_AS, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Codebedingung AS", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVAL_PEM_FROM, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Auswertung PEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVAL_PEM_TO, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Auswertung PEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_FROM_AS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab (AS)", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_TO_AS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis (AS)", databaseDescription);

        // Farbnummern (ctDA_COLOR_NUMBER) (=FNR)
        addFieldDefinition(TABLE_DA_COLOR_NUMBER, FIELD_DCN_COLOR_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Farbnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLOR_NUMBER, FIELD_DCN_SDA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC, 80, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_COLOR_NUMBER, FIELD_DCN_EDAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Anlagedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_COLOR_NUMBER, FIELD_DCN_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_COLOR_NUMBER, FIELD_DCN_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);


        // Werkseinsatzdaten für Inhalt Farbtabelle und Zuordnung Teil-Farbtabelle [ctDA_COLORTABLE_FACTORY] (X10P und X9P)
        // TODO: Mit MB abklären
//        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_TABLE_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, true, "!!Variantentabellennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_TABLE_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Variantentabellennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werk", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_DATA_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Datensatz Zugehörigkeit", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMB, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!PEM Termin ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!PEM Termin bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Steuercode ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCB, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Steuercode bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_POS_SOURCE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position (Datenquelle)", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_ORIGINAL_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Original Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_IS_DELETED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Löschkennzeichen", databaseDescription);


        // Rückmeldedaten
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werk", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_BMAA, 40, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Baumuster", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_PEM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktionseinsatzmeldung", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_IDENT, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fahrzeugidentnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_AS_DATA, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!AS-Daten", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_TEXT, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Text", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_AGG_TYPE, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Aggregatetyp", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_VALID, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Gültigkeit", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_WHC, defaultTextKurzLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Weltherstellercode", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!ELDAS Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_DATA, FIELD_DRD_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);


        // Rückmeldedaten Ausreißer [DA_RESPONSE_SPIKES]
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werk", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_BMAA, 40, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Baumuster", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_IDENT, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fahrzeugidentnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_SPIKE_IDENT, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ausreißer Fahrzeugidentnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_PEM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktionseinsatzmeldung", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_AS_DATA, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!AS-Daten", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_VALID, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Gültigkeit", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);


        // Codestamm
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_CODE_ID, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Code ID", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_PGRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE, FIELD_DC_DESC, 80, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);


        // Daimler, UserManagement
        // Benutzer [DA_USERS]
        addFieldDefinition(TABLE_DA_UM_USERS, FIELD_DA_U_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzer GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USERS, FIELD_DA_U_ID, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benutzer ID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USERS, FIELD_DA_U_ALIAS, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benutzer", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USERS, FIELD_DA_U_TITLE, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Titel", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USERS, FIELD_DA_U_FIRSTNAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Vorname", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USERS, FIELD_DA_U_LASTNAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Nachname", databaseDescription);
        // Gruppe [DA_UM_GROUPS]
        addFieldDefinition(TABLE_DA_UM_GROUPS, FIELD_DA_G_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzergruppen GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_GROUPS, FIELD_DA_G_ID, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benutzergruppen ID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_GROUPS, FIELD_DA_G_ALIAS, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benutzergruppe", databaseDescription);
        // DAIMLER-13091, Focus Auswahl der AS-PLM-Benutzergruppen auf Company einschränken
        addFieldDefinition(TABLE_DA_UM_GROUPS, FIELD_DA_G_SUPPLIER_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Lieferantennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_GROUPS, FIELD_DA_G_BRANCH, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Unternehmenszugehörigkeit", databaseDescription);


        // Rolle [DA_UM_ROLES]
        addFieldDefinition(TABLE_DA_UM_ROLES, FIELD_DA_R_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzerrollen GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_ROLES, FIELD_DA_R_ID, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benutzerrollen ID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_ROLES, FIELD_DA_R_ALIAS, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benutzerrolle", databaseDescription);
        // Zuordnung Gruppen zu einem Benutzer [DA_UM_USER_GROUPS]
        addFieldDefinition(TABLE_DA_UM_USER_GROUPS, FIELD_DA_UG_UGUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzer GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USER_GROUPS, FIELD_DA_UG_GGUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzergruppen GUID", databaseDescription);
        // Zuordnung Rollen zu einem Benutzer [DA_UM_USER_ROLES]
        addFieldDefinition(TABLE_DA_UM_USER_ROLES, FIELD_DA_UR_UGUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzer GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_UM_USER_ROLES, FIELD_DA_UR_RGUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzerrollen GUID", databaseDescription);

        // Tabelle ctDA_DICT_SPRACHE: Dictionary Metadaten (Erweiterung von SPRACHE) DAIMLER-1665 DG
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID (intern)", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_SPRACH, 5, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sprachkürzel", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_CREATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erzeugt am:", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_CHANGE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Geändert am:", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TRANS_JOBID, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Übersetzungsauftrags ID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TRANS_STATE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Übersetzungsstatus", databaseDescription);

        // Tabelle ctDA_DICT_META : Dictionary Metadaten (Textobjekt MetaDaten) DAIMLER-1665 DG
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!TextArtID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID (intern)", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_FOREIGNID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fremd-Id", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Fremd-Quelle", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_STATE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_CREATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erzeugt am:", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_CHANGE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Geändert am:", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_USERID, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Benutzer", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_DIALOGID, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DIALOG-Id", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_META, FIELD_DA_DICT_META_ELDASID, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!ELDAS-Id", databaseDescription);

        // Tabelle ctDA_DICT_TXTKIND_USAGE: Dictionary Textart zu Datenbank-Feld DAIMLER-1665 DG
        addFieldDefinition(TABLE_DA_DICT_TXTKIND_USAGE, FIELD_DA_DICT_TKU_TXTKIND_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!TextArtID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND_USAGE, FIELD_DA_DICT_TKU_FELD, 40, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!DB-Feld", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND_USAGE, FIELD_DA_DICT_TKU_CHANGE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Geändert am:", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND_USAGE, FIELD_DA_DICT_TKU_USERID, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Benutzer", databaseDescription);

        // Tabelle ctDA_DICT_TXTKIND: Dictionary Textart DAIMLER-1665 DG
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_TXTKIND_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!TextArtID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_NAME, 40, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Name", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_LENGTH, 40, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Anzahl Zeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_NEUTRAL, 5, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sprachneutral", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_CHANGE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Geändert am:", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_USERID, defaultGUIDLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Benutzer", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_FOREIGN_TKIND, 40, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fremd-Textart", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TXTKIND, FIELD_DA_DICT_TK_TRANSIT_TKIND, 40, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fremd-Textart Transit", databaseDescription);

        // Tabelle [DA_TRANSIT_LANG_MAPPING], ctDA_TRANSIT_LANG_MAPPING: Sprachenmapping aus TRANSIT
        addFieldDefinition(TABLE_DA_TRANSIT_LANG_MAPPING, FIELD_DA_TLM_TRANSIT_LANGUAGE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Transit-Sprachkürzel", databaseDescription);
        addFieldDefinition(TABLE_DA_TRANSIT_LANG_MAPPING, FIELD_DA_TLM_ISO_LANGUAGE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!ISO-Sprachkürzel", databaseDescription);
        addFieldDefinition(TABLE_DA_TRANSIT_LANG_MAPPING, FIELD_DA_TLM_COMMENT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kommentar", databaseDescription);
        addFieldDefinition(TABLE_DA_TRANSIT_LANG_MAPPING, FIELD_DA_TLM_LANG_ID, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sprach-ID", databaseDescription);


        // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
        // Tabelle [DA_DICT_TRANS_JOB], ctDA_DICT_TRANS_JOB: Übersetzungsaufträge
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_TEXTID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_JOBID, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Übersetzungsauftrags ID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_SOURCE_LANG, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ausgangssprache", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_DEST_LANG, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Zielsprache", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_TRANSLATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Übersetzungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_BUNDLE_NAME, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Paketname", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_TRANSLATION_STATE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_STATE_CHANGE, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Statusänderung", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_LAST_MODIFIED, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Letzte Änderung", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_JOB_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auftragstyp", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_TEXTKIND, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Textart", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_USER_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzerkennung", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_ERROR_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Fehlernummer", databaseDescription);

        // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
        // Tabelle [DA_DICT_TRANS_JOB_HISTORY], ctDA_DICT_TRANS_JOB_HISTORY: Übersetzungsauftragshistorie
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_TEXTID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_JOBID, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Übersetzungsauftrags ID", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_SOURCE_LANG, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ausgangssprache", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_DEST_LANG, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Zielsprache", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_LAST_MODIFIED, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Letzte Änderung", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_TRANSLATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Übersetzungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_BUNDLE_NAME, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Paketname", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_TRANSLATION_STATE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_STATE_CHANGE, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Statusänderung", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_JOB_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auftragstyp", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_TEXTKIND, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Textart", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_USER_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Benutzerkennung", databaseDescription);
        addFieldDefinition(TABLE_DA_DICT_TRANS_JOB_HISTORY, FIELD_DTJH_ERROR_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Fehlernummer", databaseDescription);

        // DA_KGTU_AS
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_PRODUCT, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produkt", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_KG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Konstruktionsgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Technischer Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_DESC, 20, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_EDAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_KGTU_AS, FIELD_DA_DKM_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);


        // DA_SA_MODULES
        addFieldDefinition(TABLE_DA_SA_MODULES, FIELD_DSM_SA_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SA-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);


        // DA_SA_PRODUCT_SAS
        addFieldDefinition(TABLE_DA_PRODUCT_SAS, FIELD_DPS_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_SAS, FIELD_DPS_SA_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SA-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_SAS, FIELD_DPS_KG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_PRODUCT_SAS, FIELD_DPS_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);


        // Tabelle [DA_COMB_TEXT] Textpositionen innerhalb einer Stückliste
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_MODULE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);      // k_vari
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_MODVER, defaultVersionLen, EtkFieldLengthType.flVer, EtkFieldType.feString, false, false, "!!Version", databaseDescription);          // k_ver
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_SEQNO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);  // k_lfdnr
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_TEXT_SEQNO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Textnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Text (intern)", databaseDescription);
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_TEXT_NEUTRAL, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Sprachneutraler Text", databaseDescription);
        // DAIMLER-14624, Kenner für GenVO-Ergänzungstext anzeigen
        addFieldDefinition(TABLE_DA_COMB_TEXT, FIELD_DCT_SOURCE_GENVO, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Quelle GenVO", databaseDescription);


        // Die Fußnotentabellen
        // [DA_FN], Tabelle für die Fußnotenstammdaten
        addFieldDefinition(TABLE_DA_FN, FIELD_DFN_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_FN, FIELD_DFN_NAME, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnotenname", databaseDescription);
        addFieldDefinition(TABLE_DA_FN, FIELD_DFN_STANDARD, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Standardfußnote", databaseDescription);
        addFieldDefinition(TABLE_DA_FN, FIELD_DFN_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Fußnotentyp", databaseDescription);

        // [DA_FN_CONTENT], Tabelle für den Fußnoteninhalt, auch Tabellenfußnoten
        addFieldDefinition(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO, defaultLfdNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Zeilennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Text (intern)", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT_NEUTRAL, 1000, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sprachneutraler Text", databaseDescription);

        // ctDA_FN_POS, DA_FN_POS, Tabelle für die Fußnoten zur Teileposition aus DIALOG, VBFN
        // BCTE-Schlüssel, SeriesNo, Hm, M, SM, PosE, PosV, WW, ETZ, AA, SDATA
        addFieldDefinition(TABLE_DA_FN_POS, FIELD_DFNP_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datensatzkenner", databaseDescription);
        // Die restlichen Felder des PKs, die im BCTE-Schlüssel nicht enthalten sind
        addFieldDefinition(TABLE_DA_FN_POS, FIELD_DFNP_SESI, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Strukturerzeugende Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_POS, FIELD_DFNP_POSP, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsnummer Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_POS, FIELD_DFNP_FN_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        // Felder, die nicht im Primärschlüssel enthalten sind:
        addFieldDefinition(TABLE_DA_FN_POS, FIELD_DFNP_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_POS, FIELD_DFNP_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);

        // [DA_FN_KATALOG_REF], Tabelle für die Verbindung zwischen den Fußnoten und den Positionene der Aftersales Stücklisten in [KATALOG]
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODULE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);      // k_vari
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODVER, defaultVersionLen, EtkFieldLengthType.flVer, EtkFieldType.feString, false, false, "!!Version", databaseDescription);          // k_ver
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_SEQNO, defaultLfdNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);  // k_lfdnr
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FNID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FN_SEQNO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer der Fußnote", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FN_MARKED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!In Ansicht hervorheben", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_COLORTABLEFOOTNOTE, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Fußnoten zur Farbtabellenfußnote", databaseDescription);

        // [DA_FN_SAA_REF], Verknüpfung zwischen SAA-Stammdaten für SA Kataloge, Verbindungs-SA
        addFieldDefinition(TABLE_DA_FN_SAA_REF, FIELD_DFNS_SAA, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_SAA_REF, FIELD_DFNS_FNID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_SAA_REF, FIELD_DFNS_FN_SEQNO, defaultLfdNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);

        // [DA_FN_MAT_REF], Tabelle für die Verbindung Fußnote zum Teil [MAT]
        addFieldDefinition(TABLE_DA_FN_MAT_REF, FIELD_DFNM_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Materialnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_MAT_REF, FIELD_DFNM_FNID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_FN_MAT_REF, FIELD_DFNM_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);

        // [DA_EPC_FN_CONTENT] Tabelle für den Fußnoteninhalt von EPC
        addFieldDefinition(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TYPE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumuster oder SA", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TEXT_ID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_LINE_NO, defaultLfdNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Zeilennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TEXT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Text", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_ABBR, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Platzhalterkenner", databaseDescription);

        // [DA_EPC_FN_KATALOG_REF] Tabelle für die Verbindung zwischen den Fußnoten und den Positionen der Aftersales Stücklisten in [KATALOG]
        addFieldDefinition(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_PRODUCT_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_KG, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_FN_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_TEXT_ID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_GROUP, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Fußnotengruppe", databaseDescription);

        // [DA_EPC_FN_SA_REF] Tabelle für die Fußnoten, die auf SAs referenzieren
        addFieldDefinition(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_SA_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SA-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_FN_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_TEXT_ID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID", databaseDescription);
        addFieldDefinition(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_GROUP, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Fußnotengruppe", databaseDescription);

        // [DA_AS_CODES], Tabelle für die zu entfernenden Aftersales-Codes
        addFieldDefinition(TABLE_DA_AS_CODES, FIELD_DAS_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Zu entfernender After Sales Code", databaseDescription);

        // [DA_ACC_CODES], Tabelle für die zu entfernenden Codes für Zubehörteile (=Accessory)
        addFieldDefinition(TABLE_DA_ACCESSORY_CODES, FIELD_DACC_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Zu entfernender Zubehör Code", databaseDescription);

        // [DA_CONST_STATUS_CODES], DAIMLER-8332, Codeliste für Statusauswertung Konstruktion,
        // Enthält Code die nicht einsatzgesteuert werden. Diese Code sollen nicht beim Filter entfernt werden, ausser sie sind zusätzlich in der ET-/Zubehör-Liste enthalten.
        addFieldDefinition(TABLE_DA_CONST_STATUS_CODES, FIELD_DASC_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Code", databaseDescription);

        // [DA_AGGS_MAPPING], Tabelle für das Mapping von DIALOG-Aggregatetypen auf MAD-Aggregatetypen
        addFieldDefinition(TABLE_DA_AGGS_MAPPING, FIELD_DAM_DIALOG_AGG_TYPE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!DIALOG Aggregatetyp", databaseDescription);
        addFieldDefinition(TABLE_DA_AGGS_MAPPING, FIELD_DAM_MAD_AGG_TYPE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!MAD Aggregatetyp", databaseDescription);

        // [DA_AC_PC_MAPPING], Tabelle für das Mapping von
        // Sortimentsklassen (=AssortmentClasses) auf Aftersales Produktklassen (=ProductClasses)
        addFieldDefinition(TABLE_DA_AC_PC_MAPPING, FIELD_DAPM_ASSORTMENT_CLASS, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Sortimentsklasse", databaseDescription);
        addFieldDefinition(TABLE_DA_AC_PC_MAPPING, FIELD_DAPM_AS_PRODUCT_CLASS, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Aftersales Produktklasse", databaseDescription);

        // [DA_BRANCH_PC_MAPPING] Tabelle für das Mapping von Branch auf AS-Produktklassen
        addFieldDefinition(TABLE_DA_BRANCH_PC_MAPPING, FIELD_DBM_BRANCH, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sparte", databaseDescription);
        addFieldDefinition(TABLE_DA_BRANCH_PC_MAPPING, FIELD_DBM_AS_PRODUCT_CLASSES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Aftersales Produktklassen", databaseDescription);

        // [DA_OMITTED_PARTS], Tabelle für Entfallteile
        addFieldDefinition(TABLE_DA_OMITTED_PARTS, FIELD_DA_OP_PARTNO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Entfallteil", databaseDescription);

        // [DA_REPLACE_PART], Tabelle für die Teileersetzung
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_VARI, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Vorgänger Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_VER, defaultMatNrLen, EtkFieldLengthType.flVer, EtkFieldType.feString, false, false, "!!Vorgänger Modulversion", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_LFDNR, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Vorgänger laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_SEQNO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer der Ersetzungen", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Nachfolger Materialnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_LFDNR, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Nachfolger laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEA, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!RFMEA FLags", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEN, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!RFMEN FLags", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_SOURCE_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Konstruktions-Vorläufer GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_SOURCE_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Konstruktions-Nachfolger GUID", databaseDescription);


        // [DA_INCLUDE_PART], Tabelle für die Mitlieferteile
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_VARI, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_VER, defaultMatNrLen, EtkFieldLengthType.flVer, EtkFieldType.feString, false, false, "!!Modulversion", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_LFDNR, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Vorgänger laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_REPLACE_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Nachfolger Materialnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_REPLACE_LFDNR, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Nachfolger laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_SEQNO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer der Mitlieferteile", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Mitlieferteil Materialnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Mitlieferteil Menge", databaseDescription);


        // [DA_FACTORY_MODEL], Tabelle für die Beziehung zwischen WMI/WHC und Baumuster bzw. Typkennzahl
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_WMI, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Weltherstellercode", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_FACTORY_SIGN, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werkskennbuchstabe", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werksnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_MODEL_PREFIX, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusterpräfix", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_ADD_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Zusatzwerk", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_AGG_TYPE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Aggregateart", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_SEQ_NO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Reihenfolge Werkskennbuchstabe", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_BELT_SIGN, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bandkennzahl", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_BELT_GROUPING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bandbündelung", databaseDescription);
        addFieldDefinition(TABLE_DA_FACTORY_MODEL, FIELD_DFM_FACTORY_SIGN_GROUPING, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!WKB Guppierung", databaseDescription);


        // [TABLE_DA_SPRING_MAPPING] Tabelle für das Mapping ZB Federbein auf Feder
        addFieldDefinition(TABLE_DA_SPRING_MAPPING, FIELD_DSM_ZB_SPRING_LEG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!ZB Federbein Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SPRING_MAPPING, FIELD_DSM_SPRING, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Feder Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SPRING_MAPPING, FIELD_DSM_EDAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Anlagedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_SPRING_MAPPING, FIELD_DSM_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);

        // [TABLE_DA_CODE_MAPPING] Tabelle für das Mapping Code(Typkennzahl/VeDoc Sparte) auf Code
        addFieldDefinition(TABLE_DA_CODE_MAPPING, FIELD_DCM_CATEGORY, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!VeDoc Sparte", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE_MAPPING, FIELD_DCM_MODEL_TYPE_ID, defaultTextKurzLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Typkennzahl", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE_MAPPING, FIELD_DCM_INITIAL_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Ausgangscode", databaseDescription);
        addFieldDefinition(TABLE_DA_CODE_MAPPING, FIELD_DCM_TARGET_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Zielcode", databaseDescription);

        // [DA_AGG_PART_CODES] Tabelle für das Anreichern von Code zu ZB Aggregat Teilenummer
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!ZB Aggregat Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Code", databaseDescription);
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_SERIES_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werksnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_FACTORY_SIGN, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werkskennbuchstabe", databaseDescription);
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_DATE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_AGG_PART_CODES, FIELD_DAPC_DATE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Datum bis", databaseDescription);


        // [DA_ES1] Tabelle für PRIMUS, Verwaltung von ES1-Schlüsseln
        addFieldDefinition(TABLE_DA_ES1, FIELD_DES_ES1, 2, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!ES1 Schlüssel", databaseDescription);
        addFieldDefinition(TABLE_DA_ES1, FIELD_DES_FNID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Fußnoten Id", databaseDescription);
        addFieldDefinition(TABLE_DA_ES1, FIELD_DES_TYPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Alternativteil Typ", databaseDescription);


        // Tabellen für Autorenaufträge
        // [DA_AUTHOR_ORDER]
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Autoren-Auftrags-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_DESC, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_STATUS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATION_USER_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Ersteller", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CHANGE_SET_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_USER_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Aktueller Benutzer", databaseDescription);
        // DAIMLER-8152, Benutzergruppe: Erweiterung Autorenauftrag
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATOR_GRP_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Benutzergruppe Ersteller", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_GRP_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Delegiert an Benutzergruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_BST_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Arbeitsauftragsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_BST_SUPPLIED, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Versorgt an BST", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_BST_ERROR, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Fehlermeldung von BST", databaseDescription);
        addFieldDefinition(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_RELDATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum", databaseDescription);


        // [DA_AO_HISTORY]
        addFieldDefinition(TABLE_DA_AO_HISTORY, FIELD_DAH_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Autoren-Auftrags-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_AO_HISTORY, FIELD_DAH_SEQNO, defaultMatNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_AO_HISTORY, FIELD_DAH_CHANGE_USER_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Bearbeiter", databaseDescription);
//        addFieldDefinition(TABLE_DA_AO_HISTORY, FIELD_DAH_CHANGE_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_AO_HISTORY, FIELD_DAH_ACTION, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Aktion", databaseDescription);

        // Changeset um weitere Geschäftsfälle und Informationen anreichern (DAIMLER-6356)
        // Definitionen über eine Tabelle konfigurierbar machen.
        // ctDA_CHANGE_SET_INFO_DEFS, "DA_CHANGE_SET_INFO_DEFS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_CHANGE_SET_INFO_DEFS, FIELD_DCID_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_INFO_DEFS, FIELD_DCID_FELD, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DatenbankFeld", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_INFO_DEFS, FIELD_DCID_AS_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Relevant für After-Sales", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_INFO_DEFS, FIELD_DCID_MUSTFIELD, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!MussFeld", databaseDescription);


        // Tabellen für die Änderungssets
        // [DA_CHANGE_SETS]
        addFieldDefinition(TABLE_DA_CHANGE_SET, FIELD_DCS_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
//        addFieldDefinition(TABLE_DA_CHANGE_SET, FIELD_DCS_COMMIT_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET, FIELD_DCS_SOURCE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Quelle", databaseDescription);

        // [DA_CHANGE_SET_ENTRY]
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID_OLD, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Alte Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_CURRENT_DATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Aktuelle Daten", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_HISTORY_DATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Historische Daten", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Datensatz GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_EDIT_INFO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Edit-Information", databaseDescription);
        addFieldDefinition(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Materialnummer", databaseDescription);


        // KonFLiktmanagement, Reservierung von benutzten Primärschlüsseln
        // ctDA_RESERVED_PK, DA_RESERVED_PK
        addFieldDefinition(TABLE_DA_RESERVED_PK, FIELD_DRP_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_RESERVED_PK, FIELD_DRP_DO_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_RESERVED_PK, FIELD_DRP_CHANGE_SET_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset", databaseDescription);


        // Tabelle für die Bestätigung von Änderungen (allgemein, soll später nicht nur für ChangeSets herhalten)
        // [ctDA_CONFIRM_CHANGES, DA_CONFIRM_CHANGES]
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_CHANGE_SET_ID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_DO_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_PARTLIST_ENTRY_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Stücklisteneintrag", databaseDescription);
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_DO_SOURCE_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verweis Konstruktion Datensatz GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_CONFIRMATION_USER, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Benutzer", databaseDescription);
        addFieldDefinition(TABLE_DA_CONFIRM_CHANGES, FIELD_DCC_CONFIRMATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Quittierungsdatum", databaseDescription);


        // Tabellen für die BOM-DB, Baumustergruppe Stammdaten, sprachunabhängig
        // [DA_OPS_GROUP]
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_GROUP, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumustergruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_AS_FROM, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_AS_TO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_INVALID, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Ungültig Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_DESC, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_GROUP, FIELD_DOG_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);

        // [DA_OPS_SCOPE]
        addFieldDefinition(TABLE_DA_OPS_SCOPE, FIELD_DOS_SCOPE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_SCOPE, FIELD_DOS_DESC, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_OPS_SCOPE, FIELD_DOS_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);


        // [DA_VIN_MODEL_MAPPING], Tabelle für das Mapping von VIN Prefix auf Baumuster Prefix
        addFieldDefinition(TABLE_DA_VIN_MODEL_MAPPING, FIELD_DVM_VIN_PREFIX, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!VIN-Prefix", databaseDescription);
        addFieldDefinition(TABLE_DA_VIN_MODEL_MAPPING, FIELD_DVM_MODEL_PREFIX, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Baumusterprefix", databaseDescription);


        // [DA_COUNTRY_CODE_MAPPING] Tabelle für das Mapping Bereichscode auf ISO 3166_2 Ländercode
        addFieldDefinition(TABLE_DA_COUNTRY_CODE_MAPPING, FIELD_DCM_REGION_CODE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bereichscode", databaseDescription);
        addFieldDefinition(TABLE_DA_COUNTRY_CODE_MAPPING, FIELD_DCM_COUNTRY_CODES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Ländercodes", databaseDescription);


        // [DA_BAD_CODE] Code die dafür sorgen, dass Teilepositionen ungültig sind
        addFieldDefinition(TABLE_DA_BAD_CODE, FIELD_DBC_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_BAD_CODE, FIELD_DBC_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_BAD_CODE, FIELD_DBC_CODE_ID, 390, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Badcode", databaseDescription);
        addFieldDefinition(TABLE_DA_BAD_CODE, FIELD_DBC_EXPIRY_DATE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Verfallsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_BAD_CODE, FIELD_DBC_PERMANENT_BAD_CODE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Dauerhaft", databaseDescription);


        // Tabelle zur Verwaltung von DIALOG-Änderungen zur Anzeige und Prüfung
        // ctDA_DIALOG_CHANGES, "DA_DIALOG_CHANGES", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_HASH, 40, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hash-Wert", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_SERIES_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_BCTE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DIALOG-Referenz", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_MATNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Materialnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_CHANGE_SET_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_KATALOG_ID, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Katalog ID", databaseDescription);


        // Tabelle für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
        // ctDA_DIALOG_DSR, "DA_DIALOG_DSR", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MATNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Materialnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_TYPE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_NO, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Merkmalsnummer", databaseDescription);
//        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
//        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK1, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Merkmal Komponente 1", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK2, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Merkmal Komponente 2", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK3, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Merkmal Komponente 3", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK4, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Merkmal Komponente 4", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK5, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Merkmal Komponente 5", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK6, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Merkmal Komponente 6", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK7, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Merkmal Komponente 7", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK_TEXT, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Freitext", databaseDescription);
        addFieldDefinition(TABLE_DA_DIALOG_DSR, FIELD_DSR_MK_ID, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Merkmals-ID", databaseDescription);

        // Felder für Tabelle [DA_BOM_MAT_HISTORY]
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Material", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_PART_VER, defaultMatNrLen, EtkFieldLengthType.flVer, EtkFieldType.feString, false, false, "!!Material Version", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_REV_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_KEM_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_KEM_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
//        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin ab", databaseDescription);
//        addFieldDefinition(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin bis", databaseDescription);


        // Felder für die Tabelle für die internen Texte an Teilepositionen
        // ctDA_INTERNAL_TEXT [DA_INTERNAL_TEXT]
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_U_ID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Benutzer ID", databaseDescription);
//        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CREATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_DO_TYPE, 80, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_DO_ID, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
//        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CHANGE_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_TITEL, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Titel", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_TEXT, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Text", databaseDescription);
        addFieldDefinition(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_ATTACHMENT, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Anhänge", databaseDescription);


        // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
        // Ersetzungen: ctDA_REPLACE_CONST_MAT, [DA_REPLACE_CONST_MAT]
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
//        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_PRE_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Vorgänger Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_VOR_KZ_K, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Platzhalter (VOR_KZ_K)", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_RFME, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!RFME FLags", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_PRE_RFME, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!RFME FLags Vorgänger", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_LOCK_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sperrvermerk", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_ANFO, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Angefordert", databaseDescription);


        // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
        // Mitlieferteile:  ctDA_INCLUDE_CONST_MAT, [DA_INCLUDE_CONST_MAT]
        addFieldDefinition(TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
//        addFieldDefinition(TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_INCLUDE_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Mitlieferteilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_INCLUDE_PART_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);


        // [ctDA_REPLACE_CONST_PART], (T10RTS7), Konstruktionsdaten Ersetzungen Teilestamm Änderungstexte mit Sprachschlüssel
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
//        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
//        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_FACTORY_IDS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werkskennungen", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_RFME, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!RFME FLags", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_TEXT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Änderungstext", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_PRE_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Vorgänger Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_REPLACE_MATNR, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Nachfolger Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_AVAILABLE_MATERIAL, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Vorhandenes Material", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_TOOL_CHANGE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Werkzeugänderung", databaseDescription);
        addFieldDefinition(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_MATERIAL_CHANGE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Materialänderung", databaseDescription);


        // DIALOG-Tabelle (ZBVE) Baukasteninhalt (Construction Kit) [ctDA_CONST_KIT_CONTENT = ", iPartsDataBaseDescriptionDA_CONST_KIT_CONTENT", iPartsDataBaseDescription]
        // ctDA_CONST_KIT_CONTENT, "DA_CONST_KIT_CONTENT", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer ZBV", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_DCKC_POSE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Position im BK", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SDA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SDB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SUB_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Untere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_KEM_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kem ab", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_KEM_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kem bis", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_QUANTITY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Menge pro ZBV", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SOURCE_KEY, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ursachenschlüssel", databaseDescription);
        addFieldDefinition(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_PROPOSED_SOURCE_TYPE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Vorschlag Bezugsart", databaseDescription);


        // Tabelle "Termin Start of Production" zur DIALOG Baureihe
        // ctDA_SERIES_SOP, "DA_SERIES_SOP", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_SERIES_SOP, FIELD_DSP_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_SOP, FIELD_DSP_AA, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_SOP, FIELD_DSP_START_OF_PROD, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Produktionsstart", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_SOP, FIELD_DSP_KEM_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM-Stichtag", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_SOP, FIELD_DSP_ACTIVE, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Aktiviert", databaseDescription);

        // Tabelle für die Baubarkeit, gültige Code zur Baureihe DAIMLER-5634
        // ctDA_SERIES_CODES, "DA_SERIES_CODES", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_SERIES_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_GROUP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Baumustergruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_POS, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_POSV, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_REGULATION, 30, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Regelelement", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_STEERING, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_CGKZ, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!BG-/CG-KZ", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_ZBED, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Zusteuerbedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_RFG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Reifegrad der Struktur", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_DISTR, 150, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verteiler", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_FED, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Federführende Konstruktions Freigabe", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_PRODUCT_GRP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Codebedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_FEASIBILITY_COND, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Baubarkeitsbedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_GLOBAL_CODE_SIGN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Pauschale Codebedingung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_EVENT_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID ab", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_CODES, FIELD_DSC_EVENT_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID bis", databaseDescription);


        // Tabelle (T10REREI, EREI) für die Ereignissteuerung, Events pro Baureihe, Baureihen-Events, DAIMLER-6990
        // ctDA_SERIES_EVENTS, "DA_SERIES_EVENTS", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_EVENT_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Ereignis-ID", databaseDescription);
//        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum von", databaseDescription);
//        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_PREVIOUS_EVENT_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Vorgänger Ereignis-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_DESC, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_REMARK, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_CONV_RELEVANT, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Konvertierungsrelevant", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_STATUS, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EVENTS, FIELD_DSE_CODES, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Codebedingung", databaseDescription);


        // Tabelle zur Speicherung der geänderten Anzahl Teilepositionen auf Ebene BR/HM/M/SM
        // ctDA_REPORT_CONST_NODES, "DA_REPORT_CONST_NODES", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_REPORT_CONST_NODES, FIELD_DRCN_SERIES_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_REPORT_CONST_NODES, FIELD_DRCN_NODE_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Knoten-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_REPORT_CONST_NODES, FIELD_DRCN_CHANGESET_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Änderungsset-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_REPORT_CONST_NODES, FIELD_DRCN_OPEN_ENTRIES, defaultIntegerLen, EtkFieldLengthType.flInteger, EtkFieldType.feString, false, false, "!!Offene Teilepositionen", databaseDescription);
        addFieldDefinition(TABLE_DA_REPORT_CONST_NODES, FIELD_DRCN_CHANGED_ENTRIES, defaultIntegerLen, EtkFieldLengthType.flInteger, EtkFieldType.feString, false, false, "!!Geänderte Teilepositionen", databaseDescription);
        addFieldDefinition(TABLE_DA_REPORT_CONST_NODES, FIELD_DRCN_CALCULATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Berechnungszeitpunkt", databaseDescription);


        // Tabelle für die KEM (KonstruktionsEinsatzMeldungen) Stammdaten aus DIALOG
        // ctDA_KEM_MASTERDATA, "DA_KEM_MASTERDATA", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_KEM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM-Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SDA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SDB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_OUTPUT_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ausgabesteuerkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_HANDLING_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verarbeitungskennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_WORKER_IDX, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bearbeiterindex", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SECRECY_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Geheimhaltungskennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SECRECY_LEVEL, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Geheimhaltungsstufe", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_APPLICATION_NO, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Antragsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_REASON_CODE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ursachenschlüssel", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_DESC, 70, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SPEC, 130, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Einsatzvorgabe/-begrenzung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_REMARK, 160, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_PERMISSION_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Gesetz/Zulassung", databaseDescription);
        // Enthält ungültige Datumsangaben und muss deshalb als String gespeichert werden:
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_PERMISSION_DATA, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gesetz ab Datum", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_TECHNICAL_LETTER_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Technische Rundschreiben", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SPECIAL_TOOL_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!ZKD-Sonderwerkzeuge", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_EMISSION_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Abgas betroffen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_STOP_KEM_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Kennzeichen Stopmeldung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_STOP_KEM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Stopmeldung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_ANNULMENT_KEM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Aufhebe-KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_ANNULMENT_DATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Aufhebetermin", databaseDescription);
        // Sollte ein Datum enthalten, enthält aber ungültige Längen und muss daher als String gespeichert werden:
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_EXTENSION_DATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verlängerungstermin", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_JOINED_KEM1, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(1) Zusammen mit KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_JOINED_KEM2, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(2) Zusammen mit KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_JOINED_KEM3, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(3) Zusammen mit KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_JOINED_KEM4, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(4) Zusammen mit KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SP_HANDLING_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(ET) Verarbeitungs-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SP_JOINED_KEM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(ET) verbundene KEM", databaseDescription);
        // Enthält für einen Zeitstempel keine Sekunden und wird als String gespeichert:
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SP_DATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(ET) Datum Abzug SPR-Bearbeitung", databaseDescription);
        // Enthält für einen Zeitstempel keine Sekunden und wird als String gespeichert:
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SP_DATR, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(ET) Datum Rücklauf SPR-Bearbeitung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SP_BT_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(ET) BT-Bearbeitung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SP_FOREIGN_LANG_PROC, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!(ET) Fremdsprachenbearbeitung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_REASON, 250, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Grund", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_KEM_REVISION_STATE, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!KEM-Änderungsstand", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_TDAT_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Technische Daten", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SYSTEM_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Systemkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_SKEM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!KEM zur Jahr 2000-gerechten Sortierung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_PRIORITY, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Priorität der KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_DEVIATION_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Bemusterung bei Abweichungserlaubnis betroffen", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_DEVIATION_PLANNED_START, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Geplanter Start der Abweichung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_DEVIATION_PLANNED_END, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Geplantes Ende der Abweichung", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_MASTERDATA, FIELD_DKM_DEVIATION_DURATION, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Dauer der Abweichung", databaseDescription);


        // Tabelle für PEM Stammdaten (ProduktionsEinsatzMeldungen) aus DIALOG
        // ctDA_PEM_MASTERDATA, "DA_PEM_MASTERDATA", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_FACTORY_NO, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Werksnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Einsatztermin", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_DESC, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_STC, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Steuercode", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_ADAT, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Änderungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);


        // Tabelle für Fehlerorte aus DIALOG
        // ctDA_ERROR_LOCATION, "DA_ERROR_LOCATION", iPartsDataBaseDescription
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_POSE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Dialogposition", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_PARTNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_DAMAGE_PART, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Schadensteil", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_SDA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!S-Datum KEM-ab", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_SDB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!S-Datum KEM-bis", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_ORD, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ordnung", databaseDescription);
        addFieldDefinition(TABLE_DA_ERROR_LOCATION, FIELD_DEL_USERID, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!User ID", databaseDescription);


        // Stammdaten eines Bearbeitungsauftrags aus BST
        // ctDA_WORKORDER, DA_WORKORDER
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_BST_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Arbeitsauftragsnummer", databaseDescription); // [PK]
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_ORDER_NO, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bestellnummer (BANF)", databaseDescription);  // [SK 1]
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_SERIES, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihen", true, databaseDescription);       // String Array
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_BRANCH, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sparte", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_SUB_BRANCHES, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Untersparten", true, databaseDescription);    // String Array
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_COST_NEUTRAL, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Kostenneutral", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_INTERNAL_ORDER, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Interner Bearbeitungsauftrag", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_RELEASE_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Abrufnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_TITLE, 1000, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Arbeitstitel", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_DELIVERY_DATE_PLANNED, defaultDateLen, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Geplantes Lieferdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_START_OF_WORK, defaultDateLen, EtkFieldLengthType.flMatNr, EtkFieldType.feDate, false, false, "!!Leistungsbeginn", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_SUPPLIER_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Lieferantennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_SUPPLIER_SHORTNAME, 50, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Lieferantenname kurz", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER, FIELD_DWO_SUPPLIER_NAME, 255, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Lieferantenname", databaseDescription);


        // Einzelaufträge eines Bearbeitungsauftrags aus BST
        // ctDA_WORKORDER_TASKS, DA_WORKORDER_TASKS
        addFieldDefinition(TABLE_DA_WORKORDER_TASKS, FIELD_DWT_BST_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Arbeitsauftragsnummer", databaseDescription); // [PK]
        addFieldDefinition(TABLE_DA_WORKORDER_TASKS, FIELD_DWT_LFDNR, defaultLfdNrLen, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);       // [PK]
        addFieldDefinition(TABLE_DA_WORKORDER_TASKS, FIELD_DWT_ACTIVITY_NAME, 255, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Leistungsname", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER_TASKS, FIELD_DWT_ACTIVITY_TYPE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Leistungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_WORKORDER_TASKS, FIELD_DWT_AMOUNT, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);


        // Abrechnungsrelevante Bearbeitungen aus dem ChangeSet für den manuellen Abrechnungsprozess
        // ctDA_INVOICE_RELEVANCE, DA_INVOICE_RELEVANCE
        addFieldDefinition(TABLE_DA_INVOICE_RELEVANCE, FIELD_DIR_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_INVOICE_RELEVANCE, FIELD_DIR_FIELD, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!DatenbankFeld", databaseDescription);


        // DAIMLER-9276, Nachrichtenpostkorb, die Nachrichten an sich
        // ctDA_MESSAGE, DA_MESSAGE
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Nachrichten-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_TYPE, 50, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Nachrichten-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_DO_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_SERIES_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_SUBJECT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Betreff", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_MESSAGE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Nachricht", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_USER_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erstellt von", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellt am", databaseDescription);
        // DAIMLER-15786, Postkorb Benachrichtigungen mit einer Erinnerung neu festlegen
        addFieldDefinition(TABLE_DA_MESSAGE, FIELD_DMSG_RESUBMISSION_DATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Wiedervorlagetermin", databaseDescription);


        // DAIMLER-9276, Nachrichtenpostkorb, die Empfänger und die Quittierungsarten User/Group/Organisation+Role
        // ctDA_MESSAGE_TO, DA_MESSAGE_TO
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_GUID, defaultGUIDLen, EtkFieldLengthType.flGUID, EtkFieldType.feString, false, false, "!!Nachrichten-GUID", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_USER_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!An Benutzer", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_GROUP_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!An Benutzergruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!An Organisation", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Für Rolle", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_BY_USER_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gelesen von", databaseDescription);
        addFieldDefinition(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Gelesen am", databaseDescription);

        // DAIMLER-9429, Anreicherung der Anreicherung bei PKW Motoren
        // ctDA_VEHICLE_DATACARD_CODES, DA_VEHICLE_DATACARD_CODES
        addFieldDefinition(TABLE_DA_VEHICLE_DATACARD_CODES, FIELD_DVDC_CODE, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Fahrzeug-Code", databaseDescription);


        // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
        // ctDA_EXPORT_REQUEST, Die Export-Anforderung, ein Gesamtauftrag
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_JOB_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Auftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_CUSTOMER_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Kundennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_JOB_ID_EXTERN, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Auftragsnummer extern", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_LANGUAGES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!Angeforderte Sprachen", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_INCLUDE_SAS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Incl. freie SAs", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_INCLUDE_PICTURES, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Incl. Bilder", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_INCLUDE_AGGS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Incl. Aggregate", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_OUTPUT_FORMAT, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ausgabeformat", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_DIRECT_DOWNLOAD, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Direkter Download", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_CREATION_USER_ID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ersteller", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_CREATION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_COMPLETION_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Fertigstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_STATE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Gesamtstatus", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_ERROR_TEXT, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Fehlertext", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_SAVE_LOCATION, defaultFileLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Kd. spez. Unterverzeichnis", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_COLLECTION_ARCHIVE_FILE, defaultFileLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gesamtarchivdatei", databaseDescription);
        // DAIMLER-12926, Web-Service_Export um Bildformatauswahl erweitern, Tabellendefinition
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_PICTURE_FORMAT, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!gewünschtes Bildformat", databaseDescription);
        // XML-BM-Export: PRIMUS-MAT-Eigenschaften optional ausgeben
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_INCLUDE_MAT_PROPERTIES, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Incl. Materialeigenschaften", databaseDescription);
        // DAIMLER-13346, EinPAS-Attribute über XML-Export-Service ausgeben
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_INCLUDE_EINPAS, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Incl. EinPAS-Attribute", databaseDescription);
        // DAIMLER-16325, PSK: Webservice ExportPartsList um optionale Ausgabe der Fzg-Navigation erweitern
        addFieldDefinition(TABLE_DA_EXPORT_REQUEST, FIELD_DER_INCLUDE_VISUAL_NAV, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Incl. Navigationsperspektive", databaseDescription);

        // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
        // ctDA_EXPORT_CONTENT, Die einzelnen Unteraufträge bzw. Job-Inhalte
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_JOB_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Auftragsnummer intern", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_DO_TYPE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_DO_ID, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datenobjekt-ID", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_STATE, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_ERROR_TEXT, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Fehlertext", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_ARCHIVE_FILE, defaultFileLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Archivdatei", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_NUMBER_PICTURES, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Anzahl Bilder", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_NUMBER_PARTLIST_ITEMS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Anzahl Teilepositionen", databaseDescription);
        addFieldDefinition(TABLE_DA_EXPORT_CONTENT, FIELD_DEC_ARCHIVE_SIZE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Größe des Archivs", databaseDescription);


        // DAIMLER-9623, EDS/BCS: Weitere Teilestammdaten sprachunabhängig
        // ctDA_EDS_MAT_REMARKS, Die Tabelle für die 10 Bemerkungen (0 - 9)
        addFieldDefinition(TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_REMARK_NO, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkungsziffer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_REMARK, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkung", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_TEXT, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, true, false, "!!Text", databaseDescription);


        // ctDA_EDS_MAT_WW_FLAGS, Die Tabelle für die 26 verschiedenen Wahlweise-Kennzeichen (1-26)
        addFieldDefinition(TABLE_DA_EDS_MAT_WW_FLAGS, FIELD_DEMW_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_WW_FLAGS, FIELD_DEMW_REV_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_WW_FLAGS, FIELD_DEMW_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_EDS_MAT_WW_FLAGS, FIELD_DEMW_TEXT, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Beschreibung", databaseDescription);


        // DAIMLER-9744, EDS-Arbeitsvorrat für KEMs bei Truck
        // ctDA_KEM_WORK_BASKET
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_KEM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA/BK-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_KG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_MODULE_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_DOCU_RELEVANT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Doku-relevant", databaseDescription);


        // DAIMLER-10428, MBS-Arbeitsvorrat für KEMs bei Truck
        // ctDA_KEM_WORK_BASKET_MBS
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KEM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA/BK-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_GROUP, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_MODULE_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_DOCU_RELEVANT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Doku-relevant", databaseDescription);


        // DAIMLER-9827, ctDA_NUTZDOK_SAA, Tabelle für SAAs aus NutzDok
        // ctDA_NUTZDOK_SAA
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_GROUP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_TO_FROM_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Bis/Ab-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_FLASH_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!FLash-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_EVO_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!EVO-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PRIORITY_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Eilt-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_TC_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!TC-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_DISTRIBUTION, 120, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verteiler", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_EVALUATION_FLAG, 120, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auswertekennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_ETS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!ET-Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_LAST_USER, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bearbeiter", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_DOCU_START_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Dokumentations-Start", databaseDescription);
        // SAA extends KEM-/SAA-Base
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PLAN_NUMBER, 25, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Planzahl", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_MANUAL_START_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Manueller Einsatztermin", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_BEGIN_USAGE_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Anlaufdatum", databaseDescription);
        // Eigene StatusFelder
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PROCESSING_STATE, 30, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Verarbeitungsstatus", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PROCESSED_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Verarbeiter Zeitstempel", databaseDescription);
        //DAIMLER-13225
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_ETS_UNCONFIRMED, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!ET-Sicht (nicht bestätigt)", databaseDescription);
        // DAIMLER-14021, ScopeID & KG-Mapping - Anpassungen
        addFieldDefinition(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SCOPE_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Umfang", databaseDescription);


        // DAIMLER-9827, ctDA_NUTZDOK_KEM, Tabelle für KEMs aus NutzDok
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_GROUP, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Gruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_TO_FROM_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Bis/Ab-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_FLASH_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!FLash-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_EVO_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!EVO-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PRIORITY_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Eilt-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_TC_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!TC-Kennung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_DISTRIBUTION, 120, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Verteiler", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_EVALUATION_FLAG, 120, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Auswertekennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_ETS, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!ET-Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_LAST_USER, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bearbeiter", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_DOCU_START_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Dokumentations-Start", databaseDescription);
        // KEM extends KEM-/SAA-Base
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_DOCU_TEAM, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Doku-Team", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_DOCU_USER, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bearbeiter", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_REMARK, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkungstext", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_SIMPLIFIED_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Vereinfacht", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PAPER_FLAG, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!Papierform", databaseDescription);
        // Eigene StatusFelder
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PROCESSING_STATE, 30, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Verarbeitungsstatus", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PROCESSED_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Verarbeiter Zeitstempel", databaseDescription);
        // DAIMLER-10994, KEM-Verarbeitung aus NutzDok: PEM-Felder hinzufügen
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PEM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PEM_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!PEM-Termin", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PEM_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!PEM-Status", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_MANUAL_START_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Manueller Einsatztermin", databaseDescription);
        //DAIMLER-13225
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_ETS_UNCONFIRMED, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feSetOfEnum, false, false, "!!ET-Sicht (nicht bestätigt)", databaseDescription);
        // DAIMLER-14021, ScopeID & KG-Mapping - Anpassungen
        addFieldDefinition(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_SCOPE_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Umfang", databaseDescription);


        // DAIMLER-10050 SAP.MBS: Import "Navigationsstruktur"
        // ctDA_STRUCTURE_MBS                  = ", iPartsDataBaseDescriptionDA_STRUCTURE_MBS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Obere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR_SUFFIX, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Suffix obere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SORT, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sortierung", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_KEM_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_KEM_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Untere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR_SUFFIX, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Suffix untere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Coderegel", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR_TEXT, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Text", databaseDescription);
        addFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_CTT_QUANTITY_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!CTT Aussteuerung", databaseDescription);


        // DAIMLER-10127, SAP.MBS, Import Stückliste
        // ctDA_PARTSLIST_MBS
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Obere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_POS, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Position", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SORT, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Sortierung", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_FROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_TO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabedatum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Untere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR_SUFFIX, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Suffix untere Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_QUANTITY_FLAG, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Mengeneinheit", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Coderegel", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR_TEXT, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Text", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_REMARK_ID, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkungsziffer", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_REMARK_TEXT, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkungsziffertext", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_WW_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_WW_TEXT, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise Beschreibung", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SERVICE_CONST_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Kennzeichen Leitungsbaukasten", databaseDescription);
        addFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_CTT_QUANTITY_FLAG, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!CTT Aussteuerung", databaseDescription);


        // DAIMLER-10101, SAA-Arbeitsvorrat, Manuell Autorenauftragsstatus pFLegen
        // ctDA_WB_SAA_STATES
        addFieldDefinition(TABLE_DA_WB_SAA_STATES, FIELD_WBS_MODEL_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_STATES, FIELD_WBS_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_STATES, FIELD_WBS_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_STATES, FIELD_WBS_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_STATES, FIELD_WBS_DOCU_RELEVANT, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Doku-relevant", databaseDescription);


        // DAIMLER-10131, PRIMUS, Import der Hinweise (Mitlieferteile+Ersetzungen) aus der MQ-Versorgung
        // ctDA_PRIMUS_REPLACE_PART, Ersetzungen aus PRIMUS
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Vorgänger Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_SUCCESSOR_PARTNO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Nachfolger Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_BRAND, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Marke", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_PSS_CODE_FORWARD, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hinweiscode vorwärts", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_PSS_CODE_BACK, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hinweiscode rückwärts", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_PSS_INFO_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Info-Typ des Hinweises", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_LIFECYCLE_STATE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Status des Teilehinweises", databaseDescription);

        // ctDA_PRIMUS_INCLUDE_PART, Mitlieferteile aus PRIMUS
        addFieldDefinition(TABLE_DA_PRIMUS_INCLUDE_PART, FIELD_PIP_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Vorgänger Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_INCLUDE_PART, FIELD_PIP_INCLUDE_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Mitlieferteil Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_INCLUDE_PART, FIELD_PIP_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);


        // DAIMLER-10135, Webservice zur Anlage + Bearbeitung von Bemerkungstexten zu SAA/KEMs
        // ctDA_NUTZDOK_REMARK, Neue und geänderte Bemerkungstexte bzw. Kommentar-Dokumente zu SAAs oder KEMs aus NutzDok.
        addFieldDefinition(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REF_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Eindeutige Kennzeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REF_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Bemerkungs ID", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_LAST_USER, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bearbeiter", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_LAST_MODIFIED, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Letzte Änderung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REMARK, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Bemerkungsdokument", databaseDescription);


        // DAIMLER-10318, Ident-Rückmeldungen aus ePEP (elektronischer ProduktionsEinsatzProzess)
        // ctDA_KEM_RESPONSE_DATA
        addFieldDefinition(TABLE_DA_KEM_RESPONSE_DATA, FIELD_KRD_FACTORY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Werk", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_RESPONSE_DATA, FIELD_KRD_KEM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_RESPONSE_DATA, FIELD_KRD_FIN, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!FIN", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_RESPONSE_DATA, FIELD_KRD_FIN_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!FIN-Meldedatum", databaseDescription);
        addFieldDefinition(TABLE_DA_KEM_RESPONSE_DATA, FIELD_KRD_KEM_UNKNOWN, defaultBoolLen, EtkFieldLengthType.flBool, EtkFieldType.feBoolean, false, false, "!!GPS-KEM unbekannt", databaseDescription);


        // DAIMLER-10570, SAA-Arbeitsvorrat EDS/BCS und SAP.MBS: Performance Optimierung,
        // Tabelle zur Speicherung MIN/MAX-Freigabedatum zu Baumuster + SAA
        // ctDA_WB_SAA_CALCULATION                = ", iPartsDataBaseDescriptionDA_WB_SAA_CALCULATION", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SOURCE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Datenquelle", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MIN_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Kleinstes KEM-Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MAX_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Größtes KEM-Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Coderegel", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_FACTORIES, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!EDS Werke", databaseDescription);

        // DAIMLER-11044, Truck: Import Zuordnung Dokumentationsumfänge zum Dienstleister
        // Extent of documentation to Supplier Mapping
        // ctDA_WB_SUPPLIER_MAPPING              = ", iPartsDataBaseDescriptionDA_WB_SUPPLIER_MAPPING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_WB_SUPPLIER_MAPPING, FIELD_DWSM_MODEL_TYPE_ID, defaultTextKurzLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Typkennzahl", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SUPPLIER_MAPPING, FIELD_DWSM_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SUPPLIER_MAPPING, FIELD_DWSM_KG_FROM, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG von", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SUPPLIER_MAPPING, FIELD_DWSM_KG_TO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG bis", databaseDescription);
        addFieldDefinition(TABLE_DA_WB_SUPPLIER_MAPPING, FIELD_DWSM_SUPPLIER_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Lieferantennummer", databaseDescription);


        // DAIMLER-11300, StarParts-Teile nur noch in erlaubten Ländern ausgeben
        // Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen:
        // ctDA_COUNTRY_VALID_SERIES = ", iPartsDataBaseDescriptionDA_COUNTRY_VALID_SERIES", iPartsDataBaseDescription;
        // Baureihe
        addFieldDefinition(TABLE_DA_COUNTRY_VALID_SERIES, FIELD_DCVS_SERIES_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        // Ländercode, gültig in Land, CountryISO3166
        addFieldDefinition(TABLE_DA_COUNTRY_VALID_SERIES, FIELD_DCVS_COUNTRY_CODE, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!gültig in", databaseDescription);

        // Eine weitere Einschränkung: (StarPart-) Bauteile pro Land, die trotzdem (!)NICHT(!) ausgegeben werden dürfen!
        // ctDA_COUNTRY_INVALID_PARTS = ", iPartsDataBaseDescriptionDA_COUNTRY_INVALID_PARTS", iPartsDataBaseDescription;
        // Teilenummer
        addFieldDefinition(TABLE_DA_COUNTRY_INVALID_PARTS, FIELD_DCIP_PART_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        // Ländercode, UNgültig in Land, CountryISO3166
        addFieldDefinition(TABLE_DA_COUNTRY_INVALID_PARTS, FIELD_DCIP_COUNTRY_CODE, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!ungültig in", databaseDescription);


        // DAIMLER-11425, PSK: PSK-Varianten am Produkt definieren
        // ctDA_PSK_PRODUCT_VARIANTS = ", iPartsDataBaseDescriptionDA_PSK_PRODUCT_VARIANTS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_PSK_PRODUCT_VARIANTS, FIELD_DPPV_PRODUCT_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PSK_PRODUCT_VARIANTS, FIELD_DPPV_VARIANT_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Varianten ID", databaseDescription);
        addFieldDefinition(TABLE_DA_PSK_PRODUCT_VARIANTS, FIELD_DPPV_NAME1, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung1", databaseDescription);
        addFieldDefinition(TABLE_DA_PSK_PRODUCT_VARIANTS, FIELD_DPPV_NAME2, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung2", databaseDescription);
        addFieldDefinition(TABLE_DA_PSK_PRODUCT_VARIANTS, FIELD_DPPV_SUPPLY_NUMBER, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Versorgungsnummer", databaseDescription);


        // DAIMLER-11632, ShoppingCart, Import Referenz auf hoch frequentierte TUs
        // ctDA_TOP_TUS = ", iPartsDataBaseDescriptionDA_TOP_TUS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_TOP_TUS, FIELD_DTT_PRODUCT_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_TOP_TUS, FIELD_DTT_COUNTRY_CODE, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Land", databaseDescription);
        addFieldDefinition(TABLE_DA_TOP_TUS, FIELD_DTT_KG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Konstruktionsgruppe", databaseDescription);
        addFieldDefinition(TABLE_DA_TOP_TUS, FIELD_DTT_TU, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Technischer Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_TOP_TUS, FIELD_DTT_RANK, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Rang", databaseDescription);


        // DAIMLER-11672, Leitungssatzbaukästen
        // ctDA_WIRE_HARNESS = ", iPartsDataBaseDescriptionDA_WIRE_HARNESS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Leitungssatz", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_REF, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Referenznummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONNECTOR_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Steckernummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SUB_SNR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_POS, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR_TYPE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Sachnummerntyp", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_DATASET_DATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Datenstand Kontakt", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_PART_DATASET_DATE, defaultDateLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDate, false, false, "!!Datenstand Teil", databaseDescription);
        addFieldDefinition(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_ADD_TEXT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Ergänzungstext", databaseDescription);


        // DAIMLER-11908, DIALOG Urladung/Änderungsdienst: Import BCTG, Generic Part und Variantennummer zur Verwendung
        // ctDA_GENERIC_PART = ", iPartsDataBaseDescriptionDA_GENERIC_PART", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_GUID, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Datensatzkenner", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_POSE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsnummer Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_SESI, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Strukturerzeugende Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_POSP, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Positionsnummer Produktion", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_POSV, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsvariante", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_WW, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wahlweise", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_ETZ, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ersatzteilzähler", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_AA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_SDATA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum von", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_SDATB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_PARTNO, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_GENERIC_PARTNO, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Generische Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_VARIANTNO, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Variantennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_PART, FIELD_DGP_SOLUTION, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Solution", databaseDescription);


        // DAIMLER-11961, Import von EinPAS-Attributen aus CEMaT
        // ctDA_MODULE_CEMAT = ", iPartsDataBaseDescriptionDA_MODULE_CEMAT", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_MODULE_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modulnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_LFDNR, defaultLfdNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_PARTNO, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_EINPAS_HG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-HG", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_EINPAS_G, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-G", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_EINPAS_TU, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!EINPAS-TU", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CEMAT, FIELD_DMC_VERSIONS, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Versionen", databaseDescription);


        // DAIMLER-12170, DIALOG: Urladung/Änderungsdienst Import generischer Verbauort (POS)
        // ctDA_GENERIC_INSTALL_LOCATION = ", iPartsDataBaseDescriptionDA_GENERIC_INSTALL_LOCATION", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SERIES, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_POSE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsnummer Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SDA, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum ab", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SDB, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!KEM Datum bis", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SESI, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Strukturerzeugende Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_FED, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Federführung", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_HIERARCHY, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Strukturstufe", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_POS_KEY, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsschlüssel", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_MK_SIGN, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Musskomponenten-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_PET_SIGN, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Positionsersatzteilkenner", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_PWK_SIGN, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Warenkorb-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_PTK_SIGN, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Teileklassifikations-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_INFO_TEXT, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Informationstext", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_DELETE_SIGN, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Löschkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_SPLIT_SIGN, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Split-Kennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_GENERIC_INSTALL_LOCATION, FIELD_DGIL_GEN_INSTALL_LOCATION, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Generischer Verbauort", databaseDescription);

        // DAIMLER-12352, DAILOG: Auslauftermine für Werke festlegen und berücksichtigen
        // ctDA_SERIES_EXPDATE = ", iPartsDataBaseDescriptionDA_SERIES_EXPDATE", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_AA, 5, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Ausführungsart", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_FACTORY_NO, 10, EtkFieldLengthType.flTextLang, EtkFieldType.feString, false, false, "!!Werksnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_EXP_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Auslauftermin", databaseDescription);

        // DAIMLER-12594, V-Teile (vereinfachte Teile) innerhalb von Leitungssatz-BKs
        // ctDA_WH_SIMPLIFIED_PARTS = ", iPartsDataBaseDescriptionDA_WH_SIMPLIFIED_PARTS", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_WH_SIMPLIFIED_PARTS, FIELD_DWHS_PARTNO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Einzelteil", databaseDescription);
        addFieldDefinition(TABLE_DA_WH_SIMPLIFIED_PARTS, FIELD_DWHS_SUCCESSOR_PARTNO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Nachfolger", databaseDescription);


        // DAIMLER-12988, Inhalte von GetProductClasses auf Basis des Tokens filtern
        // Mapping von einer Berechtigung auf eine AS-Produktklasse
        // ctDA_AC_PC_PERMISSION_MAPPING = ", iPartsDataBaseDescriptionDA_AC_PC_PERMISSION_MAPPING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_AC_PC_PERMISSION_MAPPING, FIELD_DPPM_BRAND, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feString, false, false, "!!Marke", databaseDescription);
        addFieldDefinition(TABLE_DA_AC_PC_PERMISSION_MAPPING, FIELD_DPPM_ASSORTMENT_CLASS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sortimentsklasse", databaseDescription);
        addFieldDefinition(TABLE_DA_AC_PC_PERMISSION_MAPPING, FIELD_DPPM_AS_PRODUCT_CLASS, defaultTextKurzLen, EtkFieldLengthType.flTextKurz, EtkFieldType.feEnum, false, false, "!!Aftersales Produktklasse", databaseDescription);


        // DAIMLER-12994, Schnittstellenanpassung aufgrund CORTEX, Ablösung der Nutzdok-Technik
        // ctDA_CORTEX_IMPORT_DATA = ", iPartsDataBaseDescriptionDA_CORTEX_IMPORT_DATA", iPartsDataBaseDescription;
//        addFieldDefinition(TABLE_DA_CORTEX_IMPORT_DATA, FIELD_DCI_CREATION_TS, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Erstellungsdatum", databaseDescription);
        addFieldDefinition(TABLE_DA_CORTEX_IMPORT_DATA, FIELD_DCI_ENDPOINT_NAME, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!WS-Endpoint", databaseDescription);
        addFieldDefinition(TABLE_DA_CORTEX_IMPORT_DATA, FIELD_DCI_IMPORT_METHOD, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Import Methode", databaseDescription);
        addFieldDefinition(TABLE_DA_CORTEX_IMPORT_DATA, FIELD_DCI_STATUS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Status", databaseDescription);
        addFieldDefinition(TABLE_DA_CORTEX_IMPORT_DATA, FIELD_DCI_DATA, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feBlob, false, false, "!!Daten", databaseDescription);


        // DAIMLER-13443, Sachnummer zu Lieferantensachnummer aus SRM
        // ctDA_SUPPLIER_PARTNO_MAPPING = ", iPartsDataBaseDescriptionDA_SUPPLIER_PARTNO_MAPPING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_SUPPLIER_PARTNO_MAPPING, FIELD_DSPM_PARTNO, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SUPPLIER_PARTNO_MAPPING, FIELD_DSPM_SUPPLIER_PARTNO, 80, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Lieferantensachnummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SUPPLIER_PARTNO_MAPPING, FIELD_DSPM_SUPPLIER_NO, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Lieferantennummer", databaseDescription);
        addFieldDefinition(TABLE_DA_SUPPLIER_PARTNO_MAPPING, FIELD_DSPM_SUPPLIER_NAME, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Lieferantenname", databaseDescription);
        // DAIMLER-14056, Anpassung Suche nach Lieferantensachnummer
        addFieldDefinition(TABLE_DA_SUPPLIER_PARTNO_MAPPING, FIELD_DSPM_SUPPLIER_PARTNO_PLAIN, 80, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Lieferantensachnummer ohne Leerzeichen", databaseDescription);


        // DAIMLER-13464, Motoröle: Zuordnung Motorbaumuster zu Spezifikation mit Ölmenge
        // ctDA_MODEL_OIL = ", iPartsDataBaseDescriptionDA_MODEL_OIL", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_MODEL_OIL, FIELD_DMO_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_OIL, FIELD_DMO_SPEC_VALIDITY, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Spezifikation", databaseDescription);
        // DAIMLER-14701, Typ in DA_MODEL_OIL + DA_MODEL_OIL_QUANTITY einführen, PK-Erweiterung
        addFieldDefinition(TABLE_DA_MODEL_OIL, FIELD_DMO_SPEC_TYPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Typ", databaseDescription);
        // veraltet
//        addFieldDefinition(TABLE_DA_MODEL_OIL, "DMO_QUANTITY", 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ölwechselmenge mit Filter", databaseDescription);
        // DAIMLER-13780, Motoröle: Erweiterung des benutzerdefinierten Imports um Coderegel
        addFieldDefinition(TABLE_DA_MODEL_OIL, FIELD_DMO_CODE_VALIDITY, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Coderegel", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_OIL, FIELD_DMO_TEXT_ID, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!TextID", databaseDescription);
        // DAIMLER-14917, mbSpecs um SAE Klasse erweitern
        addFieldDefinition(TABLE_DA_MODEL_OIL, FIELD_DMO_SAE_CLASS, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!SAE-Klasse", databaseDescription);


        // DAIMLER-14245, Öl-Nachfüllmenge über eigene DB-Tabelle neu ermitteln
        // ctDA_MODEL_OIL_QUANTITY = ", iPartsDataBaseDescriptionDA_MODEL_OIL_QUANTITY", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_MODEL_OIL_QUANTITY, FIELD_DMOQ_MODEL_NO, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumusternummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_OIL_QUANTITY, FIELD_DMOQ_CODE_VALIDITY, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Coderegel", databaseDescription);
        // DAIMLER-14701, Typ in DA_MODEL_OIL + DA_MODEL_OIL_QUANTITY einführen, PK-Erweiterung
        addFieldDefinition(TABLE_DA_MODEL_OIL_QUANTITY, FIELD_DMOQ_SPEC_TYPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Typ", databaseDescription);
        //
        addFieldDefinition(TABLE_DA_MODEL_OIL_QUANTITY, FIELD_DMOQ_IDENT_TO, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Motoridentnummer bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_OIL_QUANTITY, FIELD_DMOQ_IDENT_FROM, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Motoridentnummer ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_OIL_QUANTITY, FIELD_DMOQ_QUANTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Ölwechselmenge mit Filter", databaseDescription);


        // DAIMLER-13455, Pseudo-Einsatztermine pro PEM und Werk
        // ctDA_PSEUDO_PEM_DATE = ", iPartsDataBaseDescriptionDA_PSEUDO_PEM_DATE", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_PSEUDO_PEM_DATE, FIELD_DPD_PEM_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Pseudo-Einsatztermin", databaseDescription);


        // DAIMLER-13685, PPUA (Parts Potetinal Usage Analysis) Daten
        // Info wie oft ein Teil in einer Baureihe in einem Jahr verbaut wurde bzw. wie oft eine Baureihe in einem Jahr gebaut wurde
        // ctDA_PPUA = ", iPartsDataBaseDescriptionDA_PPUA", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_PARTNO, 100, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_REGION, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Region", databaseDescription);
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_SERIES, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_ENTITY, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Qualität", databaseDescription);
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_TYPE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_YEAR, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Jahr", databaseDescription);
        addFieldDefinition(TABLE_DA_PPUA, FIELD_DA_PPUA_VALUE, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Wert", databaseDescription);


        // DAIMLER-13926, ScopeID & KG-Mapping importieren
        // ctDA_SCOPE_KG_MAPPING = ", iPartsDataBaseDescriptionDA_SCOPE_KG_MAPPING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_SCOPE_KG_MAPPING, FIELD_DSKM_SCOPE_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Umfang", databaseDescription);
        addFieldDefinition(TABLE_DA_SCOPE_KG_MAPPING, FIELD_DSKM_KG, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KG", databaseDescription);


        // DAIMLER-14199, Mapping-Tabelle für Ergänzungstexte zum GenVO
        // ctDA_GENVO_SUPP_TEXT = ", iPartsDataBaseDescriptionDA_GENVO_SUPP_TEXT", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_GENVO_SUPP_TEXT, FIELD_DA_GENVO_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Generischer Verbauort", databaseDescription);
        addFieldDefinition(TABLE_DA_GENVO_SUPP_TEXT, FIELD_DA_GENVO_DESCR, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, true, false, "!!Bezeichnung", databaseDescription);


        // DAIMLER-15019, Tabelle für Links-Rechts-Pärchen zu GenVO
        // ctDA_GENVO_PAIRING = ", iPartsDataBaseDescriptionDA_GENVO_PAIRING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_GENVO_PAIRING, FIELD_DGP_GENVO_L, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!GenVO links", databaseDescription);
        addFieldDefinition(TABLE_DA_GENVO_PAIRING, FIELD_DGP_GENVO_R, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!GenVO rechts", databaseDescription);


        // DAIMLER-14190, CORTEX-Anbindung: Bemerkungen aus NutzDok
        // ctDA_NUTZDOK_ANNOTATION = ", iPartsDataBaseDescriptionDA_NUTZDOK_ANNOTATION", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_REF_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Eindeutige Kennzeichnung", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_REF_TYPE, 10, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_ETS, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!ET-Sicht", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_LFDNR, 10, EtkFieldLengthType.flLfdnr, EtkFieldType.feString, false, false, "!!Laufende Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_DATE, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Datum", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_AUTHOR, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Autor", databaseDescription);
        addFieldDefinition(TABLE_DA_NUTZDOK_ANNOTATION, FIELD_DNA_ANNOTATION, 300, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Bemerkung", databaseDescription);

        //DAIMLER-14530 Import HMO-SAA-Mapping
        // ctDA_HMO_SAA_MAPPING = ", iPartsDataBaseDescriptionDA_HMO_SAA_MAPPING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!HMO Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!SAA Nummer", databaseDescription);


        // DAIMLER-14571, TB.f Umstellung auf neue Produktstruktur, Mapping von alter auf neue EDS/BCS Struktur
        // ctDA_MODEL_ELEMENT_USAGE = ", iPartsDataBaseDescriptionDA_MODEL_ELEMENT_USAGE", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_MODELNO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baumuster", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_MODULE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_SUB_MODULE, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Submodul", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_POS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Position", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_STEERING, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_LEGACY_NUMBER, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Unterscheidungs-Nummer", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_REVFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_REVTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_KEMFROM, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_KEMTO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!KEM bis", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_RELEASE_FROM, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_RELEASE_TO, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Freigabetermin bis", databaseDescription);
        // UPPERCASE Feld für Indizierung:
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_SUB_ELEMENT, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, true, "!!Unterelement", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_RFG, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feEnum, false, false, "!!Reifegrad", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_QUANTITY, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Menge", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_PGKZ, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Produktgruppenkennzeichen", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_CODE, 400, EtkFieldLengthType.flUserDefined, EtkFieldType.feMemo, false, false, "!!Code", databaseDescription);
        addFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_PLANTSUPPLY, 200, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Werke", databaseDescription);


        // DAIMLER-14568, TruckBOM.foundation: Umstellung auf neue Produktstruktur - Bestehende TB.f Importer anpassen
        // DAIMLER-14574, Neue Tabelle für die EDS/BCS Struktur: DA_MODULE_CATEGORY (Ersatz für: DA_OPS_GROUP)
        // ctDA_MODULE_CATEGORY = ", iPartsDataBaseDescriptionDA_MODULE_CATEGORY", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_MODULE_CATEGORY, FIELD_DMC_MODULE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CATEGORY, FIELD_DMC_AS_FROM, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Änderungsstand ab", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CATEGORY, FIELD_DMC_DESC, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_MODULE_CATEGORY, FIELD_DMC_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);

        // DAIMLER-14574, Neue Tabelle für die EDS/BCS Struktur: DA_SUB_MODULE_CATEGORY (Ersatz für: DA_OPS_SCOPE)
        // ctDA_SUB_MODULE_CATEGORY = ", iPartsDataBaseDescriptionDA_SUB_MODULE_CATEGORY", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_SUB_MODULE_CATEGORY, FIELD_DSMC_SUB_MODULE, defaultBenennungLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Sub-Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_SUB_MODULE_CATEGORY, FIELD_DSMC_DESC, defaultBenennungLen, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Benennung", databaseDescription);
        addFieldDefinition(TABLE_DA_SUB_MODULE_CATEGORY, FIELD_DSMC_PICTURE, 24, EtkFieldLengthType.flTextLang, EtkFieldType.fePicture, false, false, "!!Strukturbild", databaseDescription);


        // DAIMLER-14629, Importer für SPK-Mapping Entwicklung zu Aftersales
        // ctDA_SPK_MAPPING = ", iPartsDataBaseDescriptionDA_SPK_MAPPING", iPartsDataBaseDescription;
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_SERIES_NO, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihe", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_HM, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Hauptmodul", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_M, 20, EtkFieldLengthType.flUserDefined, EtkFieldType.feString, false, false, "!!Modul", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_KURZ_E, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kurzbenennung Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_KURZ_AS, 20, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Kurzbenennung Retail", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_LANG_E, 20, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Langbenennung Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_LANG_AS, 20, EtkFieldLengthType.flTextLang, EtkFieldType.feString, true, false, "!!Langbenennung Retail", databaseDescription);
        // DAIMLER-15199, Importer für SPK-Mapping Entwicklung - AS erweitern, Tabellenerweiterung + PK Erweiterung
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_CONNECTOR_E, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Steckernummer Entwicklung", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_CONNECTOR_AS, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Steckernummer AS", databaseDescription);
        addFieldDefinition(TABLE_DA_SPK_MAPPING, FIELD_SPKM_STEERING, 3, EtkFieldLengthType.flUserDefined, EtkFieldType.feEnum, false, false, "!!Lenkung", databaseDescription);

        // TODO: Differences to the PROD DB that need to be removed in a future sprint
        addFieldDefinition("DA_PRODUCT_SERIES", FIELD_DPS_PRODUCT_NO, 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Produktnummer", databaseDescription);
        addFieldDefinition("DA_PRODUCT_SERIES", "DPS_SERIES_NO", 50, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Baureihennummer", databaseDescription);


        // DAIMLER-16457, PRIMUS: Import der WW-Hinweise aus MQ-Versorgung
        addFieldDefinition(TABLE_DA_PRIMUS_WW_PART, FIELD_PWP_ID, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false,false, "!!WW-Sachverhalt", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_WW_PART, FIELD_PWP_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_WW_PART, FIELD_PWP_WW_PART_NO, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!WW-Teilenummer", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_WW_PART, FIELD_PWP_WW_TYPE, defaultMatNrLen, EtkFieldLengthType.flMatNr, EtkFieldType.feString, false, false, "!!Typ", databaseDescription);
        addFieldDefinition(TABLE_DA_PRIMUS_WW_PART, FIELD_PWP_TIMESTAMP, defaultDateTimeLen, EtkFieldLengthType.flUserDefined, EtkFieldType.feDateTime, false, false, "!!Zeitstempel", databaseDescription);
    }

    public static boolean skipValidationForTable(String tableName) {
        return TABLES_TO_SKIP_DURING_DB_VALIDATION.contains(tableName.toLowerCase());
    }
}