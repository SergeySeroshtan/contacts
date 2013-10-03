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
package grytsenko.contacts.web.data.mapper;

import grytsenko.contacts.api.Address;
import grytsenko.contacts.web.data.Office;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps to get information about addresses.
 */
public class AddressMapper {

    /**
     * Gets addresses of offices.
     * 
     * @param offices
     *            the list of offices.
     * 
     * @return the list of addresses.
     */
    public static List<Address> map(List<Office> offices) {
        List<Address> addresses = new ArrayList<Address>(offices.size());
        for (Office office : offices) {
            addresses.add(map(office));
        }
        return addresses;
    }

    /**
     * Gets address of office.
     * 
     * @param office
     *            the office.
     * 
     * @return the address of office
     */
    public static Address map(Office office) {
        Address address = new Address();

        address.setCountry(office.getCountry());
        address.setRegion(office.getRegion());
        address.setCity(office.getCity());
        address.setPostcode(office.getPostcode());
        address.setStreet(office.getStreet());
        address.setLocation(office.getLocation());

        return address;
    }

}
