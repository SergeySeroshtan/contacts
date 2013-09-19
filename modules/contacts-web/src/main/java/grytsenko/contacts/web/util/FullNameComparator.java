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
package grytsenko.contacts.web.util;

import grytsenko.contacts.api.Contact;

import java.util.Comparator;

import org.springframework.util.StringUtils;

/**
 * Compares contacts by full name of employee.
 */
public final class FullNameComparator implements Comparator<Contact> {

    @Override
    public int compare(Contact first, Contact second) {
        validateName(first);
        validateName(second);

        int lastNames = first.getLastName().compareTo(second.getLastName());
        if (lastNames != 0) {
            return lastNames;
        }

        return first.getFirstName().compareTo(second.getFirstName());
    }

    private void validateName(Contact contact) {
        if (!StringUtils.hasText(contact.getFirstName())) {
            throw new IllegalArgumentException("First name not defined.");
        }
        if (!StringUtils.hasText(contact.getLastName())) {
            throw new IllegalArgumentException("Last name not defined.");
        }
    }

}
