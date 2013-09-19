/**
 * Copyright (C) 2013 Anton Grytsenko (anthony.grytsenko@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grytsenko.contacts.web.rest;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.api.MobileAppHeaders;
import grytsenko.contacts.web.service.SearchContactsService;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Processes requests to REST API.
 */
@Controller
public class RestController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RestController.class);

    @Autowired
    SearchContactsService searchContactsService;

    /**
     * Finds contact of current user.
     */
    @RequestMapping(value = "my", method = RequestMethod.GET)
    @ResponseBody
    public Contact my(
            Principal principal,
            @RequestHeader(value = MobileAppHeaders.Platform.HEADER_NAME, required = false) String mobileAppPlatform) {
        String username = principal.getName();
        LOGGER.debug("Get contact of {}.", username);

        processMobileAppInfo(username, mobileAppPlatform);

        return searchContactsService.findEmployee(username);
    }

    /**
     * Finds contacts of coworkers of current user.
     */
    @RequestMapping(value = "coworkers", method = RequestMethod.GET)
    @ResponseBody
    public List<Contact> coworkers(
            Principal principal,
            @RequestHeader(value = MobileAppHeaders.Platform.HEADER_NAME, required = false) String mobileAppPlatform) {
        String username = principal.getName();
        LOGGER.debug("Get coworkers of {}.", username);

        processMobileAppInfo(username, mobileAppPlatform);

        List<Contact> contacts = searchContactsService.findCoworkers(username);
        LOGGER.debug("Found {} coworkers.", contacts.size());

        return contacts;
    }

    private void processMobileAppInfo(String username, String platform) {
        if (!StringUtils.hasLength(platform)) {
            return;
        }

        LOGGER.debug("{} has used mobile app for {}.", username, platform);
    }

}
