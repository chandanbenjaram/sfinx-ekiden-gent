/*
 * Copyright 2011 Peter Kuterna
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.peterkuterna.appengine.apps.devoxxsched.shared;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "net.peterkuterna.appengine.apps.devoxxsched.server.Session", locator = "net.peterkuterna.appengine.apps.devoxxsched.server.SessionLocator")
public interface SessionProxy extends ValueProxy {

	Long getId();

	String getSessionId();

	void setSessionId(String sessionId);

	String getEmailAddress();

	void setEmailAddress(String emailAddress);

	String getUserId();

	void setUserId(String userId);

}
