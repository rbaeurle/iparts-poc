/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsSvgOutlineResult;

/**
 * Helfer zum Aggregieren der möglichen Outputs eines Bildimports
 */
public class ImageFileImporterResult {

    private iPartsSvgOutlineResult outlineResult;
    private boolean isSuccessful;

    public ImageFileImporterResult() {
        this.isSuccessful = true;
    }

    public void setOutlineFilterResult(iPartsSvgOutlineResult outlineResult) {
        this.outlineResult = outlineResult;
    }

    public boolean importSuccessful() {
        return isSuccessful;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public iPartsSvgOutlineResult getOutlineResult() {
        return outlineResult;
    }
}
