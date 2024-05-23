/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper für die Umsetzung von SingleEdit in den Edit-RelatedInfos
 */
public class RelatedInfoSingleEditHelper {

    private static Map<String, String> activeRelatedInfoMap;

    private static Map<String, String> getActiveRelatedInfoMap() {
        if (activeRelatedInfoMap == null) {
            activeRelatedInfoMap = new HashMap<>();
            activeRelatedInfoMap.put(iPartsConst.CONFIG_KEY_RELATED_INFO_WW_PARTS_DATA, iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA);
            activeRelatedInfoMap.put(iPartsConst.CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA, iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA);
            activeRelatedInfoMap.put(iPartsConst.CONFIG_KEY_RELATED_INFO_FACTORY_DATA, iPartsConst.CONFIG_KEY_RELATED_INFO_SUPER_EDIT_DATA);
            activeRelatedInfoMap.put(iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA, iPartsConst.CONFIG_KEY_RELATED_INFO_SUPER_EDIT_DATA);
        }
        return activeRelatedInfoMap;
    }

    public static String getActiveRelatedInfo(EtkProject project, String activeInfo) {
        if (!iPartsUserSettingsHelper.isSingleEdit(project)) {
            if (getActiveRelatedInfoMap().containsKey(activeInfo)) {
                activeInfo = getActiveRelatedInfoMap().get(activeInfo);
            }
        }
        return activeInfo;
    }

    public static boolean isActiveRelatedInfoVisible(EtkProject project, String activeInfo) {
        if (!iPartsUserSettingsHelper.isSingleEdit(project)) {
            if (getActiveRelatedInfoMap().containsKey(activeInfo)) {
                return false;
            }
        }
        return true;
    }


}
