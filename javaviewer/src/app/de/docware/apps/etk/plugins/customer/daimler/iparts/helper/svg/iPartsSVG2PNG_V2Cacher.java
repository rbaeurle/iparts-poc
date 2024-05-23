/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.svg;

import de.docware.util.j2ee.logger.Logger;
import de.docware.util.svg.converter.SVG2PNG_V2;
import de.docware.util.svg.converter.SVG2PNG_V2Cacher;

/**
 * Erweiterung von {@link SVG2PNG_V2Cacher} um iParts-spezifische Methoden und Daten.
 */
public class iPartsSVG2PNG_V2Cacher extends SVG2PNG_V2Cacher {

    @Override
    protected SVG2PNG_V2 createSvg2Png(byte[] src, Logger logger) {
        return new iPartsSVG2PNG_V2(src, logger);
    }
}