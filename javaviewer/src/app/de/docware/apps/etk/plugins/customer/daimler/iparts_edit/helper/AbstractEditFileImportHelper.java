/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.file.DWFile;

import java.util.List;
import java.util.Map;

/**
 * Basisklasse für den Import von SAP.CTT und PSK-Dateien zur Erzeugung von Teilepositionen
 * Verbindet den FileChooser und eine MessageLogForm und alle relevanten Abläufe
 */
public abstract class AbstractEditFileImportHelper {

    private AbstractJavaViewerFormIConnector dataConnector;
    protected Map<PartId, EtkDataPart> newDataParts;  // neue Elemente für den MatStamm
    protected GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved;  // Liste für neu erzeugte Objekte
    protected GuiFileChooserTextfield importFileChooser;
    protected EtkMessageLogForm messageLogForm;
    protected boolean hasErrors;  // Kennung ob Fehler aufgetreten sind
    protected boolean hasErrorsOrWarnings;
    protected Session session;

    /**
     * Erzeugt eine Instanz von EditImportImageForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     */
    public AbstractEditFileImportHelper(AbstractJavaViewerFormIConnector dataConnector) {
        this.dataConnector = dataConnector;
        this.dataObjectsToBeSaved = null;
        this.session = null;
        postCreateGui();
    }

    protected void postCreateGui() {
        importFileChooser = createImportFileChooser();
        importFileChooser.setPurpose(FileChooserPurpose.OPEN);

        messageLogForm = createMessageLogForm();
    }

    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)dataConnector;
    }

    public EtkProject getProject() {
        return getConnector().getProject();
    }

    public GenericEtkDataObjectList<EtkDataObject> getDataObjectsToBeSaved() {
        return dataObjectsToBeSaved;
    }

    public ModalResult showModal() {
        importFileChooser.showDialog();
        if (!importFileChooser.getSelectedFiles().isEmpty()) {
            session = Session.get();
            return messageLogForm.showModal(createImportRunnable(importFileChooser.getSelectedFiles()));
        } else {
            return ModalResult.CANCEL;
        }
    }

    /**
     * Rumpf für die Abarbeitung der Dateien
     *
     * @param selectedFiles
     * @return
     */
    private FrameworkRunnable createImportRunnable(final List<DWFile> selectedFiles) {
        return new FrameworkRunnable() {

            @Override
            public void run(FrameworkThread thread) {
                int maxProgress = selectedFiles.size();
                int currentProgress = 0;
                hasErrors = false;
                hasErrorsOrWarnings = false;
                if (maxProgress > 1) {
                    messageLogForm.getGui().setHeight(500);
                }
                fireProgress(currentProgress, maxProgress);
                // Vorbereitung für Import
                if (!prepareFileImport()) {
                    fireMessage("!!Import mit Warnungen/Fehlern beendet.");
                    messageLogForm.getMessageLog().hideProgress();
                    enableMessageLogFormButtons(true);
                    messageLogForm.setAutoClose(false);
                    return;
                }

                try {
                    // Schleife über alle Dateien
                    for (DWFile file : selectedFiles) {
                        fireMessage("!!Analysiere %1", file.getName());
                        // File lesen und analysieren
                        handleFile(file);
                        currentProgress++;
                        fireProgress(currentProgress, maxProgress);
                    }
                    fireProgress(maxProgress, maxProgress);

                    // Abfrage, ob der Import fortgesetzt werden soll
                    boolean isImportAllowed = isImportAllowed();
                    if (isImportAllowed) {
                        // Import vornehmen
                        doImport();
                        if (!hasErrorsOrWarnings) {
                            fireMessage("!!Fertig");
                        }
                    }
                    // zum Testen
//                    hasErrorsOrWarnings = true;
                    // wenn Fehler oder Warnungen =>
                    if (hasErrorsOrWarnings) {
                        // Fehlermeldung am Ende
                        fireMessage("!!Import mit Warnungen/Fehlern beendet.");
                        messageLogForm.getMessageLog().hideProgress();
                        enableMessageLogFormButtons(true);
                        messageLogForm.setAutoClose(false);
                        if ((dataObjectsToBeSaved != null) && !dataObjectsToBeSaved.isEmpty()) {
                            // falls Elemente (Mat oder partListEntries) erzeugt wurden => speichere zumindest die erzeugten
                            // Kennzeichen für close() ModalResult.OK zu liefern
                            hasErrorsOrWarnings = false;
                        }
                    }
                } finally {
                    // Aufräumen
                }
            }

            private void enableMessageLogFormButtons(boolean enabled) {
                Runnable runnable = (() -> messageLogForm.setButtonsEnabled(enabled));

                if (session != null) {
                    session.invokeThreadSafe(() -> runnable.run());
                } else {
                    runnable.run();
                }
            }
        };
    }

    /**
     * Vorbereitung für Import
     *
     * @return
     */
    protected abstract boolean prepareFileImport();

    /**
     * eine Datei lesen und analysieren
     *
     * @param file
     */
    protected abstract void handleFile(DWFile file);

    /**
     * Abfrage ob, nach Lesen aller Dateien, der Import fortgesetzt werden soll
     *
     * @return
     */
    protected abstract boolean isImportAllowed();

    /**
     * Import vornehmen
     */
    protected abstract void doImport();

    /**
     * MessageLogForm anlegen. Sind Fehler aufgetreten, so bleibt sie offen, sonst wird sie automatisch geschlossen
     *
     * @return
     */
    protected EtkMessageLogForm createMessageLogForm() {
        EtkMessageLogForm msgLogForm = new EtkMessageLogForm("!!Importieren", "!!SAP.CTT Dateien importieren",
                                                             null /*iPartsToolbarButtonAlias.EDIT_DELETE.getImage()*/,
                                                             false) {
            @Override
            protected void close(Event event) {
                if (hasErrorsOrWarnings) {
                    closeWindow(ModalResult.CANCEL);
                } else {
                    closeWindow(ModalResult.OK);
                }
            }
        };
        msgLogForm.disableButtons(true);
        msgLogForm.getGui().setHeight(500);
        return msgLogForm;
    }

    /**
     * FileChooser anlegen
     *
     * @return
     */
    protected GuiFileChooserTextfield createImportFileChooser() {
        GuiFileChooserTextfield importFileChooser = new GuiFileChooserTextfield();
        importFileChooser.setName("importFileChooser");
        importFileChooser.__internal_setGenerationDpi(96);
        importFileChooser.registerTranslationHandler(null /*translationHandler*/);
        importFileChooser.setScaleForResolution(true);
        importFileChooser.setMinimumWidth(10);
        importFileChooser.setMinimumHeight(10);
        importFileChooser.setServerMode(false);
        importFileChooser.setEditable(false);
        return importFileChooser;
    }

    public void fireMessage(String key, String... placeHolderTexts) {
        fireMessage(TranslationHandler.translate(key, placeHolderTexts), MessageLogType.tmlMessage);
    }

    public void fireWarning(String key, String... placeHolderTexts) {
        fireMessage(TranslationHandler.translate(key, placeHolderTexts), MessageLogType.tmlWarning);
        hasErrorsOrWarnings = true;
    }

    public void fireError(String key, String... placeHolderTexts) {
        fireMessage(TranslationHandler.translate(key, placeHolderTexts), MessageLogType.tmlError);
        hasErrors = true;
    }

    protected void fireMessage(String msg, MessageLogType logType) {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(msg, logType, MessageLogOption.TIME_STAMP);
        }
    }

    public void fireProgress(int pos, int maxPos) {
        if ((messageLogForm != null) && (maxPos > 1)) {
            messageLogForm.getMessageLog().fireProgress(pos, maxPos, "", false, false);
        }
    }

    public void hideProgress() {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().hideProgress();
        }
    }


    public boolean containsDataPart(PartId dataPartId) {
        return newDataParts.containsKey(dataPartId);
    }

    public void addDataPart(EtkDataPart dataPart) {
        newDataParts.put(dataPart.getAsId(), dataPart);
    }

    public Map<PartId, EtkDataPart> getNewDataParts() {
        return newDataParts;
    }
}
