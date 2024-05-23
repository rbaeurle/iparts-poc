/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

/**
 * Helfer zu Aggregieren des Outputs nach der Anreicherung von SVG Bildern
 */
public class iPartsSvgOutlineResult {

    private byte[] originalData;
    private byte[] processedData;
    private boolean markerAttached;
    private boolean hasClipPaths;
    private boolean hasException;

    public iPartsSvgOutlineResult(byte[] originalData) {
        this.originalData = originalData;
    }

    public byte[] getPictureByteData() {
        return ((processedData == null) || isMarkerAttached() || hasClipPaths() || hasException()) ? originalData : processedData;
    }

    public void setProcessedData(byte[] processedData) {
        this.processedData = processedData;
    }

    public boolean isMarkerAttached() {
        return markerAttached;
    }

    public void setMarkerAttached(boolean markerAttached) {
        this.markerAttached = markerAttached;
    }

    public boolean hasClipPaths() {
        return hasClipPaths;
    }

    public void setHasClipPaths(boolean hasClipPaths) {
        this.hasClipPaths = hasClipPaths;
    }

    public boolean hasException() {
        return hasException;
    }

    public void setHasException(boolean hasException) {
        this.hasException = hasException;
    }

}
