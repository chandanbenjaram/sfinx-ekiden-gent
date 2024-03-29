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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import be.sfinxekidengent.R;
import be.sfinxekidengent.provider.CfpContract;
import be.sfinxekidengent.ui.tablet.NowPlayingMultiPaneActivity;
import be.sfinxekidengent.util.AnalyticsUtils;
import be.sfinxekidengent.util.UIUtils;

/**
 * A fragment used in {@link HomeActivity} that shows either a countdown, 'now
 * playing' link to current sessions, or 'thank you' text, at different times
 * (before/during/after the conference). It also shows a 'Realtime Search'
 * button on phones, as a replacement for the {@link TagStreamFragment} that is
 * visible on tablets on the home screen.
 */
public class WhatsOnFragment extends Fragment {

	private Handler mMessageHandler = new Handler();

	private TextView mCountdownTextView;
	private ViewGroup mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_whats_on,
				container);
		refresh();
		return mRootView;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mMessageHandler.removeCallbacks(mCountdownRunnable);
	}

	private void refresh() {
		mMessageHandler.removeCallbacks(mCountdownRunnable);
		mRootView.removeAllViews();

		final long currentTimeMillis = UIUtils.getCurrentTime(getActivity());

		// Show Loading... and load the view corresponding to the current state
		if (currentTimeMillis < UIUtils.CONFERENCE_START_MILLIS) {
			setupBefore();
		} else if (currentTimeMillis > UIUtils.CONFERENCE_END_MILLIS) {
			setupAfter();
		} else {
			setupDuring();
		}
	}

	private void setupBefore() {
		// Before conference, show countdown.
		mCountdownTextView = (TextView) getActivity().getLayoutInflater()
				.inflate(R.layout.whats_on_countdown, mRootView, false);
		mRootView.addView(mCountdownTextView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mMessageHandler.post(mCountdownRunnable);
	}

	private void setupAfter() {
		// After conference, show canned text.
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.whats_on_thank_you, mRootView, false);
		mRootView.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	private void setupDuring() {
		// Conference in progress, show "Now Playing" link.
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.whats_on_now_playing, mRootView, false);
		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AnalyticsUtils.getInstance(getActivity()).trackEvent(
						"Home Screen Dashboard", "Click", "Now Playing", 0);
				if (UIUtils.isHoneycombTablet(getActivity())) {
					startActivity(new Intent(getActivity(),
							NowPlayingMultiPaneActivity.class));
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(CfpContract.Sessions
							.buildSessionsAtDirUri(UIUtils.getCurrentTime(getActivity())));
					intent.putExtra(Intent.EXTRA_TITLE,
							getString(R.string.title_now_playing));
					startActivity(intent);
				}
			}
		});
		mRootView.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/**
	 * Event that updates countdown timer. Posts itself again to
	 * {@link #mMessageHandler} to continue updating time.
	 */
	private Runnable mCountdownRunnable = new Runnable() {
		public void run() {
			int remainingSec = (int) Math.max(0,
					(UIUtils.CONFERENCE_START_MILLIS - System
							.currentTimeMillis()) / 1000);
			final boolean conferenceStarted = remainingSec == 0;

			if (conferenceStarted) {
				// Conference started while in countdown mode, switch modes and
				// bail on future countdown updates.
				mMessageHandler.postDelayed(new Runnable() {
					public void run() {
						refresh();
					}
				}, 100);
				return;
			}

			final int secs = remainingSec % 86400;
			final int days = remainingSec / 86400;
			final String str = getResources().getQuantityString(
					R.plurals.whats_on_countdown_title, days, days,
					DateUtils.formatElapsedTime(secs));
			mCountdownTextView.setText(str);

			// Repost ourselves to keep updating countdown
			mMessageHandler.postDelayed(mCountdownRunnable, 1000);
		}
	};
}
