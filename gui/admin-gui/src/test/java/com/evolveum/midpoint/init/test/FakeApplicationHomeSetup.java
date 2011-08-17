/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */

package com.evolveum.midpoint.init.test;

/**
 * @author mamut
 */

import com.evolveum.midpoint.init.ApplicationHomeSetup;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

public class FakeApplicationHomeSetup extends ApplicationHomeSetup{
	
	private static final transient Trace logger = TraceManager.getTrace(FakeApplicationHomeSetup.class);
	
	public void init() {
		System.setProperty("midpoint.home", "target/midHome");
		logger.info("midpoint.home = " + System.getProperty("midpoint.home"));
		
		super.directorySetup("target/midHome");
	}

}
