/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.framework.utils.VarParam;

import java.util.Map;
import java.util.Set;

public interface WorkbasketSAASearchCallback {

    boolean searchWasCanceled();

    void showProgress(int progress, VarParam<Long> lastUpdateResultsCountTime);

    void addResults(boolean lastResults, Set<String> usedModelNumbers, Map<String, EtkDataObject> attribJoinMap);

    String getVisualValueOfDbValue(String tableName, String fieldName, String dbValue);
}
