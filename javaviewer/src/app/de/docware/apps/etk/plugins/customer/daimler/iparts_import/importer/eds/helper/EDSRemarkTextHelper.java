/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.helper.DictTextSearchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictPrefixAndSuffix;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.map.LRUMap;

/**
 * Helfer zum Bestimmen des richtigen {@link EtkMultiSprache} Objekts für EDS/BCS Bemerkungen zu SAAs oder Baukasten
 */
public class EDSRemarkTextHelper implements iPartsConst {

    public static EtkMultiSprache determineMultiLangForEDSBCSRemark(EtkProject project, String textValue, LRUMap textCache, String tableName, String textFieldName) {
        Object multiLangObject = (textCache == null) ? null : textCache.get(textValue);
        if (multiLangObject instanceof EtkMultiSprache) {
            // Text wurde samt Text-Id schon während dem Konvertieren verarbeitet
            return (EtkMultiSprache)multiLangObject;
        } else {
            // Suche nach dem Text in der DB
            EtkDataTextEntryList textIds = searchRemarkInDBForGermanLanguage(project, textValue, tableName, textFieldName);
            if (!textIds.isEmpty()) {
                // Text gefunden -> Treffer aus DB nutzen
                return project.getDbLayer().getLanguagesTextsByTextId(textIds.get(0).getFieldValue(FIELD_S_TEXTID));
            } else {
                // Text nicht gefunden -> Neu anlegen
                return new EtkMultiSprache(DictHelper.buildTextId(iPartsDictPrefixAndSuffix.EDS_BCS_REMARKS.getPrefixValue(),
                                                                  StrUtils.makeGUID()));
            }
        }
    }

    public static EtkDataTextEntryList searchRemarkInDBForGermanLanguage(EtkProject project, String textValue, String tableName, String textFieldName) {
        return searchRemarkInDB(project, textValue, tableName, textFieldName, Language.DE);
    }

    public static EtkDataTextEntryList searchRemarkInDB(EtkProject project, String textValue, String tableName, String textFieldName, Language language) {
        return DictTextSearchHelper.searchTextAttributesInTableSprache(project, textValue,
                                                                       new String[]{ TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD) },
                                                                       new String[]{ TableAndFieldName.make(tableName, textFieldName) },
                                                                       DictHelper.buildTextId(iPartsDictPrefixAndSuffix.EDS_BCS_REMARKS.getPrefixValue(), "*"),
                                                                       language, true);
    }

}
