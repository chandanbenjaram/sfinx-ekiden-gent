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

import java.util.ArrayList;
import java.util.TimeZone;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import be.sfinxekidengent.R;
import be.sfinxekidengent.provider.CfpContract;
import be.sfinxekidengent.provider.CfpContract.Blocks;
import be.sfinxekidengent.provider.CfpContract.Sessions;
import be.sfinxekidengent.ui.widget.BlockScheduleItem;
import be.sfinxekidengent.ui.widget.ObservableScrollView;
import be.sfinxekidengent.ui.widget.ScheduleItem;
import be.sfinxekidengent.ui.widget.ScheduleItemView;
import be.sfinxekidengent.ui.widget.ScheduleItemsLayout;
import be.sfinxekidengent.util.AnalyticsUtils;
import be.sfinxekidengent.util.Lists;
import be.sfinxekidengent.util.NotifyingAsyncQueryHandler;
import be.sfinxekidengent.util.UIUtils;

public class BlockScheduleItemsFragment extends ScheduleItemsFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener,
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	private static final String TAG = "BlockScheduleItemsFragment";
	
	private static final boolean DEBUG = false;

	private NotifyingAsyncQueryHandler mHandler;

	public static BlockScheduleItemsFragment newInstance(int id,
			long startMillis, int scrollY) {
		if(DEBUG){Log.v(TAG,"BlockScheduleItemsFragment.newInstance(id="+id+";startMillis="+startMillis+";scrollY="+scrollY+")");}
		BlockScheduleItemsFragment f = new BlockScheduleItemsFragment();

		Bundle args = new Bundle();
		args.putInt("id", id);
		args.putLong("startMillis", startMillis);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if(DEBUG){Log.v(TAG,"onActivityCreated("+(savedInstanceState==null?"null":savedInstanceState.isEmpty())+")");}
		super.onActivityCreated(savedInstanceState);

		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);

		setContentShown(false);

		getLoaderManager().initLoader(getArguments().getInt("id"), null, this);
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().getContentResolver().unregisterContentObserver(
				mSessionChangesObserver);
	}

	@Override
	public void onResume() {
		super.onResume();

		getLoaderManager().restartLoader(mDay.index, null, this);

		getActivity().getContentResolver()
				.registerContentObserver(CfpContract.Sessions.CONTENT_URI,
						true, mSessionChangesObserver);
	}

	protected void setupDay(ViewGroup rootView) {
		if(DEBUG){Log.v(TAG,"setUpDay(rootView="+(rootView==null?"null":rootView.getId())+")");}
		final int position = getArguments().getInt("id");
		final long startMillis = getArguments().getLong("startMillis");

		mDay = new Day();

		// Setup data
		mDay.index = position;
		mDay.timeStart = startMillis;
		mDay.timeEnd = startMillis + DateUtils.DAY_IN_MILLIS;
		mDay.loaderUri = CfpContract.Blocks.buildBlocksBetweenDirUri(
				mDay.timeStart, mDay.timeEnd);

		// Setup views
		mDay.rootView = rootView;

		mDay.scrollView = (ObservableScrollView) mDay.rootView
				.findViewById(R.id.schedule_items_scroll);

		mDay.scheduleItemsView = (ScheduleItemsLayout) mDay.rootView
				.findViewById(R.id.schedule_items);
		mDay.nowView = mDay.rootView.findViewById(R.id.schedule_items_now);

		// mDay.blocksView.setDrawingCacheEnabled(true);
		// mDay.blocksView.setAlwaysDrawnWithCacheEnabled(true);

		TimeZone.setDefault(UIUtils.CONFERENCE_TIME_ZONE);
		String dtTimeFmt = DateUtils.formatDateTime(getActivity(), startMillis,
				TIME_FLAGS);
		mDay.label = dtTimeFmt;
	}

	/** {@inheritDoc} */
	public void onClick(View view) {
		if(DEBUG){Log.v(TAG,"onClick("+(view==null?"null":view.getId())+")");}
		if (view instanceof ScheduleItemView) {
			final ScheduleItemView itemView = (ScheduleItemView) view;
			final ScheduleItem item = itemView.getScheduleItem();
			final String title = itemView.getText().toString();
			AnalyticsUtils.getInstance(getActivity()).trackEvent("Schedule",
					"Session Click", title, 0);
			final String id = item.getId();
			// final int sessionsCount = block.getSessionsCount();
			// if (sessionsCount == 1) {
			// final Uri sessionUri = CfpContract.Sessions
			// .buildSessionUri(block.getSessionId());
			// final Intent intent = new Intent(Intent.ACTION_VIEW, sessionUri);
			// ((BaseActivity) getSupportActivity())
			// .openActivityOrFragment(intent);
			// } else {
			final Uri sessionsUri = CfpContract.Blocks.buildSessionsUri(id);
			final Intent intent = new Intent(Intent.ACTION_VIEW, sessionsUri);
			intent.putExtra(Intent.EXTRA_TITLE, title);
			((BaseActivity) getSupportActivity())
					.openActivityOrFragment(intent);
			// }
		}
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		try {
			if (cursor.moveToFirst()) {
				final BlockScheduleItem block = (BlockScheduleItem) cookie;
				final String sessionId = cursor.getString(cursor
						.getColumnIndex(Sessions.SESSION_ID));
				BlockScheduleItem.updateSessionId(block, sessionId);
			}
		} finally {
			cursor.close();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(DEBUG){Log.v(TAG,"onCreateLoader(id="+id+",args="+(args==null?"null":""+args.isEmpty())
				+") loaderURI: " + mDay.loaderUri);}
		return new CursorLoader(getActivity(), mDay.loaderUri,
				BlocksQuery.PROJECTION, null, null,
				CfpContract.Blocks.DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(DEBUG){Log.v(TAG,"onLoadFinished(data="+(data==null?"null":"notnull")+")");}
		if (data == null || !data.moveToFirst()) {
			return;
		}

		mDay.scheduleItemsView.removeAllBlocks();

		final ArrayList<BlockScheduleItem> blocksList = Lists.newArrayList();
		BlockScheduleItem.loadBlocks(getActivity(), data, blocksList);
		for (BlockScheduleItem block : blocksList) {
			final ScheduleItemView view = new ScheduleItemView(getActivity(),
					block);

			if (block.getSessionsCount() == 1) {
				final Uri uri = Blocks.buildSessionUri(block.getId());
				mHandler.startQuery(uri, null, null, null, null, block);
			}

			if (block.getSessionsCount() > 0) {
				view.setOnClickListener(this);
			} else {
				view.setFocusable(false);
				view.setEnabled(false);
				LayerDrawable buttonDrawable = (LayerDrawable) view
						.getBackground();
				buttonDrawable.getDrawable(0).setAlpha(DISABLED_BLOCK_ALPHA);
				buttonDrawable.getDrawable(11).setAlpha(DISABLED_BLOCK_ALPHA);
			}

			mDay.scheduleItemsView.addBlock(view);
		}

		if (getView() != null) {
			if (isResumed()) {
				setContentShown(true);
			} else {
				setContentShownNoAnimation(true);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if(DEBUG){Log.v(TAG,"onLoaderReset(...)");}
		mDay.scheduleItemsView.removeAllBlocks();
	}

	public ScrollView getScrollView() {
		if (mDay != null) {
			return mDay.scrollView;
		}
		return null;
	}

	private ContentObserver mSessionChangesObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (getActivity() != null) {
				getLoaderManager().restartLoader(getArguments().getInt("id"), null,
						BlockScheduleItemsFragment.this);
			}
		}
	};

	/**
	 * {@link Blocks} query parameters.
	 */
	public interface BlocksQuery {
		String[] PROJECTION = { BaseColumns._ID, CfpContract.Blocks.BLOCK_ID,
				CfpContract.Blocks.BLOCK_TITLE, CfpContract.Blocks.BLOCK_START,
				CfpContract.Blocks.BLOCK_END, CfpContract.Blocks.BLOCK_KIND,
				CfpContract.Blocks.BLOCK_TYPE, CfpContract.Blocks.BLOCK_CODE,
				CfpContract.Blocks.SESSIONS_COUNT,
				CfpContract.Blocks.CONTAINS_STARRED, };

		int _ID = 0;
		int BLOCK_ID = 1;
		int BLOCK_TITLE = 2;
		int BLOCK_START = 3;
		int BLOCK_END = 4;
		int BLOCK_KIND = 5;
		int BLOCK_TYPE = 6;
		int BLOCK_CODE = 7;
		int SESSIONS_COUNT = 8;
		int CONTAINS_STARRED = 9;
	}

}
