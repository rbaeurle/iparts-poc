package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events;

import de.docware.apps.etk.base.project.events.AbstractEtkProjectEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;

/**
 * Event, das gefeuert wird wenn ein Autorenauftrag aktiviert oder deaktiviert wird.
 */
public class AuthorOrderChangedEvent extends AbstractEtkProjectEvent {

    private iPartsAuthorOrderId authorOrderId;

    /**
     * @param authorOrderId der aktivierte Autorenauftrag oder {@code null}, falls der Autorenauftrag deaktiviert wurde.
     */
    public AuthorOrderChangedEvent(iPartsAuthorOrderId authorOrderId) {
        this.authorOrderId = authorOrderId;
    }

    public iPartsAuthorOrderId getAuthorOrderId() {
        return authorOrderId;
    }

}
