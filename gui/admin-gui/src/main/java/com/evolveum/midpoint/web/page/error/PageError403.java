/**
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.web.page.error;

import com.evolveum.midpoint.web.application.PageDescriptor;

/**
 * @author lazyman
 */
@PageDescriptor(url = "/error/403", permitAll = true)
public class PageError403 extends PageError {

    public PageError403() {
        super(403);
    }

    @Override
    protected String getErrorMessageKey(){
        return "PageError403.message";
    }
}
