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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import be.sfinxekidengent.R;
import be.sfinxekidengent.ui.widget.SponsorBanner;
import be.sfinxekidengent.util.Prefs;
import be.sfinxekidengent.util.Prefs.DevoxxPrefs;

public class SponsorBannerFragment extends Fragment {

	private static final int DELAY = 3000;

	private int mCurrentBanner = -1;

	private SponsorBanner mBanner;
	private Handler mHandler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_sponsor_banner, null);
		mBanner = (SponsorBanner) rootView.findViewById(R.id.sponsor_banner);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		final SharedPreferences prefs = Prefs.get(getActivity());
		mCurrentBanner = prefs.getInt(DevoxxPrefs.DISPLAYED_BANNER_INDEX, -1);
		if (mCurrentBanner != -1) {
			mBanner.setDisplayedChild(mCurrentBanner);
		}

		mHandler.postDelayed(mAnimateBannerRunnable, DELAY);
	}

	@Override
	public void onPause() {
		super.onPause();

		final int currentBanner = mBanner.getDisplayedChild();
		if (currentBanner >= 0) {
			SharedPreferences prefs = Prefs.get(getActivity());
			prefs.edit()
					.putInt(DevoxxPrefs.DISPLAYED_BANNER_INDEX, currentBanner)
					.commit();
		}

		mHandler.removeCallbacks(mAnimateBannerRunnable);
	}

	private Runnable mAnimateBannerRunnable = new Runnable() {
		public void run() {
			mBanner.showNext();

			mHandler.postDelayed(mAnimateBannerRunnable, DELAY);
		}
	};

}
