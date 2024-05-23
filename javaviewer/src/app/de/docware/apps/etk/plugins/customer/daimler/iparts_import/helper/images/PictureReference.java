/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPoolEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PoolEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPicReferenceId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADDateTimeHandler;
import de.docware.util.StrUtils;

/**
 * Klasse, die eine Bildreferenz aus ELDAS representiert
 */
public class PictureReference {

    private String pictureNumber;
    private String pictureDate;
    private String varItemId;
    private String varItemRevId;
    private String errorOrWarningText;
    private boolean imageExists;
    private EtkProject project;

    public PictureReference(EtkProject project, String pictureReference) {
        this.project = project;
        initReference(pictureReference);
    }

    public PictureReference(EtkProject project, String pictureNumber, String pictureDate) {
        this.project = project;
        this.pictureNumber = pictureNumber;
        setPictureDate(pictureDate);
        checkIfImageAlreadyExists();
    }

    private void initReference(String pictureReference) {
        if (pictureReference.length() > 12) {
            pictureNumber = pictureReference.substring(0, 12);
            String date = pictureReference.substring(12);
            if (date.length() == 6) {
                // yyMMdd -> ddMMyy für iPartsMADDateTimeHandler
                iPartsMADDateTimeHandler handler = new iPartsMADDateTimeHandler(date.substring(4, 6) + date.substring(2, 4) + date.substring(0, 2));
                setPictureDate(handler.getDBDateTime());
                if (StrUtils.isEmpty(pictureDate)) {
                    errorOrWarningText = "!!Bildreferenz besitzt ungültiges Datum!";
                }
            } else {
                errorOrWarningText = "!!Bildreferenzsuffix hat nicht Länge 6 für ein gültiges Datum!";
            }
        } else if (pictureReference.length() == 12) {
            pictureNumber = pictureReference;
            errorOrWarningText = "!!Bildreferenz enthält kein Datum!";
        } else {
            errorOrWarningText = "!!Bildnummer ist zu kurz! Bildreferenz konnte nicht angelegt werden.";
        }
        checkIfImageAlreadyExists();
    }

    /**
     * Überprüft, ob in der Datenbank schon ein Bild zur übergebenen DASTI Referenz existiert. Falls ja, dann werden
     * MediaContainer Id und MediaContainer Rev Id gesetzt.
     */
    private void checkIfImageAlreadyExists() {
        imageExists = false;
        iPartsDataPicReferenceList list;
        if (StrUtils.isEmpty(pictureDate)) {
            list = iPartsDataPicReferenceList.loadPicReferencesWithoutDate(project, new iPartsPicReferenceId(pictureNumber, ""));
        } else {
            list = iPartsDataPicReferenceList.loadPicReferencesWithDate(project, new iPartsPicReferenceId(pictureNumber, pictureDate));
        }
        if ((list == null) || list.isEmpty()) {
            return;
        }
        for (iPartsDataPicReference picReference : list) {
            if (StrUtils.isEmpty(pictureDate) || picReference.containsRefDate(pictureDate)) {
                varItemId = picReference.getVarId();
                if (varItemId.isEmpty()) {
                    continue;
                }
                varItemRevId = picReference.getVarRevId();
                EtkDataPoolEntry poolEntry = EtkDataObjectFactory.createDataPoolEntry();
                poolEntry.init(project);
                PoolEntryId poolEntryId = new PoolEntryId(varItemId, varItemRevId);
                if (poolEntry.loadFromDB(poolEntryId)) {
                    imageExists = true;
                }
                return;
            }
        }
    }

    public boolean isImageExists() {
        return imageExists;
    }

    public String getVarItemId() {
        return varItemId;
    }

    public String getVarItemRevId() {
        return varItemRevId;
    }

    public String getPictureNumber() {
        return pictureNumber;
    }

    public String getPictureDate() {
        return pictureDate;
    }

    public boolean isValid() {
        return !StrUtils.isEmpty(pictureNumber);
    }

    public boolean hasErrorsOrWarnings() {
        return !StrUtils.isEmpty(errorOrWarningText);
    }

    public String getErrorOrWarningText() {
        return errorOrWarningText;
    }

    private void setPictureDate(String pictureDate) {
        if (pictureDate == null) {
            this.pictureDate = "";
        } else {
            this.pictureDate = pictureDate;
        }
    }
}

