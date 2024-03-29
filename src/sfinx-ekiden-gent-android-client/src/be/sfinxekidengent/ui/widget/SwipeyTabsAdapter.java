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

package be.sfinxekidengent.ui.widget;

import android.support.v4.view.PagerAdapter;
import android.widget.TextView;

public interface SwipeyTabsAdapter {

	/**
	 * Return the number swipey tabs. Needs to be aligned with the number of
	 * items in your {@link PagerAdapter}.
	 * 
	 * @return
	 */
	int getCount();

	/**
	 * Build {@link TextView} to diplay as a swipey tab.
	 * 
	 * @param position
	 *            the position of the tab
	 * @param root
	 *            the root view
	 * @return
	 */
	TextView getTab(int position, SwipeyTabs root);

}
