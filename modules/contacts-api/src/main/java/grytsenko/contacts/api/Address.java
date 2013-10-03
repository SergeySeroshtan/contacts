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
package grytsenko.contacts.api;

/**
 * The postal address for location.
 */
public class Address {

    private String location;

    private String country;
    private String region;
    private String city;
    private String postcode;
    private String street;

    public Address() {
    }

    /**
     * Returns the unique name of location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the unique name of location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the name of country.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the name of country.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the name of region.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the name of region.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Returns the name of city.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the name of city.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the postal code.
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postal code.
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Returns the name of street and number of building.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the name of street and number of building.
     */
    public void setStreet(String street) {
        this.street = street;
    }

}
