/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.EtkDatabaseDescription;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPoolVariants;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsDataPool;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.MultipleInputToOutputStream;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.inputdialog.InputDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.framework.utils.forms.CopyTextWindow;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFileCoding;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortBetweenHelper;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Hilfsklasse für spezielle Datenbankaktionen in iPartsEdit.
 */
public class EditDatabaseHelper implements iPartsConst {

    /**
     * Datenbankbereinigung: Löschen der DIALOG Konstruktions-Daten aus der Datenbank.
     * Größtenteils werden die kompletten Tabellen geleert.
     * Wenn Kenner vorhanden sind, woher die Daten kamen, nur die von "DIALOG" kommenden Daten löschen, alle anderen bleiben erhalten.
     *
     * @param project Projekt, das zum Löschen verwendet werden soll
     * @return
     */
    public static boolean eraseDialogConstructionDataFromDB(EtkProject project) {
        ExecuteSQLStatementsRunnable runnable = new ExecuteSQLStatementsRunnable() {

            @Override
            public boolean executeSQLStatements(EtkProject project, DBBase db, EtkMessageLog messageLog) {
                final int maxPos = 5;
                final String originDIALOG = iPartsImportDataOrigin.DIALOG.getOrigin();

                messageLog.fireMessage("!!Lösche Stücklisten...");
                messageLog.fireProgress(0, maxPos, "", false, true);
                db.delete(TABLE_DA_DIALOG); // DELETE FROM DA_DIALOG;
                db.delete(TABLE_DA_DIALOG_PARTLIST_TEXT); // DELETE FROM DA_DIALOG_ADD_DATA;
                db.delete(TABLE_DA_DIALOG_POS_TEXT); // DELETE FROM DA_DIALOG_POS_TEXT;

                messageLog.fireMessage("!!Lösche Code...");
                messageLog.fireProgress(1, maxPos, "", false, true);
                // DELETE FROM DA_CODE WHERE DC_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_CODE, new String[]{ FIELD_DC_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_CODE, new String[]{ FIELD_DC_SOURCE }, new String[]{ "" });

                messageLog.fireMessage("!!Lösche Baumuster...");
                messageLog.fireProgress(2, maxPos, "", false, true);
                db.delete(TABLE_DA_VS2US_RELATION); // DELETE FROM DA_VS2US_RELATION;
                db.delete(TABLE_DA_MODEL_DATA); // DELETE FROM DA_MODEL_DATA;

                // DELETE FROM DA_MODEL_PROPERTIES WHERE DMA_SOURCE IN ('DIALOG', '', ' ', NULL);
                db.delete(TABLE_DA_MODEL_PROPERTIES, new String[]{ FIELD_DMA_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_MODEL_PROPERTIES, new String[]{ FIELD_DMA_SOURCE }, new String[]{ "" });

                // DELETE FROM DA_MODEL WHERE DM_SOURCE IN ('DIALOG', '', ' ', NULL);
                db.delete(TABLE_DA_MODEL, new String[]{ FIELD_DM_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_MODEL, new String[]{ FIELD_DM_SOURCE }, new String[]{ "" });

                messageLog.fireMessage("!!Lösche HM/M/SM...");
                messageLog.fireProgress(3, maxPos, "", false, true);
                db.delete(TABLE_DA_HMMSM); // DELETE FROM DA_HMMSM;
                db.delete(TABLE_DA_HMMSMDESC); // DELETE FROM DA_HMMSMDESC;

                // -- DELETE FROM DA_EINPASHMMSM; Benutzerdefinierter Import, <<============================= aktuell NICHT löschen!
//                db.delete(TABLE_DA_EINPASHMMSM);

                messageLog.fireMessage("!!Lösche Werkseinsatzdaten...");
                messageLog.fireProgress(4, maxPos, "", false, true);
                // DELETE FROM DA_FACTORY_DATA WHERE DFD_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SOURCE }, new String[]{ "" });
                // DELETE FROM DA_RESPONSE_DATA WHERE DRD_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_SOURCE }, new String[]{ "" });
                // DELETE FROM DA_RESPONSE_SPIKES WHERE DRS_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_SOURCE }, new String[]{ "" });

                messageLog.fireMessage("!!Lösche Farbvariantendaten...");
                messageLog.fireProgress(5, maxPos, "", false, true);
                // DELETE FROM DA_COLORTABLE_FACTORY WHERE DCCF_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_SOURCE }, new String[]{ "" });
                // DELETE FROM DA_COLORTABLE_DATA WHERE DCTD_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_COLORTABLE_DATA, new String[]{ FIELD_DCTD_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_COLORTABLE_DATA, new String[]{ FIELD_DCTD_SOURCE }, new String[]{ "" });
                // DELETE FROM DA_COLORTABLE_CONTENT WHERE DCTC_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_COLORTABLE_CONTENT, new String[]{ FIELD_DCTC_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_COLORTABLE_CONTENT, new String[]{ FIELD_DCTC_SOURCE }, new String[]{ "" });
                // DELETE FROM DA_COLORTABLE_PART WHERE DCTP_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_SOURCE }, new String[]{ originDIALOG });
                db.delete(TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_SOURCE }, new String[]{ "" });
                // DELETE FROM DA_COLOR_NUMBER WHERE DCN_SOURCE IN ('DIALOG', '');
                db.delete(TABLE_DA_COLOR_NUMBER, new String[]{ FIELD_DCN_SOURCE }, new String[]{ originDIALOG }); // "DIALOG"
                db.delete(TABLE_DA_COLOR_NUMBER, new String[]{ FIELD_DCN_SOURCE }, new String[]{ "" });

                return true;
            }
        };
        return executeSQLStatements(project, "!!Alle DIALOG-Konstruktionsdaten löschen", "!!Wollen Sie alle DIALOG-Konstruktionsdaten inkl. HM/M/SM-Struktur, dazugehöriger Konstruktions- und After-Sales-Baumuster, Werkseinsatzdaten, Rückmeldedaten und Farbtabellen (inkl. Werkseinsatzdaten) aus der Datenbank löschen?",
                                    "!!Wollen Sie wirklich löschen?", runnable, true);
    }

    /**
     * Überprüft die Datenbank-Konsistenz inkl. Korrektur/Löschen von fehlerhaften Altdaten
     *
     * @param project Projekt, das zum Korrigieren/Löschen verwendet werden soll
     * @return
     */
    public static boolean checkDBConsistency(EtkProject project) {
        ExecuteSQLStatementsRunnable runnable = new ExecuteSQLStatementsRunnable() {

            @Override
            public boolean executeSQLStatements(EtkProject project, DBBase db, EtkMessageLog messageLog) {
//                messageLog.fireMessage("!!Lösche veraltete SA-Module...");
//
//                // Veraltete SA-Module suchen
//                iPartsDataModuleList moduleList = iPartsDataModuleList.loadDataForModuleNumber(project, "SA-*");
//                List<String> saModuleNumbersToDelete = new ArrayList<String>();
//                for (iPartsDataModule dataModule : moduleList) {
//                    String moduleNumber = dataModule.getAsId().getModuleNumber();
//                    // Suche SA-Module ohne Z bzw. anderen Buchstaben am Anfang der SA-Nummer
//                    if ((moduleNumber.length() >= 4) && !Character.isLetter(moduleNumber.charAt(3))) {
//                        saModuleNumbersToDelete.add(moduleNumber);
//                    }
//                }
//
//                // Veraltete SA-Module löschen
//                int maxPos = saModuleNumbersToDelete.size();
//                int pos = 0;
//                for (String saModuleNumber : saModuleNumbersToDelete) {
//                    iPartsDataAssembly assembly = new iPartsDataAssembly(project, saModuleNumber, "", false);
//                    assembly.delete_iPartsAssembly();
//                    pos++;
//                    messageLog.fireMessage(TranslationHandler.translate("!!Veraltetes SA-Modul \"%1\" gelöscht", saModuleNumber));
//                    messageLog.fireProgress(pos, maxPos, "", false, true);
//                }
//
//
//                messageLog.fireMessage("");
//                messageLog.fireMessage(TranslationKeys.LINE_SEPARATOR);
//                messageLog.fireMessage("");


                // KATALOG.K_SOURCE_CONTEXT von Stücklisteneinträgen korrigieren
//                messageLog.fireMessage("!!Korrigiere Feldinhalt KATALOG.K_SOURCE_CONTEXT von veralteten Stücklisteneinträgen...");
//
//                // Stücklisteneinträge mit K_SOURCE_TYPE = K_SOURCE_TYPE_DIALOG suchen
//                EtkDataPartListEntryList partListEntryList = new EtkDataPartListEntryList();
//                partListEntryList.searchWithWildCardsSortAndFill(project, new String[] { FIELD_K_SOURCE_TYPE },
//                                                                 new String[] { K_SOURCE_TYPE_DIALOG }, null,
//                                                                 DBDataObjectList.LoadType.COMPLETE,
//                                                                 DBActionOrigin.FROM_DB);
//
//                // Stücklisteneinträge mit K_SOURCE_TYPE = K_SOURCE_TYPE_DIALOG überprüfen
//                messageLog.fireMessage(TranslationHandler.translate("!!%1 Stücklisteneinträge mit KATALOG.K_SOURCE_TYPE = \"%2\" werden überprüft...",
//                                                                    String.valueOf(partListEntryList.size()), K_SOURCE_TYPE_DIALOG));
//                maxPos = partListEntryList.size();
//                messageLog.fireProgress(0, maxPos, "", false, true);
//                pos = 0;
//                int correctedPartListEntriesCounter = 0;
//                for (EtkDataPartListEntry partListEntry : partListEntryList) {
//                    String oldSourceContext = partListEntry.getFieldValue(FIELD_K_SOURCE_CONTEXT);
//                    String[] split = oldSourceContext.split(K_SOURCE_CONTEXT_DELIMITER);
//                    if (split.length == 5) { // altes Format mit Baureihe&HM/M/SM
//                        HmMSmId hmMSmId = new HmMSmId(split[1], split[2], split[3], split[4]);
//                        String newSourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsModuleTypes.DialogRetail,
//                                                                                                     hmMSmId);
//                        if (!oldSourceContext.equals(newSourceContext)) { // K_SOURCE_CONTEXT muss korrigiert werden
//                            partListEntry.setFieldValue(FIELD_K_SOURCE_CONTEXT, newSourceContext, DBActionOrigin.FROM_EDIT);
//                            partListEntry.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
//                            correctedPartListEntriesCounter++;
//                        }
//                    }
//                    pos++;
//                    messageLog.fireProgress(pos, maxPos, "", false, true);
//                }
//                messageLog.fireMessage(TranslationHandler.translate("!!%1 veraltete Stücklisteneinträge wurden korrigiert",
//                                                                    String.valueOf(correctedPartListEntriesCounter)));
                return false; // Ist ja alles auskommentiert
            }
        };

        return executeSQLStatements(project, "!!Datenbank-Konsistenz überprüfen", "!!Soll die Datenbank-Konsistenz überprüft werden inkl. Korrigieren und Löschen von fehlerhaften Altdaten?",
                                    null, runnable, true);
    }


    /**
     * Überprüft die Datenbank-Konsistenz inkl. Korrektur/Löschen von fehlerhaften Altdaten
     *
     * @param project Projekt, das zum Korrigieren/Löschen verwendet werden soll
     * @return
     */
    public static boolean checkTextIdConsistency(EtkProject project) {
        ExecuteSQLStatementsRunnable runnable = new ExecuteSQLStatementsRunnable() {

            @Override
            public boolean executeSQLStatements(EtkProject project, DBBase db, EtkMessageLog messageLog) {
                {   // Teste, ob unter einer TextId verschiedene Benennungen gespeichert sind
                    messageLog.fireMessage("!!Suche Text-IDs mit falschen Texten...");

                    // select distinct sprache.s_textid from sprache
                    // where exists (select * from sprache s2 where (sprache.s_textid = s2.s_textid and sprache.s_sprach = s2.s_sprach and (sprache.s_benenn <> s2.s_benenn or sprache.s_benenn_lang not like s2.s_benenn_lang)))
                    // and sprache.s_textid <> ''

                    SQLQuery subQuery = project.getEtkDbs().getDBForTable(TABLE_SPRACHE).getNewQuery();
                    subQuery.select("*").from(new Tables(TABLE_SPRACHE).as(new Tables("s2")));
                    AbstractCondition subQueryWhereCondition = new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make("s2", FIELD_S_TEXTID)))
                            .and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH), Condition.OPERATOR_EQUALS, new Fields(CaseMode.UPPERCASE, TableAndFieldName.make("s2", FIELD_S_SPRACH)), CaseMode.UPPERCASE));


                    if (project.getConfig().getDataBaseVersion() >= 6.2) {
                        // Falls die langen Texte vorhanden sind, diese auch testen

                        // Memos müssen beim SQL-Server mit = abgefragt werden, bei Oracle mit like !!!
                        String operator = Condition.OPERATOR_NOT_LIKE;
                        if (project.getEtkDbs().getDatabaseType(TABLE_SPRACHE) == DatabaseType.MSSQL) {
                            operator = Condition.OPERATOR_NOT_EQUALS;
                        }
                        subQueryWhereCondition = subQueryWhereCondition.and(new ConditionList(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN_LANG), operator, new Fields(TableAndFieldName.make("s2", FIELD_S_BENENN_LANG)))
                                                                                                      .or(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN), Condition.OPERATOR_NOT_EQUALS, new Fields(TableAndFieldName.make("s2", FIELD_S_BENENN))))));
                    } else {
                        subQueryWhereCondition = subQueryWhereCondition.and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN), Condition.OPERATOR_NOT_EQUALS, new Fields(TableAndFieldName.make("s2", FIELD_S_BENENN))));
                    }

                    subQuery.where(subQueryWhereCondition);


                    DBSQLQuery query = project.getEtkDbs().getDBForTable(TABLE_SPRACHE).getNewQuery();
                    query.selectDistinct(new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID))).from(TABLE_SPRACHE);
                    query.where(new Exists(subQuery)
                                        .and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID), Condition.OPERATOR_NOT_EQUALS, "")));


                    DBDataSet dbSet = query.executeQuery();

                    List<String> textIds = new LinkedList<String>();

                    while (dbSet.next()) {
                        textIds.add(dbSet.getStringList().get(0));
                    }
                    dbSet.close();

                    for (String textId : textIds) {
                        project.getDbLayer().reorgTextId(textId);
                        messageLog.fireMessage(TranslationHandler.translate("!!Fehler in Text-ID gefunden:") + " " + textId);
                        project.getEtkDbs().commit();
                        project.getEtkDbs().startTransaction();
                    }
                    messageLog.fireMessage(TranslationHandler.translate("!!%1 fehlerhafte Einträge wurden gefunden und korrigiert.", Long.toString(textIds.size())));
                }

                {   // Teste jetzt, ob in den Referenzen der TextIds Sprachen fehlen
                    messageLog.fireMessage("!!Suche Text-IDs mit fehlenden Sprachen...");

                    // select distinct sprache.s_textid from sprache
                    // inner join sprache s2 on (sprache.s_textid = s2.s_textid and upper(sprache.s_feld) <> upper(s2.s_feld))
                    // where not exists (select * from sprache s3 where (sprache.s_textid = s3.s_textid and upper(sprache.s_sprach) = upper(s3.s_sprach) and upper(s2.s_feld) = upper(s3.s_feld)))
                    // and sprache.s_textid <> ''
                    // Uppercase wegen Performance, H2 findet sonst nicht den richtigen Index


                    SQLQuery subQuery = project.getEtkDbs().getDBForTable(TABLE_SPRACHE).getNewQuery();
                    subQuery.select("*").from(new Tables(TABLE_SPRACHE).as(new Tables("s3")));
                    subQuery.where(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make("s3", FIELD_S_TEXTID)))
                                           .and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH), Condition.OPERATOR_EQUALS, new Fields(CaseMode.UPPERCASE, TableAndFieldName.make("s3", FIELD_S_SPRACH)), CaseMode.UPPERCASE))
                                           .and(new Condition(TableAndFieldName.make("s2", FIELD_S_FELD), Condition.OPERATOR_EQUALS, new Fields(CaseMode.UPPERCASE, TableAndFieldName.make("s3", FIELD_S_FELD)), CaseMode.UPPERCASE)));


                    DBSQLQuery query = project.getEtkDbs().getDBForTable(TABLE_SPRACHE).getNewQuery();
                    query.selectDistinct(new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID))).from(TABLE_SPRACHE);
                    query.join(new InnerJoin(new Tables(TABLE_SPRACHE).as(new Tables("s2")),
                                             new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make("s2", FIELD_S_TEXTID)))
                                                     .and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD), Condition.OPERATOR_NOT_EQUALS, new Fields(CaseMode.UPPERCASE, TableAndFieldName.make("s2", FIELD_S_FELD)), CaseMode.UPPERCASE))));

                    query.where(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID), Condition.OPERATOR_NOT_EQUALS, ""))
                            .andNot(new Exists(subQuery));


                    DBDataSet dbSet = query.executeQuery();

                    List<String> textIds = new LinkedList<String>();

                    while (dbSet.next()) {
                        textIds.add(dbSet.getStringList().get(0));
                    }
                    dbSet.close();

                    for (String textId : textIds) {
                        project.getDbLayer().reorgTextId(textId);
                        messageLog.fireMessage(TranslationHandler.translate("!!Fehler in Text-ID gefunden:") + " " + textId);
                        project.getEtkDbs().commit();
                        project.getEtkDbs().startTransaction();
                    }
                    messageLog.fireMessage(TranslationHandler.translate("!!%1 fehlerhafte Einträge wurden gefunden und korrigiert.", Long.toString(textIds.size())));
                }

                return true;
            }
        };

        return executeSQLStatements(project, "!!Datenbank-Konsistenz Text-IDs überprüfen", "!!Soll die Datenbank nach fehlerhaften Text-IDs durchsucht werden?",
                                    null, runnable, true);
    }

    /**
     * Überprüft in der Datenbank, ob TermIds enthalten sind, bei denen die führende Null nicht entfernt wurde. Wenn ein
     * Datensatz gefunden wurde, wird die TermId korrigiert.
     *
     * @param project
     * @return
     */
    public static boolean checkTermIdConsistency(EtkProject project) {
        ExecuteSQLStatementsRunnable runnable = new ExecuteSQLStatementsRunnable() {
            @Override
            public boolean executeSQLStatements(EtkProject project, DBBase db, EtkMessageLog messageLog) {
                boolean saveToDB = true;
                String[] excludedTables = new String[]{ TABLE_SPRACHE, TABLE_USERGROUP, TABLE_USERSETTINGS,
                                                        TABLE_STRUKT, TABLE_ICONS, TABLE_ENUM, TABLE_ESTRUCT,
                                                        TABLE_DA_DICT_META, TABLE_DA_DICT_SPRACHE };
                String[] excludedTablesPrefixes = new String[]{ "UA_", "S_", "E_" };
                List<String> excludeTables = StrUtils.toStringArrayList(excludedTables);

                // Teste, ob unter einer TextId verschiedene Benennungen gespeichert sind
                messageLog.fireMessage("!!Suche Term-IDs mit führender Null...");
                //List<String> tables = project.getEtkDbs().getTables();
                Collection<EtkDatabaseTable> tables = project.getConfig().getDBDescription().getTableList();
                for (EtkDatabaseTable dbTable : tables) {
                    String tableName = dbTable.getName();
                    boolean doTest = true;
                    if (excludeTables.contains(tableName)) {
                        doTest = false;
                    } else {
                        for (String prefix : excludedTablesPrefixes) {
                            if (tableName.startsWith(prefix)) {
                                doTest = false;
                                break;
                            }
                        }
                    }
                    if (!doTest) {
                        continue;
                    }
                    List<String> handleFieldNames = dbTable.getMultiLangFields();
                    if (handleFieldNames.isEmpty()) {
                        continue;
                    }
                    messageLog.fireMessage(TranslationHandler.translate("!!Bearbeite Tabelle \"%1\" mit Feldern \"%2\"",
                                                                        tableName, StrUtils.stringListToString(handleFieldNames, ",")));

                    String[] pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());
                    for (String fieldName : handleFieldNames) {
                        List<EtkRecord> foundRecords = searchInvalidTermIds(project, dbTable, tableName, fieldName);

                        messageLog.fireMessage(TranslationHandler.translate("!!Anzahl \"%1\" bei \"%2\"",
                                                                            "" + foundRecords.size(), fieldName));
                        if (!foundRecords.isEmpty()) {
                            int count = 0;
                            for (EtkRecord currentRec : foundRecords) {
                                List<String> pkValues = new DwList<String>();
                                for (String pkFieldName : pkFields) {
                                    pkValues.add(currentRec.getField(pkFieldName).getAsString());
                                }
                                EtkRecord rec = new EtkRecord();
                                String termId = currentRec.getField(fieldName).getAsString();
                                String prefix = TableAndFieldName.getFirstPart(termId);
                                String termNo = TableAndFieldName.getSecondPart(termId);
                                if (prefix.isEmpty() || termNo.equals(termId) || (termNo.length() > 10)) {
                                    continue;
                                }
                                termNo = StrUtils.removeLeadingCharsFromString(termNo, '0');
                                termId = TableAndFieldName.make(prefix, termNo);
                                // umbauen
                                rec.addField(fieldName, termId);
                                // Records zurückschreiben
                                if (saveToDB) {
                                    project.getEtkDbs().update(tableName, pkFields, ArrayUtil.toStringArray(pkValues), rec, null);
                                }
                                count++;
                            }
                            messageLog.fireMessage(TranslationHandler.translate("!!Tabelle \"%1\" mit Feld \"%2\" behandelt %3",
                                                                                tableName, fieldName, "" + count));
                        }
                    }
                    messageLog.fireMessage(StrUtils.prefixStringWithCharsUpToLength("=", '=', 50));
                }

                handleLanguageTable(project, messageLog, saveToDB);
                handleDictMetaTable(project, messageLog, saveToDB);
                handleDictLanguageTable(project, messageLog, saveToDB);

                messageLog.fireMessage("!!Beendet");

                return true;
            }
        };

        return executeSQLStatements(project, "!!Term-IDs überprüfen", "!!Soll die Datenbank nach fehlerhaften Term-IDs durchsucht werden?",
                                    null, runnable, true);
    }

    /**
     * Korrigiert die TermIds in der Tabelle TABLE_DA_DICT_SPRACHE
     *
     * @param project
     * @param messageLog
     * @param saveToDB
     */
    private static void handleDictLanguageTable(EtkProject project, EtkMessageLog messageLog, boolean saveToDB) {
        // SQL-Query auf-/absetzen
        messageLog.fireMessage(TranslationHandler.translate("!!Bearbeite Tabelle \"%1\" mit Feld \"%2\"",
                                                            TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID));
        EtkDatabaseTable dbTable = project.getConfig().getDBDescription().getTable(TABLE_DA_DICT_SPRACHE);
        List<EtkRecord> foundRecords = searchInvalidTermIds(project, dbTable, TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID);

        if (!foundRecords.isEmpty()) {
            String[] pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());

            int count = 0;
            for (EtkRecord currentRec : foundRecords) {
                List<String> pkValues = new DwList<String>();
                for (String pkFieldName : pkFields) {
                    pkValues.add(currentRec.getField(pkFieldName).getAsString());
                }
                EtkRecord rec = new EtkRecord();
                String termId = currentRec.getField(FIELD_DA_DICT_SPRACHE_TEXTID).getAsString();
                String prefix = TableAndFieldName.getFirstPart(termId);
                String termNo = TableAndFieldName.getSecondPart(termId);
                if (prefix.isEmpty() || termNo.equals(termId) || (termNo.length() > 10)) {
                    continue;
                }
                termNo = StrUtils.removeLeadingCharsFromString(termNo, '0');
                termId = TableAndFieldName.make(prefix, termNo);
                // umbauen
                rec.addField(FIELD_DA_DICT_SPRACHE_TEXTID, termId);
                // Records zurückschreiben
                if (saveToDB) {
                    project.getEtkDbs().update(TABLE_DA_DICT_SPRACHE, pkFields, ArrayUtil.toStringArray(pkValues), rec, null);
                }
                count++;
            }
            messageLog.fireMessage(TranslationHandler.translate("!!Tabelle \"%1\" mit Feld \"%2\" behandelt %3",
                                                                TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID, "" + count));
        }

    }

    /**
     * Korrigiert die TermIds in der Tabelle TABLE_DA_DICT_META
     *
     * @param project
     * @param messageLog
     * @param saveToDB
     */
    private static void handleDictMetaTable(EtkProject project, EtkMessageLog messageLog, boolean saveToDB) {
        // SQL-Query auf-/absetzen
        messageLog.fireMessage(TranslationHandler.translate("!!Bearbeite Tabelle \"%1\" mit Feld \"%2\"",
                                                            TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID));
        EtkDatabaseTable dbTable = project.getConfig().getDBDescription().getTable(TABLE_DA_DICT_META);
        List<EtkRecord> foundRecords = searchInvalidTermIds(project, dbTable, TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID);

        if (!foundRecords.isEmpty()) {
            String[] pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());

            int count = 0;
            for (EtkRecord currentRec : foundRecords) {
                List<String> pkValues = new DwList<String>();
                for (String pkFieldName : pkFields) {
                    pkValues.add(currentRec.getField(pkFieldName).getAsString());
                }
                EtkRecord rec = new EtkRecord();
                String termId = currentRec.getField(FIELD_DA_DICT_META_TEXTID).getAsString();
                String prefix = TableAndFieldName.getFirstPart(termId);
                String termNo = TableAndFieldName.getSecondPart(termId);
                if (prefix.isEmpty() || termNo.equals(termId) || (termNo.length() > 10)) {
                    continue;
                }
                termNo = StrUtils.removeLeadingCharsFromString(termNo, '0');
                termId = TableAndFieldName.make(prefix, termNo);
                // umbauen
                rec.addField(FIELD_DA_DICT_META_TEXTID, termId);
                String foreignId = currentRec.getField(FIELD_DA_DICT_META_FOREIGNID).getAsString();
                foreignId = StrUtils.removeLeadingCharsFromString(foreignId, '0');
                rec.addField(FIELD_DA_DICT_META_FOREIGNID, foreignId);
                // Records zurückschreiben
                if (saveToDB) {
                    project.getEtkDbs().update(TABLE_DA_DICT_META, pkFields, ArrayUtil.toStringArray(pkValues), rec, null);
                }
                count++;
            }
            messageLog.fireMessage(TranslationHandler.translate("!!Tabelle \"%1\" mit Feld \"%2\" behandelt %3",
                                                                TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID, "" + count));
        }
        messageLog.fireMessage(StrUtils.prefixStringWithCharsUpToLength("=", '=', 50));
    }

    /**
     * Korrigiert die TermIds in der Tabelle SPRACHE
     *
     * @param project
     * @param messageLog
     * @param saveToDB
     */
    private static void handleLanguageTable(EtkProject project, EtkMessageLog messageLog, boolean saveToDB) {
        // SQL-Query auf-/absetzen
        messageLog.fireMessage(TranslationHandler.translate("!!Bearbeite Tabelle \"%1\" mit Feld \"%2\"",
                                                            TABLE_SPRACHE, FIELD_S_TEXTID));
        EtkDatabaseTable dbTable = project.getConfig().getDBDescription().getTable(TABLE_SPRACHE);
        List<EtkRecord> foundRecords = searchInvalidTermIds(project, dbTable, TABLE_SPRACHE, FIELD_S_TEXTID);

        if (!foundRecords.isEmpty()) {
            String[] pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());

            int count = 0;
            for (EtkRecord currentRec : foundRecords) {
                List<String> pkValues = new DwList<String>();
                for (String pkFieldName : pkFields) {
                    pkValues.add(currentRec.getField(pkFieldName).getAsString());
                }
                EtkRecord rec = new EtkRecord();
                String termId = currentRec.getField(FIELD_S_TEXTID).getAsString();
                String prefix = TableAndFieldName.getFirstPart(termId);
                String termNo = TableAndFieldName.getSecondPart(termId);
                if (prefix.isEmpty() || termNo.equals(termId) || (termNo.length() > 10)) {
                    continue;
                }
                termNo = StrUtils.removeLeadingCharsFromString(termNo, '0');
                termId = TableAndFieldName.make(prefix, termNo);
                // umbauen
                rec.addField(FIELD_S_TEXTID, termId);
                rec.addField(FIELD_S_TEXTNR, termId);
                // Records zurückschreiben
                if (saveToDB) {
                    project.getEtkDbs().update(TABLE_SPRACHE, pkFields, ArrayUtil.toStringArray(pkValues), rec, null);
                }
                count++;
            }
            messageLog.fireMessage(TranslationHandler.translate("!!Tabelle \"%1\" mit Feld \"%2\" behandelt %3",
                                                                TABLE_SPRACHE, FIELD_S_TEXTID, "" + count));
        }
        messageLog.fireMessage(StrUtils.prefixStringWithCharsUpToLength("=", '=', 50));
    }

    private static List<EtkRecord> searchInvalidTermIds(EtkProject project, EtkDatabaseTable dbTable, String tableName, String fieldName) {
        // SQL-Query auf-/absetzen
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.select(new Fields(dbTable.getFieldListAsStringList(false))).from(tableName);
        String searchText = SQLUtils.wildcardExpressionToSQLLike("*.0*", false, false, false);
        Condition condition = new Condition(fieldName, Condition.OPERATOR_LIKE, searchText);
        query.where(condition);
        DBDataSet dbSet = query.executeQuery();

        List<EtkRecord> foundRecords = new LinkedList<EtkRecord>();

        while (dbSet.next()) {
            foundRecords.add(dbSet.getRecord(dbTable.getFieldListAsStringList(false)));
        }
        dbSet.close();
        return foundRecords;
    }

    private static boolean executeSQLStatements(final EtkProject project, final String messageTitle, String question1, String question2,
                                                final ExecuteSQLStatementsRunnable sqlStatementsRunnable, boolean clearCaches) {
        if (project == null) {
            MessageDialog.showError("!!Fehler: EtkProject ist null!", messageTitle);
            return false;
        }

        final DBBase db = project.getDB();
        if (db == null) {
            MessageDialog.showError("!!Fehler: DBBase ist null!", messageTitle);
            return false;
        }

        // Die doppelte Sicherheitsabfrage:
        boolean cancelDeletion = true;
        if (!StrUtils.isValid(question1) || MessageDialog.showYesNo(question1, messageTitle) == ModalResult.YES) {
            if (!StrUtils.isValid(question2) || MessageDialog.showYesNo(question2, messageTitle) == ModalResult.YES) {
                cancelDeletion = false;
            }
        }
        if (cancelDeletion) {
            MessageDialog.showWarning("!!Abbruch durch Benutzer!", messageTitle);
            return false;
        }


        final VarParam<Boolean> result = new VarParam<Boolean>(false);
        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("", messageTitle, null);
        messageLogForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                db.startTransaction();
                db.startBatchStatement();

                try {
                    if (sqlStatementsRunnable.executeSQLStatements(project, db, messageLogForm.getMessageLog())) {
                        db.endBatchStatement();
                        db.commit();

                        if (clearCaches) {
                            CacheHelper.invalidateCaches();
                        }

                        messageLogForm.getMessageLog().fireMessage("");
                        messageLogForm.getMessageLog().fireMessage("!!Fertig");
                        messageLogForm.getMessageLog().fireProgress(100, 100, "", false, false);
                        result.setValue(true);
                    } else {
                        db.cancelBatchStatement();
                        db.rollback();

                        result.setValue(false);
                    }
                } catch (Exception e) { // Bei Fehlern die Rolle rückwärts machen
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    messageLogForm.getMessageLog().fireMessage(Logger.getLogger().exceptionToString(e), MessageLogType.tmlError);

                    db.cancelBatchStatement();
                    db.rollback();

                    result.setValue(false);
                }
            }
        });
        return result.getValue();
    }

    /**
     * Im Modul die Sequenznummern der Stücklisteneinträge reorganisieren. Die Funktion speichert die neuen Nummern gleich
     * direkt in der Datenbank bzw. im ChangeSet (falls eines aktiv ist).
     *
     * @param assembly
     */
    public static void reorgSeqenceNumbers(iPartsDataAssembly assembly) {
        DBDataObjectList<EtkDataPartListEntry> entries = assembly.getPartListUnfiltered(null);
        reorgSeqenceNumbers(entries.getAsList());
        assembly.savePartListEntries();
    }

    /**
     * Die Sequenznummern aller übergebenen Stücklisteneinträge in der Reihenfolge der Liste reorganisieren.
     *
     * @param entries
     */
    public static void reorgSeqenceNumbers(List<EtkDataPartListEntry> entries) {
        String seqNumber = "";

        for (EtkDataPartListEntry entry : entries) {
            // Nächste Seqenznummer ermitteln
            seqNumber = SortBetweenHelper.getSortBetween(seqNumber, "");
            entry.setFieldValue(FIELD_K_SEQNR, seqNumber, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Suche nicht freigegebene Autoren-Aufträge für DIALOG-Stücklisteneinträge mit Materialnummer
     *
     * @return
     */
    public static EtkFunction searchChangeSetsForMaterialNumber(final EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                GuiPanel inputPanel = new GuiPanel(new LayoutGridBag(false));
                GuiLabel seriesLabel = new GuiLabel(TranslationHandler.translate("!!Baureihe"));
                seriesLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0,
                                                                  ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                                  4, 4, 4, 4));
                inputPanel.addChild(seriesLabel);
                GuiTextField seriesInput = new GuiTextField();
                seriesInput.setMinimumWidth(200);
                seriesInput.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 0.0, 100.0,
                                                                  ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                                                  4, 4, 4, 4));
                inputPanel.addChild(seriesInput);
                GuiLabel matNrLabel = new GuiLabel(TranslationHandler.translate("!!Materialnummer"));
                matNrLabel.setConstraints(new ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0,
                                                                 ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                                 4, 4, 4, 4));
                inputPanel.addChild(matNrLabel);
                GuiTextField matNrInput = new GuiTextField();
                matNrInput.setConstraints(new ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0,
                                                                 ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                                                 4, 4, 4, 4));
                inputPanel.addChild(matNrInput);
                seriesInput.requestFocus();

                if (InputDialog.show("!!Suche in nicht-freigegebenen Autoren-Aufträgen", "!!Suche nach Materialnummer", inputPanel, null, seriesInput) != ModalResult.OK) {
                    return;
                }

                String seriesNumber = seriesInput.getTrimmedText();
                String materialNumber = matNrInput.getTrimmedText();

                if (!StrUtils.isValid(seriesNumber, materialNumber)) {
                    return;
                }

                // Leerzeichen entfernen
                materialNumber = StrUtils.removeCharsFromString(materialNumber, new char[]{ ' ' });

                // Suche alle relevanten DIALOG-Konstruktions-Stücklisteneinträge für die Baureihe und Materialnummer
                Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys = new HashSet<>();
                EtkDataObjectList<iPartsDataDialogData> dialogDataList =
                        iPartsDataDialogDataList.loadBCTEKeysForSeriesAndMatNr(project, seriesNumber, materialNumber);
                for (iPartsDataDialogData dialogData : dialogDataList) {
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogData.getAsId().getDialogGuid());
                    relevantBCTEPrimaryKeys.add(bctePrimaryKey);
                }

                // Suche alle ChangeSetEntries für Stücklisteneinträge für die Baureihe
                String partialGUID = seriesNumber + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + "*";
                String result = findMatchingChangeSets(relevantBCTEPrimaryKeys, partialGUID, project,
                                                       TranslationHandler.translate("!!Nicht freigegebene Autoren-Aufträge für DIALOG-Stücklisteneinträge mit Baureihe \"%1\" und Materialnummer \"%2\":",
                                                                                    seriesNumber, materialNumber));

                CopyTextWindow copyTextWindow = new CopyTextWindow(result);
                copyTextWindow.maximize();
                copyTextWindow.showModal();
            }
        };
    }

    /**
     * Suche nicht freigegebene Autoren-Aufträge für DIALOG-Stücklisteneinträge mit BCTE Schlüssel
     *
     * @return
     */
    public static EtkFunction searchChangeSetsForBCTEkey(final EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String bcteKey = InputDialog.show("!!Suche in nicht-freigegebenen Autoren-Aufträgen", "!!Suche nach BCTE-Schlüssel", "", false);

                if (!StrUtils.isValid(bcteKey)) {
                    return;
                }
                iPartsDialogBCTEPrimaryKey primaryBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKey);
                if ((primaryBCTEKey == null) || StrUtils.isEmpty(primaryBCTEKey.seriesNo)) {
                    return;
                }
                String seriesNumber = primaryBCTEKey.seriesNo;
                Set<iPartsDialogBCTEPrimaryKey> bctePrimaryKeySet = new HashSet<>();
                bctePrimaryKeySet.add(primaryBCTEKey);

                // Suche alle ChangeSetEntries für Stücklisteneinträge für die Baureihe
                String partialGUID = seriesNumber + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + "*";
                String result = findMatchingChangeSets(bctePrimaryKeySet, partialGUID, project,
                                                       TranslationHandler.translate("!!Nicht freigegebene Autoren-Aufträge für DIALOG-Stücklisteneinträge mit BCTE Schlüssel \"%1\":",
                                                                                    bcteKey));

                CopyTextWindow copyTextWindow = new CopyTextWindow(result);
                copyTextWindow.maximize();
                copyTextWindow.showModal();
            }
        };
    }

    private static String findMatchingChangeSets(Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys, String searchGUID, EtkProject project,
                                                 String title) {
        iPartsDataChangeSetEntryList dataChangeSetEntries = iPartsRevisionsHelper.getActiveChangeSetEntriesContainingSourceGUID(PartListEntryId.TYPE,
                                                                                                                                searchGUID, project);

        // Suche darin alle ChangeSetIDs für die relevanten DIALOG-Konstruktions-Stücklisteneinträge
        Set<String> changeSetIDs = new TreeSet<>();
        for (iPartsDialogBCTEPrimaryKey primaryBCTEKey : relevantBCTEPrimaryKeys) {
            String dialogGUID = primaryBCTEKey.createDialogGUID();
            for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntries) {
                if (dataChangeSetEntry.getSourceGUID().equals(dialogGUID)) {
                    changeSetIDs.add(dataChangeSetEntry.getAsId().getGUID());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (!changeSetIDs.isEmpty()) {
            for (String changeSetID : changeSetIDs) {
                sb.append("\n- ");

                iPartsDataAuthorOrder authorOrder = iPartsDataAuthorOrderList.getAuthorOrderByChangeSetId(project,
                                                                                                          new ChangeSetId(changeSetID));
                if (authorOrder != null) {
                    sb.append(TranslationHandler.translate("!!Autoren-Auftrag: \"%1\"", authorOrder.getAuthorOrderName()));
                    sb.append("; ");
                    sb.append(TranslationHandler.translate("!!Bearbeiter: \"%1\"", authorOrder.getCurrentUser()));
                    sb.append("; ");
                    sb.append(TranslationHandler.translate("!!Änderungsset: %1", changeSetID));
                } else {
                    sb.append(TranslationHandler.translate("!!Verwaistes Änderungsset ohne Autoren-Auftrag: %1", changeSetID));
                }
            }
        } else {
            sb.append('\n');
            sb.append(TranslationHandler.translate("!!Keine Einträge"));
        }
        return sb.toString();
    }

    /**
     * Exportiert alle relevanten Tabellendefinition in eine CSV-Datei.
     *
     * @return
     */
    public static EtkFunction exportTableDefinitionsAsCSVFile(final EtkProject project) {
        final String separator = "\t";

        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                Set<EtkDatabaseTable> relevantTables = new TreeSet<>((o1, o2) -> {
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                });

                // Diese Standard-Tabellen sind für iParts relevant:
                EtkDatabaseDescription dbDescription = project.getConfig().getDBDescription();
                relevantTables.add(dbDescription.getTable(TABLE_CUSTPROP));
                relevantTables.add(dbDescription.getTable(TABLE_DOKU));
                relevantTables.add(dbDescription.getTable(TABLE_DWARRAY));
                relevantTables.add(dbDescription.getTable(TABLE_ENUM));
                relevantTables.add(dbDescription.getTable(TABLE_ENUMLINK));
                relevantTables.add(dbDescription.getTable(TABLE_FAVORITES));
                relevantTables.add(dbDescription.getTable(TABLE_AUTOCOMPLETE));
                relevantTables.add(dbDescription.getTable(TABLE_ICONS));
                relevantTables.add(dbDescription.getTable(TABLE_IMAGES));
                relevantTables.add(dbDescription.getTable(TABLE_KAPITEL));
                relevantTables.add(dbDescription.getTable(TABLE_KATALOG));
                relevantTables.add(dbDescription.getTable(TABLE_KEYVALUE));
                relevantTables.add(dbDescription.getTable(TABLE_LINKS));
                relevantTables.add(dbDescription.getTable(TABLE_MAT));
                relevantTables.add(dbDescription.getTable(TABLE_NOTIZ));
                relevantTables.add(dbDescription.getTable(TABLE_POOL));
                relevantTables.add(dbDescription.getTable(TABLE_POOLENTRY));
                relevantTables.add(dbDescription.getTable(TABLE_SPRACHE));


                // ... und alle iParts- und Benutzerverwaltungs-spezifischen Tabellen
                Collection<EtkDatabaseTable> tables = dbDescription.getTableList();
                for (EtkDatabaseTable table : tables) {
                    String tableName = table.getName();
                    if (tableName.startsWith("DA_") || tableName.startsWith("UA_")) {
                        relevantTables.add(table);
                    }
                }

                // CSV zusammenbasteln mit deutschen Texten
                StringBuilder tableDefinitionsAsCSV = new StringBuilder();
                addHeader(tableDefinitionsAsCSV);

                for (EtkDatabaseTable table : relevantTables) {
                    if (table != null) {
                        tableDefinitionsAsCSV.append(StrUtils.DW_NEWLINE); // Leerzeile
                        appendTableEntries(tableDefinitionsAsCSV, table);
                    }
                }

                buildCSVAndDownload(tableDefinitionsAsCSV, "Tabellendefinitionen.csv");
            }

            private void addHeader(StringBuilder tableDefinitionsAsCSV) {
                appendValue(tableDefinitionsAsCSV, "Tabelle");
                appendValue(tableDefinitionsAsCSV, "Feld");
                appendValue(tableDefinitionsAsCSV, "Datentyp");
                appendValue(tableDefinitionsAsCSV, "Länge");
                appendValue(tableDefinitionsAsCSV, "Beschreibung", StrUtils.DW_NEWLINE);
            }

            private void appendTableEntries(StringBuilder tableDefinitionsAsCSV, EtkDatabaseTable table) {
                String tableName = table.getName();
                for (EtkDatabaseField field : table.getFieldList()) {
                    String fieldname = field.getName();
                    if (fieldname.equals(FIELD_STAMP)) { // Feld T_STAMP nicht exportieren
                        continue;
                    }

                    // "Tabelle"
                    appendValue(tableDefinitionsAsCSV, tableName);
                    // "Feld"
                    appendValue(tableDefinitionsAsCSV, fieldname);
                    String value = "";
                    if (field.isMultiLanguage()) {
                        value = "Mehrsprachiger String";
                    } else {
                        value = TranslationHandler.translateForLanguage(field.getType().getText(), Language.DE.getCode());
                        if (field.isArray()) {
                            value += "[]";
                        }
                    }
                    // "Datentyp"
                    appendValue(tableDefinitionsAsCSV, value);
                    // "Länge"
                    appendValue(tableDefinitionsAsCSV, String.valueOf(field.getFieldSize()));
                    // "Beschreibung"
                    appendValue(tableDefinitionsAsCSV, field.getDisplayText(Language.DE.getCode(), project.getDataBaseFallbackLanguages()), StrUtils.DW_NEWLINE);
                }
            }

            private void appendValue(StringBuilder tableDefinitionsAsCSV, String value) {
                appendValue(tableDefinitionsAsCSV, value, separator);
            }

            private void appendValue(StringBuilder tableDefinitionsAsCSV, String value, String delimiter) {
                tableDefinitionsAsCSV.append(value);
                tableDefinitionsAsCSV.append(delimiter);
            }
        };
    }

    public static boolean buildCSVAndDownload(StringBuilder content, String fileName) {
        try {
            byte[] fileContent = content.toString().getBytes(DWFileCoding.UTF8_BOM.getJavaCharsetName());

            // UTF-8 BOM hinzufügen
            byte[] fileBOM = DWFileCoding.UTF8_BOM.getBom();
            if (fileBOM.length > 0) {
                byte[] csvContent = fileContent;
                fileContent = new byte[fileBOM.length + fileContent.length];
                System.arraycopy(fileBOM, 0, fileContent, 0, fileBOM.length);
                System.arraycopy(csvContent, 0, fileContent, fileBOM.length, csvContent.length);
            }

            MultipleInputToOutputStream mitos = new MultipleInputToOutputStream(fileContent, fileName);
            // GuiFileChooserDialog für den eigentlichen Download
            GuiFileChooserDialog saveFileDialog = new GuiFileChooserDialog(FileChooserPurpose.SAVE,
                                                                           GuiFileChooserDialog.FILE_MODE_FILES,
                                                                           null, false);
            saveFileDialog.setServerMode(false);
            saveFileDialog.setVisible(mitos);
            return true;
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            MessageDialog.showError("!!Fehler beim Speichern.");
        }
        return false;
    }

    /**
     * KATALOG.K_SOURCE_GUID auf Modul+LfdNr setzen für alle Nicht-DIALOG-Stücklisteneinträge inkl. Korrektur der dazugehörigen
     * Werkseinsatzdaten in AS-Stücklisten sowie nicht-freigegebenen Autoren-Aufträgen.
     *
     * @param project
     * @return
     */
    public static void correctSourceGUIDForNonDIALOGPartListEntriesInDB(EtkProject project) {
        ExecuteSQLStatementsRunnable runnable = (project1, db, messageLog) -> {
            messageLog.fireMessage("!!Suche Nicht-DIALOG-Stücklisteneinträge mit alter Quell-GUID...");

            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_GUID, false, false));

            // K_SOURCE_GUID enthält | oder ist leer für einen Nicht-DIALOG-Stücklisteneintrag
            String[][] whereTableAndFields = new String[2][];
            EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 0,
                                                TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SOURCE_GUID),
                                                TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SOURCE_GUID));
            EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 1,
                                                TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SOURCE_TYPE));

            String[][] whereValues = new String[2][];
            EtkDataObjectList.addElemsTo2dArray(whereValues, 0, "*|*", "");
            EtkDataObjectList.addElemsTo2dArray(whereValues, 1, EtkDataObjectList.getNotWhereValue(iPartsEntrySourceType.DIALOG.getDbValue()));

            EtkDataPartListEntryList partListEntryList = new EtkDataPartListEntryList();
            partListEntryList.searchSortAndFillWithJoin(project, null, selectFields,
                                                        whereTableAndFields,
                                                        whereValues,
                                                        false, null, null, false, null, false, true, false, null, false);
            messageLog.fireMessage(TranslationHandler.translate("!!%1 Nicht-DIALOG-Stücklisteneinträge mit alter Quell-GUID gefunden",
                                                                String.valueOf(partListEntryList.size())));

            messageLog.fireMessage("!!Lade dazugehörige Werkseinsatzdaten und korrigiere Quell-GUIDs:");
            VarParam<Integer> progress = new VarParam<>(0);
            int sectionMaxProgress = partListEntryList.size();
            if (sectionMaxProgress == 0) {
                sectionMaxProgress = 1;
            }
            VarParam<Integer> maxProgress = new VarParam<>(2 * sectionMaxProgress);
            messageLog.fireProgress(0, maxProgress.getValue(), "", false, false);

            // Stücklisteneinträge korrigieren inkl. Werkseinsatzdaten
            EtkDataObjectList modifiedDataObjects = collectDataForCorrectedSourceGUIDForNonDIALOGPartListEntries(partListEntryList,
                                                                                                                 messageLog, progress,
                                                                                                                 maxProgress.getValue(), project);
            messageLog.fireProgress(sectionMaxProgress, maxProgress.getValue(), "", false, false);

            modifiedDataObjects.saveToDB(project);
            messageLog.fireProgress(maxProgress.getValue(), maxProgress.getValue(), "", false, false);

            // Nicht freigegebene Autoren-Aufträge durchsuchen
            messageLog.fireMessageWithSeparators("!!Durchsuche nicht-freigegebene Autoren-Aufträge...");

            // Alle relevanten Stücklisteneinträge in nicht-freigegebenen ChangeSets suchen, wobei der SourceType != DIALOG
            // hier noch nicht berücksichtigt werden kann
            Map<String, Set<PartListEntryId>> relevantChangeSetIdsMap = new TreeMap<>();
            iPartsDataChangeSetEntryList dataChangeSetEntries = new iPartsDataChangeSetEntryList();
            selectFields = new EtkDisplayFields();
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID, false, false));

            // K_SOURCE_GUID enthält | oder ist leer für einen nicht freigegebenen ChangeSetEntry eines Stücklisteneintrags
            whereTableAndFields = new String[3][];
            EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 0, TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE));
            EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 1,
                                                TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID),
                                                TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID));
            EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 2, TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCS_STATUS));


            whereValues = new String[3][];
            EtkDataObjectList.addElemsTo2dArray(whereValues, 0, PartListEntryId.TYPE);
            EtkDataObjectList.addElemsTo2dArray(whereValues, 1, "*|*", "");
            EtkDataObjectList.addElemsTo2dArray(whereValues, 2, EtkDataObjectList.getNotWhereValue(iPartsChangeSetStatus.COMMITTED.name()));

            dataChangeSetEntries.searchSortAndFillWithJoin(project, null, selectFields,
                                                           whereTableAndFields,
                                                           whereValues,
                                                           false, null, null, false, null, false, true, false,
                                                           new EtkDataObjectList.FoundAttributesCallback() {
                                                               @Override
                                                               public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                                   // Pro relevanter ChangeSetId ein Set aller evtl. relevanten
                                                                   // PartListEntryIds merken
                                                                   Set<PartListEntryId> partListEntryIds = relevantChangeSetIdsMap.computeIfAbsent(attributes.getFieldValue(FIELD_DCE_GUID),
                                                                                                                                                   changeSetId -> new TreeSet<>());

                                                                   IdWithType objectId = IdWithType.fromDBString(PartListEntryId.TYPE,
                                                                                                                 attributes.getFieldValue(FIELD_DCE_DO_ID));
                                                                   PartListEntryId partListEntryId = IdWithType.fromStringArrayWithTypeFromClass(PartListEntryId.class,
                                                                                                                                                 objectId.toStringArrayWithoutType());
                                                                   partListEntryIds.add(partListEntryId);
                                                                   return false;
                                                               }
                                                           }, false,
                                                           new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET,
                                                                                          new String[]{ FIELD_DCE_GUID },
                                                                                          new String[]{ FIELD_DCS_GUID },
                                                                                          false, false));

            // maxProgress an Anzahl relevanter ChangeSets anpassen (mit Faktor 10 für die Berücksichtigung von Stücklisteneinträgen
            // im ChangeSet und somit halbwegs realistischer maxProgress)
            maxProgress.setValue(10 * relevantChangeSetIdsMap.size());
            messageLog.fireProgress(0, maxProgress.getValue(), "", false, false);

            // Alle relevanten ChangeSets laden und Stücklisteneinträge auf SourceType != DIALOG und für die Korrektur von
            // K_SOURCE_GUID auch auf Status neu einschränken
            List<ChangeSetModificator.ChangeSetModificationTask> changeSetModificationTasks = new DwList<>();
            for (Map.Entry<String, Set<PartListEntryId>> relevantChangeSetIdEntry : relevantChangeSetIdsMap.entrySet()) {
                String changeSetId = relevantChangeSetIdEntry.getKey();
                ChangeSetModificator.ChangeSetModificationTask changeSetModificationTask = new ChangeSetModificator.ChangeSetModificationTask(changeSetId) {
                    @Override
                    public void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                GenericEtkDataObjectList dataObjectListForChangeSet) {
                        // Nur die Stücklisteneinträge sowie Module mit Meta-Daten ins ChangeSet laden
                        authorOrderChangeSet.loadSerializedDataObjectsBySourceGUID("", PartListEntryId.TYPE);
                        authorOrderChangeSet.loadSerializedDataObjectsBySourceGUID("", AssemblyId.TYPE);
                        authorOrderChangeSet.loadSerializedDataObjectsBySourceGUID("", iPartsModuleId.TYPE);

                        // Pro ChangeSet bei allen relevanten Stücklisteneinträgen aus diesem ChangeSet prüfen, ob K_SOURCE_GUID
                        // korrigiert werden muss+
                        EtkDataPartListEntryList pleListInChangeSet = new EtkDataPartListEntryList();
                        Map<AssemblyId, iPartsDocumentationType> assemblyToDocumentationTypeMap = new HashMap<>();
                        for (PartListEntryId partListEntryId : relevantChangeSetIdEntry.getValue()) {
                            iPartsDocumentationType docuType = assemblyToDocumentationTypeMap.computeIfAbsent(partListEntryId.getOwnerAssemblyId(), assemblyId -> {
                                EtkDataAssembly assemblyInChangeSet = EtkDataObjectFactory.createDataAssembly(projectForChangeSet, assemblyId);
                                if (assemblyInChangeSet.existsInDB() && (assemblyInChangeSet instanceof iPartsDataAssembly)) {
                                    return ((iPartsDataAssembly)assemblyInChangeSet).getDocumentationType();
                                } else {
                                    return iPartsDocumentationType.UNKNOWN;
                                }
                            });

                            // DIALOG-Stücklisteneinträge und unbekannte Stücklisteneinträge ignorieren
                            if (docuType.isPKWDocumentationType() || (docuType == iPartsDocumentationType.UNKNOWN)) {
                                continue;
                            }

                            // Zunächst das Feld DCE_DO_SOURCE_GUID im ChangeSetEntry korrigieren
                            String correctedKSourceGUID = EditConstructionToRetailHelper.createNonDIALOGSourceGUID(partListEntryId);
                            iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(authorOrderChangeSet.getChangeSetId(),
                                                                                                 partListEntryId);
                            iPartsDataChangeSetEntry dataChangeSetEntry = new iPartsDataChangeSetEntry(projectForChangeSet,
                                                                                                       changeSetEntryId);
                            dataChangeSetEntry.setFieldValue(FIELD_DCE_DO_SOURCE_GUID, correctedKSourceGUID, DBActionOrigin.FROM_EDIT);
                            dataChangeSetEntry.saveToDB();

                            SerializedDBDataObject serializedDBDataObject = authorOrderChangeSet.getSerializedDataObject(partListEntryId);
                            if ((serializedDBDataObject.getState() == SerializedDBDataObjectState.NEW) || (serializedDBDataObject.getState() == SerializedDBDataObjectState.REPLACED)) {
                                EtkDataPartListEntry partListEntryInChangeSet = EtkDataObjectFactory.createDataPartListEntry(projectForChangeSet,
                                                                                                                             partListEntryId);
                                if (partListEntryInChangeSet.existsInDB()) {
                                    pleListInChangeSet.add(partListEntryInChangeSet, DBActionOrigin.FROM_DB);
                                }
                            }
                        }

                        if (!pleListInChangeSet.isEmpty()) {
                            messageLog.fireMessage("");
                            messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere Autoren-Auftrag mit Änderungsset-ID '%1':",
                                                                                changeSetId));
                            messageLog.fireMessage(TranslationHandler.translate("!!%1 Nicht-DIALOG-Stücklisteneinträge mit alter Quell-GUID gefunden",
                                                                                String.valueOf(pleListInChangeSet.size())));
                            messageLog.fireMessage("!!Lade dazugehörige Werkseinsatzdaten und korrigiere Quell-GUIDs:");

                            // maxProgress an Anzahl relevanter Stücklisteneinträge im ChangeSet anpassen
                            maxProgress.setValue(maxProgress.getValue() + pleListInChangeSet.size());

                            // Jetzt auch noch die Werkseinsatzdaten ins ChangeSet laden
                            authorOrderChangeSet.loadSerializedDataObjectsBySourceGUID("", iPartsFactoryDataId.TYPE);

                            // Stücklisteneinträge im ChangeSet korrigieren inkl. Werkseinsatzdaten
                            EtkDataObjectList modifiedDataObjectsInChangeSet = collectDataForCorrectedSourceGUIDForNonDIALOGPartListEntries(pleListInChangeSet,
                                                                                                                                            messageLog, progress,
                                                                                                                                            maxProgress.getValue(),
                                                                                                                                            projectForChangeSet);
                            authorOrderChangeSet.addDataObjectList(modifiedDataObjectsInChangeSet);
                        }

                        // progress + 10 für die Abarbeitung vom ChangeSet selbst
                        progress.setValue(progress.getValue() + 10);
                        messageLog.fireProgress(progress.getValue(), maxProgress.getValue(), "", false, true);
                    }
                };
                changeSetModificationTasks.add(changeSetModificationTask);
            }
            ChangeSetModificator changeSetModificator = new ChangeSetModificator(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS);
            changeSetModificator.executeChangesInAllChangeSets(changeSetModificationTasks, false, TECHNICAL_USER_DATA_CORRECTION);

            return true;
        };
        project.getRevisionsHelper().executeWithoutActiveChangeSets(
                () -> executeSQLStatements(project, "!!Quell-GUIDs korrigieren für Nicht-DIALOG-Stücklisteneinträge",
                                           "!!Sollen alle Quell-GUIDs für Nicht-DIALOG-Stücklisteneinträge inkl. der dazugehörigen Werkseinsatzdaten korrigiert werden?",
                                           null, runnable, true), false, project);
    }

    private static EtkDataObjectList collectDataForCorrectedSourceGUIDForNonDIALOGPartListEntries(EtkDataPartListEntryList partListEntryList,
                                                                                                  EtkMessageLog messageLog, VarParam<Integer> progress,
                                                                                                  int maxProgress, EtkProject project) {
        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        int modifiedFactoryDataCount = 0;
        for (EtkDataPartListEntry partListEntry : partListEntryList) {
            String oldSourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere Stücklisteneintrag '%1' mit bisheriger Quell-GUID '%2'...",
                                                                partListEntry.getAsId().toStringForLogMessages(), oldSourceGUID));

            // K_SOURCE_GUID korrigieren
            partListEntry.setFieldValue(FIELD_K_SOURCE_GUID, EditConstructionToRetailHelper.createNonDIALOGSourceGUID(partListEntry.getAsId()),
                                        DBActionOrigin.FROM_EDIT);
            modifiedDataObjects.add(partListEntry, DBActionOrigin.FROM_EDIT);

            // Dazugehörige Werkseinsatzdaten laden sofern die alte K_SOURCE_GUID nicht leer war
            iPartsDataFactoryDataList factoryDataList = new iPartsDataFactoryDataList();
            if (!oldSourceGUID.isEmpty()) {
                factoryDataList.loadFactoryDataForGUIDFromDB(project, oldSourceGUID, DBActionOrigin.FROM_DB);
            }
            if (!factoryDataList.isEmpty()) {
                modifiedFactoryDataCount += factoryDataList.size();
                messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere %1 Werkseinsatzdaten für Stücklisteneintrag '%2'...",
                                                                    String.valueOf(factoryDataList.size()), partListEntry.getAsId().toStringForLogMessages()));

                // Werkseinsatzdaten kopieren
                iPartsDataFactoryDataList copiedFactoryDataList = CopyAndPasteData.copyNonDIALOGFactoryDataOfPartListEntry(partListEntry,
                                                                                                                           factoryDataList,
                                                                                                                           project);
                if (!copiedFactoryDataList.isEmpty()) {
                    modifiedDataObjects.addAll(copiedFactoryDataList, DBActionOrigin.FROM_EDIT);
                }

                // Alte Werkseinsatzdaten zum Löschen vormerken
                modifiedDataObjects.deleteAll(factoryDataList, true, false, DBActionOrigin.FROM_EDIT);
            }

            progress.setValue(progress.getValue() + 1);
            messageLog.fireProgress(progress.getValue(), maxProgress, "", false, true);
        }

        messageLog.fireMessage(TranslationHandler.translate("!!Speichere %1 korrigierte Quell-GUIDs und %2 dazugehörige Werkseinsatzdaten...",
                                                            String.valueOf(partListEntryList.size()), String.valueOf(modifiedFactoryDataCount)));
        return modifiedDataObjects;
    }

    /**
     * Speichert bei allen Truck-TUs in allen Produkten bzw. einem expliziten Produkt alle SAA/BK-Gültigkeiten aller Stücklisteneinträge
     * in den passenden Datenstrukturen für die schnellere Filterung.
     *
     * @return
     */
    public static EtkFunction saveAllModuleSAAValiditiesForFilter(final EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String productNumber = InputDialog.show("!!SAA/BK-Gültigkeiten für TUs berechnen", "!!Produkt (alle Truck-Produkte falls leer)",
                                                        "", false);
                if (productNumber == null) {
                    return;
                }

                String message;
                if (productNumber.isEmpty()) {
                    message = "!!Wirklich die TUs aller Truck-Produkte korrigieren?";
                } else {
                    message = null; // Nachfrage bei einzelnem Produkt nicht notwendig, weil das schnell geht
                }

                ExecuteSQLStatementsRunnable runnable = (project, db, messageLog) -> {
                    Set<iPartsProductId> productIds = new TreeSet<>();
                    if (!productNumber.isEmpty()) {
                        iPartsProductId productId = new iPartsProductId(productNumber);
                        iPartsDocumentationType documentationType = iPartsProduct.getInstance(project, productId).getDocumentationType();
                        if (documentationType.equals(iPartsDocumentationType.UNKNOWN)) {
                            messageLog.fireMessage(TranslationHandler.translate("!!Produkt \"%1\" existiert nicht.",
                                                                                productNumber), MessageLogType.tmlError);
                            return false;
                        } else if (!iPartsProduct.getInstance(project, productId).getDocumentationType().isTruckDocumentationType()) {
                            messageLog.fireMessage(TranslationHandler.translate("!!Produkt \"%1\" ist kein Truck-Produkt.",
                                                                                productNumber), MessageLogType.tmlError);
                            return false;
                        }

                        productIds.add(productId);
                    } else {
                        // Alle Truck-Produkte ermitteln
                        List<iPartsProduct> allProducts = iPartsProduct.getAllProducts(project);
                        for (iPartsProduct product : allProducts) {
                            if (product.getDocumentationType().isTruckDocumentationType()) {
                                productIds.add(product.getAsId());
                            }
                        }
                        messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere %1 Truck-Produkte", String.valueOf(productIds.size())),
                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    }

                    final int modulesPerProduct = 500; // Normierung auf 500 Module pro Produkt für den Fortschritt
                    int maxProgress = productIds.size() * modulesPerProduct;
                    messageLog.fireProgress(0, maxProgress, "", false, false);

                    int productCounter = 0;
                    for (iPartsProductId productId : productIds) {
                        if (Thread.currentThread().isInterrupted()) {
                            messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                            return false;
                        }

                        messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere Produkt \"%1\":", productId.getProductNumber()),
                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        Set<AssemblyId> assemblyIds = iPartsProductStructures.getInstance(project, productId).getModuleIds(project);
                        messageLog.fireMessage(TranslationHandler.translate("!!- SAA/BK-Gültigkeiten für %1 TUs werden ermittelt und gespeichert...",
                                                                            String.valueOf(assemblyIds.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                        // SAA/BK-Gültigkeiten der Module ermitteln und speichern
                        double assemblyCounter = 0.0d;
                        for (AssemblyId assemblyId : assemblyIds) {
                            if (Thread.currentThread().isInterrupted()) {
                                messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                                return false;
                            }

                            // Ohne Caches, da nur minimale Daten verwendet werden und ansonsten auch viel zu viele Module
                            // im Cache landen würden
                            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId, false);
                            if (assembly instanceof iPartsDataAssembly) {
                                ((iPartsDataAssembly)assembly).saveAllSAAValiditiesForFilter();
                            }
                            assemblyCounter++;
                            int progress = productCounter * modulesPerProduct + (int)Math.ceil(assemblyCounter * modulesPerProduct / assemblyIds.size());
                            messageLog.fireProgress(progress, maxProgress, "", false, true);
                        }
                        productCounter++;
                    }

                    return true;
                };

                VarParam<Boolean> result = new VarParam<>(false);
                project.getRevisionsHelper().executeWithoutActiveChangeSets(
                        () -> result.setValue(executeSQLStatements(project, "!!SAA/BK-Gültigkeiten für TUs ermitteln und speichern",
                                                                   message, null, runnable, false)),
                        false, project);

                if (result.getValue()) {
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                }
            }
        };
    }

    /**
     * Ermittelt und speichert den Gültigkeitsbereich für alle Zeichnungen in der Datenbank.
     *
     * @return
     */
    public static EtkFunction saveAllValidityScopesForImages(final EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                ExecuteSQLStatementsRunnable runnable = (project, db, messageLog) -> {
                    messageLog.fireMessage("!!Suche Bildtafeln ohne Gültigkeitsbereich...", MessageLogType.tmlMessage,
                                           MessageLogOption.TIME_STAMP);

                    EtkDataPoolVariants dataPoolList = EtkDataObjectFactory.createDataPoolVariants();
                    dataPoolList.searchAndFill(project, TABLE_POOL, new String[]{ FIELD_P_VALIDITY_SCOPE }, new String[]{ "" },
                                               DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
                    int maxPos = dataPoolList.size();
                    messageLog.fireMessage(TranslationHandler.translate("!!%1 Bildtafeln ohne Gültigkeitsbereich gefunden",
                                                                        String.valueOf(maxPos)), MessageLogType.tmlMessage,
                                           MessageLogOption.TIME_STAMP);
                    if (maxPos == 0) { // Nix gefunden? -> gleich raus
                        return true;
                    }

                    messageLog.fireMessage("!!Starte Ermittlung und speichere die Änderungen...", MessageLogType.tmlMessage,
                                           MessageLogOption.TIME_STAMP);

                    int pos = 0;
                    messageLog.fireProgress(pos, maxPos, "", false, false);
                    for (EtkDataPool dataPool : dataPoolList) {
                        if (Thread.currentThread().isInterrupted()) {
                            messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                            return false;
                        }

                        if (dataPool instanceof iPartsDataPool) {
                            ((iPartsDataPool)dataPool).updateValidityScope();
                            dataPool.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                        }
                        pos++;
                        messageLog.fireProgress(pos, maxPos, "", false, true);
                    }
                    messageLog.fireProgress(maxPos, maxPos, "", false, false);
                    return true;
                };

                project.getRevisionsHelper().executeWithoutActiveChangeSets(
                        () -> {
                            String title = TranslationHandler.translate("!!Gültigkeitsbereich von Bildtafeln ermitteln und speichern");
                            executeSQLStatements(project, title, TranslationHandler.translate("!!Wirklich %1?", title),
                                                 null, runnable, false);
                        },
                        false, project);
            }
        };
    }

    /**
     * Ermittelt und persistiert die Materialnummern in allen ChangeSetEntries von Stücklisteneinträgen.
     *
     * @param project
     * @return
     */
    public static EtkFunction extractAndSaveMaterialNumberInChangeSetEntry(EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                ExecuteSQLStatementsRunnable runnable = (project, db, messageLog) -> {
                    messageLog.fireMessage("!!Ermittle und lade ChangeSet-Einträge für Stücklisteneinträge mit leerer Materialnummer...");

                    iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
                    changeSetEntryList.searchSortAndFill(project, TABLE_DA_CHANGE_SET_ENTRY,
                                                         new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID, FIELD_DCE_MATNR },
                                                         new String[]{ FIELD_DCE_DO_TYPE, FIELD_DCE_MATNR },
                                                         new String[]{ PartListEntryId.TYPE, "" }, null, null,
                                                         null, DBDataObjectList.LoadType.ONLY_IDS, false, DBActionOrigin.FROM_DB);

                    messageLog.fireMessage(TranslationHandler.translate("!!%1 ChangeSet-Einträge gefunden.",
                                                                        String.valueOf(changeSetEntryList.size())));

                    if ((changeSetEntryList != null) && !changeSetEntryList.isEmpty()) {
                        messageLog.fireMessage("!!Ermittle und persistiere die Materialnummern in den ChangeSet-Einträgen...");

                        int pos = 0;
                        int maxPos = 2 * changeSetEntryList.size();
                        int processed = 0;
                        messageLog.fireProgress(pos, maxPos, "", false, false);
                        for (iPartsDataChangeSetEntry changeSetEntry : changeSetEntryList) {
                            if (Thread.currentThread().isInterrupted()) {
                                messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                                return false;
                            }

                            String kMatNr = changeSetEntry.getSerializedDBDataObject().getAttributeValue(FIELD_K_MATNR,
                                                                                                         true, project);
                            if (StrUtils.isValid(kMatNr)) {
                                changeSetEntry.setFieldValue(FIELD_DCE_MATNR, kMatNr, DBActionOrigin.FROM_EDIT);
                                processed++;
                            } else {
                                messageLog.fireMessage(TranslationHandler.translate("!!Für \"%1\" konnte keine Materialnummer ermittelt werden.",
                                                                                    changeSetEntry.getAsId().toStringForLogMessages()));
                            }
                            pos++;
                            messageLog.fireProgress(pos, maxPos, "", false, true);
                        }
                        messageLog.fireMessage(TranslationHandler.translate("!!Speichere %1 angepasste ChangeSet-Einträge...",
                                                                            String.valueOf(processed)));
                        messageLog.fireMessage(TranslationHandler.translate("!!%1 ChangeSet-Einträge konnten aufgrund fehlender Materialnummer nicht ergänzt werden.",
                                                                            String.valueOf(changeSetEntryList.size() - processed)));
                        changeSetEntryList.saveToDB(project, false);
                        messageLog.fireProgress(maxPos, maxPos, "", false, false);
                    } else {
                        messageLog.fireMessage("!!Es konnten keine ChangeSet-Einträge für Stücklisteneinträge mit leerer Materialnummer ermittelt werden.");
                    }

                    return true;
                };

                project.getRevisionsHelper().executeWithoutActiveChangeSets(
                        () -> executeSQLStatements(project, "!!Materialnummer in ChangeSet-Einträgen ergänzen",
                                                   "!!Sollen alle ChangeSet-Einträge für Stücklisteneinträge mit leerer Materialnummer ergänzt werden?",
                                                   null, runnable, false), false, project);
            }
        };
    }

    /**
     * Alle Werte für minimales KEM-Datum-ab und maximales KEM-Datum-bis für die ausgewählte Baureihe neu berechnen.
     *
     * @return
     */
    public static EtkFunction recalculateMinMaxKEMDates(final EtkProject project) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                String seriesNumber = InputDialog.show("!!KEM-Datumswerte neu berechnen", "!!Baureihe", "", false);
                if (StrUtils.isEmpty(seriesNumber)) {
                    return;
                }

                ExecuteSQLStatementsRunnable runnable = (project, db, messageLog) -> {
                    List<iPartsProduct> products = iPartsProductHelper.getProductsForSeries(project, new iPartsSeriesId(seriesNumber), null, null);
                    messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere %1 Produkte für Baureihe \"%2\"...",
                                                                        String.valueOf(products.size()), seriesNumber),
                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    if (products.isEmpty()) {
                        return false;
                    }

                    final int modulesPerProduct = 500; // Normierung auf 500 Module pro Produkt für den Fortschritt
                    int maxProgress = products.size() * modulesPerProduct;
                    messageLog.fireProgress(0, maxProgress, "", false, false);

                    final iPartsDIALOGPositionsHelper missingDialogPositionsHelper = new iPartsDIALOGPositionsHelper(null);
                    ObjectInstanceLRUList<HmMSmId, iPartsDIALOGPositionsHelper> dialogPositionsHelperMap = new ObjectInstanceLRUList<>(500, MAX_CACHE_LIFE_TIME_CORE);
                    int productCounter = 0;
                    for (iPartsProduct product : products) {
                        messageLog.fireMessage(TranslationHandler.translate("!!Korrigiere Produkt \"%1\":", product.getAsId().getProductNumber()),
                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        Set<AssemblyId> assemblyIds = iPartsProductStructures.getInstance(project, product.getAsId()).getModuleIds(project);
                        messageLog.fireMessage(TranslationHandler.translate("!!- Minimales KEM-Datum-ab und maximales KEM-Datum-bis für %1 TUs werden neu berechnet...",
                                                                            String.valueOf(assemblyIds.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                        // Minimales KEM-Datum-ab und maximales KEM-Datum-bis neu berechnen
                        // Mischung aus VTNVDataImporter.getBCTEKeysForKemChain() und EditTransferToASHelper.createAndTransferPartListEntriesDIALOG()
                        double assemblyCounter = 0.0d;
                        for (AssemblyId assemblyId : assemblyIds) {
                            // Ohne Caches, da ansonsten zu viele Module im Cache landen würden
                            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId, false);
                            for (EtkDataPartListEntry partListEntry : assembly.getPartListUnfiltered(null, false, false)) {
                                if (Thread.currentThread().isInterrupted()) {
                                    messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                                    return false;
                                }

                                iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
                                if (bcteKey == null) {
                                    continue;
                                }

                                HmMSmId hmMSmId = bcteKey.getHmMSmId();
                                iPartsDIALOGPositionsHelper dialogPositionsHelper = dialogPositionsHelperMap.get(hmMSmId);
                                if (dialogPositionsHelper == null) {
                                    String virtuelIdString = iPartsVirtualNode.getVirtualIdString(hmMSmId);
                                    AssemblyId constructionAssemblyId = new AssemblyId(virtuelIdString, "");

                                    EtkDataAssembly constructionAssembly = EtkDataObjectFactory.createDataAssembly(project,
                                                                                                                   constructionAssemblyId,
                                                                                                                   false);

                                    if (constructionAssembly.existsInDB()) {
                                        dialogPositionsHelper = new iPartsDIALOGPositionsHelper(constructionAssembly.getPartListUnfiltered(null));
                                        dialogPositionsHelperMap.put(hmMSmId, dialogPositionsHelper);
                                    } else {
                                        messageLog.fireMessage(TranslationHandler.translate("!!DIALOG-Stückliste fehlt für HM/M/SM-Knoten %1",
                                                                                            hmMSmId.toString("/")),
                                                               MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                                        dialogPositionsHelper = missingDialogPositionsHelper;
                                        dialogPositionsHelperMap.put(hmMSmId, missingDialogPositionsHelper);
                                    }
                                }

                                if (dialogPositionsHelper != missingDialogPositionsHelper) {
                                    EtkDataPartListEntry constructionPLE = dialogPositionsHelper.getPositionVariantByBCTEKey(bcteKey);
                                    if (constructionPLE != null) {
                                        EditConstructionToRetailHelper.calculateMinMaxKEMDates(constructionPLE, dialogPositionsHelper);
                                        String minKemDateFrom = constructionPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM);
                                        String maxKemDateTo = constructionPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO);
                                        partListEntry.setFieldValue(FIELD_K_MIN_KEM_DATE_FROM, minKemDateFrom, DBActionOrigin.FROM_EDIT);
                                        partListEntry.setFieldValue(FIELD_K_MAX_KEM_DATE_TO, maxKemDateTo, DBActionOrigin.FROM_EDIT);
                                        partListEntry.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                    }
                                }
                            }
                            assemblyCounter++;
                            int progress = productCounter * modulesPerProduct + (int)Math.ceil(assemblyCounter * modulesPerProduct / assemblyIds.size());
                            messageLog.fireProgress(progress, maxProgress, "", false, true);
                        }
                        productCounter++;
                    }

                    return productCounter > 0;
                };

                VarParam<Boolean> result = new VarParam<>(false);
                project.getRevisionsHelper().executeWithoutActiveChangeSets(
                        () -> result.setValue(executeSQLStatements(project, "!!KEM-Datumswerte neu berechnen",
                                                                   null, null, runnable, false)),
                        false, project);

                if (result.getValue()) {
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                }
            }
        };
    }

    private interface ExecuteSQLStatementsRunnable {

        /**
         * Führt beliebige SQL-Statements in dem übergebenen {@link EtkProject} mit der übergebenen {@link DBBase} und dem
         * {@link EtkMessageLog} aus.
         *
         * @param project
         * @param db
         * @param messageLog
         * @return {@code true} falls die SQL-Statements erfolgreich beendet werden konnten und die Änderungen committed
         * werden sollen
         */
        boolean executeSQLStatements(EtkProject project, DBBase db, EtkMessageLog messageLog);
    }
}
