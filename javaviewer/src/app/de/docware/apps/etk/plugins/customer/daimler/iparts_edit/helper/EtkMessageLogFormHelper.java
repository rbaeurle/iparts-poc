package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;

public class EtkMessageLogFormHelper {

    private EtkMessageLogForm messageLog;
    private OnCancelEvent onCancelEvent;
    private int maxProgress;
    private int currentProgress;
    private boolean isCancelled;

    public EtkMessageLogFormHelper(String windowTitle, String title, OnCancelEvent onCancelEvent) {
        this.onCancelEvent = onCancelEvent;
        messageLog = new EtkMessageLogForm(windowTitle, title, null) {
            @Override
            protected void cancel(Event event) {
                cancelCalculation();
                super.cancel(event);
            }
        };
        isCancelled = false;
        messageLog.getGui().setSize(600, 250);
        resetMessageLog(0);
    }

    private void cancelCalculation() {
        isCancelled = true;
        if (onCancelEvent != null) {
            onCancelEvent.cancelCalculation();
        }
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled() {
        isCancelled = true;
    }

    public void resetMessageLog(int maxProgress) {
        this.maxProgress = maxProgress;
        currentProgress = 0;
    }

    public EtkMessageLogForm getMessageLog() {
        return messageLog;
    }

    public void fireMessage(String message) {
        if (messageLog != null) {
            messageLog.getMessageLog().fireMessage(message);
        }
    }

    public void fireProgress() {
        currentProgress++;
        if (messageLog != null) {
            messageLog.getMessageLog().fireProgress(currentProgress, maxProgress, "", false, true);
        }
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int incMaxProgress(int incValue) {
        return maxProgress += incValue;
    }

    public void hideMessageLog() {
        if (messageLog != null) {
            messageLog.getMessageLog().hideProgress();
            messageLog.closeWindow(ModalResult.OK);
        }
    }

}
