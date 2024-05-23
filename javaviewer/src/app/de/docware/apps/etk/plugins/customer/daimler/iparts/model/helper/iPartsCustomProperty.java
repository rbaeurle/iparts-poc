/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.common.EtkNoteCategory;
import de.docware.apps.etk.base.project.common.EtkNoteCategoryList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoDescription;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCustomPropertyList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Cache für die Custom Properties aus der Tabelle {@code CUSTPROP}
 */
public class iPartsCustomProperty implements CacheForGetCacheDataEvent<iPartsCustomProperty>, iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsCustomProperty> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    @JsonProperty
    protected Map<String, Map<String, CustomProperty>> customPropertyMap = new HashMap<>();

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsCustomProperty.class, "CustomProperty", false);
    }

    public static synchronized iPartsCustomProperty getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        iPartsCustomProperty result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsCustomProperty(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsCustomProperty();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }


    @Override
    public iPartsCustomProperty createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        return createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));
    }

    private void load(EtkProject project) {
        EtkNoteCategoryList categoryList = new EtkNoteCategoryList();
        categoryList.load(project.getConfig(), EtkRelatedInfoDescription.RELATEDINFO_STUELI + EtkRelatedInfoDescription.RELATEDINFO_COMMON
                                               + EtkConfigConst.REL_CUSTOM_PROPERTIES);
        Map<String, EtkNoteCategory> keyToCategoryMap = new HashMap<>();
        categoryList.forEach(category -> keyToCategoryMap.put(category.getKey(), category));

        iPartsDataCustomPropertyList customPropertyList = iPartsDataCustomPropertyList.loadAllCustomProperties(project);

        customPropertyList.getAsList().forEach(dataCustomProperty -> {
            String matNr = dataCustomProperty.getFieldValue(FIELD_C_MATNR);
            if (!matNr.isEmpty()) {
                String categoryKey = dataCustomProperty.getFieldValue(FIELD_C_KEY);
                EtkNoteCategory category = keyToCategoryMap.get(categoryKey);
                if (category != null) {
                    Map<String, CustomProperty> customPropertyMapForMatNr = customPropertyMap.computeIfAbsent(dataCustomProperty.getFieldValue(FIELD_C_MATNR),
                                                                                                              matNumber -> new TreeMap<>());

                    CustomProperty customProperty = customPropertyMapForMatNr.computeIfAbsent(categoryKey, key -> {
                        CustomProperty newCustomProperty = new CustomProperty();
                        newCustomProperty.setType(categoryKey);
                        newCustomProperty.setDescription(category.getTitle());
                        return newCustomProperty;
                    });

                    customProperty.setValue(dataCustomProperty.getFieldValue(FIELD_C_SPRACH), dataCustomProperty.getFieldValue(FIELD_C_TEXT));
                }
            }
        });
    }

    /**
     * Gibt eine Liste von Custom Properties für eine Materialnummer zurück.
     *
     * @param matNr
     * @return
     */
    public Collection<CustomProperty> getCustomProperties(String matNr) {
        if (StrUtils.isValid(matNr)) {
            Map<String, CustomProperty> customPropertyMapForMatNr = customPropertyMap.get(matNr);
            if (customPropertyMapForMatNr != null) {
                return Collections.unmodifiableCollection(customPropertyMapForMatNr.values());
            }
        }
        return null;
    }


    /**
     * Eine konkrete Custom Property inkl. Beschreibungstexten aus der Workbench.
     */
    public static class CustomProperty implements RESTfulTransferObjectInterface {

        @JsonProperty
        public String type;
        @JsonProperty
        public EtkMultiSprache description;
        @JsonProperty
        public EtkMultiSprache value = new EtkMultiSprache();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public EtkMultiSprache getDescription() {
            return description;
        }

        public void setDescription(EtkMultiSprache description) {
            this.description = description;
        }

        public EtkMultiSprache getValueMultiLang() {
            return value;
        }

        public void setValueMultiLang(EtkMultiSprache value) {
            this.value = value;
        }

        public String getValue(String language, List<String> fallbackLanguages) {
            // Sprachneutralen Text als letzten Fallback hinzufügen
            fallbackLanguages = new ArrayList<>(fallbackLanguages);
            fallbackLanguages.add("");

            return value.getTextByNearestLanguage(language, fallbackLanguages);
        }

        public void setValue(String language, String value) {
            this.value.setText(language, value);
        }
    }
}