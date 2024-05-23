/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaCacheId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsRetailUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Event in iParts, der anzeigt, dass über Edit-Funktionalitäten (z.B. Stammdateneditor) Daten verändert wurden. Der konkret
 * veränderte Datentyp wird über {@link DataType} angegeben, die durchgeführte Aktion über {@link Action} und die betroffenen
 * IDs über {@link #elementIds}. Außerdem kann angegeben werden, ob der gesamte Cache von diesem Datentyp gelöscht
 * werden muss.
 */
public class iPartsDataChangedEventByEdit<E extends IdWithType> extends AbstractEtkClusterEvent {

    // Achtung! Wenn hier ein neuer DataType hinzugefügt wird, dann muss dieser auch in setElementIdsForJSON() beachtet werden!
    public enum DataType {
        PRODUCT, SA, SAA, SERIES, MODEL, MATERIAL, PART_LIST, RETAIL_USAGE, DRAWING, HMMSM, PEM, DICTIONARY
    }

    public enum Action {NEW, MODIFIED, DELETED}

    private DataType dataType;
    private Action action;
    private Collection<E> elementIds;
    private boolean clearDataTypeCache;

    public iPartsDataChangedEventByEdit() {
    }

    /**
     * Event zum Löschen des Caches für den angegebenen {@link DataType}.
     *
     * @param dataType
     */
    public iPartsDataChangedEventByEdit(DataType dataType) {
        super(null, dataType != DataType.DICTIONARY);
        this.dataType = dataType;
        clearDataTypeCache = true;
    }

    /**
     * Event für die angegebene {@link Action} von einem Element vom angegebenen {@link DataType} und optionaler
     * <i>elementId</i> sowie Hinweis, ob der Cache dieses {@link DataType}s gelöscht werden soll.
     *
     * @param dataType
     * @param action
     * @param elementId
     * @param clearDataTypeCache
     */
    public iPartsDataChangedEventByEdit(DataType dataType, Action action, E elementId, boolean clearDataTypeCache) {
        super(null, dataType != DataType.DICTIONARY);
        this.dataType = dataType;
        this.action = action;
        if (elementId != null) {
            this.elementIds = new DwList<>(1);
            elementIds.add(elementId);
        }
        this.clearDataTypeCache = clearDataTypeCache;
    }

    /**
     * Event für die angegebene {@link Action} von mehreren Elementen vom angegebenen {@link DataType} und optionalen
     * <i>elementIds</i> sowie Hinweis, ob der Cache dieses {@link DataType}s gelöscht werden soll.
     *
     * @param dataType
     * @param action
     * @param elementIds
     * @param clearDataTypeCache
     */
    public iPartsDataChangedEventByEdit(DataType dataType, Action action, Collection<E> elementIds, boolean clearDataTypeCache) {
        super(null, dataType != DataType.DICTIONARY);
        this.dataType = dataType;
        this.action = action;
        this.elementIds = elementIds;
        this.clearDataTypeCache = clearDataTypeCache;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @JsonIgnore
    public Collection<E> getElementIds() {
        return elementIds;
    }

    @JsonIgnore
    public void setElementIds(List<E> elementIds) {
        this.elementIds = elementIds;
    }

    /**
     * Wird nur für die Serialisierung der {@link #elementIds} nach JSON benötigt.
     *
     * @return
     */
    public List<String[]> getElementIdsForJSON() {
        if (elementIds != null) {
            List<String[]> elementIdsAsString = new ArrayList<String[]>(elementIds.size());
            for (E elementId : elementIds) {
                elementIdsAsString.add(elementId.toStringArrayWithoutType());
            }
            return elementIdsAsString;
        }

        return null;
    }

    /**
     * Wird nur für die Deserialisierung der {@link #elementIds} von JSON benötigt.
     *
     * @param elementIdsAsString
     */
    public void setElementIdsForJSON(List<String[]> elementIdsAsString) {
        if (elementIdsAsString != null) {
            elementIds = new ArrayList<E>(elementIdsAsString.size());
            for (String[] elementIdAsString : elementIdsAsString) {
                switch (dataType) {
                    case PRODUCT:
                        elementIds.add((E)new iPartsProductId(elementIdAsString[0]));
                        break;
                    case SA:
                        elementIds.add((E)new iPartsSaId(elementIdAsString[0]));
                        break;
                    case SAA:
                        elementIds.add((E)new iPartsSaaId(elementIdAsString[0]));
                        break;
                    case SERIES:
                        elementIds.add((E)new iPartsSeriesId(elementIdAsString[0]));
                        break;
                    case MODEL:
                        elementIds.add((E)new iPartsModelId(elementIdAsString[0]));
                        break;
                    case MATERIAL:
                        elementIds.add((E)new iPartsPartId(elementIdAsString[0], elementIdAsString[1]));
                        break;
                    case PART_LIST:
                        elementIds.add((E)new iPartsAssemblyId(elementIdAsString[0], elementIdAsString[1]));
                        break;
                    case RETAIL_USAGE:
                        elementIds.add((E)new iPartsRetailUsageId(elementIdAsString[0], elementIdAsString[1]));
                        break;
                    case DRAWING:
                        elementIds.add((E)new PoolId(elementIdAsString[0], elementIdAsString[1], elementIdAsString[2], elementIdAsString[3]));
                        break;
                    case HMMSM:
                        elementIds.add((E)new HmMSmId(elementIdAsString[0], elementIdAsString[1], elementIdAsString[2], elementIdAsString[3]));
                        break;
                    case PEM:
                        elementIds.add((E)new iPartsPemId(elementIdAsString[0], elementIdAsString[1]));
                        break;
                    case DICTIONARY:
                        elementIds.add((E)new iPartsDictMetaCacheId(elementIdAsString[0], elementIdAsString[1], elementIdAsString[2], elementIdAsString[3], elementIdAsString[4], elementIdAsString[5], elementIdAsString[6]));
                        break;
                    default:
                        Logger.log(iPartsPlugin.LOG_CHANNEL_INTER_APP_COM_CLIENT, LogType.ERROR, "Unknown data type in iPartsDataChangedEventByEdit for deserialization: "
                                                                                                 + dataType.name());
                        break;
                }
            }
        } else {
            elementIds = null;
        }
    }

    public boolean isClearDataTypeCache() {
        return clearDataTypeCache;
    }

    public void setClearDataTypeCache(boolean clearDataTypeCache) {
        this.clearDataTypeCache = clearDataTypeCache;
    }
}
