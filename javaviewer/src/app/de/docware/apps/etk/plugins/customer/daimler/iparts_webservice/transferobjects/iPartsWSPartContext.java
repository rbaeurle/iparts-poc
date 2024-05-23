/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * PartContext Data Transfer Object für die iParts Webservices
 * Wird verwendet von {@link iPartsWSPartBase}
 */
public class iPartsWSPartContext extends WSRequestTransferObject {

    private String moduleId;
    private String sequenceId;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartContext() {
    }

    public iPartsWSPartContext(EtkDataPartListEntry partListEntry) {
        this.moduleId = partListEntry.getAsId().getKVari();
        this.sequenceId = partListEntry.getAsId().getKLfdnr();
    }

    iPartsWSPartContext(String kVari, String kLfdNr) {
        this.moduleId = kVari;
        this.sequenceId = kLfdNr;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // moduleId und sequenceId dürfen beide nicht leer sein
        checkAttribValid(path, "moduleId", moduleId);
        checkAttribValid(path, "sequenceId", sequenceId);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ moduleId, sequenceId };
    }

    // Getter and Setter
    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}