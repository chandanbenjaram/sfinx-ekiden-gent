/*
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

package be.sfinxekidengent.ui.phone;

import be.sfinxekidengent.ui.BaseSinglePaneActivity;
import be.sfinxekidengent.ui.TwitterSearchFragment;
import android.support.v4.app.Fragment;

public class TwitterSearchActivity extends BaseSinglePaneActivity {

	@Override
	public Fragment onCreatePane() {
		return new TwitterSearchFragment();
	}

}
