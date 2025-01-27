/**
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.schrodinger.page.configuration;

import com.codeborne.selenide.SelenideElement;
import com.evolveum.midpoint.schrodinger.component.configuration.*;
import com.evolveum.midpoint.schrodinger.page.BasicPage;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InternalsConfigurationPage extends BasicPage {

    public ClockTab clockTab() {
        //todo implement
        SelenideElement element = null;
        return new ClockTab(this, element);
    }

    public DebugUtilTab debugUtilTab() {
        //todo implement
        SelenideElement element = null;
        return new DebugUtilTab(this, element);
    }

    public InternalConfigurationTab internalConfigurationTab() {
        //todo implement
        SelenideElement element = null;
        return new InternalConfigurationTab(this, element);
    }

    public TracesTab tracesTab() {
        //todo implement
        SelenideElement element = null;
        return new TracesTab(this, element);
    }

    public CountersTab countersTab() {
        //todo implement
        SelenideElement element = null;
        return new CountersTab(this, element);
    }
}
