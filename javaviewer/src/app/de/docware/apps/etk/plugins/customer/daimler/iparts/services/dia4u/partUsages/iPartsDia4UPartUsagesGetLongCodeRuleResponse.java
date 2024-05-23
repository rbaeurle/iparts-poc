/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.partUsages;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Objekt des Dia4U PartUsage Services bei der Abfrage von Lange Code Regeln
 */
public class iPartsDia4UPartUsagesGetLongCodeRuleResponse implements RESTfulTransferObjectInterface {

    // Für Abgleich
    private String brteBr;
    private String brteRas;
    private String brtePose;
    private String brtePosp;
    private String brtePv;
    private String brteAa;
    private String brteEtz;
    private String brteWw;
    private String brteSesi;

    // Für Ausgabe
    private String brteKema;
    private String brteKemb;
    private String brteL;
    private String brteSdata;
    private String brteSdatb;
    private String brteLcr;

    public iPartsDia4UPartUsagesGetLongCodeRuleResponse() {
    }

    public String getBrteBr() {
        return brteBr;
    }

    public void setBrteBr(String brteBr) {
        this.brteBr = brteBr;
    }

    public String getBrteRas() {
        return brteRas;
    }

    public void setBrteRas(String brteRas) {
        this.brteRas = brteRas;
    }

    public String getBrtePose() {
        return brtePose;
    }

    public void setBrtePose(String brtePose) {
        this.brtePose = brtePose;
    }

    public String getBrtePosp() {
        return brtePosp;
    }

    public void setBrtePosp(String brtePosp) {
        this.brtePosp = brtePosp;
    }

    public String getBrtePv() {
        return brtePv;
    }

    public void setBrtePv(String brtePv) {
        this.brtePv = brtePv;
    }

    public String getBrteAa() {
        return brteAa;
    }

    public void setBrteAa(String brteAa) {
        this.brteAa = brteAa;
    }

    public String getBrteEtz() {
        return brteEtz;
    }

    public void setBrteEtz(String brteEtz) {
        this.brteEtz = brteEtz;
    }

    public String getBrteWw() {
        return brteWw;
    }

    public void setBrteWw(String brteWw) {
        this.brteWw = brteWw;
    }

    public String getBrteSesi() {
        return brteSesi;
    }

    public void setBrteSesi(String brteSesi) {
        this.brteSesi = brteSesi;
    }

    public String getBrteKema() {
        return brteKema;
    }

    public void setBrteKema(String brteKema) {
        this.brteKema = brteKema;
    }

    public String getBrteKemb() {
        return brteKemb;
    }

    public void setBrteKemb(String brteKemb) {
        this.brteKemb = brteKemb;
    }

    public String getBrteL() {
        return brteL;
    }

    public void setBrteL(String brteL) {
        this.brteL = brteL;
    }

    public String getBrteSdata() {
        return brteSdata;
    }

    public void setBrteSdata(String brteSdata) {
        this.brteSdata = brteSdata;
    }

    public String getBrteSdatb() {
        return brteSdatb;
    }

    public void setBrteSdatb(String brteSdatb) {
        this.brteSdatb = brteSdatb;
    }

    public String getBrteLcr() {
        return brteLcr;
    }

    public void setBrteLcr(String brteLcr) {
        this.brteLcr = brteLcr;
    }
}
