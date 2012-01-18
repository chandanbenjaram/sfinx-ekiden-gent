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

package be.sfinxekidengent.ui;

import android.app.FragmentBreadCrumbs;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import be.sfinxekidengent.R;
import be.sfinxekidengent.ui.phone.SessionDetailActivity;
import be.sfinxekidengent.ui.phone.SessionsActivity;
import be.sfinxekidengent.util.UIUtils;

import com.google.android.maps.MapView;

/**
 * A multi-pane activity to display a Google ({@link MapView}.
 */
public class MapActivity extends BaseMultiPaneMapActivity implements
		View.OnClickListener, FragmentManager.OnBackStackChangedListener {

	private FragmentManager mFragmentManager;
	private FragmentBreadCrumbs mFragmentBreadCrumbs;

	private MapFragment mMapFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		mFragmentManager = getSupportFragmentManager();
		if (UIUtils.isHoneycombTablet(this)) {
			mFragmentBreadCrumbs = (FragmentBreadCrumbs) findViewById(R.id.breadcrumbs);
			mFragmentBreadCrumbs.setActivity(this);
		}
		mFragmentManager.addOnBackStackChangedListener(this);

		final FragmentManager fm = getSupportFragmentManager();
		mMapFragment = (MapFragment) fm.findFragmentByTag("map");
		if (mMapFragment == null) {
			mMapFragment = new MapFragment();
			mMapFragment.setArguments(intentToFragmentArguments(getIntent()));
			fm.beginTransaction().add(R.id.root_container, mMapFragment, "map")
					.commit();
		}

		final View closeButton = findViewById(R.id.close_button);
		if (closeButton != null) {
			closeButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					clearBackStack(getSupportFragmentManager());
					showHideDetailAndPan(false);
				}
			});
		}

		updateBreadCrumb();
	}

	@Override
	public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(
			String activityClassName) {
		if (findViewById(R.id.map_detail_popup) != null) {
			if (SessionsActivity.class.getName().equals(activityClassName)) {
				mFragmentManager.popBackStack();
				showHideDetailAndPan(true);
				return new FragmentReplaceInfo(SessionsFragment.class,
						"sessions", R.id.fragment_container_map_detail);
			} else if (SessionDetailActivity.class.getName().equals(
					activityClassName)) {
				showHideDetailAndPan(true);
				return new FragmentReplaceInfo(SessionDetailFragment.class,
						"session_detail", R.id.fragment_container_map_detail);
			}
		}
		return null;
	}

	@Override
	public void onBeforeCommitReplaceFragment(FragmentManager fm,
			FragmentTransaction ft, Fragment fragment) {
		super.onBeforeCommitReplaceFragment(fm, ft, fragment);
		if (fragment instanceof SessionsFragment) {
			fm.popBackStack();
		} else if (fragment instanceof SessionDetailFragment) {
			if (fm.getBackStackEntryCount() > 0) {
				fm.popBackStack();
			}
			ft.addToBackStack(null);
		}
		updateBreadCrumb();
	}

	/**
	 * Handler for the breadcrumb parent.
	 */
	public void onClick(View view) {
		mFragmentManager.popBackStack();
	}

	private void clearBackStack(FragmentManager fm) {
		while (fm.getBackStackEntryCount() > 0) {
			fm.popBackStackImmediate();
		}
	}

	public void onBackStackChanged() {
		updateBreadCrumb();
	}

	private void showHideDetailAndPan(boolean show) {
		View detailPopup = findViewById(R.id.map_detail_popup);
		if (show != (detailPopup.getVisibility() == View.VISIBLE)) {
			detailPopup.setVisibility(show ? View.VISIBLE : View.GONE);
			final int orientation = getResources().getConfiguration().orientation;
			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mMapFragment.panLeft(show ? 0.25f : -0.25f);
			} else {
				mMapFragment.panTop(show ? -0.25f : 0.25f);
			}
		}
	}

	public void updateBreadCrumb() {
		if (UIUtils.isHoneycombTablet(this)) {
			final String title = getString(R.string.title_sessions);
			final String detailTitle = getString(R.string.title_session_detail);

			if (mFragmentManager.getBackStackEntryCount() >= 1) {
				mFragmentBreadCrumbs.setParentTitle(title, title, this);
				mFragmentBreadCrumbs.setTitle(detailTitle, detailTitle);
			} else {
				mFragmentBreadCrumbs.setParentTitle(null, null, null);
				mFragmentBreadCrumbs.setTitle(title, title);
			}
		}
	}

}
