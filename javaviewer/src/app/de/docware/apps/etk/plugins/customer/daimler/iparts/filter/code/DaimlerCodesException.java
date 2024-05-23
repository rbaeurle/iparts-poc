package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

/**
 *
 */
public class DaimlerCodesException extends Exception {

    String[] messageParams; // Platzhalter-Strings für TranslationHandler

    public DaimlerCodesException(String message, String... messageParams) {
        super(message);
        this.messageParams = messageParams;
    }

    public String[] getMessageParams() {
        return messageParams;
    }

    @Override
    public String getLocalizedMessage() {
        return TranslationHandler.translate(getMessage(), messageParams);
    }

}
