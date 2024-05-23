package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEMList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAAList;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.*;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.TABLE_DA_SCOPE_KG_MAPPING;

/**
 * Helper-Klasse zur Überprüfung der Scope- und KG-Werte aus NutzDokSAA/KEM
 */
public class iPartsNutzDokConverter {

    private EtkProject project;
    private EtkMessageLogForm messageLogForm;
    private int totalDbNutzDokSaaCount;
    private int totalDbNutzDokKemCount;
    private int totalDbCount;
    private int counter;
    private Map<String, Set<String>> kgsPerScopeMap;
    private Map<String, Set<String>> scopesPerKgMap;
    private Set<String> knownKgs;

    public iPartsNutzDokConverter(EtkProject project, EtkMessageLogForm messageLogForm) {
        this.project = project;
        this.messageLogForm = messageLogForm;
    }

    private void init() {
        kgsPerScopeMap = new HashMap<>();
        scopesPerKgMap = new HashMap<>();
        knownKgs = new HashSet<>();
        // nochmals die Werte aus dem Cache bestimmen
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_SCOPE_KG_MAPPING);
        // Alle Daten aus der Datenbank lesen
        for (DBDataObjectAttributes attribute : attributesList) {
            // Den Scope (=Umfang) gibt es nur einmal.
            String scope = attribute.getFieldValue(iPartsConst.FIELD_DSKM_SCOPE_ID);
            // Aber zum Scope kann es mehrere KGs geben!
            String kg = attribute.getFieldValue(iPartsConst.FIELD_DSKM_KG);
            if (StrUtils.isValid(scope, kg)) {
                Set<String> kgs = kgsPerScopeMap.computeIfAbsent(scope, k -> new TreeSet<>());
                kgs.add(kg);
                // umgekehrte Map (Scopes pro KG)
                Set<String> scopes = scopesPerKgMap.computeIfAbsent(kg, s -> new TreeSet<>());
                scopes.add(scope);
                // gültige KGs aufsammeln
                knownKgs.add(kg);
            }
        }
        // für die Zählungen und Anzeige
        totalDbNutzDokSaaCount = project.getEtkDbs().getRecordCount(iPartsConst.TABLE_DA_NUTZDOK_SAA);
        totalDbNutzDokKemCount = project.getEtkDbs().getRecordCount(iPartsConst.TABLE_DA_NUTZDOK_KEM);
        totalDbCount = totalDbNutzDokSaaCount + totalDbNutzDokKemCount;
        counter = 0;
    }

    public boolean doConvert() {
        init();
        iPartsDataNutzDokSAAList saaList = handleNutzDokSAA();
        iPartsDataNutzDokKEMList kemList = handleNutzDokKEM();

        fireMessage("!!%1 Einträge überprüft. (NutzDokSAA: %2, NutzDokKEM: %3)",
                    String.valueOf(totalDbCount), String.valueOf(totalDbNutzDokSaaCount), String.valueOf(totalDbNutzDokKemCount));

        if ((saaList.size() + kemList.size()) > 0) {
            fireMessage("!!Es wurden %1 Einträge geändert und werden gespeichert. (NutzDokSAA: %2, NutzDokKEM: %3)",
                        String.valueOf(saaList.size() + kemList.size()), String.valueOf(saaList.size()), String.valueOf(kemList.size()));
            if (!doSave(saaList, kemList)) {
                fireMessage("!!Fehler beim Speichern!");
                return false;
            }
        } else {
            fireMessage("!!Keine Änderungen");
        }
        return true;
    }

    private boolean doSave(iPartsDataNutzDokSAAList saaList, iPartsDataNutzDokKEMList kemList) {
        EtkDbObjectsLayer dbLayer = project.getDbLayer();
        dbLayer.startTransaction();
        try {
            saaList.saveToDB(project);
            kemList.saveToDB(project);
            dbLayer.commit();
            return true;
        } catch (Exception e) {
            dbLayer.rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
        return false;
    }

    /**
     * Nach searchSortAndFillWithJoin() bleiben nur die modifizierten DataObjects übrig
     *
     * @return
     */
    private iPartsDataNutzDokSAAList handleNutzDokSAA() {
        iPartsDataNutzDokSAAList list = new iPartsDataNutzDokSAAList();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = createCallBack(iPartsConst.FIELD_DNS_SAA,
                                                                                           iPartsConst.FIELD_DNS_SCOPE_ID,
                                                                                           iPartsConst.FIELD_DNS_GROUP);
        list.searchSortAndFillWithJoin(project, null, null, null, null,
                                       false, new String[]{ iPartsConst.FIELD_DNS_SAA }, false,
                                       false, foundAttributesCallback);
        return list;
    }

    /**
     * Nach searchSortAndFillWithJoin() bleiben nur die modifizierten DataObjects übrig
     *
     * @return
     */
    private iPartsDataNutzDokKEMList handleNutzDokKEM() {
        iPartsDataNutzDokKEMList list = new iPartsDataNutzDokKEMList();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = createCallBack(iPartsConst.FIELD_DNK_KEM,
                                                                                           iPartsConst.FIELD_DNK_SCOPE_ID,
                                                                                           iPartsConst.FIELD_DNK_GROUP);
        list.searchSortAndFillWithJoin(project, null, null, null, null,
                                       false, new String[]{ iPartsConst.FIELD_DNK_KEM }, false,
                                       false, foundAttributesCallback);
        return list;
    }

    /**
     * Callback für beide Tabellen
     *
     * @param pkFieldName
     * @param scopeFieldName
     * @param kgFieldName
     * @return
     */
    private EtkDataObjectList.FoundAttributesCallback createCallBack(String pkFieldName, String scopeFieldName, String kgFieldName) {
        String tableSuffix = pkFieldName.equals(iPartsConst.FIELD_DNS_SAA) ? "SAA" : "KEM";

        return new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                counter++;
                fireProgress();
                String pkValue = attributes.getFieldValue(pkFieldName);
                String scope = attributes.getFieldValue(scopeFieldName);
                String kg = attributes.getFieldValue(kgFieldName);
                if (StrUtils.isValid(scope)) {
                    // Scope besetzt => neuer Cortex-Importer überprüfe scope und KG
                    Set<String> kgs = kgsPerScopeMap.get(scope);
                    String scopeKG = "";
                    if ((kgs != null) && !kgs.isEmpty()) {
                        scopeKG = kgs.iterator().next();
                    } else {
                        fireWarning("!!NutzDok%1: %2: %3 Scope %4 besitzt in der Mapping-Tabelle keine KG-Werte",
                                    tableSuffix, tableSuffix, pkValue, scope);
                    }
                    if (StrUtils.isValid(scopeKG) && !kg.equals(scopeKG)) {
                        fireInfo(pkValue, scope, kg, scope, scopeKG, "!!Scope besetzt, KG geändert");
                        attributes.addField(kgFieldName, scopeKG, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        return true;
                    }
                } else {
                    // Scope nicht besetzt => alter NutzDok/Cortex Importer
                    // Feststellen, ob in KG eine KG oder Scope steht
                    if (StrUtils.isValid(kg)) {
                        // probier erstmal, ob der KG-Wert ein Scope ist
                        Set<String> kgs = kgsPerScopeMap.get(kg);
                        if ((kgs != null) && !kgs.isEmpty()) {
                            // in KG steht wirklich ein Scope-Wert => tauschen und eintragen
                            String mappedKG = kgs.iterator().next();
                            fireInfo(pkValue, scope, kg, kg, mappedKG, "!!Scope leer, KG war Scope");
                            attributes.addField(scopeFieldName, kg, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                            attributes.addField(kgFieldName, mappedKG, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                            return true;
                        }
                        // steht eine bekannte KG drin?
                        if (knownKgs.contains(kg)) {
                            // es ist eine bekannte kg => Auffullen
                            Set<String> scopes = scopesPerKgMap.get(kg);
                            if ((scopes != null) && !scopes.isEmpty()) {
                                // in KG steht wirklich ein KG-Wert => Scope eintragen
                                fireInfo(pkValue, scope, kg, scopes.iterator().next(), kg, "!!Scope leer, echte KG => neuer Scope");
                                attributes.addField(scopeFieldName, scopes.iterator().next(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                                return true;
                            }
                        } else {
                            fireWarning("!!NutzDok%1: %2: %3 der KG-Wert %4 ist nicht in der Mapping-Tabelle vorhanden",
                                        tableSuffix, tableSuffix, pkValue, kg);
                        }
                    } else {
                        fireWarning("!!NutzDok%1: %2: %3 besitzt weder Scope- noch KG-Wert",
                                    tableSuffix, tableSuffix, pkValue);
                    }
                }
                return false;
            }

            private void fireProgress() {
                messageLogForm.getMessageLog().fireProgress(counter, totalDbCount, "", true, true);
            }

            private void fireInfo(String pkValue, String scope, String kg, String newScope, String scopeKG, String info) {
                if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                    fireMessage(TranslationHandler.translate("!!%1: %2 Scope: \"%3\", KG: \"%4\"; NewScope: \"%5\" ScopeKG: \"%6\". %7",
                                                             String.valueOf(counter), pkValue, scope, kg, newScope, scopeKG,
                                                             TranslationHandler.translate(info)));
                }
            }
        };
    }

    private void fireMessage(String key, String... placeHolderTexts) {
        fireMsg(MessageLogType.tmlMessage, key, placeHolderTexts);
    }

    private void fireWarning(String key, String... placeHolderTexts) {
        fireMsg(MessageLogType.tmlWarning, key, placeHolderTexts);
    }

    private void fireMsg(MessageLogType logType, String key, String... placeHolderTexts) {
        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate(key, placeHolderTexts), logType, MessageLogOption.TIME_STAMP);
    }
}
