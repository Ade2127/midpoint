/**
 * Copyright (c) 2018 Evolveum
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
package com.evolveum.midpoint.model.intest.manual;

import com.evolveum.midpoint.provisioning.ucf.api.ConfigurationProperty;

/**
 * @author semancik
 *
 */
public class DummyItsmIntegrationConnectorConfiguration {
	
	private String uselessString;
	private String[] uselessArray;

	@ConfigurationProperty
	public String getUselessString() {
		return uselessString;
	}

	public void setUselessString(String uselessString) {
		this.uselessString = uselessString;
	}

	@ConfigurationProperty
	public String[] getUselessArray() {
		return uselessArray;
	}

	public void setUselessArray(String[] uselessArray) {
		this.uselessArray = uselessArray;
	}
}
