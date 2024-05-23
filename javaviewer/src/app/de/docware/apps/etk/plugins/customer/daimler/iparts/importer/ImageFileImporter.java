/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.drawing.*;
import de.docware.apps.etk.base.project.mechanic.ids.PoolEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsValidityScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsSvgOutlineHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsSvgOutlineResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLBinaryFile;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaBinaryFile;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.file.DWFile;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;

/**
 * Importer für eine Zeichnungsdatei und optionale Hotspotdatei.
 */
public class ImageFileImporter extends AbstractGenericImporter {

    private MessageEvent messageListenerForLogger;

    public ImageFileImporter(EtkProject project) {
        super(project, "!!Zeichnungsdatei");
    }


    /**
     * Importiert die übergebene Bild-, Vorschaubild- und Hotspotdatei mit den übergebenen Werten
     *
     * @param imageFile
     * @param hotspotFile
     * @param imageNumber
     * @param imageVersion
     * @param imageLanguage
     * @param imageUsage
     * @param setValidityScope
     * @return
     */
    public ImageFileImporterResult importImageFromFile(DWFile imageFile, DWFile hotspotFile, DWFile previewFile, String imageNumber, String imageVersion, String imageLanguage,
                                                       ImageVariant imageUsage, boolean deleteImages, boolean removeLeadingZerosOnHotspots,
                                                       boolean setValidityScope) {
        ImageFileImporterResult result = new ImageFileImporterResult();
        if (errorInStartTransaction) { // Ohne DB-Transaktion macht ein Import keinen Sinn
            result.setSuccessful(false);
            return result;
        }

        // Pool-Eintrag
        EtkDataPool pool;
        if (previewFile != null) {
            pool = initEtkDataPool(imageFile.readByteArray(), imageNumber, imageVersion, imageLanguage, imageUsage,
                                   imageFile.extractExtension(false), previewFile.readByteArray(), previewFile.extractExtension(false),
                                   setValidityScope, result);
        } else {
            pool = initEtkDataPool(imageFile.readByteArray(), imageNumber, imageVersion, imageLanguage, imageUsage,
                                   imageFile.extractExtension(false), setValidityScope, result);
        }

        // PoolEntry-Eintrag
        EtkDataPoolEntry poolEntry = initEtkDataPoolEntry(pool.getAsId());

        // Zunächst die alten Hotspots aus der DB entfernen. Auch entfernen, wenn keine SEN-Datei da ist, da sonst alte ungültige Hotspots in der
        // Datenbank stehen würden. Ein Import ohne SEN ist eigentlich nicht erlaubt, außer bei Übersichtbildern, die wirklich keine Hotspots haben
        EtkDataHotspotList.deleteHotspots(getProject(), imageNumber, imageVersion, imageLanguage, imageUsage.getUsage());

        // Datei muss eine 2D- oder iv3D-Datei sein mit gültigen (X-)SEN-Hotspots oder eine SVG-Datei
        EtkDataHotspotList hotspotList = null;
        boolean isSVG = MimeTypes.isSvgFile(imageFile.getName());
        if (((hotspotFile != null) && MimeTypes.isSenFile(hotspotFile.getName()) && (pool.getImgType().isEmpty() || MimeTypes.isIv3dFile(imageFile.getName())))
            || isSVG) {
            // Neue Hotspots aus der Datei auslesen
            if (isSVG) {
                byte[] svgContent = pool.getFieldValueAsBlob(EtkDbConst.FIELD_P_DATA); // SVG wird durch den iPartsSvgOutlineHelper beim Aufruf von initEtkDataPool() verändert
                hotspotList = loadHotspotsFromByteArray(svgContent, true, imageNumber, imageVersion, imageLanguage, imageUsage,
                                                        getMessageLog());
            } else {
                // SEN-Datei
                hotspotList = loadHotspotsFromFile(hotspotFile, imageNumber, imageVersion, imageLanguage, imageUsage, getMessageLog(),
                                                   removeLeadingZerosOnHotspots);
            }
            if (hotspotList == null) {
                cancelImport();
                result.setSuccessful(false);
                return result;
            }
        }

        if (poolEntry != null) {
            saveToDB(poolEntry);
        }
        saveToDB(pool);

        if (hotspotList != null) {
            hotspotList.saveToDB(getProject(), false);
        }

        if (J2EEHandler.isJ2EE() && deleteImages) {
            imageFile.deleteRecursivelyWithRepeat();
            if (hotspotFile != null) {
                hotspotFile.deleteRecursivelyWithRepeat();
            }
            if (previewFile != null) {
                previewFile.deleteRecursivelyWithRepeat();
            }
        }
        return result;
    }

    public ImageFileImporterResult importImageFromFile(DWFile imageFile, DWFile hotspotFile, String imageNumber, String imageVersion, String imageLanguage,
                                                       ImageVariant imageUsage, boolean deleteFiles, boolean removeLeadingZerosOnHotspots,
                                                       boolean setValidityScope) {
        return importImageFromFile(imageFile, hotspotFile, null, imageNumber, imageVersion, imageLanguage, imageUsage,
                                   deleteFiles, removeLeadingZerosOnHotspots, setValidityScope);
    }

    /**
     * Importiert die in der {@link iPartsXMLMediaVariant} enthaltenen Bild- und Hotspot-Dateien
     *
     * @param variant
     * @return
     */
    public ImageFileImporterResult importImageFromMediaVariant(final iPartsXMLMediaVariant variant) {
        ImageFileImporterResult result = new ImageFileImporterResult();
        if (errorInStartTransaction) { // Ohne DB-Transaktion macht ein Import keinen Sinn
            result.setSuccessful(false);
            return result;
        }

        iPartsXMLMediaBinaryFile svgBinaryFile = variant.getSVGBinaryFile();
        if (svgBinaryFile != null) {
            // Pool-Eintrag
            byte[] svgContent = Base64.decodeBase64(svgBinaryFile.getBase64String());
            String variantItemId = variant.getItemId();
            EtkDataPool pool = initEtkDataPool(svgContent, variantItemId, variant.getItemRevId(), variant.getPictureLanguage(),
                                               ImageVariant.ivSVG, svgBinaryFile.getFileType(), false, result);
            svgContent = pool.getFieldValueAsBlob(EtkDbConst.FIELD_P_DATA); // SVG wird durch den iPartsSvgOutlineHelper beim Aufruf von initEtkDataPool() verändert

            // PoolEntry-Eintrag
            EtkDataPoolEntry poolEntry = initEtkDataPoolEntry(pool.getAsId());

            // Zunächst die alten Hotspots aus der DB entfernen.
            EtkDataHotspotList.deleteHotspots(getProject(), variantItemId, variant.getItemRevId(), variant.getPictureLanguage(),
                                              EtkDataImage.IMAGE_USAGE_SVG);

            EtkDataHotspotList hotspotList;
            try {
                EtkMessageLog messageLog = getMessageLog();
                // Neue Hotspots aus dem SVG auslesen
                hotspotList = loadHotspotsFromByteArray(svgContent, true, variantItemId, variant.getItemRevId(), variant.getPictureLanguage(),
                                                        ImageVariant.ivSVG, messageLog);
            } finally {
                if (messageListenerForLogger != null) {
                    getMessageLog().removeMessageEventListener(messageListenerForLogger);
                }
            }

            if (poolEntry != null) {
                saveToDB(poolEntry);
            }
            saveToDB(pool);

            if (hotspotList != null) {
                hotspotList.saveToDB(getProject(), false);
            }
        }

        iPartsXMLMediaBinaryFile pngBinaryFile = variant.getPNGBinaryFile();

        // PNG fehlt, aber SVG ist vorhanden -> ist OK
        if ((pngBinaryFile == null) && (svgBinaryFile != null)) {
            return result;
        }

        if (!checkVariantFile(variant, pngBinaryFile, "!!PNG Bilddatei")) {
            cancelImport();
            result.setSuccessful(false);
            return result;
        }

        // Pool-Eintrag
        EtkDataPool pool = initEtkDataPool(Base64.decodeBase64(pngBinaryFile.getBase64String()), variant.getItemId(),
                                           variant.getItemRevId(), variant.getPictureLanguage(), ImageVariant.iv2D,
                                           pngBinaryFile.getFileType(), false, result);

        // PoolEntry-Eintrag
        EtkDataPoolEntry poolEntry = initEtkDataPoolEntry(pool.getAsId());

        EtkDataHotspotList hotspotList = null;
        iPartsXMLMediaBinaryFile senBinaryFile = variant.getSENBinaryFile();
        if (checkVariantFile(variant, senBinaryFile, "!!SEN Hotspot-Datei")) {
            // Zunächst die alten Hotspots aus der DB entfernen. Auch entfernen, wenn keine SEN-Datei da ist, da sonst alte ungültige Hotspots in der
            // Datenbank stehen würden. Ein Import ohne SEN ist eigentlich nicht erlaubt, außer bei Übersichtbildern, die wirklich keine Hotspots haben
            EtkDataHotspotList.deleteHotspots(getProject(), variant.getItemId(), variant.getItemRevId(), variant.getPictureLanguage(), EtkDataImage.IMAGE_USAGE_2D);

            String senFileType = senBinaryFile.getFileType();
            if ((senFileType != null) && senFileType.equalsIgnoreCase(MimeTypes.EXTENSION_SEN)) {
                try {
                    EtkMessageLog messageLog = getMessageLog();
                    // Neue Hotspots aus der Datei auslesen
                    hotspotList = loadHotspotsFromByteArray(Base64.decodeBase64(senBinaryFile.getBase64String()), false, variant.getItemId(),
                                                            variant.getItemRevId(), variant.getPictureLanguage(), ImageVariant.iv2D,
                                                            messageLog);

                } finally {
                    if (messageListenerForLogger != null) {
                        getMessageLog().removeMessageEventListener(messageListenerForLogger);
                    }
                }
            }
        }

        if (poolEntry != null) {
            saveToDB(poolEntry);
        }
        saveToDB(pool);

        if (hotspotList != null) {
            hotspotList.saveToDB(getProject(), false);
        }
        return result;
    }

    /**
     * Importiert nachträglich das übergebene MQ Vorschaubild. Über {@code pImages} und {@code pVer} wird die Verknüpfung
     * zum Original im Zeichnungspool hergestellt.
     *
     * @param previewBinaryFile
     * @param pImages
     * @param pVer
     * @return
     */
    public boolean importPreviewFile(AbstractXMLBinaryFile previewBinaryFile, String pImages, String pVer) {
        if (errorInStartTransaction) { // Ohne DB-Transaktion macht ein Import keinen Sinn
            return false;
        }

        if (previewBinaryFile == null) {
            return false;
        }

        byte[] previewContent = Base64.decodeBase64(previewBinaryFile.getBase64String());

        // PreviewImageType nur bei erweiterten Dateitypen wie SVG und 3D setzen
        String previewImageType = previewBinaryFile.getFileType();
        if (MimeTypes.isExtImageExtension(previewImageType)) {
            previewImageType = previewImageType.toUpperCase();
        } else {
            previewImageType = "";
        }

        // Pool-Eintrag um Vorschaubild erweitern
        EtkDataPoolVariants poolVariants = EtkDataObjectFactory.createDataPoolVariants();
        poolVariants.loadPoolVariants(getProject(), pImages, pVer, DBActionOrigin.FROM_DB);
        for (EtkDataPool pool : poolVariants) {
            pool.setFieldValueAsBlob(iPartsConst.FIELD_P_PREVIEW_DATA, previewContent, DBActionOrigin.FROM_EDIT);
            pool.setFieldValue(iPartsConst.FIELD_P_PREVIEW_IMGTYPE, previewImageType, DBActionOrigin.FROM_EDIT);
            saveToDB(pool);
        }
        return true;
    }

    /**
     * Initialisiert mit der übergebenen <code>poolId</code> einen {@link EtkDataPoolEntry}. Sollte der DB Eintrag schon
     * existieren, wird nichts gemacht und null zurückgeliefert.
     *
     * @param poolId
     * @return
     */
    private EtkDataPoolEntry initEtkDataPoolEntry(PoolId poolId) {
        EtkDataPoolEntry poolEntry = EtkDataObjectFactory.createDataPoolEntry();
        poolEntry.init(getProject());
        boolean newImagePoolEntry = !poolEntry.loadFromDB(new PoolEntryId(poolId.getPImages(), poolId.getPVer()));
        if (newImagePoolEntry) {
            poolEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            return poolEntry;
        }
        return null;
    }

    /**
     * Initialisiert mit den übergebenen Werten das dazugehörige {@link EtkDataPool} Objekt. Die Werte eines schon
     * existierenden Objekts werden überschrieben.
     *
     * @param pictureData
     * @param imageNumber
     * @param imageVersion
     * @param imageLanguage
     * @param imageUsage
     * @param imageType
     * @param setValidityScope Soll der Gültigkeitsbereich gesetzt werden?
     * @return
     */
    private EtkDataPool initEtkDataPool(byte[] pictureData, String imageNumber, String imageVersion, String imageLanguage,
                                        ImageVariant imageUsage, String imageType, byte[] previewPictureData, String previewImageType,
                                        boolean setValidityScope, ImageFileImporterResult imageFileImporterResult) {
        // ImageType nur bei erweiterten Dateitypen wie SVG und 3D setzen
        if (MimeTypes.isExtImageExtension(imageType)) {
            imageType = imageType.toUpperCase();
        } else {
            imageType = "";
        }

        if (MimeTypes.isSvgFile(imageType)) {
            // SVG Binärdaten werden mit Outline Informationen angereichert (Filter / ...)
            // Ungeachtet der Anzahl an Aufrufe wird das Dokument nur 1x modifziert
            iPartsSvgOutlineResult outLineResult = iPartsSvgOutlineHelper.attachOutlineFilter(pictureData);
            imageFileImporterResult.setOutlineFilterResult(outLineResult);
            pictureData = outLineResult.getPictureByteData();
        }

        EtkDataPool pool = EtkDataObjectFactory.createDataPool();
        pool.init(getProject());
        PoolId poolId = new PoolId(imageNumber, imageVersion, imageLanguage, imageUsage.getUsage());
        boolean newImagePool = !pool.loadFromDB(poolId);
        if (newImagePool) {
            pool.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        pool.setFieldValueAsBlob(EtkDbConst.FIELD_P_DATA, pictureData, DBActionOrigin.FROM_EDIT);
        pool.setFieldValueAsDate(iPartsConst.FIELD_P_IMPORTDATE, GregorianCalendar.getInstance(), DBActionOrigin.FROM_EDIT);
        pool.setFieldValue(EtkDbConst.FIELD_P_IMGTYPE, imageType, DBActionOrigin.FROM_EDIT);
        if (previewPictureData != null) {
            pool.setFieldValueAsBlob(iPartsConst.FIELD_P_PREVIEW_DATA, previewPictureData, DBActionOrigin.FROM_EDIT);
            pool.setFieldValue(iPartsConst.FIELD_P_PREVIEW_IMGTYPE, previewImageType, DBActionOrigin.FROM_EDIT);
        }

        if (setValidityScope) {
            // Gültigkeitsbereich setzen
            String validityScopeString;
            if (iPartsRight.checkUserHasBothVehicleTypeRightsInSession()) {
                validityScopeString = iPartsValidityScope.IPARTS.getScopeKey();
            } else if (iPartsRight.checkCarAndVanInSession()) {
                validityScopeString = iPartsValidityScope.IPARTS_MB.getScopeKey();
            } else if (iPartsRight.checkTruckAndBusInSession()) {
                validityScopeString = iPartsValidityScope.IPARTS_TRUCK.getScopeKey();
            } else {
                // UNUSED sollte nur dann gesetzt werden, wenn das Bild auch wirklich schon eine Verwendung haben kann, was
                // beim Import nicht der Fall ist
                validityScopeString = "";
            }
            pool.setFieldValue(iPartsConst.FIELD_P_VALIDITY_SCOPE, validityScopeString, DBActionOrigin.FROM_EDIT);
        }

        return pool;
    }

    private EtkDataPool initEtkDataPool(byte[] pictureData, String imageNumber, String imageVersion, String imageLanguage,
                                        ImageVariant imageUsage, String imageType, boolean setValidityScope,
                                        ImageFileImporterResult imageFileImporterResult) {
        return initEtkDataPool(pictureData, imageNumber, imageVersion, imageLanguage, imageUsage, imageType, null,
                               "", setValidityScope, imageFileImporterResult);
    }

    /**
     * Lädt die Hotspots aus der übergebenen Datei und fügt sie dem {@link EtkDataHotspotList}-Rückgabewert mit den anderen
     * übergebenen Parametern hinzu.
     *
     * @param file          Entweder eine SEN-Datei oder eine SVG-Datei, aus der die Hotspots extrahiert werden.
     * @param imageNumber
     * @param imageVersion
     * @param imageLanguage
     * @param imageUsage
     * @param messageLog
     * @return
     */
    public EtkDataHotspotList loadHotspotsFromFile(DWFile file, String imageNumber, String imageVersion, String imageLanguage,
                                                   ImageVariant imageUsage, EtkMessageLog messageLog, boolean removeLeadingZeros) {
        EtkDataHotspotList hotspotList = EtkDataObjectFactory.createDataHotspotList();
        if (file.length() == 0) { // SEN-Datei ist leer
            return hotspotList;
        } else if (hotspotList.loadHotspotsFromFile(file, imageNumber, imageVersion, imageLanguage, imageUsage, getProject(),
                                                    messageLog, removeLeadingZeros)) {
            return hotspotList;
        } else {
            getMessageLog().fireMessage(translateForLog("!!Fehler in Datei:") + ' ' + file.getName(), MessageLogType.tmlError,
                                        MessageLogOption.TIME_STAMP);
            getMessageLog().fireMessage("!!Hotspots nicht bestimmbar", MessageLogType.tmlError);
            return null;
        }
    }

    public EtkDataHotspotList loadHotspotsFromFile(DWFile file, String imageNumber, String imageVersion, String imageLanguage,
                                                   ImageVariant imageUsage, EtkMessageLog messageLog) {
        return loadHotspotsFromFile(file, imageNumber, imageVersion, imageLanguage, imageUsage, messageLog, false);
    }

    /**
     * Lädt die Hotspots aus dem übergebenen Byte-Array (SEN/SVG-Datei) und fügt sie dem {@link EtkDataHotspotList}-Rückgabewert
     * mit den anderen übergebenen Parametern hinzu.
     *
     * @param hotspotByteArray ByteArray einer SEN/SVG-Datei
     * @param isSVG
     * @param imageNumber
     * @param imageVersion
     * @param imageLanguage
     * @param imageUsage
     * @param messageLog
     * @return
     */
    public EtkDataHotspotList loadHotspotsFromByteArray(byte[] hotspotByteArray, boolean isSVG, String imageNumber, String imageVersion,
                                                        String imageLanguage, ImageVariant imageUsage, EtkMessageLog messageLog) {
        EtkDataHotspotList hotspotList = EtkDataObjectFactory.createDataHotspotList();
        if (hotspotByteArray.length == 0) { // SEN/SVG-ByteArray ist leer
            return hotspotList;
        } else {
            if (isSVG) {
                if (hotspotList.loadHotspotsFromSVGContent(hotspotByteArray, imageNumber, imageVersion, imageLanguage, imageUsage,
                                                           getProject(), messageLog)) {
                    return hotspotList;
                }
            } else {
                if (hotspotList.loadHotspotsFromInputstream(new ByteArrayInputStream(hotspotByteArray), MimeTypes.EXTENSION_SEN,
                                                            imageNumber, imageVersion, imageLanguage, imageUsage, getProject(),
                                                            messageLog, true)) {
                    return hotspotList;
                }
            }

            getMessageLog().fireMessage(translateForLog("!!Fehler beim Import der Hotspot-Datei aus der XML Nachricht für " +
                                                        "PV ID: \"%1\" und PV Rev ID: \"%2\".", imageNumber, imageVersion),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            getMessageLog().fireMessage("!!Hotspots nicht bestimmbar", MessageLogType.tmlError);
            return null;
        }
    }

    /**
     * Überprüft, ob die übergebene {@link iPartsXMLMediaBinaryFile} valide ist. Falls nicht, wird ein MessageLog Eintrag erzeugt
     *
     * @param variant
     * @param mediaBinaryFile
     * @param messagePrefix
     * @return
     */
    private boolean checkVariantFile(iPartsXMLMediaVariant variant, iPartsXMLMediaBinaryFile mediaBinaryFile, String messagePrefix) {
        if (mediaBinaryFile == null) {
            getMessageLog().fireMessage(translateForLog("!!%1 konnte in der XML Nachricht für PV ID \"%2\" und PV Rev ID \"%3\" nicht gefunden werden!",
                                                        translateForLog(messagePrefix), variant.getItemId(), variant.getItemRevId()),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }
        return true;
    }

    /**
     * {@link MessageEvent} für Warnungen und Fehler während dem Import.
     *
     * @return
     */
    public MessageEvent getMessageListenerForLogger() {
        if (messageListenerForLogger == null) {
            messageListenerForLogger = new MessageEvent() {
                @Override
                public void fireEvent(MessageEventData event) {
                    if ((event.getMessageLogType() == MessageLogType.tmlError) || (event.getMessageLogType() == MessageLogType.tmlWarning)) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, (event.getMessageLogType() == MessageLogType.tmlError) ? LogType.ERROR : LogType.DEBUG,
                                   event.getFormattedMessage(Language.EN.getCode()));
                    }
                }
            };
        }
        return messageListenerForLogger;
    }

    @Override
    public EtkMessageLog getMessageLog() {
        // Bei einem Import ohne GUI sollen die Warnungen und Fehler beim Hotspot-Import in den iParts-Log
        // geschrieben werden
        if (messageLogInitialized()) {
            return super.getMessageLog();
        } else {
            EtkMessageLog messageLogForLogger = new EtkMessageLog();
            messageLogForLogger.addMessageEventListener(getMessageListenerForLogger());
            return messageLogForLogger;
        }
    }
}
