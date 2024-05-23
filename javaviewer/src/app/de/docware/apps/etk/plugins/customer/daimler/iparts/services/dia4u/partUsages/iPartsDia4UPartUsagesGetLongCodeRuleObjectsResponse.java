/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.partUsages;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Set;

/**
 * Äußere Klammer in der Response für den DIA4U PartUsage Service
 */
public class iPartsDia4UPartUsagesGetLongCodeRuleObjectsResponse implements RESTfulTransferObjectInterface {

    private Set<iPartsDia4UPartUsagesGetLongCodeRuleResponse> content;

    public iPartsDia4UPartUsagesGetLongCodeRuleObjectsResponse() {
    }

    public Set<iPartsDia4UPartUsagesGetLongCodeRuleResponse> getContent() {
        return content;
    }

    public void setContent(Set<iPartsDia4UPartUsagesGetLongCodeRuleResponse> content) {
        this.content = content;
    }
}
