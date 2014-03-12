/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.repo.sql.data.common.any;

import com.evolveum.midpoint.repo.sql.data.common.RAnyContainer;
import com.evolveum.midpoint.repo.sql.data.common.id.RAnyClobId;
import com.evolveum.midpoint.repo.sql.data.common.other.RContainerType;
import com.evolveum.midpoint.repo.sql.data.common.other.RObjectType;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * @author lazyman
 */
@Entity
@IdClass(RAnyClobId.class)
@Table(name = "m_any_clob")
public class RAnyClob implements RAnyValue {

    //owner entity
    private RAnyContainer anyContainer;
    private String ownerOid;
    private RObjectType ownerType;

    private boolean dynamic;
    private String name;
    private String type;
    private RValueType valueType;
    private String value;
    private String checksum;

    public RAnyClob() {
    }

    public RAnyClob(String value) {
        setValue(value);
    }

    @ForeignKey(name = "fk_any_clob")
    @MapsId("owner")
    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "anyContainer_owner_oid", referencedColumnName = "ownerOid"),
            @PrimaryKeyJoinColumn(name = "anyContainer_owner_id", referencedColumnName = "ownerId"),
            @PrimaryKeyJoinColumn(name = "anyContainer_owner_type", referencedColumnName = "owner_type")
    })
    public RAnyContainer getAnyContainer() {
        return anyContainer;
    }

    @Id
    @Column(name = "anyContainer_owner_oid", length = RUtil.COLUMN_LENGTH_OID)
    public String getOwnerOid() {
        if (ownerOid == null && anyContainer != null) {
            ownerOid = anyContainer.getOwnerOid();
        }
        return ownerOid;
    }

    @Id
    @Column(name = "anyContainer_owner_type")
    public RObjectType getOwnerType() {
        if (ownerType == null && anyContainer != null) {
            ownerType = anyContainer.getOwnerType();
        }
        return ownerType;
    }

    /**
     * This method is used for content comparing when querying database (we don't want to compare clob values).
     *
     * @return md5 hash of {@link com.evolveum.midpoint.repo.sql.data.common.any.RAnyClob#getValue()}
     */
    @Id
    @Column(length = 32, name = "checksum")
    public String getChecksum() {
        return checksum;
    }

    @Id
    @Column(name = "eName", length = RUtil.COLUMN_LENGTH_QNAME)
    public String getName() {
        return name;
    }

    @Id
    @Column(name = "eType", length = RUtil.COLUMN_LENGTH_QNAME)
    public String getType() {
        return type;
    }

    @Enumerated(EnumType.ORDINAL)
    public RValueType getValueType() {
        return valueType;
    }

    /**
     * @return true if this property has dynamic definition
     */
    @Column(name = "dynamicDef")
    public boolean isDynamic() {
        return dynamic;
    }

    @Lob
    @Type(type = RUtil.LOB_STRING_TYPE)
    @Column(name = "clobValue")
    public String getValue() {
        return value;
    }

    public void setChecksum(String checksum) {
        //checksum is always computed from value, this setter is only for hibernate satisfaction
    }

    public void setValue(String value) {
        this.value = value;

        checksum = StringUtils.isNotEmpty(this.value) ? DigestUtils.md5Hex(this.value) : "";
    }

    public void setValueType(RValueType valueType) {
        this.valueType = valueType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setAnyContainer(RAnyContainer anyContainer) {
        this.anyContainer = anyContainer;
    }

    public void setOwnerOid(String ownerOid) {
        this.ownerOid = ownerOid;
    }

    public void setOwnerType(RObjectType ownerType) {
        this.ownerType = ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RAnyClob that = (RAnyClob) o;

        if (dynamic != that.dynamic) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (valueType != that.valueType) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (dynamic ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        return result;
    }
}
