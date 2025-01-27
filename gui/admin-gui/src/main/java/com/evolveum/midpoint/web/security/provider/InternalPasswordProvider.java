/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.web.security.provider;

import com.evolveum.midpoint.model.api.AuthenticationEvaluator;
import com.evolveum.midpoint.model.api.authentication.MidPointUserProfilePrincipal;
import com.evolveum.midpoint.model.api.context.PasswordAuthenticationContext;
import com.evolveum.midpoint.model.api.context.PreAuthenticationContext;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.security.api.ConnectionEnvironment;
import com.evolveum.midpoint.security.api.MidPointPrincipal;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.List;

/**
 * @author skublik
 */

public class InternalPasswordProvider extends PasswordProvider {

    private static final Trace LOGGER = TraceManager.getTrace(InternalPasswordProvider.class);

    @Autowired
    private transient AuthenticationEvaluator<PasswordAuthenticationContext> passwordAuthenticationEvaluator;

    public InternalPasswordProvider() {
        this(null);
    }

    public InternalPasswordProvider(String nameOfCredential) {
        super(nameOfCredential);
    }

    @Override
    protected AuthenticationEvaluator<PasswordAuthenticationContext> getEvaluator() {
        return passwordAuthenticationEvaluator;
    }

    @Override
    protected Authentication internalAuthentication(Authentication authentication, List<ObjectReferenceType> requireAssignment) throws AuthenticationException {
        if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof MidPointUserProfilePrincipal) {
            return authentication;
        }
        String enteredUsername = (String) authentication.getPrincipal();
        LOGGER.trace("Authenticating username '{}'", enteredUsername);

        ConnectionEnvironment connEnv = ConnectionEnvironment.create(SchemaConstants.CHANNEL_GUI_USER_URI);

        try {
            Authentication token;
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                String enteredPassword = (String) authentication.getCredentials();
                token = getEvaluator().authenticate(connEnv, new PasswordAuthenticationContext(enteredUsername, enteredPassword, requireAssignment));
            } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
                token = passwordAuthenticationEvaluator.authenticateUserPreAuthenticated(connEnv, new PreAuthenticationContext(enteredUsername, requireAssignment));
            } else {
                LOGGER.error("Unsupported authentication {}", authentication);
                throw new AuthenticationServiceException("web.security.provider.unavailable");
            }

            MidPointPrincipal principal = (MidPointPrincipal)token.getPrincipal();

            LOGGER.debug("User '{}' authenticated ({}), authorities: {}", authentication.getPrincipal(),
                    authentication.getClass().getSimpleName(), principal.getAuthorities());
            return token;

        } catch (AuthenticationException e) {
            LOGGER.info("Authentication failed for {}: {}", enteredUsername, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        if (UsernamePasswordAuthenticationToken.class.equals(authentication)) {
            return true;
        }
        if (PreAuthenticatedAuthenticationToken.class.equals(authentication)) {
            return true;
        }

        return false;
    }
}
