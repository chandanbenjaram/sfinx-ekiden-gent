/*
 * Copyright 2011 Google Inc.
 * Copyright 2011 Peter Kuterna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.sfinxekidengent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A {@link AbstractActivity} that simply contains a single fragment. The intent
 * used to invoke this activity is forwarded to the fragment as arguments during
 * fragment instantiation.
 */
public abstract class BaseSinglePaneActivity extends AbstractActivity implements
		SinglePaneActivity {

	private Fragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivityHelper.onCreate(savedInstanceState);
	}

	@Override
	public void setFragment(Fragment fragment) {
		this.mFragment = fragment;
	}

}
