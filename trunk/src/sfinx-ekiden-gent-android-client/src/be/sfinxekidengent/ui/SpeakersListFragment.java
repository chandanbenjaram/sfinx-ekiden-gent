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

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import be.sfinxekidengent.R;
import be.sfinxekidengent.provider.CfpContract;
import be.sfinxekidengent.provider.CfpContract.Speakers;
import be.sfinxekidengent.util.ActivityHelper;
import be.sfinxekidengent.util.AnalyticsUtils;
import be.sfinxekidengent.util.ImageDownloader;
import be.sfinxekidengent.util.UIUtils;

public class SpeakersListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "SpeakersListFragment";

	private static final String STATE_CHECKED_POSITION = "checkedPosition";

	private CursorAdapter mAdapter;
	private int mCheckedPosition = -1;
	private Uri mSpeakersUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Speakers");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText(getString(R.string.empty_speakers));

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		Log.i(TAG, "reload from arguments: " + getArguments());
		// reloadFromArguments(getArguments(), false);
		// TEST
		mAdapter = new SpeakersAdapter(getActivity(), null,
				R.layout.list_item_speaker);
		setListAdapter(mAdapter);
		// TEST END

		Log.i(TAG, "Set list shown false");
		setListShown(false);

		getLoaderManager().initLoader(SpeakersQuery._TOKEN, null, this);
		/*
		 * if (savedInstanceState != null) { mCheckedPosition =
		 * savedInstanceState.getInt( STATE_CHECKED_POSITION, -1); }
		 */
	}

	public void reloadFromArguments(Bundle arguments, boolean reset) {
		mCheckedPosition = -1;

		// Load new arguments
		final Intent intent = ActivityHelper
				.fragmentArgumentsToIntent(arguments);
		mSpeakersUri = intent.getData();
		if (mSpeakersUri == null) {
			return;
		}

		final int speakersQueryId;
		if (!CfpContract.Speakers.isSearchUri(mSpeakersUri)) {
			mAdapter = new SpeakersAdapter(getActivity(), null,
					R.layout.list_item_speaker);
			speakersQueryId = SpeakersAdapter.SpeakersQuery._TOKEN;
		} else {
			mAdapter = new SearchAdapter(getActivity(), null);
			speakersQueryId = SearchQuery._TOKEN;
		}

		setListAdapter(mAdapter);

		//TODO fixme
		/*
		if (!reset) {
			getLoaderManager().initLoader(speakersQueryId, null,
					mSpeakersLoaderCallback);
		} else {
			getLoaderManager().restartLoader(speakersQueryId, null,
					mSpeakersLoaderCallback);
		}
		*/
		if (!reset) {
			getLoaderManager().initLoader(speakersQueryId, null,
					this);
		} else {
			getLoaderManager().restartLoader(speakersQueryId, null,
					this);
		}
		
		
	}

	@Override
	public void onResume() {
		super.onResume();

		getLoaderManager().restartLoader(SpeakersQuery._TOKEN, null, this);

		getActivity().getContentResolver().registerContentObserver(
				Speakers.CONTENT_URI, true, mSpeakerChangesObserver);

		/*
		 * getActivity().getContentResolver()
		 * .registerContentObserver(CfpContract.Speakers.CONTENT_URI, true,
		 * mSpeakerChangesObserver);
		 * 
		 * if (mSpeakersUri != null) { if
		 * (!CfpContract.Speakers.isSearchUri(mSpeakersUri)) {
		 * getLoaderManager().restartLoader(SpeakersQuery._TOKEN, null,
		 * mSpeakersLoaderCallback); } else {
		 * getLoaderManager().restartLoader(SearchQuery._TOKEN, null,
		 * mSpeakersLoaderCallback); } }
		 */
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().getContentResolver().unregisterContentObserver(
				mSpeakerChangesObserver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String speakerId = cursor
				.getString(SpeakersAdapter.SpeakersQuery.SPEAKER_ID);
		final String firstName = cursor
				.getString(SpeakersAdapter.SpeakersQuery.SPEAKER_FIRSTNAME);
		final String lastName = cursor
				.getString(SpeakersAdapter.SpeakersQuery.SPEAKER_LASTNAME);
		final String speakerName = UIUtils.formatSpeakerName(firstName,
				lastName);
		final String title = getResources().getString(
				R.string.title_sessions_of, speakerName);
		AnalyticsUtils.getInstance(getActivity()).trackEvent("Speakers List",
				"Click", speakerName, 0);
		final Uri sessionsUri = CfpContract.Speakers
				.buildSessionsDirUri(speakerId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, sessionsUri);
		intent.putExtra(Intent.EXTRA_TITLE, title);
		((AbstractActivity) getActivity()).openActivityOrFragment(intent);
		getListView().setItemChecked(position, true);
		mCheckedPosition = position;
	}

	public void clearCheckedPosition() {
		if (mCheckedPosition >= 0) {
			getListView().setItemChecked(mCheckedPosition, false);
			mCheckedPosition = -1;
		}
	}

	/*
	private LoaderManager.LoaderCallbacks<Cursor> mSpeakersLoaderCallback = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Loader<Cursor> result = null;
			if (id == SpeakersAdapter.SpeakersQuery._TOKEN) {
				result = new CursorLoader(getActivity(), mSpeakersUri,
						SpeakersAdapter.SpeakersQuery.PROJECTION, null, null,
						CfpContract.Speakers.DEFAULT_SORT);
				Log.v(TAG,
						"Returning "
								+ result.dataToString(((CursorLoader) result)
										.getCursor()));
				Log.v(TAG, "result = " + result);
				return result;
			} else if (id == SearchQuery._TOKEN) {
				result = new CursorLoader(getActivity(), mSpeakersUri,
						SearchQuery.PROJECTION, null, null,
						CfpContract.Speakers.DEFAULT_SORT);
				Log.v(TAG,
						"Returning "
								+ result.dataToString(((CursorLoader) result)
										.getCursor()));
				Log.v(TAG, "result = " + result);
				return result;
			}
			Log.v(TAG, "Returning NULL");
			return result;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			Log.v("SpeakersListFragment", "data size: " + data.getCount()
					+ "; string: " + data.toString());
			Cursor oldCursor = mAdapter.swapCursor(data);
			Log.v("SpeakersListFragment", "old cursor data size: "
					+ (oldCursor == null ? "" : oldCursor.getCount()
							+ "; string: " + oldCursor.toString()));

			if (getView() != null) {
				if (isResumed()) {
					Log.i("SpeakersListFragment", "set list shown true");
					setListShown(true);
				} else {
					setListShownNoAnimation(true);
				}
			} else {
				Log.i("SpeakersListFragment", "view is null");
			}

			if (mCheckedPosition >= 0 && getView() != null) {
				getListView().setItemChecked(mCheckedPosition, true);
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			Log.i("SpeakersListFragment", "loader reset");
			mAdapter.swapCursor(null);
		}

	};
	*/

	private ContentObserver mSpeakerChangesObserver = new ContentObserver(
			new Handler()) {
		@Override
		/*
		public void onChange(boolean selfChange) {
			if (getActivity() != null) {
				if (mSpeakersUri != null) {
					if (!CfpContract.Speakers.isSearchUri(mSpeakersUri)) {
						getLoaderManager().restartLoader(
								SpeakersAdapter.SpeakersQuery._TOKEN, null,
								mSpeakersLoaderCallback);
					} else {
						getLoaderManager().restartLoader(SearchQuery._TOKEN,
								null, mSpeakersLoaderCallback);
					}
				}
			}
		}
		*/
		public void onChange(boolean selfChange) {
			if (getActivity() != null) {
				if (mSpeakersUri != null) {
					if (!CfpContract.Speakers.isSearchUri(mSpeakersUri)) {
						getLoaderManager().restartLoader(
								SpeakersAdapter.SpeakersQuery._TOKEN, null,
								SpeakersListFragment.this);
					} else {
						getLoaderManager().restartLoader(SearchQuery._TOKEN,
								null, SpeakersListFragment.this);
					}
				}
			}
		}
	};

	/**
	 * {@link CursorAdapter} that renders a {@link SearchQuery}.
	 */
	private class SearchAdapter extends CursorAdapter {

		private final ImageDownloader mImageDownloader;

		public SearchAdapter(Context context, Cursor cursor) {
			super(context, cursor, 0);

			this.mImageDownloader = new ImageDownloader(context);
		}

		/** {@inheritDoc} */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return LayoutInflater.from(context).inflate(
					R.layout.list_item_speaker, parent, false);
		}

		/** {@inheritDoc} */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final String firstName = cursor.getString(SearchQuery.FIRSTNAME);
			final String lastName = cursor.getString(SearchQuery.LASTNAME);
			final String speakerName = UIUtils.formatSpeakerName(firstName,
					lastName);
			final String imageUrl = cursor.getString(SearchQuery.IMAGE_URL);

			((TextView) view.findViewById(R.id.speaker)).setText(speakerName);

			final String snippet = cursor.getString(SearchQuery.SEARCH_SNIPPET);

			final Spannable styledSnippet = UIUtils.buildStyledSnippet(snippet);
			((TextView) view.findViewById(R.id.company)).setText(styledSnippet);

			mImageDownloader.download(imageUrl,
					(ImageView) view.findViewById(R.id.image),
					R.drawable.speaker_image_empty);
		}
	}

	/**
	 * {@link Speakers} query parameters.
	 */
	private interface SpeakersQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID,
				CfpContract.Speakers.SPEAKER_ID,
				CfpContract.Speakers.SPEAKER_FIRSTNAME,
				CfpContract.Speakers.SPEAKER_LASTNAME,
				CfpContract.Speakers.SPEAKER_COMPANY,
				CfpContract.Speakers.SPEAKER_BIO,
				CfpContract.Speakers.SPEAKER_IMAGE_URL, };

		int _ID = 0;
		int SPEAKER_ID = 1;
		int SPEAKER_FIRSTNAME = 2;
		int SPEAKER_LASTNAME = 3;
		int SPEAKER_COMPANY = 4;
		int SPEAKER_BIO = 5;
		int SPEAKER_IMAGE_URL = 6;
	}

	/**
	 * {@link Speakers} search query parameters.
	 */
	private interface SearchQuery {
		int _TOKEN = 0x3;

		String[] PROJECTION = { BaseColumns._ID,
				CfpContract.Speakers.SPEAKER_ID,
				CfpContract.Speakers.SPEAKER_FIRSTNAME,
				CfpContract.Speakers.SPEAKER_LASTNAME,
				CfpContract.Speakers.SPEAKER_IMAGE_URL,
				CfpContract.Speakers.SEARCH_SNIPPET, };

		int _ID = 0;
		int SPEAKER_ID = 1;
		int FIRSTNAME = 2;
		int LASTNAME = 3;
		int IMAGE_URL = 4;
		int SEARCH_SNIPPET = 5;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Speakers.CONTENT_URI,
				SpeakersQuery.PROJECTION, null, null, Speakers.DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		Log.v(TAG, "data size: " + data.getCount());
		//TODO removeme!
		Cursor cursor = null;
		try {
			Log.v(TAG, "Query " + Speakers.CONTENT_URI);
			String[] projection = { BaseColumns._ID,
					CfpContract.Speakers.SPEAKER_ID,
					CfpContract.Speakers.SPEAKER_FIRSTNAME,
					CfpContract.Speakers.SPEAKER_LASTNAME,
					CfpContract.Speakers.SPEAKER_COMPANY,
					CfpContract.Speakers.SPEAKER_BIO,
					CfpContract.Speakers.SPEAKER_IMAGE_URL, };
			cursor = getActivity().getContentResolver().query(Speakers.CONTENT_URI, projection,
					null, null, Speakers.DEFAULT_SORT);
			if (cursor != null) {
				// Ensure the cursor window is filled
				int count = cursor.getCount();
				Log.v(TAG, "Cursor count: " + count);
			} else {
				Log.v(TAG, "Cursor is null");
			}
		} finally {
			cursor.close();
		}
		//
		if (getView() != null) {
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

}
