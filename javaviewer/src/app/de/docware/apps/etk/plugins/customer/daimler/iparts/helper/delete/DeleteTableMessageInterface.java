/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

public interface DeleteTableMessageInterface {

    void addMessage(String key, String... placeHolderTexts);

    void addWarning(String key, String... placeHolderTexts);

    void addError(String key, String... placeHolderTexts);

    String getLogLanguage();

    void fireProgress(int pos, int maxPos);

    void hideProgress();
}
