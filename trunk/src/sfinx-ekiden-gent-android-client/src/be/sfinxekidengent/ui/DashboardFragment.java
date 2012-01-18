/*
 * Copyright 2011 Google Inc.
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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import be.sfinxekidengent.R;
import be.sfinxekidengent.ui.phone.ScheduleActivity;
import be.sfinxekidengent.ui.tablet.ScheduleMultiPaneActivity;
import be.sfinxekidengent.util.AnalyticsUtils;
import be.sfinxekidengent.util.UIUtils;

public class DashboardFragment extends Fragment {

	public void fireTrackerEvent(String label) {
		AnalyticsUtils.getInstance(getActivity()).trackEvent(
				"Home Screen Dashboard", "Click", label, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_dashboard, container);

		// Attach event handlers
		root.findViewById(R.id.home_btn_schedule).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Schedule");
						if (UIUtils.isHoneycombTablet(getActivity())) {
							startActivity(new Intent(getActivity(),
									ScheduleMultiPaneActivity.class));
						} else {
							startActivity(new Intent(getActivity(),
									ScheduleActivity.class));
						}

					}

				});

		/*
		root.findViewById(R.id.home_btn_sessions).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Sessions");
						if (UIUtils.isHoneycombTablet(getActivity())) {
							startActivity(new Intent(getActivity(),
									SessionsMultiPaneActivity.class));
						} else {
							final Intent intent = new Intent(
									Intent.ACTION_VIEW,
									CfpContract.Tracks.CONTENT_URI);
							intent.putExtra(Intent.EXTRA_TITLE,
									getString(R.string.title_session_tracks));
							startActivity(intent);
						}

					}
				});

		root.findViewById(R.id.home_btn_starred).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Starred");
						startActivity(new Intent(getActivity(),
								StarredActivity.class));
					}
				});
		*/

		root.findViewById(R.id.home_btn_speakers).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Speakers");
						startActivity(new Intent(getActivity(),
								ContactPersonsActivity.class));
					}
				});

		root.findViewById(R.id.home_btn_map).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Map");
						startActivity(new Intent(getActivity(),
								MapActivity.class));
					}
				});

		root.findViewById(R.id.home_btn_announcements).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Bulletin");
						Intent intent = new Intent(getActivity(),
								BulletinActivity.class);
						startActivity(intent);
					}
				});

		return root;
	}
}
