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

package be.sfinxekidengent.service;

import org.apache.http.client.HttpClient;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;
import be.sfinxekidengent.R;
import be.sfinxekidengent.io.LocalExecutor;
import be.sfinxekidengent.io.ParleysPresentationsHandler;
import be.sfinxekidengent.io.RemoteExecutor;
import be.sfinxekidengent.io.RoomsHandler;
import be.sfinxekidengent.io.ScheduleHandler;
import be.sfinxekidengent.io.SearchSuggestHandler;
import be.sfinxekidengent.io.SessionTypesHandler;
import be.sfinxekidengent.io.SessionsHandler;
import be.sfinxekidengent.io.SpeakersHandler;
import be.sfinxekidengent.io.TracksHandler;
import be.sfinxekidengent.provider.CfpDatabase;
import be.sfinxekidengent.provider.CfpProvider;
import be.sfinxekidengent.provider.CfpContract.Sessions;
import be.sfinxekidengent.util.HttpUtils;
import be.sfinxekidengent.util.NotifierManager;
import be.sfinxekidengent.util.Prefs;
import be.sfinxekidengent.util.Prefs.DevoxxPrefs;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link CfpProvider}. Reads data from both local {@link Resources} and from
 * remote sources (CFP REST interface).
 */
public class CfpSyncService extends AbstractSyncService {

	protected static final String TAG = "CfpSyncService";

	// public static final String BASE_URL =
	// "https://cfp.devoxx.com/rest/v1/events/4/";

	// public static final String BASE_URL = "http://10.0.2.2:8080/";
	// public static final String SCHEDULE_BASE_URL = BASE_URL + "windapp/";

	public static final String BASE_URL = "http://ec2-176-34-201-184.eu-west-1.compute.amazonaws.com/windapp-0.1/";

	public static final String SCHEDULE_URL = BASE_URL + "forecast/";
	// public static final String PRESENTATIONS_URL = BASE_URL +
	// "presentations/";
	public static final String SPEAKERS_URL = BASE_URL + "speakers/";

	public static final int NOTIFICATION_NEW_SESSIONS = 1;

	private static final int VERSION_NONE = 0;
	private static final int VERSION_CURRENT = 2;

	private LocalExecutor mLocalExecutor;
	private RemoteExecutor mRemoteExecutor;
	private HttpClient mHttpClient;
	private ContentResolver mResolver;

	public CfpSyncService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mHttpClient = HttpUtils.getHttpClient(this);
		mResolver = getContentResolver();

		mLocalExecutor = new LocalExecutor(getResources(), mResolver);
		mRemoteExecutor = new RemoteExecutor(mHttpClient, mResolver);
	}

	@Override
	protected void doSync(Intent intent) throws Exception {
		Log.d(TAG, "Start sync using base url: " + BASE_URL);

		Thread.sleep(5000);

		final Context context = this;

		final SharedPreferences settings = Prefs.get(context);
		final int localVersion = settings.getInt(DevoxxPrefs.CFP_LOCAL_VERSION,
				VERSION_NONE);

		final long startLocal = System.currentTimeMillis();
		final boolean localParse = localVersion < VERSION_CURRENT;
		Log.d(TAG, "found localVersion=" + localVersion
				+ " and VERSION_CURRENT=" + VERSION_CURRENT);
		if (localParse) {
			// Parse values from local cache first
			mLocalExecutor.execute(R.xml.search_suggest,
					new SearchSuggestHandler());
			mLocalExecutor.execute(R.xml.rooms, new RoomsHandler());
			mLocalExecutor.execute(R.xml.tracks, new TracksHandler());
			mLocalExecutor.execute(R.xml.presentationtypes,
					new SessionTypesHandler());
			mLocalExecutor.execute(context, "cache-speakers.json",
					new SpeakersHandler());
			mLocalExecutor.execute(context, "cache-presentations.json",
					new SessionsHandler());
			mLocalExecutor.execute(context, "cache-schedule.json",
					new ScheduleHandler());

			mLocalExecutor.execute(context, "cache-parleys-presentations.json",
					new ParleysPresentationsHandler());

			// Save local parsed version
			settings.edit()
					.putInt(DevoxxPrefs.CFP_LOCAL_VERSION, VERSION_CURRENT)
					.commit();

			final ContentValues values = new ContentValues();
			values.put(Sessions.SESSION_NEW, 0);
			getContentResolver().update(Sessions.CONTENT_NEW_URI, values, null,
					null);
		}
		Log.d(TAG, "Local sync took "
				+ (System.currentTimeMillis() - startLocal) + "ms");

		final CfpSyncManager syncManager = new CfpSyncManager(context);
		if (syncManager.shouldPerformRemoteSync(Intent.ACTION_SYNC
				.equals(intent.getAction()))) {
			Log.d(TAG, "Should perform remote sync");
			final long startRemote = System.currentTimeMillis();

			Editor prefsEditor = syncManager
					.hasRemoteContentChanged(mHttpClient);
			//TODO fixme
			if (/*prefsEditor != null*/true) {
				Log.d(TAG, "Remote content was changed");
				Log.d(TAG, "Fetch data from " + SPEAKERS_URL);
				mRemoteExecutor.executeGet(SPEAKERS_URL, new SpeakersHandler());
				/*
				 * mRemoteExecutor.executeGet(PRESENTATIONS_URL, new
				 * SessionsHandler());
				 */
				Log.d(TAG, "Fetch data from " + SCHEDULE_URL);
				mRemoteExecutor.executeGet(SCHEDULE_URL, new ScheduleHandler());
				//TODO fixme, remove null check
				if(prefsEditor!=null){
				prefsEditor.commit();
				}
			} else {
				Log.v(TAG, "Remote data was not changed");
			}
			Log.d(TAG, "Remote sync took "
					+ (System.currentTimeMillis() - startRemote) + "ms");
		} else {
			Log.d(TAG, "Should not perform remote sync");
		}

		final CfpDatabase database = new CfpDatabase(this);
		database.cleanupLinkTables();

		final NotifierManager notifierManager = new NotifierManager(this);
		notifierManager.notifyNewSessions();

		Log.d(TAG, "Sync finished");
	}

}
