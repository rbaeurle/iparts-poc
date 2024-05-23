/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * Virtuelle Sicht auf die Retaildaten gemappt nach EinPAS
 */
public class iPartsVirtualAssemblyProductEinPasKgTu extends iPartsVirtualAssemblyEinPasBase {

    public iPartsVirtualAssemblyProductEinPasKgTu(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(getEtkProject(), (iPartsProductId)(getRootNode().getId()));
        iPartsCatalogNode nodes = productStructures.getCompleteEinPasStructureWithVirtualKgTu(getEtkProject(), getRootNode().getType().isProductStructureWithAggregates());
        EinPasId subId = null;
        // Suche den EinPAS-Knoten
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof EinPasId) {
                subId = (EinPasId)iPartsVirtualNodeId;
                break;
            }
        }
        // Knoten für subId oder subId == null, dann erste Ebene laden
        return loadVirtualEinPas(nodes, getRootNode(), subId);
    }

    @Override
    public SubAssemblyState getSubAssemblyState() {
        return SubAssemblyState.HAS_ALWAYS;
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsProductId) { // Produkt
            iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), (iPartsProductId)lastNodeId);

            String titlePrefix = "";
            if (product.isPSK()) {
                titlePrefix = "PSK: ";
            }
            String titlePostfix = " (EinPAS)";

            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache productTitle = product.getProductTitle(getEtkProject());

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                String productText = productTitle.getTextByNearestLanguage(lang, fallbackLanguages);
                if (!product.isRetailRelevantFromDB()) {
                    productText = "<" + productText + "> " + TranslationHandler.translateForLanguage("!!(nicht Retail-relevant)", lang);
                }
                result.setText(lang, titlePrefix + productText + titlePostfix);
            }

            return result;
        }

        return super.getTexts();
    }
}
