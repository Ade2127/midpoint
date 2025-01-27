/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.Test;

import com.evolveum.midpoint.gui.test.TestMidPointSpringApplication;
import com.evolveum.midpoint.model.api.ModelExecuteOptions;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.AbstractInitializedGuiIntegrationTest;
import com.evolveum.midpoint.web.page.admin.configuration.PageSystemConfiguration;
import com.evolveum.midpoint.web.page.admin.home.PageDashboardInfo;
import com.evolveum.midpoint.web.page.admin.resources.content.PageAccount;
import com.evolveum.midpoint.web.page.admin.users.PageOrgTree;
import com.evolveum.midpoint.web.page.admin.users.PageOrgUnit;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author skublik
 */
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(classes = TestMidPointSpringApplication.class)
public class TestPageOrg extends AbstractInitializedGuiIntegrationTest {

    private static final transient Trace LOGGER = TraceManager.getTrace(TestPageOrg.class);

    private static final String MAIN_FORM = "mainPanel:mainForm";
    private static final String FORM_INPUT_DESCRIPTION = "tabPanel:panel:basicSystemConfiguration:values:0:value:propertiesLabel:properties:1:property:values:0:valueContainer:form:input:input";
    private static final String PATH_FORM_NAME = "tabPanel:panel:main:values:0:value:propertiesLabel:properties:0:property:values:0:valueContainer:form:input:originValueContainer:origValueWithButton:origValue:input";
    private static final String FORM_SAVE = "save";

    @Override
    public void initSystem(Task initTask, OperationResult initResult) throws Exception {
        super.initSystem(initTask, initResult);
        PrismObject<SystemConfigurationType> systemConfig = parseObject(SYSTEM_CONFIGURATION_FILE);

        LOGGER.info("adding system config page");
        addObject(systemConfig, ModelExecuteOptions.createOverwrite(), initTask, initResult);
    }

    @Test
    public void test001testPageOrgUnit() {
        renderPage(PageOrgUnit.class);
    }

    @Test
    public void test002testPageOrgTree() {
        renderPage(PageOrgTree.class);
    }

    @Test
    public void test003testAddNewOrg() throws Exception {
        renderPage(PageOrgUnit.class);

        FormTester formTester = tester.newFormTester(MAIN_FORM, false);
        formTester.setValue(PATH_FORM_NAME, "newOrg");
        formTester = formTester.submit(FORM_SAVE);

        Thread.sleep(5000);

        PrismObject<OrgType> newOrg = findObjectByName(OrgType.class, "newOrg");
        assertNotNull(newOrg, "New org not created.");
        LOGGER.info("created org: {}", newOrg.debugDump());
    }

    @Test
    public void test004testCreateChild() throws Exception {
        renderPage(PageOrgTree.class);
        tester.clickLink("orgPanel:tabs:panel:treePanel:treeContainer:tree:subtree:branches:1:node:content:menu:inlineMenuPanel:dropDownMenu:menuItem:6:menuItemBody:menuItemLink");
        tester.assertRenderedPage(PageOrgUnit.class);

        FormTester formTester = tester.newFormTester(MAIN_FORM, false);
        formTester.setValue(PATH_FORM_NAME, "newOrgChild");
        formTester = formTester.submit(FORM_SAVE);

        Thread.sleep(5000);

        PrismObject<OrgType> newOrgChild = findObjectByName(OrgType.class, "newOrgChild");
        PrismObject<OrgType> newOrg = findObjectByName(OrgType.class, "newOrg");
        assertNotNull(newOrgChild, "New org not created.");
        assertAssignedOrg(newOrgChild, newOrg.getOid());
    }

    private Page renderPage(Class<? extends Page> expectedRenderedPageClass) {
        LOGGER.info("render page system configuration");
        PageParameters params = new PageParameters();
        Page pageAccount = tester.startPage(expectedRenderedPageClass, params);

        tester.assertRenderedPage(expectedRenderedPageClass);

        return pageAccount;
    }

}
