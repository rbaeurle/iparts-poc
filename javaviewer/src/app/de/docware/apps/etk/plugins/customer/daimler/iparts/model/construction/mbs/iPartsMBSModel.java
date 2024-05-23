/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructureList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsMBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMBSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.iPartsMBSModelTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataSetCancelable;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Klasse zum cachen der echten Struktur zu einem MBS Baumuster. "Echt" bedeutet, dass am Ende einer Baugruppe auch eine
 * Stückliste vorhanden ist. {@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructure} enthält
 * die möglichen Strukturen zu allen MBS Baumuster
 */
public class iPartsMBSModel implements iPartsConst {

    private static ObjectInstanceLRUList<Object, iPartsMBSModel> instances = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_MODELS,
                                                                                                         iPartsPlugin.getCachesLifeTime());
    private static final String ALIAS_FOR_JOIN = "C";

    public static final String FIELD_STRUCTURE_MBS_RELEASE_FROM_DATE = VirtualFieldsUtils.addVirtualFieldMask("STRUCTURE_MBS_RELEASE_FROM_DATE");

    private volatile Set<MBSStructureId> mbsStructureModuleIds;
    private volatile Map<MBSStructureId, iPartsDataMBSStructure> structureIdToData;

    protected iPartsModelId modelId;
    protected EtkMultiSprache modelTitle;
    private iPartsMBSModelTextHelper modelHelper;

    private volatile iPartsCatalogNode cachedMBSStructure;

    /**
     * Handelt es sich bei der übergebenen Baumusternummer um ein gültiges MBS-Baumuster bezogen auf das in der Session
     * eingestellte MBS-Konstruktions-Datum?
     * Dabei wird ein leeres Freigabedatum berücksichtigt und, falls modelData besetzt ist, das größte ReleaseFrom-Datum
     * aus Structure_MBS hiunzugefügt
     *
     * @param modelNumber
     * @param modelData
     * @param project
     * @return
     */
    public static boolean isValidMBSModel(String modelNumber, iPartsDataModel modelData, EtkProject project) {
        // Pseudo-Baumuster, die nur ein Mapping zwischen C- und identischem D-Baumuster bzw. C-Baumuster mit Suffix sind,
        // sollen hier nicht berücksichtigt werden
        if (modelNumber.endsWith(MBS_VEHICLE_AGGREGATE_MAPPING_SUFFIX)) {
            return false;
        }

        // Check, ob zum Baumuster mind. ein Datensatz existiert, der eine gültige MBS Struktur sowie gültige Stücklisten-
        // positionen enthält
        // Datum zur Prüfung aus der Session holen
        String validationDate = SessionKeyHelper.getMbsConstructionDBDate();
        String lastReleaseFrom = null;
        if (!StrUtils.isValid(validationDate) && (modelData != null)) {
            // Leeres Freigabedatum => suche größtes ReleaseFrom Datum
            lastReleaseFrom = searchLastReleaseFrom(project, modelNumber);
            if (lastReleaseFrom == null) {
                return false;
            }
        }
        DBSQLQuery query = iPartsMBSHelper.createQueryForValidMBSStructureData(project, modelNumber, validationDate);

        DBDataSetCancelable dataSet = null;
        try {
            // Query ausführen
            dataSet = query.executeQueryCancelable();
            if (dataSet != null) {
                while (dataSet.next()) {
                    // Datensatz gefunden
                    List<String> fieldValues = dataSet.getStringList();
                    if (!StrUtils.isValid(fieldValues.get(0))) { // Obere Sachnummer = Baumuster
                        continue;
                    }

                    String subSnr = fieldValues.get(1); // Unter Sachnummer
                    // Ist die untere Sachnummer leer, ein Aggregate- oder ein Fahrzeugbaumuster, kann der Datensatz übersprungen werden
                    if (subSnr.isEmpty() || iPartsModel.isAggregateModel(subSnr) || iPartsModel.isVehicleModel(subSnr)) {
                        continue;
                    }

                    if (modelData != null) {
                        String value = "";
                        if (lastReleaseFrom != null) {
                            value = lastReleaseFrom;
                        }
                        if (!modelData.existsInDB()) {
                            modelData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                        }
                        modelData.getAttributes().addField(FIELD_STRUCTURE_MBS_RELEASE_FROM_DATE, value, true, DBActionOrigin.FROM_DB);
                    }

                    return true;
                }
            }
        } catch (CanceledException ignored) {
        } finally {
            // Verbindung schließen
            if (dataSet != null) {
                dataSet.close();
            }
        }
        return false;
    }

    /**
     * Handelt es sich bei der übergebenen Baumusternummer um ein gültiges MBS-Baumuster bezogen auf das in der Session
     * eingestellte MBS-Konstruktions-Datum?
     *
     * @param modelNumber
     * @param project
     * @return
     */
    public static boolean isValidMBSModel(String modelNumber, EtkProject project) {
        return isValidMBSModel(modelNumber, null, project);
    }

    /**
     * Bestimmt in {@code DA_STRUCTURE_MBS} das größte {@code DSM_RELEASE_FROM} zu einem Baumuster
     *
     * @param project
     * @param modelNumber
     * @return
     */
    private static String searchLastReleaseFrom(EtkProject project, String modelNumber) {
        iPartsDataMBSStructureList list = iPartsDataMBSStructureList.searchLastReleaseDateFrom(project, modelNumber);
        if (list.isEmpty()) {
            return null;
        } else {
            // Erster Eintrag ist aufgrund der absteigenden Sortierung das höchste DSM_RELEASE_FROM
            return list.get(0).getFieldValue(FIELD_DSM_RELEASE_FROM);
        }
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsMBSModel getInstance(EtkProject project, iPartsModelId modelId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsMBSModel.class, modelId.getModelNumber(), false);
        iPartsMBSModel result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsMBSModel(project, modelId);
            instances.put(hashObject, result);
        }

        return result;
    }

    protected iPartsMBSModel(EtkProject project, iPartsModelId modelId) {
        this.modelId = modelId;
        this.structureIdToData = new HashMap<>();
        this.modelHelper = new iPartsMBSModelTextHelper(modelId);
        loadHeader(project);
    }

    private boolean loadHeader(EtkProject project) {
        if (modelId.isValidId()) {
            modelTitle = new EtkMultiSprache(modelId.getModelNumber(), project.getConfig().getDatabaseLanguages());
            return true;
        } else {
            modelTitle = new EtkMultiSprache("!!Baumuster '%1' nicht gefunden", project.getConfig().getDatabaseLanguages(),
                                             modelId.getModelNumber());
            return false;
        }
    }

    public String getModelNumber() {
        return modelId.getModelNumber();
    }

    public EtkMultiSprache getModelTitle() {
        return modelTitle;
    }

    protected iPartsCatalogNode getOrCreateMBSNode(iPartsCatalogNode parentNode, MBSStructureId mbsStructureId) {
        // Erst die MBS-Struktur erstellen oder holen
        iPartsCatalogNode listNumber = parentNode.getOrCreateChild(new MBSStructureId(mbsStructureId.getListNumber(), ""), true);
        return listNumber.getOrCreateChild(mbsStructureId, true);
    }

    // Module der MBS Struktur laden
    protected void loadIfNeeded(EtkProject project) {
        if (mbsStructureModuleIds == null) {
            Set<MBSStructureId> newMBSStructureModuleIds = new LinkedHashSet<>();
            iPartsDataMBSStructureList structureList = new iPartsDataMBSStructureList();

            EtkDataObjectList.FoundAttributesCallback callback = getCallbackForStructureLoad(project, newMBSStructureModuleIds);
            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_STRUCTURE_MBS));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, false, false));
            structureList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                                    new String[]{ TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR) },
                                                    new String[]{ modelId.getModelNumber() },
                                                    false,
                                                    new String[]{ StrUtils.makeDelimitedString("_", ALIAS_FOR_JOIN, FIELD_DSM_SNR),
                                                                  StrUtils.makeDelimitedString("_", ALIAS_FOR_JOIN, FIELD_DSM_SUB_SNR) },
                                                    false,
                                                    callback,
                                                    new EtkDataObjectList.JoinData(TABLE_DA_STRUCTURE_MBS,
                                                                                   new String[]{ FIELD_DSM_SUB_SNR },
                                                                                   new String[]{ FIELD_DSM_SNR },
                                                                                   false, false, ALIAS_FOR_JOIN.charAt(0)),
                                                    new EtkDataObjectList.JoinData(TABLE_DA_PARTSLIST_MBS,
                                                                                   new String[]{ TableAndFieldName.make(ALIAS_FOR_JOIN, FIELD_DSM_SUB_SNR) },
                                                                                   new String[]{ FIELD_DPM_SNR },
                                                                                   // LeftOuter Join, um an SAA/GS -> Teilenummern ranzukommen
                                                                                   true, false));

            synchronized (this) {
                if (mbsStructureModuleIds == null) {
                    mbsStructureModuleIds = newMBSStructureModuleIds;
                }
            }
        }
    }

    /**
     * Liefert den Callback zum erzeugen der MBS Struktur Baumuster auf GS/SAA und GS/SAA -> KG zu GS/SAA
     *
     * @param project
     * @param newMBSStructureModuleIds
     * @return
     */
    private EtkDataObjectList.FoundAttributesCallback getCallbackForStructureLoad(EtkProject project, Set<MBSStructureId> newMBSStructureModuleIds) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String listNumber = attributes.getFieldValue(StrUtils.makeDelimitedString("_", ALIAS_FOR_JOIN, FIELD_DSM_SNR));
                // C und D Nummern (Baumuster) dürfen in der Struktur nicht berücksichtigt werden
                if (iPartsModel.isAggregateModel(listNumber) || iPartsModel.isVehicleModel(listNumber)) {
                    return false;
                }
                String conGroup = attributes.getFieldValue(StrUtils.makeDelimitedString("_", ALIAS_FOR_JOIN, FIELD_DSM_SUB_SNR));
                if (iPartsModel.isAggregateModel(conGroup) || iPartsModel.isVehicleModel(conGroup)) {
                    return false;
                }

                // Flag, ob es eine SAA/GS mit direkten Teilenummern ist (ohne KG Ebene)
                boolean isListNumberWithPartNumber;
                if (StrUtils.isValid(conGroup)) {
                    isListNumberWithPartNumber = !conGroup.startsWith(BASE_LIST_NUMBER_PREFIX) && !conGroup.startsWith(SAA_NUMBER_PREFIX);
                    if (!isListNumberWithPartNumber) {
                        // Check, ob es echte Stücklistendaten gibt (Blick in DA_PARTSLIST_MBS)
                        String partslistData = attributes.getFieldValue(StrUtils.makeDelimitedString(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR));
                        if (StrUtils.isEmpty(partslistData)) {
                            return false;
                        }
                    }
                } else {
                    // Falls die ConGroup leer ist, hängt direkt ein Text an der SAA/GS -> auch dafür einen Pseudo-KG Knoten
                    // erstellen, falls es Knoten gibt, die nur Texte in der Unterstruktur haben
                    isListNumberWithPartNumber = true;
                }

                // Da wir hier einen Join von DA_STRUCTURE_MBS auf DA_STRUCTURE_MBS, d.h. pro attributes haben wir
                // die Informationen zu zwei Ebenen. Um aus dem attributes-Datensatz zwei machen zu können, wird hier
                // ein attributes-Datensatz für den unteren Strukturknoten erzeugt
                DBDataObjectAttributes subStructAttributes = new DBDataObjectAttributes();
                attributes.values().removeIf(attribute -> {
                    // Attribute mit dem ALIAS sind Attribute des Datensatzes zur unteren Ebene
                    if (attribute.getName().startsWith(ALIAS_FOR_JOIN + "_")) {
                        attribute.__internal_setName(StrUtils.removeFirstCharacterIfCharacterIs(attribute.getName(), ALIAS_FOR_JOIN + "_"));
                        subStructAttributes.addField(attribute, DBActionOrigin.FROM_DB);
                        return true;
                    }
                    return false;
                });


                // Falls die KEM-Datums ab und bis eines Datensatzes gleich sind, ist dieser ungültig
                // und soll nicht verarbeitet werden
                String releaseFromDate = attributes.getFieldValue(FIELD_DSM_RELEASE_FROM);
                String releaseToDate = attributes.getFieldValue(FIELD_DSM_RELEASE_TO);
                if (releaseFromDate.equals(releaseToDate)) {
                    return false;
                }

                // Auch für die Unterstruktur prüfen
                String subAttributesReleaseFrom = subStructAttributes.getFieldValue(FIELD_DSM_RELEASE_FROM);
                String subAttributesReleaseTo = subStructAttributes.getFieldValue(FIELD_DSM_RELEASE_TO);
                if (subAttributesReleaseFrom.equals(subAttributesReleaseTo)) {
                    return false;
                }

                // Attribute zur oberen Ebene (Baumuster auf GS/SAA)
                // Falls der Datensatz ungültig ist oder nicht der neuste, wird er nicht verarbeitet.
                // Dann müssen auch die Unterstrukturen nicht verarbeitet werden
                boolean objectAdded = addObject(attributes);

                // Bei SAA/GS mit Teilenummern wird der Datensatz für die SAA/GS auch für die KG-Ebene verwendet
                if (isListNumberWithPartNumber) {
                    MBSStructureId mbsStructureId = new MBSStructureId(listNumber, listNumber);
                    newMBSStructureModuleIds.add(mbsStructureId);
                    // Datensatz für ohneKG auch aktualisieren, wenn der zugehörige Struktur-Datensatz überschrieben wurde
                    if (objectAdded) {
                        // Gleichen Datensatz mit Anpassungen bzgl. der Sortierung (immer ganz oben) verwenden
                        iPartsDataMBSStructure objectForListNumber = structureIdToData.get(new MBSStructureId(listNumber, ""));
                        if (objectForListNumber != null) {
                            objectForListNumber = objectForListNumber.cloneMe(project);
                            objectForListNumber.setFieldValue(FIELD_DSM_POS, "0", DBActionOrigin.FROM_DB);
                            objectForListNumber.setFieldValue(FIELD_DSM_SORT, "0", DBActionOrigin.FROM_DB);
                            objectForListNumber.updateIdFromPrimaryKeys(); // DSM_POS und DSM_SORT sind Teil vom Primärschlüssel
                            objectForListNumber.updateOldId();
                            structureIdToData.put(mbsStructureId, objectForListNumber);
                        }
                    }
                } else {
                    // Attribute zur unteren Ebene (GS/SAA auf KG zu GS/SAA)
                    MBSStructureId mbsStructureId = new MBSStructureId(listNumber, conGroup);
                    newMBSStructureModuleIds.add(mbsStructureId);
                    addObject(subStructAttributes);
                }

                return false;
            }

            /**
             * Fügt ein {@link iPartsDataMBSStructure} zu den übergebenen Attributen hinzu
             *
             * @param attributes
             * @return Ob das Objekt hinzugefügt wurde
             */
            private boolean addObject(DBDataObjectAttributes attributes) {
                String snr = attributes.getFieldValue(FIELD_DSM_SNR);
                String subSnr = attributes.getFieldValue(FIELD_DSM_SUB_SNR);

                // Textdatensätze, die nicht direkt unter einer SAA/GS hängen sollen nicht verarbeitet werden,
                // da diese im Helper geladen werden
                if (StrUtils.isEmpty(subSnr)) {
                    return false;
                }

                // Ist die obere Sachnummer, das Baumuster nach dem gesucht wurde, dann beziehen sich die Daten auf die
                // untere Sachnummer (Infos in jeder DB Zeile beziehen sich auf die untere Sachnummer)
                if (snr.equals(modelId.getModelNumber())) {
                    snr = subSnr;
                    subSnr = "";
                }

                MBSStructureId mbsStructureId = new MBSStructureId(snr, subSnr);
                // Es kann zu einem Strukturknoten mehrere Stände geben. Damit wir beim Laden der Stückliste auf die neuesten
                // Informationen zugreifen können, muss hier der neueste Datensatz gespeichert werden
                // (Beim Laden wird anhand des Datums geprüft, ob ein Knoten gültig ist)
                // Ab DAIMLER-11043: Falls die zwei Datensätze ein gleiches KEM ab Datum haben, gewinnt der Datensatz, der
                // das höhere KEM bis Datum hat.
                iPartsDataMBSStructure existingObject = structureIdToData.get(mbsStructureId);
                if (existingObject != null) {
                    String existingReleaseFromDate = existingObject.getFieldValue(FIELD_DSM_RELEASE_FROM);
                    String existingReleaseToDate = existingObject.getFieldValue(FIELD_DSM_RELEASE_TO);
                    String newReleaseFromDate = attributes.getFieldValue(FIELD_DSM_RELEASE_FROM);
                    if (existingReleaseFromDate.compareTo(newReleaseFromDate) > 0) {
                        return false;
                    }

                    if (existingReleaseFromDate.equals(newReleaseFromDate)) {
                        // Falls das existierende KEM bis unendlich ist, ist es eh am höchsten
                        if (StrUtils.isEmpty(existingReleaseToDate)) {
                            return false;
                        }

                        String newReleaseToDate = attributes.getFieldValue(FIELD_DSM_RELEASE_TO);
                        if (existingReleaseToDate.compareTo(newReleaseToDate) > 0) {
                            return false;
                        }
                    }
                }

                String snrSuffix = attributes.getFieldValue(FIELD_DSM_SNR_SUFFIX);
                String pos = attributes.getFieldValue(FIELD_DSM_POS);
                String sortValue = attributes.getFieldValue(FIELD_DSM_SORT);
                String kemFrom = attributes.getFieldValue(FIELD_DSM_KEM_FROM);
                iPartsMBSStructureId structureId = new iPartsMBSStructureId(snr, snrSuffix, pos, sortValue, kemFrom);
                iPartsDataMBSStructure structure = new iPartsDataMBSStructure(project, structureId);
                structure.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
                structureIdToData.put(mbsStructureId, structure);
                return true;
            }
        };
    }

    public iPartsCatalogNode getCompleteMBSStructure(EtkProject project) {
        if (cachedMBSStructure != null) {
            return cachedMBSStructure;
        }

        loadIfNeeded(project);
        iPartsCatalogNode result = new iPartsCatalogNode(modelId, true);
        for (MBSStructureId structureId : mbsStructureModuleIds) {
            getOrCreateMBSNode(result, structureId);
        }
        synchronized (this) {
            if (cachedMBSStructure == null) {
                cachedMBSStructure = result;
            }
        }

        return cachedMBSStructure;
    }


    /**
     * Liefert die komplette Struktur gefiltert aus Basis des aktuellen Datums
     *
     * @param project
     * @return
     */
    public iPartsCatalogNode getCompleteMBSStructureFiltered(EtkProject project) {
        iPartsCatalogNode completeStructure = getCompleteMBSStructure(project);
        iPartsCatalogNode result = new iPartsCatalogNode(modelId, true);
        for (iPartsCatalogNode node : completeStructure.getChildren()) {
            addNodeToFilteredResult(result, node);
        }
        return result;
    }

    /**
     * Prüft, ob der übergebene Knoten zum aktuellen Datum passt. Falls ja wird er an den <code>upperNode</code> gehängt.
     * Hat der gültige Knote noch Kindknoten, werden diese ebenfalls geprüft.
     *
     * @param upperNode
     * @param node
     */
    private void addNodeToFilteredResult(iPartsCatalogNode upperNode, iPartsCatalogNode node) {
        if (node.getId() instanceof MBSStructureId) {
            MBSStructureId structureId = (MBSStructureId)node.getId();
            if (!isValidStructure(structureId)) {
                return;
            }
            upperNode.addChild(node);
            if (structureId.isListNumberNode()) {
                for (iPartsCatalogNode conGroupNode : node.getChildren()) {
                    addNodeToFilteredResult(node, conGroupNode);
                }
            }
        }
    }

    public boolean isValidStructure(MBSStructureId structureId) {
        iPartsDataMBSStructure dataMBSStructure = getDataObjectForStructureId(structureId);
        return iPartsMBSHelper.isValidDataset(dataMBSStructure);
    }

    public iPartsDataMBSStructure getDataObjectForStructureId(MBSStructureId structureId) {
        return structureIdToData.get(structureId);
    }

    public void loadSubTextsForModelId(EtkProject project) {
        this.modelHelper.loadSubTextsForModelId(project);
    }

    public void loadSubTextsForStructureId(EtkProject project, MBSStructureId structureId) {
        this.modelHelper.loadSubTextsForStructureId(project, structureId);
    }

    /**
     * Lädt die Struktur und die Texte des Baumusters
     *
     * @param project
     */
    public void loadStructureAndTexts(EtkProject project) {
        getCompleteMBSStructure(project);
        loadSubTextsForModelId(project);
    }
}
