/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataHotspot;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSCallout;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSImage;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class iPartsWSImageHelper {

    public static List<iPartsWSImage> getImagesForModule(EtkDataAssembly assembly, Collection<EtkDataPartListEntry> partListEntries,
                                                         EtkProject project, String language, boolean includeCallouts) {
        return getImagesForModule(assembly, partListEntries, project, language, includeCallouts, false);
    }

    public static List<iPartsWSImage> getImagesForModule(EtkDataAssembly assembly, Collection<EtkDataPartListEntry> partListEntries,
                                                         EtkProject project, String language, boolean includeCallouts, boolean reducedCalloutOutput) {
        List<EtkDataImage> dataImages = assembly.getImages();
        List<iPartsWSImage> images = new ArrayList<>(dataImages.size());
        if (dataImages.isEmpty()) {
            return images;
        }

        Boolean useSVGs = null;

        // Für alle Zeichnung und darin für alle Verwendungen ein iPartsWSImage-Objekt erzeugen und dem Ergebnis hinzufügen
        for (EtkDataImage dataImage : dataImages) {
            for (EtkDataPool imageVariant : dataImage.getFilteredPoolVariants()) {
                PoolId imagePoolId = imageVariant.getAsId();

                // Sollen SVGs ausgegeben werden?
                if (imagePoolId.getPUsage().equals(EtkDataImage.IMAGE_USAGE_SVG)) {
                    if (useSVGs == null) { // useSVGs lazy bestimmen
                        useSVGs = checkIfUseSVGImages(assembly, project);
                    }

                    if (!useSVGs) {
                        continue;
                    }
                }

                iPartsWSImage wsImage = new iPartsWSImage(imagePoolId, dataImage, language, project); // wsImage mit imagePoolId initialisieren inkl. URLs

                if (includeCallouts) {
                    // Hotspots nur bei 2D
                    if (imageVariant.is2DVariant()) {
                        List<EtkDataHotspot> dataHotspots = imageVariant.getHotspots();
                        if (!dataHotspots.isEmpty()) {
                            Map<String, String> hotspotTooltips = new HashMap<>(dataHotspots.size());

                            // Tooltiptexte bestimmen und in einer Map von Hotspotnummer auf Tooltiptext ablegen
                            String hotspotFieldName = TableAndFieldName.getFieldName(assembly.getEbene().getKeyFeldName());
                            if (StrUtils.isValid(hotspotFieldName)) {
                                for (EtkDataPartListEntry partListEntry : partListEntries) {
                                    String hotspotNumber = partListEntry.getFieldValue(hotspotFieldName);
                                    if (!hotspotTooltips.containsKey(hotspotNumber)) {
                                        // Materialname vom ersten gefundenen Stücklisteneintrag für den Hotspot als Tooltiptext merken
                                        hotspotTooltips.put(hotspotNumber, partListEntry.getPart().getFieldValue(EtkDbConst.FIELD_M_TEXTNR,
                                                                                                                 project.getDBLanguage(), true));
                                    }
                                }
                            }

                            // Für alle Hotspots ein iPartsWSCallout-Objekt erzeugen und den wsCallouts hinzufügen
                            List<iPartsWSCallout> wsCallouts = new ArrayList<>(dataHotspots.size());
                            for (EtkDataHotspot dataHotspot : dataHotspots) {
                                iPartsWSCallout wsCallout = new iPartsWSCallout();
                                String hotspotNumber = dataHotspot.getKey();
                                wsCallout.setId(hotspotNumber);
                                wsCallout.setActive(hotspotTooltips.containsKey(hotspotNumber));

                                if (!reducedCalloutOutput) {
                                    String hotspotTooltip = "[" + wsCallout.getId() + "]";
                                    String materialText = hotspotTooltips.get(hotspotNumber);
                                    if (materialText != null) {
                                        hotspotTooltip += " " + materialText;
                                    }
                                    wsCallout.setTooltip(hotspotTooltip);

                                    int width = dataHotspot.getRight() - dataHotspot.getLeft() + 1;
                                    int height = dataHotspot.getBottom() - dataHotspot.getTop() + 1;

                                    // (x, y) sind das Zentrum des Hotspots
                                    wsCallout.setX(dataHotspot.getLeft() + width / 2);
                                    wsCallout.setY(dataHotspot.getTop() + height / 2);

                                    // (left, top, width, height) ergibt die Bounding-Box des Hotspots
                                    wsCallout.setLeft(dataHotspot.getLeft());
                                    wsCallout.setTop(dataHotspot.getTop());
                                    wsCallout.setWidth(width);
                                    wsCallout.setHeight(height);
                                }

                                wsCallouts.add(wsCallout);
                            }
                            wsImage.setCallouts(wsCallouts);
                        }
                    }
                }

                images.add(wsImage);
            }
        }

        return images;
    }

    /**
     * Überprüft, ob SVGs benutzt werden sollen
     *
     * @param assembly
     * @param project
     * @return
     */
    public static Boolean checkIfUseSVGImages(EtkDataAssembly assembly, EtkProject project) {
        if (assembly instanceof iPartsDataAssembly) {
            // Bei Navigationsmodulen sollen SVGs immer ausgegeben werden
            if (EditModuleHelper.isCarPerspectiveAssembly(assembly)) {
                return true;
            } else {
                iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    return iPartsProduct.getInstance(project, productId).isUseSVGs();
                }
            }
        }
        return false;
    }

}
