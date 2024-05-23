/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * DTO eines Textes zu einem Arbeitsauftrag aus NutzDok, für KEMs oder SAAs.
 */
public class iPartsWSWorkBasketItemAnnotation extends WSRequestTransferObject {

    private long id;
    private String date;
    private String editor;
    private String text;

    public iPartsWSWorkBasketItemAnnotation() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "id", id);
        checkAttribValid(path, "text", text);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null; // Der NutzDok Webservice hat keinen JSON-Response-Cache
    }
}
