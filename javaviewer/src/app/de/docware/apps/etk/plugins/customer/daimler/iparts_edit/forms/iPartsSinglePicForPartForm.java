/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPoolVariants;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.mediaobjects.MediaServiceMediaObjectResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.mediaobjects.MediaServiceMediaObjectsService;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dialog zur Anzeige von Einzelteilbildern
 */
public class iPartsSinglePicForPartForm implements iPartsConst {

    /**
     * Related Info Icon ermitteln
     *
     * @return
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(AbstractJavaViewerFormIConnector connector) {
        EtkProject project = connector.getProject();
        boolean viewSinglePicForPartActive = iPartsPlugin.isWebservicesSinglePicViewActive() && StrUtils.isValid(iPartsPlugin.getWebservicesSinglePicPartsBaseURI());
        AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(null, EditDefaultImages.edit_single_pic_for_part.getImage()) {
            @Override
            public void onIconClick(EtkDataPartListEntry partListEntry, NavigationPath path) {
                if (viewSinglePicForPartActive) {
                    String matNr = partListEntry.getPart().getAsId().getMatNr();
                    String formattedMatNr = iPartsNumberHelper.formatPartNo(project, matNr);

                    EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Einzelteilbilder anzeigen",
                                                                             TranslationHandler.translate("!!Teilenummer: %1",
                                                                                                          formattedMatNr),
                                                                             EditDefaultImages.edit_single_pic_for_part.getImage(),
                                                                             false);
                    messageLogForm.getGui().setResizable(false);
                    messageLogForm.getGui().setWidth(300);
                    messageLogForm.getGui().setHeight(170);
                    messageLogForm.setMessagesTitle("!!Einzelteilbilder werden geladen...");
                    messageLogForm.setMessagesTextAreaVisible(false);
                    messageLogForm.showModal(connector.getRootWindow(), thread -> {
                        messageLogForm.getMessageLog().fireProgress(0, 100, "", false, false);
                        Set<MediaServiceMediaObjectResponse> mediaObjects;
                        try {
                            mediaObjects = MediaServiceMediaObjectsService.getMediaObjectsFromMediaService(partListEntry, project);
                        } catch (CallWebserviceException e) {
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.ERROR, e);
                            MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Abrufen der URLs für die Einzelteilbilder von Teilenummer \"%1\":",
                                                                                 formattedMatNr) + "\n" + e.getHttpResponseCode()
                                                    + " - " + e.getMessage(), "!!Einzelteilbilder anzeigen");
                            messageLogForm.closeWindow(ModalResult.CANCEL);
                            return;
                        }

                        if (mediaObjects == null) {
                            mediaObjects = new HashSet<>();
                        }

                        VarParam<Integer> progress = new VarParam<>(0);
                        int maxProgress = mediaObjects.size();

                        // Fake-Liste von EtkDataImages erzeugen für die MediaObjects zur Anzeige im EditShowImagesInWindow
                        VarParam<Integer> imageCounter = new VarParam<>(1);
                        List<EtkDataImage> dataImages = mediaObjects.stream()
                                .map(mediaObject -> {
                                    if (messageLogForm.isCancelled()) {
                                        return null;
                                    }
                                    try {
                                        FrameworkImage image = MediaServiceMediaObjectsService.downloadMediaObject(mediaObject);
                                        progress.setValue(progress.getValue() + 1);
                                        messageLogForm.getMessageLog().fireProgress(progress.getValue(), maxProgress, "",
                                                                                    false, false);
                                        if (image == null) {
                                            return null;
                                        }

                                        // Fake-EtkDataImage erzeugen
                                        DBDataObjectAttributes dataImageAttributes = new DBDataObjectAttributes();
                                        dataImageAttributes.addField(FIELD_I_TIFFNAME, partListEntry.getOwnerAssemblyId().getKVari(),
                                                                     DBActionOrigin.FROM_DB);
                                        dataImageAttributes.addField(FIELD_I_VER, "", DBActionOrigin.FROM_DB);
                                        dataImageAttributes.addField(FIELD_I_BLATT, String.valueOf(imageCounter.getValue()),
                                                                     DBActionOrigin.FROM_DB);
                                        String imageNumber = "MediaObject" + imageCounter;
                                        dataImageAttributes.addField(FIELD_I_IMAGES, imageNumber, DBActionOrigin.FROM_DB);
                                        dataImageAttributes.addField(FIELD_I_PVER, "", DBActionOrigin.FROM_DB);
                                        EtkDataImage dataImage = EtkDataImage.createImage(project, partListEntry.getOwnerAssemblyId(),
                                                                                          dataImageAttributes, DBActionOrigin.FROM_DB);

                                        // Fake-EtkDataPool erzeugen
                                        EtkDataPool dataPool = EtkDataObjectFactory.createDataPool();
                                        dataPool.init(project);
                                        dataPool.setPKValues(imageNumber, "", "", EtkDataImage.IMAGE_USAGE_2D, DBActionOrigin.FROM_DB);
                                        dataPool.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                        DBDataObjectAttribute imageBLOBAttribute = new DBDataObjectAttribute(FIELD_P_DATA,
                                                                                                             DBDataObjectAttribute.TYPE.BLOB,
                                                                                                             false);
                                        imageBLOBAttribute.setValueAsBlob(image.getContent(), DBActionOrigin.FROM_DB);
                                        dataPool.getAttributes().addField(imageBLOBAttribute, DBActionOrigin.FROM_DB);

                                        // Fake-EtkDataPoolVariants erzeugen
                                        EtkDataPoolVariants dataPoolVariants = EtkDataObjectFactory.createDataPoolVariants();
                                        dataPoolVariants.add(dataPool, DBActionOrigin.FROM_DB);
                                        dataImage.setChildren(EtkDataImage.CHILDREN_NAME_POOL_VARIANTS, dataPoolVariants);

                                        imageCounter.setValue(imageCounter.getValue() + 1);
                                        return dataImage;
                                    } catch (CallWebserviceException e) {
                                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.ERROR, e);
                                        MessageDialog.showError(e.getMessage(), TranslationHandler.translate("!!Einzelteilbilder für Teilenummer \"%1\"",
                                                                                                             formattedMatNr));
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        if (messageLogForm.isCancelled()) {
                            return;
                        }

                        if (dataImages.isEmpty()) {
                            MessageDialog.show(TranslationHandler.translate("!!Es wurden keine Einzelteilbilder für das Material \"%1\" gefunden.",
                                                                            formattedMatNr), "!!Einzelteilbilder anzeigen");
                            messageLogForm.closeWindow(ModalResult.CANCEL);
                            return;
                        }

                        // Nicht-modalen Dialog fast im Vollbild anzeigen
                        Dimension screenSize = FrameworkUtils.getScreenSize();
                        String title = TranslationHandler.translate((dataImages.size() > 1) ? "!!Einzelteilbilder für Teilenummer \"%1\""
                                                                                            : "!!Einzelteilbild für Teilenummer \"%1\"",
                                                                    formattedMatNr);
                        EditShowImagesInWindow imagesWindow = new EditShowImagesInWindow(title, screenSize.width - 20,
                                                                                         screenSize.height - 20, dataImages,
                                                                                         false, -1, project);

                        imagesWindow.showNonModal(GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW);
                        messageLogForm.closeWindow(ModalResult.OK);
                    });
                }
            }
        };

        if (viewSinglePicForPartActive) {
            iconInfo.setHint("!!Einzelteilbilder anzeigen");
            iconInfo.setCursor(DWCursor.Hand);
        } else {
            iconInfo.setHint("!!Einzelteilbilder vorhanden");
        }
        iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPluginCallback);
        return iconInfo;
    }
}