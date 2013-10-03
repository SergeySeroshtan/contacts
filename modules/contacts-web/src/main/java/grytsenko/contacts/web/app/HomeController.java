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
package grytsenko.contacts.web.app;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.service.SearchService;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Provides handlers for requests for home page.
 */
@Controller
@RequestMapping(Views.HOME)
public class HomeController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HomeController.class);

    @Autowired
    SearchService searchContactsService;

    /**
     * Finds contact of current user.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String home(Principal principal, Model model) {
        String username = principal.getName();
        LOGGER.debug("Get contact of {}.", username);

        Contact contact = searchContactsService.findEmployee(username);
        model.addAttribute("contact", contact);

        return Views.HOME;
    }
}
