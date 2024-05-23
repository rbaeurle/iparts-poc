/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images;

import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

/**
 * Klasse, die benötigt wird um aus einer Funktion alle drei Pfad/Dateinamen für die Bild-, Vorschaubild-
 * und Hotspotdatei zurückgeben zu können.
 */
public class DASTiImageFiles {

    // Das eigentliche Bild, Beispiel: [B21230000091_2014.07.29.png]
    private DWFile imageFile;
    // Thumbnail: [B21230000091_2014.07.29_thumbnail.png]
    private DWFile previewFile;
    // Die Hotspots: [B21230000091_2014.07.29.sen]
    private DWFile hotspotFile;
    private String dateAsString;

    public DASTiImageFiles(DWFile pictureFile) {
        this.setImageFile(pictureFile);
    }

    /**
     * Damit vernünftige Daten importier werden können, muss immer mindestens eine Bilddatei vorhanden sein.
     *
     * @return
     */
    public boolean isValid() {
        return (imageFile != null);
    }


    /**
     * Getter und Setter
     **/
    public DWFile getHotspotFile() {
        return hotspotFile;
    }

    public void setHotspotFile(DWFile hotspotFile) {
        this.hotspotFile = hotspotFile;
    }

    public DWFile getImageFile() {
        return imageFile;
    }

    private void setImageFile(DWFile imageFile) {
        this.imageFile = imageFile;
        extractDateFromPictureName();
    }

    private void extractDateFromPictureName() {
        if (this.imageFile != null) {
            // [B21230000091_2014.07.29.png]
            String filename = this.imageFile.extractFileName(false);
            String[] filenameToken = StrUtils.toStringArray(filename, "_", false);
            if (filenameToken.length > 1) {
                dateAsString = filenameToken[1].replace(".", "");
            }
        }
    }

    public String getDateAsString() {
        return dateAsString;
    }

    public DWFile getPreviewFile() {
        return previewFile;
    }

    public void setPreviewFile(DWFile previewFile) {
        this.previewFile = previewFile;
    }
}
