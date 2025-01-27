/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import java.io.IOException;
import java.text.MessageFormat;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Implements {@link EntityResolver} and {@link ErrorHandler} that reports sax
 * errors to log.
 *
 * @author Vilo Repan
 *
 */
public class SchemaHandler implements ErrorHandler, EntityResolver {

    private static final Trace LOGGER = TraceManager.getTrace(SchemaHandler.class);

    private EntityResolver entityResolver;

    public SchemaHandler(EntityResolver entityResolver) {
        super();
        this.entityResolver = entityResolver;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        print("[Warning]", e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        print("[Error]", e);
        throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        print("[Fatal]", e);
        throw e;
    }

    private void print(String header, SAXParseException e) {
        StringBuilder builder = new StringBuilder();
        builder.append("Error occured during schema parsing: ");
        builder.append(header);
        builder.append(" ");
        builder.append(MessageFormat.format("on line {0} at {1}, {2}",
                new Object[] { Integer.toString(e.getLineNumber()), e.getSystemId(), e.getPublicId() }));
        builder.append(" ");
        builder.append(e.getMessage());

        LOGGER.error(builder.toString());
        LOGGER.trace(builder.toString(), e);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     * java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (entityResolver == null) {
            throw new IllegalStateException("Entity resolver is not set");
        }
        try {
            InputSource source = entityResolver.resolveEntity(publicId, systemId);
            LOGGER.trace("Resolved entity '{}', '{}': '{}' (resolver: {})", new Object[] { publicId, systemId, source, entityResolver});
            return source;
        } catch (SAXException e) {
            LOGGER.error("XML error resolving entity '{}', '{}': '{}-{}'",
                    new Object[] { publicId, systemId, e.getMessage(), e });
            throw e;
        } catch (IOException e) {
            LOGGER.error("IO error resolving entity '{}', '{}': '{}-{}'",
                    new Object[] { publicId, systemId, e.getMessage(), e });
            throw e;
        }
    }
}
