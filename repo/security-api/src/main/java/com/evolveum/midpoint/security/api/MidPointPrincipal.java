/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.security.api;

import java.util.ArrayList;
import java.util.Collection;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SecurityPolicyType;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.ShortDumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AdminGuiConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * Simple midPoint principal. This principal should contain only the concepts that are
 * essential for midPoint core to work. It should not contain user interface concepts
 * (e.g. adminGuiConfig). For that see MidPointUserProfilePrincipal.
 *
 * @author Radovan Semancik
 */
public class MidPointPrincipal implements UserDetails,  DebugDumpable, ShortDumpable {
    private static final long serialVersionUID = 8299738301872077768L;

    // TODO: user may be switched to FocusType later (MID-4205)
    @NotNull private final UserType user;
    private Collection<Authorization> authorizations = new ArrayList<>();
    private ActivationStatusType effectiveActivationStatus;
    private SecurityPolicyType applicableSecurityPolicy;
    // TODO: or a set?
    @NotNull private final Collection<DelegatorWithOtherPrivilegesLimitations> delegatorWithOtherPrivilegesLimitationsCollection = new ArrayList<>();
    private UserType attorney;
    private MidPointPrincipal previousPrincipal;

    public MidPointPrincipal(@NotNull UserType user) {
        Validate.notNull(user, "User must not be null.");
        this.user = user;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<Authorization> getAuthorities() {
        return authorizations;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
     */
    @Override
    public String getPassword() {
        // We won't return password
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
     */
    @Override
    public String getUsername() {
        return getUser().getName().getOrig();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
     */
    @Override
    public boolean isAccountNonExpired() {
        // TODO
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
     */
    @Override
    public boolean isAccountNonLocked() {
        // TODO
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // TODO
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        if (effectiveActivationStatus == null) {
            ActivationType activation = user.getActivation();
            if (activation == null) {
                effectiveActivationStatus = ActivationStatusType.ENABLED;
            } else {
                effectiveActivationStatus = activation.getEffectiveStatus();
                if (effectiveActivationStatus == null) {
                    throw new IllegalArgumentException("Null effective activation status in "+user);
                }
            }
        }
        return effectiveActivationStatus == ActivationStatusType.ENABLED;
    }

    /**
     * Effective identity that is used to execute all actions.
     * Authorizations of this identity will be applied.
     * This is usually the logged-in user. However, this may be the
     * user on behalf who are the actions executed (donor of power)
     * and the real logged-in user may be the attorney.
     */
    @NotNull
    public UserType getUser() {
        return user;
    }

    public PolyStringType getName() {
        return getUser().getName();
    }

    public String getFamilyName() {
        PolyStringType string = getUser().getFamilyName();
        return string != null ? string.getOrig() : null;
    }

    public String getFullName() {
        PolyStringType string = getUser().getFullName();
        return string != null ? string.getOrig() : null;
    }

    public String getGivenName() {
        PolyStringType string = getUser().getGivenName();
        return string != null ? string.getOrig() : null;
    }

    public String getOid() {
        return getUser().getOid();
    }

    /**
     * Real identity of the logged-in user. Used in cases when there is a
     * difference between logged-in user and the identity that is used to
     * execute actions and evaluate authorizations.
     * This may happen when one user (attorney) has switched identity
     * to another user (donor of power). In that case the identity of the
     * attorney is in this property. The user that was the target of the
     * switch is stored in the "user" property.
     */
    public UserType getAttorney() {
        return attorney;
    }

    public void setAttorney(UserType attorney) {
        this.attorney = attorney;
    }

    /**
     * Principal that was used before this principal was active.
     * This is used when principals are chained (e.g. attorney)
     */
    public MidPointPrincipal getPreviousPrincipal() {
        return previousPrincipal;
    }

    public void setPreviousPrincipal(MidPointPrincipal previousPrincipal) {
        this.previousPrincipal = previousPrincipal;
    }

    public SecurityPolicyType getApplicableSecurityPolicy() {
        return applicableSecurityPolicy;
    }

    public void setApplicableSecurityPolicy(SecurityPolicyType applicableSecurityPolicy) {
        this.applicableSecurityPolicy = applicableSecurityPolicy;
    }

    @NotNull
    public Collection<DelegatorWithOtherPrivilegesLimitations> getDelegatorWithOtherPrivilegesLimitationsCollection() {
        return delegatorWithOtherPrivilegesLimitationsCollection;
    }

    public void addDelegatorWithOtherPrivilegesLimitations(DelegatorWithOtherPrivilegesLimitations value) {
        delegatorWithOtherPrivilegesLimitationsCollection.add(value);
    }

    /**
     * Semi-shallow clone.
     */
    public MidPointPrincipal clone() {
        MidPointPrincipal clone = new MidPointPrincipal(this.user);
        copyValues(clone);
        return clone;
    }

    protected void copyValues(MidPointPrincipal clone) {
        clone.applicableSecurityPolicy = this.applicableSecurityPolicy;
        clone.authorizations = cloneAuthorities();
        clone.effectiveActivationStatus = this.effectiveActivationStatus;
        clone.delegatorWithOtherPrivilegesLimitationsCollection.addAll(this.delegatorWithOtherPrivilegesLimitationsCollection);
    }

    private Collection<Authorization> cloneAuthorities() {
        Collection<Authorization> clone = new ArrayList<>(authorizations.size());
        clone.addAll(authorizations);
        return clone;
    }

    /* (non-Javadoc)
     * @see com.evolveum.midpoint.util.DebugDumpable#debugDump(int)
     */
    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.debugDumpLabelLn(sb, this.getClass().getSimpleName(), indent);
        debugDumpInternal(sb, indent);
        return sb.toString();
    }

    protected void debugDumpInternal(StringBuilder sb, int indent) {
        DebugUtil.debugDumpWithLabelLn(sb, "User", user.asPrismObject(), indent + 1);
        DebugUtil.debugDumpWithLabelLn(sb, "Authorizations", authorizations, indent + 1);
        DebugUtil.debugDumpWithLabelLn(sb, "Delegators with other privilege limitations", delegatorWithOtherPrivilegesLimitationsCollection, indent + 1);
        DebugUtil.debugDumpWithLabel(sb, "Attorney", attorney==null?null:attorney.asPrismObject(), indent + 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("(");
        sb.append(user);
        if (attorney != null) {
            sb.append(" [").append(attorney).append("]");
        }
        sb.append(", autz=").append(authorizations);
        sb.append(")");
        return sb.toString();
    }

    public ObjectReferenceType toObjectReference() {
        if (user.getOid() == null) {
            return null;
        }
        ObjectReferenceType rv = new ObjectReferenceType();
        rv.setType(UserType.COMPLEX_TYPE);
        rv.setOid(user.getOid());
        return rv;
    }

    @Override
    public void shortDump(StringBuilder sb) {
        sb.append(user);
        if (attorney != null) {
            sb.append("[").append(attorney).append("]");
        }
    }
}
