package org.goflex.wp2.core.wrappers;

/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@XmlAccessorType(XmlAccessType.FIELD)
public class IneaFlexOfferRequestWrapper {

    private IneaFlexOfferWrapper[] flexOffer;

    public IneaFlexOfferWrapper[] getFlexOffer() {
        return flexOffer;
    }

    public void setFlexOffer(IneaFlexOfferWrapper[] flexOffer) {
        this.flexOffer = flexOffer;
    }


}
