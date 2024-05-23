/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarnessList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashSet;
import java.util.Set;

/**
 * Cache für die Leitungssatzbaukästen (speziell deren Materialnummern)
 */
public class iPartsWireHarness implements CacheForGetCacheDataEvent<iPartsWireHarness>, iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsWireHarness> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    @JsonProperty
    protected Set<String> wireHarnessSet;
    private volatile EtkDataPart wireHarnessDummyDataPart;

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsWireHarness.class, "WireHarness", false);
    }

    public static synchronized iPartsWireHarness getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        iPartsWireHarness result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsWireHarness(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsWireHarness();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }


    @Override
    public iPartsWireHarness createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        return createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));
    }

    private void load(EtkProject project) {
        wireHarnessSet = new HashSet<>();

        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                wireHarnessSet.add(attributes.getFieldValue(FIELD_DWH_SNR));
                return false;
            }
        };

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR, false, false));

        iPartsDataWireHarnessList dataWireHarnessList = new iPartsDataWireHarnessList();
        dataWireHarnessList.searchSortAndFillWithJoin(project, null, selectFields, null, null, false, null, null, false,
                                                      null, false, false, true, foundAttributesCallback, false);
    }

    /**
     * Überprüft, ob eine Materialnummer ein Leitungssatzbaukasten ist
     *
     * @param partId
     * @return
     */
    public boolean isWireHarness(PartId partId) {
        return isWireHarness(partId.getMatNr());
    }

    /**
     * Überprüft, ob eine Materialnummer ein Leitungssatzbaukasten ist
     *
     * @param partNo
     * @return
     */
    public boolean isWireHarness(String partNo) {
        return wireHarnessSet.contains(partNo);
    }

    public EtkDataPart getWireHarnessDummyDataPart() {
        if ((wireHarnessDummyDataPart == null) || !iPartsWireHarnessHelper.isWireHarnessDummyPart(wireHarnessDummyDataPart)) {
            this.wireHarnessDummyDataPart = null; // falls sich die Dummy-Teilenummer geändert hat

            // Es muss das Hintergrund-EtkProject zum Laden des Dummy-Materials verwendet werden, da alle anderen EtkProjects
            // und deren DB-Verbindung nur temporär leben; außerdem alle ExtendedDataTypes explizit schon laden
            iPartsPlugin.assertProjectDbIsActive(iPartsPlugin.getMqProject(), "Wire harness dummy part", iPartsPlugin.LOG_CHANNEL_DEBUG);
            EtkDataPart wireHarnessDummyDataPart = EtkDataObjectFactory.createDataPart(iPartsPlugin.getMqProject(), new iPartsPartId(iPartsWireHarnessHelper.getDummyWireHarnessPartNoFromConfig(), ""));
            if (!wireHarnessDummyDataPart.existsInDB()) {
                wireHarnessDummyDataPart.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            }
            wireHarnessDummyDataPart.loadAllExtendedDataTypeAttributes();

            // Damit die Icon Prüfung auch beim Dummy-Part funktioniert, wird hier das sonstige-KZ auf "LA" gesetzt.
            // Einzelteile eines Leitungssatz-BK dürfen nur angezeigt werden, wenn die Stücklistenposition (der eigentliche Leitungssatz)
            // den Wert sonstige-KZ = "LA" gesetzt hat. Das Dummy-Part wird auch nur verwendet, wenn der echte Leitungssatz
            // selber schon den Wert sonstige-KZ = "LA" gesetzt hat.
            wireHarnessDummyDataPart.setFieldValue(iPartsConst.FIELD_M_LAYOUT_FLAG,
                                                   iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG,
                                                   DBActionOrigin.FROM_DB);
            synchronized (this) {
                if (this.wireHarnessDummyDataPart == null) {
                    this.wireHarnessDummyDataPart = wireHarnessDummyDataPart;
                }
            }
        }
        return wireHarnessDummyDataPart;
    }
}