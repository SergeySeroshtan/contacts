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
 * Additional information about employee.
 */
@Entity
@Table(name = "employees")
public final class EmployeeDetails implements Serializable {

    private static final long serialVersionUID = 7942426620723987505L;

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "skype")
    private String skype;

    @Column(name = "position")
    private String position;

    @Version
    private Integer version;

    private EmployeeDetails() {
    }

    /**
     * Returns the unique identifier of employee.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Returns the Skype name.
     */
    public String getSkype() {
        return skype;
    }

    /**
     * Returns the position.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Returns the version of data.
     */
    public Integer getVersion() {
        return version;
    }

}
