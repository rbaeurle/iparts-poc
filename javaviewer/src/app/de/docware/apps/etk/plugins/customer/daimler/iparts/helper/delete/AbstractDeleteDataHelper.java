/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

public abstract class AbstractDeleteDataHelper implements DeleteTableMessageInterface {

    protected static final boolean TEST_MODE = false;
    protected static final boolean TEST_SYNTAX = false;

    protected EtkProject project;
    protected long deleteDuration;
    private int warningCount;
    private int errorCount;
    private StringBuilder warnings;
    private StringBuilder errors;
    protected DeleteTableMessageInterface externMsgInterface;

    public AbstractDeleteDataHelper(EtkProject project) {
        this.project = project;
    }

    protected void clear() {
        deleteDuration = -1;
        warnings = new StringBuilder();
        errors = new StringBuilder();
        warningCount = 0;
        errorCount = 0;
    }

    public long getDeleteDuration() {
        return deleteDuration;
    }

    public StringBuilder getWarnings() {
        return warnings;
    }

    public StringBuilder getErrors() {
        return errors;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public boolean hasErrorsOrWarnings() {
        return (warningCount + errorCount) > 0;
    }

    public void setMsgInterface(DeleteTableMessageInterface msgInterface) {
        this.externMsgInterface = msgInterface;
    }

    protected void showTestModi() {
        if (TEST_MODE) {
            addMessage("Testmodus: keine SQL-Abfragen");
        } else if (TEST_SYNTAX) {
            addMessage("Testmodus: SQL-Abfragen (für falsche SQL-Statements), aber Rollback");
        }
    }

    protected void addException(Throwable e) {
        addError(Logger.getLogger().exceptionToString(e));
    }

    protected void addWarnOrError(StringBuilder str, String key, String... placeHolderTexts) {
        if (str.length() > 0) {
            str.append("\\n");
        }
        str.append(TranslationHandler.translateForLanguage(key, getLogLanguage(), placeHolderTexts));
    }

    @Override
    public void addMessage(String key, String... placeHolderTexts) {
        if (externMsgInterface != null) {
            externMsgInterface.addMessage(key, placeHolderTexts);
        }
    }

    @Override
    public void addWarning(String key, String... placeHolderTexts) {
        warningCount++;
        if (externMsgInterface == null) {
            addWarnOrError(warnings, key, placeHolderTexts);
        } else {
            externMsgInterface.addWarning(key, placeHolderTexts);
        }
    }

    @Override
    public void addError(String key, String... placeHolderTexts) {
        errorCount++;
        if (externMsgInterface == null) {
            addWarnOrError(errors, key, placeHolderTexts);
        } else {
            externMsgInterface.addError(key, placeHolderTexts);
        }
    }

    public String getLogLanguage() {
        if (externMsgInterface == null) {
            return Language.DE.getCode();
        } else {
            return externMsgInterface.getLogLanguage();
        }
    }

    @Override
    public void fireProgress(int pos, int maxPos) {
        if (externMsgInterface != null) {
            externMsgInterface.fireProgress(pos, maxPos);
        }
    }

    @Override
    public void hideProgress() {
        if (externMsgInterface != null) {
            externMsgInterface.hideProgress();
        }
    }
}
