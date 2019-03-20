/*
 * Copyright (c) 2010-2019 Evolveum
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
package com.evolveum.midpoint.model.common.expression.script;

import com.evolveum.midpoint.repo.common.expression.ExpressionVariables;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.model.common.expression.script.groovy.GroovyScriptEvaluator;
import com.evolveum.midpoint.model.common.expression.script.groovy.SandboxedGroovyScriptEvaluator;
import com.evolveum.midpoint.model.common.expression.script.jsr223.Jsr223ScriptEvaluator;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.prism.util.PrismTestUtil;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

/**
 * @author Radovan Semancik
 */
public class TestGroovyExpressionsSandbox extends TestGroovyExpressions {

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.common.expression.AbstractExpressionTest#createEvaluator()
	 */
	@Override
	protected ScriptEvaluator createEvaluator(PrismContext prismContext, Protector protector) {
		return new SandboxedGroovyScriptEvaluator(prismContext, protector, localizationService);
	}
	
	/**
	 * This should NOT pass here. There restrictions should not allow to invoke
	 * poison.smell()
	 */
	@Test
	@Override
    public void testSmellPoison() throws Exception {
		Poison poison = new Poison();
		
		// WHEN
		evaluateAndAssertStringScalarExpresssionRestricted(
				"expression-poinson-smell.xml",
				"testSmellPoison",
				createPoisonVariables(poison));
		
		// THEN
		poison.assertNotSmelled();
    }
	
	/**
	 * Drinking poison should be forbidden here.
	 */
	@Test
	@Override
    public void testDrinkPoison() throws Exception {
		Poison poison = new Poison();
		
		// WHEN
		evaluateAndAssertStringScalarExpresssionRestricted(
				"expression-poinson-smell.xml",
				"testDrinkPoison",
				createPoisonVariables(poison));
		
    }

}