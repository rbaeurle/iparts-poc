/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequestor;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

public class iPartsPicOrderEditHelper implements iPartsConst {

    private static final String BUTTON_TEXT_CREATE_CHANGE_ORDER = "!!Änderungsauftrag erzeugen";
    private static final String BUTTON_TEXT_TRANSFER_PICTURES = "!!Zeichnung übernehmen";
    private static final String EXISTING_ORDER_DIALOG_TITLE = "!!Änderungsauftrag vorhanden";

    /**
     * Erstellt eine MQ Nachricht mit der übergebenen Operation.
     *
     * @param requestOperation
     * @param messageGUID
     * @param userId
     * @return
     */
    public static iPartsXMLMediaMessage createMessageFromOperation(AbstractXMLRequestOperation requestOperation, String messageGUID,
                                                                   String userId) {
        if (requestOperation == null) {
            return null;
        }
        iPartsXMLRequest request = createRequest(requestOperation, messageGUID, userId);
        iPartsXMLMediaMessage xmlMediaMessage = new iPartsXMLMediaMessage(true);
        xmlMediaMessage.setTypeObject(request);
        return xmlMediaMessage;
    }

    /**
     * Erstellt eine MQ Anfrage mit der übergebenen Operation.
     *
     * @param operation
     * @param messageGUID
     * @param userId
     * @return
     */
    private static iPartsXMLRequest createRequest(AbstractXMLRequestOperation operation, String messageGUID, String userId) {
        if (operation == null) {
            return null;
        }
        iPartsXMLRequest request = new iPartsXMLRequest(messageGUID, iPartsTransferConst.PARTICIPANT_IPARTS, iPartsTransferConst.PARTICIPANT_ASPLM);
        iPartsXMLRequestor requestor = new iPartsXMLRequestor(userId);
        request.setRequestor(requestor);
        request.setOperation(operation);
        return request;
    }

    /**
     * Liefert zurück, ob das aktuelle Modul eine freie SA ist
     *
     * @param connector
     * @return
     */
    public static boolean isRetailSa(EditModuleFormIConnector connector) {
        EtkDataAssembly currentAssembly = connector.getCurrentAssembly();
        if (currentAssembly instanceof iPartsDataAssembly) {
            return ((iPartsDataAssembly)currentAssembly).isSAAssembly();
        }
        return false;
    }


    /**
     * Liefert den neuesten Bild-/Änderungsafutrag zum übergebenen Auftrag
     *
     * @param project
     * @param currentPicOrder
     * @param assembly
     * @param isCopy          Flag, falls es ein Kopierauftrag ist
     * @return
     */
    public static Optional<iPartsDataPicOrder> getNewestChangeOrderForPicOrderFromSameModule(EtkProject project,
                                                                                             iPartsDataPicOrder currentPicOrder,
                                                                                             iPartsDataAssembly assembly,
                                                                                             boolean isCopy) {
        if (currentPicOrder == null) {
            return Optional.empty();
        }

        // Lade alle Aufträge zur MC Nummer
        List<iPartsDataPicOrder> picOrders = iPartsDataPicOrderList.loadPicOrdersForMCItemId(project, currentPicOrder.getOrderIdExtern()).getAsList();
        return checkExistingPicOrdersForChangeOrder(project, picOrders, currentPicOrder, assembly, isCopy);
    }

    /**
     * Überprüft, ob es einen abgeschlosenen Bild-/Änderungsauftrag zum übergebenen Bildauftrag gibt, der als Basis
     * für einen neuen Änderunsgauftrag genommen werden kann.
     * <p>
     * Fall es keinen abgeschlossenen Auftrag gibt, wird geprüft, ob es auf dem Weg zum neuesten Bildauftrag neuere
     * Bilder gibt. Fall ja, kann der Autor diese übernehmen.
     *
     * @param project
     * @param allPicOrdersForMCNumber
     * @param currentPicOrder
     * @param assembly
     * @param isCopy                  Flag, falls es ein Kopierauftrag ist
     * @return
     */
    public static Optional<iPartsDataPicOrder> checkExistingPicOrdersForChangeOrder(EtkProject project, List<iPartsDataPicOrder> allPicOrdersForMCNumber,
                                                                                    iPartsDataPicOrder currentPicOrder,
                                                                                    iPartsDataAssembly assembly,
                                                                                    boolean isCopy) {
        if ((allPicOrdersForMCNumber == null) || allPicOrdersForMCNumber.isEmpty()) {
            // Wurden keine Bildaufträge übergeben, dann gib den aktuellen Bildauftrag zurück oder ein leeres Optional
            return Optional.ofNullable(currentPicOrder);
        }
        // Zur Sicherheit nach Revision und Statusänderungsdatum sortieren. Statusänderung ist hier wichtig, weil ein
        // angelegter Änderunsgauftrag, der nicht verschickt wurde, die gleiche Revision hat, wie der Auftrag auf dem
        // er basiert
        allPicOrdersForMCNumber.sort((previousPicOrder, nextPicOrder) -> {
            String sortStringPrevious = Utils.toSortString(previousPicOrder.getOrderRevisionExtern());
            String sortStringNext = Utils.toSortString(nextPicOrder.getOrderRevisionExtern());
            if (sortStringPrevious.compareTo(sortStringNext) == 0) {
                sortStringPrevious = Utils.toSortString(previousPicOrder.getFieldValue(FIELD_PO_STATUS_CHANGE_DATE));
                sortStringNext = Utils.toSortString(nextPicOrder.getFieldValue(FIELD_PO_STATUS_CHANGE_DATE));
            }
            return sortStringNext.compareTo(sortStringPrevious);
        });
        // Den neuesten Auftrag holen
        Optional<iPartsDataPicOrder> newestValidAndNonCancelledPicOrder = allPicOrdersForMCNumber.stream().filter(picorder -> picorder.isValid() && !picorder.isCancelled()).findFirst();
        if (!newestValidAndNonCancelledPicOrder.isPresent()) {
            MessageDialog.show(TranslationHandler.translate("!!Zur verknüpften Bildnummer existiert kein gültiger Bildauftrag!")
                               + "\n\n"
                               + TranslationHandler.translate("!!Bitte legen Sie einen neuen Bildauftrag an."));
            return Optional.empty();
        }

        if ((currentPicOrder != null) && currentPicOrder.getAsId().getOrderGuid().equals(newestValidAndNonCancelledPicOrder.get().getAsId().getOrderGuid())) {
            return Optional.of(currentPicOrder);
        }

        if (newestValidAndNonCancelledPicOrder.get().finishedSucessfully()) {
            return handleNewestPicOrderFinished(project, newestValidAndNonCancelledPicOrder.get(), assembly, isCopy);
        }

        Optional<iPartsDataPicOrder> newestAcceptedPicOrder = allPicOrdersForMCNumber
                .stream()
                .filter(picOrder -> picOrder.finishedSucessfully() || picOrder.isReplacedByChange())
                .findFirst();
        boolean hasNewerPicOrderWithPic = newestAcceptedPicOrder.isPresent()
                                          && ((currentPicOrder == null) ||
                                              !currentPicOrder.getAsId().getOrderGuid().equals(newestAcceptedPicOrder.get().getAsId().getOrderGuid()));
        if (hasNewerPicOrderWithPic && foundPicOrderHasNewPictures(newestAcceptedPicOrder.get(), assembly)) {
            handleNewerPicOrderWithPictures(project, newestAcceptedPicOrder.get(), assembly, isCopy);
            return Optional.empty();
        } else {
            String changeOrdertype = isCopy ? "!!Kopierauftrag" : "!!weiterer Änderungsauftrag";
            MessageDialog.show(TranslationHandler.translate("!!Es existiert bereits ein offener Änderungsauftrag zur verknüpften MC Nummer. Ein " +
                                                            "%1 kann nicht erzeugt werden!", TranslationHandler.translate(changeOrdertype)));
        }

        return Optional.empty();
    }

    private static boolean foundPicOrderHasNewPictures(iPartsDataPicOrder foundPicOrder, iPartsDataAssembly assembly) {
        // Map PV Nummer auf Revision
        Map<String, Integer> currentPics = assembly.getImages().stream()
                .collect(Collectors.toMap(image -> image.getFieldValue(FIELD_I_IMAGES),
                                          image -> StrUtils.strToIntDef(image.getFieldValue(FIELD_I_PVER), 0), (p1, p2) -> (p1 > p2) ? p1 : p2));
        // Es muss mind. ein Bild existieren, dass neu ist oder eine höhere Revision hat (Es sollen aber nur PV Bilder
        // berücksichtigt werden)
        Optional<iPartsDataPicOrderPicture> aNewPicture = foundPicOrder.getPictures().getAsList().stream()
                .filter(picture -> XMLImportExportHelper.isASPLMPictureNumber(picture.getAsId().getPicItemId())
                                   && ((currentPics.get(picture.getAsId().getPicItemId()) == null)
                                       || (StrUtils.strToIntDef(picture.getAsId().getPicItemRevId(), 0) > currentPics.get(picture.getAsId().getPicItemId()))))
                .findAny();

        return aNewPicture.isPresent();
    }

    private static void handleNewerPicOrderWithPictures(EtkProject project, iPartsDataPicOrder newestAcceptedPicOrder,
                                                        iPartsDataAssembly assembly, boolean isCopy) {
        // Ein Kopierauftrag darf nicht erzeugt werden, wenn das Bild in einem offenen Änderungsauftrag vorhanden ist
        if (isCopy) {
            MessageDialog.show(TranslationHandler.translate("!!Es existiert ein offener Änderungsauftrag zur " +
                                                            "verknüpften MC Nummer \"%1\". Ein Kopierauftrag kann nicht erzeugt" +
                                                            " werden!",
                                                            newestAcceptedPicOrder.getOrderIdExtern()));
            return;
        }
        String text = TranslationHandler.translate("!!Es existiert bereits ein offener Änderungsauftrag zur " +
                                                   "verknüpften MC Nummer \"%1\". Ein neuer Änderungsauftrag kann nicht erzeugt werden!",
                                                   newestAcceptedPicOrder.getOrderIdExtern())
                      + "\n\n"
                      + TranslationHandler.translate("!!Es gibt jedoch eine aktuellere Zeichnung zur MC Nummer." +
                                                     " Soll diese übernommen werden?");


        String result = MessageDialog.show(text, EXISTING_ORDER_DIALOG_TITLE, MessageDialogIcon.INFORMATION.getImage(),
                                           BUTTON_TEXT_TRANSFER_PICTURES, MessageDialogButtons.CANCEL.getButtonText());
        if (!result.equals(MessageDialogButtons.CANCEL.getButtonText())) {
            connectPicturesWithDifferentModule(project, newestAcceptedPicOrder, assembly);
        }
    }

    private static Optional<iPartsDataPicOrder> handleNewestPicOrderFinished(EtkProject project, iPartsDataPicOrder newestPicOrder,
                                                                             iPartsDataAssembly assembly, boolean isCopy) {
        String result;
        if (foundPicOrderHasNewPictures(newestPicOrder, assembly)) {
            // Existiert ein abgeschlossener Änderungsauftrag zum Bild, dann wird bei einem Kopierauftrag IMMER der neueste
            // aktuelle Auftrag als Basis verwendet
            if (isCopy) {
                return Optional.of(newestPicOrder);
            }

            String text = TranslationHandler.translate("!!Es existiert bereits ein abgeschlossener Änderungsauftrag" +
                                                       " zur verknüpften MC Nummer \"%1\".", newestPicOrder.getOrderIdExtern())
                          + "\n\n"
                          + TranslationHandler.translate("!!Soll der Änderungsauftrag trotzdem erzeugt werden oder soll nur die" +
                                                         " aktuellste Zeichnung übernommen werden?");

            result = MessageDialog.show(text, EXISTING_ORDER_DIALOG_TITLE, MessageDialogIcon.INFORMATION.getImage(),
                                        BUTTON_TEXT_CREATE_CHANGE_ORDER, BUTTON_TEXT_TRANSFER_PICTURES,
                                        MessageDialogButtons.CANCEL.getButtonText());
        } else {
            result = BUTTON_TEXT_CREATE_CHANGE_ORDER;
        }
        if (result.equals(MessageDialogButtons.CANCEL.getButtonText())) {
            return Optional.empty();
        } else if (result.equals(BUTTON_TEXT_CREATE_CHANGE_ORDER)) {
            return Optional.of(newestPicOrder);
        } else {
            connectPicturesWithDifferentModule(project, newestPicOrder, assembly);
            return Optional.empty();
        }
    }

    /**
     * Liefert den aktuellsten Bildauftrag zum übergebenen {@link EtkDataImage}.
     *
     * @param project
     * @param picture
     * @return
     */
    public static Optional<iPartsDataPicOrder> getNewestPicOrderForPicture(EtkProject project, EtkDataImage picture) {
        if (picture == null) {
            return Optional.empty();
        }
        iPartsDataPicOrderPicturesList picturesList = iPartsDataPicOrderPicturesList.loadPicturesListForPicture(project, picture);
        if (picturesList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new iPartsDataPicOrder(project, new iPartsPicOrderId(picturesList.get(0).getAsId().getOrderGuid())));
    }

    /**
     * Verknüpft die Bilder aus dem übergebenen Bildauftrag mit der übergebenen {@link iPartsDataAssembly}.
     *
     * @param project
     * @param acceptedPicOrder
     * @param assembly
     */
    private static void connectPicturesWithDifferentModule(EtkProject project, iPartsDataPicOrder acceptedPicOrder,
                                                           iPartsDataAssembly assembly) {
        Set<String> existingImagesFromAssembly = new HashSet<>();
        for (EtkDataImage currentImage : assembly.getImages()) {
            existingImagesFromAssembly.add(currentImage.getFieldValue(FIELD_I_IMAGES) + currentImage.getFieldValue(FIELD_I_PVER));
        }
        List<EtkDataImage> imagesList = new DwList<>();
        GenericEtkDataObjectList dataObjectList = new GenericEtkDataObjectList();
        dataObjectList.add(assembly, DBActionOrigin.FROM_EDIT);
        for (iPartsDataPicOrderPicture picture : acceptedPicOrder.getPictures()) {
            if (existingImagesFromAssembly.contains(picture.getAsId().getPicItemId() + picture.getAsId().getPicItemRevId())) {
                continue;
            }
            EtkDataImage dataImage = assembly.addImage(picture.getAsId().getPicItemId(), picture.getAsId().getPicItemRevId(), true, DBActionOrigin.FROM_EDIT);
            imagesList.add(dataImage);
            dataObjectList.add(dataImage, DBActionOrigin.FROM_EDIT);
        }
        if (project.isRevisionChangeSetActiveForEdit()) {
            project.getRevisionsHelper().addDataObjectListToActiveChangeSetForEdit(dataObjectList);

            // Nach dem Hinzufügen der neuen Zeichnungen den Gültigkeitsbereich der Zeichnungen aktualisieren
            iPartsDataImage.updateAndSaveValidityScopeForImages(imagesList, project);
        }
    }

    /**
     * Check, ob der Bildauftrag angezeigt werden soll
     *
     * @param picOrder
     * @return
     */
    public static boolean isPicOrderVisible(iPartsDataPicOrder picOrder, EtkDataAssembly assembly) {
        // Ungültige Bildaufträge ausfiltern
        if (picOrder.isInvalid()) {
            return false;
        }
        // Stornierte Bildaufträge ausfiltern
        if (picOrder.isCancelled()) {
            return false;
        }
        // Abgeschlossene Aufträge und "Ersetzt durch Änderungsauftrag" werden angezeigt, wenn sie ein aktives Bild in
        // der Anzeige haben
        if (picOrder.finishedSucessfully() || picOrder.isReplacedByChange()) {
            // Kann eigentlich nicht passieren, da für die Endzustände der Auftrag ja komplett durch den Workflow ist
            if (picOrder.getPictures().isEmpty()) {
                return false;
            }
            // Alle Bilder des Moduls
            List<EtkDataImage> images = assembly.getImages();
            // Set mit Bildnummer und Revision der Bilder am Modul
            Set<String> picOrderPicIdsFromAssembly = images.stream()
                    .map(image -> image.getFieldValue(FIELD_I_IMAGES) + image.getFieldValue(FIELD_I_PVER))
                    .collect(Collectors.toSet());
            // Sobald eines der Bilder am Bildauftrag in dem Set mit den Bilder des Moduls auftaucht ist der Bildauftrag gültig
            Optional<iPartsDataPicOrderPicture> firstPictureInBothLists = picOrder.getPictures().getAsList().stream()
                    .filter(picture -> picOrderPicIdsFromAssembly.contains(picture.getAsId().getPicItemId() + picture.getAsId().getPicItemRevId()))
                    .findFirst();
            return firstPictureInBothLists.isPresent();
        }
        // Nicht abgeschlossene Aufträge sind "aktive" Aufträge und müssen angezeigt werden
        return true;
    }

    /**
     * Liefert die übergebenen Bildaufträge sortiert zurück. Optional können für das Modul nicht relevante Bildaufträge
     * ausgefiltert werden.
     *
     * @param allPicOrders
     * @param assembly
     * @return
     */
    private static List<EtkDataObject> getPicOrdersSortedAndFiltered(List<iPartsDataPicOrder> allPicOrders, EtkDataAssembly assembly) {
        return allPicOrders.stream().filter(picOrder -> (assembly == null) || isPicOrderVisible(picOrder, assembly)).sorted((o1, o2) -> {
            // Erst nach der MC Nummer sortieren
            String valueFirst = o1.getOrderIdExtern();
            String valueSecond = o2.getOrderIdExtern();
            int result = valueSecond.compareTo(valueFirst);
            if (result == 0) {
                // Sind die MC Nummern gleich, nach der Revision sortieren
                valueFirst = o1.getOrderRevisionExtern();
                valueSecond = o2.getOrderRevisionExtern();
                result = valueSecond.compareTo(valueFirst);
            }
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * Liefert die übergebenen Bildaufträge gefiltert nach ihrer Relevanz für das übergebene Modul und sortiert zurück
     *
     * @param allPicOrders
     * @param assembly
     * @return
     */
    public static List<EtkDataObject> getPicOrdersSortedAndRelevantForAssembly(List<iPartsDataPicOrder> allPicOrders, EtkDataAssembly assembly) {
        return getPicOrdersSortedAndFiltered(allPicOrders, assembly);
    }

    /**
     * Liefert die übergebenen Bildaufträge sortiert zurück
     *
     * @param allPicOrders
     * @return
     */
    public static List<EtkDataObject> getPicOrdersSorted(List<iPartsDataPicOrder> allPicOrders) {
        return getPicOrdersSortedAndFiltered(allPicOrders, null);
    }

    public static boolean simulatePicContent() {
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_SIM_PIC_CONTENT_XML);
    }

    /**
     * Liefert zum Bild die MC Nummer des Bildauftrags, mit dem das Bild verknüpft ist (sofern vorhanden)
     *
     * @param image
     * @return
     */
    public static String getMediaContainerFromImage(EtkDataImage image) {
        if ((image != null) && image.getAttributes().fieldExists(iPartsDataVirtualFieldsDefinition.DA_PICTURE_ORDER_ID)) {
            return image.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_PICTURE_ORDER_ID);
        } else {
            return "";
        }
    }

    /**
     * Fügt dem übergebenen {@link EtkDataImage} Objekt die verknüpfte MC Nummer hinzu
     *
     * @param project
     * @param images
     */
    public static void addMCNumbersToPictures(EtkProject project, DBDataObjectList<EtkDataImage> images) {
        if ((images == null) || images.isEmpty()) {
            return;
        }
        // Gehe für jedes Bild in die DA_PICORDER_PICTURES Tabelle mit Join auf die DA_PICORDER Tabelle und sortiere
        // das Ergebnis nach der Revision der MC Nummer (nicht PV Nummer). Der jüngste Auftrag liefert dann die MC Nummer
        // für das Bild. Eigentlich sollte es hier immer nur einen Treffer geben, da eine PV Revision nur mit genau einem
        // Bildauftrag (MC Nummer) kommen kann. Sortierung wird nur zur Sicherheit gemacht.
        EtkDisplayFields picorderFields = new EtkDisplayFields();
        picorderFields.addFeld(new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_ID_EXTERN, false, false));
        for (EtkDataImage dataImage : images) {
            iPartsDataPicOrderList picorderList = new iPartsDataPicOrderList();
            picorderList.searchSortAndFillWithJoin(project, null, picorderFields,
                                                   new String[]{ FIELD_DA_PO_ORDER_GUID }, TABLE_DA_PICORDER_PICTURES,
                                                   new String[]{ FIELD_DA_POP_ORDER_GUID }, false, false,
                                                   new String[]{ TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMID),
                                                                 TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMREVID) },
                                                   new String[]{ dataImage.getFieldValue(FIELD_I_IMAGES),
                                                                 dataImage.getFieldValue(FIELD_I_PVER) }, false,
                                                   new String[]{ FIELD_DA_PO_ORDER_REVISION_EXTERN }, false);
            if (!picorderList.isEmpty()) {
                iPartsDataPicOrder newestPicOrder = picorderList.getLast();
                // Der Zustand des Bildauftrags ist hier egal, da es nur darum geht, mit welchem Bidlauftrag das Bild
                // veknüpft ist
                dataImage.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_PICTURE_ORDER_ID, newestPicOrder.getOrderIdExtern(),
                                                   true, DBActionOrigin.FROM_DB);
            }
        }
    }
}
