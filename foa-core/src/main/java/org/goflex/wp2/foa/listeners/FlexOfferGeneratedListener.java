
/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/2/18 2:27 PM
 */

package org.goflex.wp2.foa.listeners;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.models.FlexOfferT;
import org.goflex.wp2.core.repository.FmanUserRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.events.FlexOfferGeneratedEvent;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.foa.service.connection.FMANAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Ivan Bizaca on 21/07/2017.
 */

@Component
public class FlexOfferGeneratedListener implements ApplicationListener<FlexOfferGeneratedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(FlexOfferGeneratedListener.class);

    private final Environment env;

    private final FOAProperties foaProperties;

    private final FOAService foaService;

    private final RestTemplate restTemplate;

    private final FmanUserRepository fmanUserRepository;

    private final FMANAccessTokenService fmanAccessTokenService;

    @Autowired
    public FlexOfferGeneratedListener(Environment env,
                                      FOAProperties foaProperties,
                                      FOAService foaService,
                                      RestTemplate restTemplate,
                                      FmanUserRepository fmanUserRepository,
                                      FMANAccessTokenService fmanAccessTokenService) {
        this.env = env;
        this.foaProperties = foaProperties;
        this.foaService = foaService;
        this.restTemplate = restTemplate;
        this.fmanUserRepository = fmanUserRepository;
        this.fmanAccessTokenService = fmanAccessTokenService;
    }

    @Override
    public void onApplicationEvent(FlexOfferGeneratedEvent event) {
        logger.info(event.getEventName() + " complete. FlexOffer id: {}. Sending FlexOffer to FMAN...",
                event.getFlexOffer().getId());

        FlexOfferT fo = foaService.getFlexOffer(event.getFlexOffer().getId());
        HttpHeaders headers = new HttpHeaders();
        String token = "Bearer " + fmanAccessTokenService.getToken("AAU");
        if (!token.equals("Bearer ")) {
            headers.set("Authorization", token);
            HttpEntity<FlexOffer> postEntity = new HttpEntity<>(fo.getFlexoffer(), headers);
            try {

                Object response = restTemplate.postForEntity(foaProperties.getFmanConnectionConfig().getURItoSendFO() +
                        "/" + event.getOrganization().getOrganizationName(), postEntity, String.class);
                foaService.updateSendToFMAN(event.getFlexOffer().getId(), 1);
                logger.debug("Flex-Offer with id: {} was successfully sent to FMAN instance for organization: {}",
                        event.getFlexOffer().getId(), event.getOrganization().getOrganizationName());
                logger.debug(response.toString());

            } catch (HttpClientErrorException ex) {

                logger.warn(ex.toString());

                if (ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                    logger.warn("FMAN rejected FlexOffer with error response: " + ex.getResponseBodyAsString());
                }

                if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    logger.warn("Could not connect to aggregator, please check FMAN instance for" +
                                    " organization: {} is running on the given address",
                            event.getOrganization().getOrganizationName());
                    logger.warn("Flex-Offer with id: {} is stored in vault to be sent later",
                            event.getFlexOffer().getId());
                    fo.setReceivedByFMAN(0);
                }

            } catch (Exception ex) {
                logger.error(ex.getLocalizedMessage());
            }

        } else {
            logger.warn("No FMAN apiKey found, please check broker account is registered on" +
                            " FMAN instance for organization: {} is running on the given address",
                    event.getOrganization().getOrganizationName());
            logger.warn("Flex-Offer with id: {} is stored in vault to be send later", event.getFlexOffer().getId());
            fo.setReceivedByFMAN(0);
        }
    }
}
