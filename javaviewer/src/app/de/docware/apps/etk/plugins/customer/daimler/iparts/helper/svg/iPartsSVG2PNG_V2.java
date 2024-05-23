/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.svg;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsSvgOutlineHelper;
import de.docware.util.StrUtils;
import de.docware.util.j2ee.logger.Logger;
import de.docware.util.svg.converter.SVG2PNG_V2;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGRect;

import java.io.File;

/**
 * Erweiterung von {@link SVG2PNG_V2} um iParts-spezifische Methoden und Daten.
 */
public class iPartsSVG2PNG_V2 extends SVG2PNG_V2 {

    private SVGDocument lastDocument;
    private Boolean isWithOutlinesByASPLM;

    public iPartsSVG2PNG_V2(byte[] src, Logger logger, File svgFile) {
        super(src, logger, svgFile);
    }

    public iPartsSVG2PNG_V2(byte[] src, Logger logger) {
        super(src, logger);
    }

    @Override
    protected boolean isHotspotValid(SVGElement element, String text, SVGRect bbox, SVGDocument document) {
        boolean hotspotValid = super.isHotspotValid(element, text, bbox, document);
        if (hotspotValid) {
            // Prüfung, ob es sich um einen gültigen Hotspot aus dem AS-PLM-SVG handelt analog zu dwImageIsHotspotNode() in svg.js
            if (isWithOutlinesByASPLM(document)) {
                // Das Text-Element muss eine ID haben, die mit "text_" beginnt
                Node elementId = element.getAttributes().getNamedItem("id");
                if ((elementId == null) || StrUtils.isEmpty(elementId.getTextContent()) || !elementId.getTextContent().startsWith("text_")) {
                    return false;
                }

                // Das Text-Element muss sich in einem Element vom Typ "g" befinden mit einer ID, die mit "hotspot_" beginnt
                Node parentNode = element.getParentNode();
                while (parentNode != null) {
                    if ((parentNode.getNodeName() != null) && parentNode.getNodeName().equalsIgnoreCase("g") && (parentNode.getAttributes() != null)) {
                        Node idNode = parentNode.getAttributes().getNamedItem("id");
                        if ((idNode != null) && idNode.getTextContent().startsWith("hotspot_")) {
                            return true;
                        }
                    }
                    parentNode = parentNode.getParentNode();
                }
                return false;
            }
        }

        return hotspotValid;
    }

    private boolean isWithOutlinesByASPLM(SVGDocument document) {
        if ((isWithOutlinesByASPLM == null) || (lastDocument != document)) {
            lastDocument = document;
            isWithOutlinesByASPLM = iPartsSvgOutlineHelper.isMarkerAttached(document);
        }
        return isWithOutlinesByASPLM;
    }
}