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
package grytsenko.contacts.web.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * The details about office.
 */
@Entity
@Table(name = "offices")
public class Office implements Serializable {

    private static final long serialVersionUID = 8768430882235872847L;

    @Id
    @Column(name = "location")
    private String location;

    @Column(name = "country")
    private String country;

    @Column(name = "region")
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "street")
    private String street;

    @Version
    private Integer version;

    private Office() {
    }

    /**
     * Returns the unique name of location of office that is used in DS.
     * 
     * @see Employee#getLocation()
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the name of country where office is located.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Returns the name of region where office is located.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the name of city where office is located.
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the postal code for office.
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Returns the name of street and number of building where office is
     * located.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Returns the version of data.
     */
    public Integer getVersion() {
        return version;
    }

}
