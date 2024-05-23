/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class KgTuHelper {

    public static Map<String, KgTuListItem> getKGTUStructure(EtkProject project, iPartsProductId productId) {
        KgTuForProduct kgTuProduct = KgTuForProduct.getInstance(project, productId);
        Map<String, KgTuTemplate> kgTuTemplateMap = KgTuTemplate.getInstance(productId, project);

        Map<String, KgTuListItem> kgTuMap = new TreeMap<String, KgTuListItem>();

        for (KgTuNode kgNode : kgTuProduct.getKgNodeList()) {
            String kgNr = kgNode.getId().getKg();
            KgTuListItem kgListItem = new KgTuListItem(kgNode, KgTuListItem.Source.PRODUCT, true);
            //alle TU Kinder auch hinzufügen
            for (KgTuNode tuNode : kgNode.getChildren()) {
                kgListItem.addChild(new KgTuListItem(tuNode, KgTuListItem.Source.PRODUCT, kgListItem, false));
            }
            kgTuMap.put(kgNr, kgListItem);
        }

        // Jetzt noch die Einträge aus den nach AS Produktklassen sortierten Templates dazu mischen
        for (KgTuTemplate kgTuTemplate : kgTuTemplateMap.values()) {
            for (KgTuNode kgNode : kgTuTemplate.getKgNodeList()) {
                String kgNr = kgNode.getId().getKg();
                KgTuListItem kgListItem = kgTuMap.get(kgNr);
                if (kgListItem == null) {
                    kgListItem = new KgTuListItem(kgNode, KgTuListItem.Source.TEMPLATE, true);
                    kgTuMap.put(kgNr, kgListItem);
                }
                //alle TU Kinder auch hinzufügen
                for (KgTuNode tuNode : kgNode.getChildren()) {
                    kgListItem.addChild(new KgTuListItem(tuNode, KgTuListItem.Source.TEMPLATE, kgListItem, false));
                }
            }
        }
        return kgTuMap;
    }

    static public String buildKgTuComboText(KgTuListItem nodeItem, String language, List<String> fallbackLanguages) {
        if (nodeItem != null) {
            if (nodeItem.isSourceTemplate()) {
                return (nodeItem.getKgTuNode().getNumberAndTitle(language, fallbackLanguages) + " *");
            } else {
                return nodeItem.getKgTuNode().getNumberAndTitle(language, fallbackLanguages);
            }
        }
        return "";
    }

    static public String buildKgTuShortComboText(KgTuListItem nodeItem) {
        if (nodeItem != null) {
            if (nodeItem.isSourceTemplate()) {
                return (nodeItem.getKgTuNode().getNumber() + " *");
            } else {
                return nodeItem.getKgTuNode().getNumber();
            }
        }
        return "";
    }


}
