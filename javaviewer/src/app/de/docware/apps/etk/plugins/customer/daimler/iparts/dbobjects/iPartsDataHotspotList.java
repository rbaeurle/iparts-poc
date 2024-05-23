/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataHotspotList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.svg.iPartsSVG2PNG_V2Cacher;
import de.docware.util.svg.converter.SVG2PNG_V2Cacher;

/**
 * Erweiterung von {@link EtkDataHotspotList} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataHotspotList extends EtkDataHotspotList {

    @Override
    protected SVG2PNG_V2Cacher createSvg2PngCacher() {
        iPartsSVG2PNG_V2Cacher svg2PNGV2Cacher = new iPartsSVG2PNG_V2Cacher();
        svg2PNGV2Cacher.setForceCreateNewCachedImage(true); // Beim Laden der Hotspots aus dem SVG nie die Daten aus dem Cache verwenden
        return svg2PNGV2Cacher;
    }
}