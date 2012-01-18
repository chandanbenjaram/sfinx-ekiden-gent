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

package be.sfinxekidengent.io;

import java.io.IOException;
import java.io.InputStream;


import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import be.sfinxekidengent.io.BaseHandler.HandlerException;
import be.sfinxekidengent.provider.CfpContract;
import be.sfinxekidengent.provider.CfpContract.Speakers;
import be.sfinxekidengent.util.JsonHandlerUtils;
import be.sfinxekidengent.util.XmlHandlerUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Opens a local resource file and passes the resulting {@link XmlPullParser} or
 * {@link JsonParser} to the given {@link XmlHandler} or {@link JsonHandler}.
 */
public class LocalExecutor {
	private static final String TAG = "LocalExecutor";
	private Resources mRes;
	private ContentResolver mResolver;

	public LocalExecutor(Resources res, ContentResolver resolver) {
		mRes = res;
		mResolver = resolver;
	}

	public void execute(Context context, String assetName, XmlHandler handler)
			throws HandlerException {
		try {
			final InputStream input = context.getAssets().open(assetName);
			final XmlPullParser parser = XmlHandlerUtils.newPullParser(input);
			handler.parseAndApply(parser, mResolver);
		} catch (HandlerException e) {
			throw e;
		} catch (XmlPullParserException e) {
			throw new HandlerException("Problem parsing local asset: "
					+ assetName, e);
		} catch (IOException e) {
			throw new HandlerException("Problem parsing local asset: "
					+ assetName, e);
		}
	}

	public void execute(int resId, XmlHandler handler) throws HandlerException {
		final XmlResourceParser parser = mRes.getXml(resId);
		try {
			handler.parseAndApply(parser, mResolver);
		} finally {
			parser.close();
		}
	}

	public void execute(Context context, String assetName, JsonHandler handler)
			throws HandlerException {
		try {
			final InputStream input = context.getAssets().open(assetName);
			final JsonParser parser = JsonHandlerUtils.newJsonParser(input);
			Log.v(TAG, "Parse and reply...");
			handler.parseAndApply(parser, mResolver);
			//TODO: removeme
			Log.v(TAG, "Query " + Speakers.CONTENT_URI);
			String[] projection = { BaseColumns._ID,
					CfpContract.Speakers.SPEAKER_ID,
					CfpContract.Speakers.SPEAKER_FIRSTNAME,
					CfpContract.Speakers.SPEAKER_LASTNAME,
					CfpContract.Speakers.SPEAKER_COMPANY,
					CfpContract.Speakers.SPEAKER_BIO,
					CfpContract.Speakers.SPEAKER_IMAGE_URL, };
			Cursor cursor = mResolver.query(Speakers.CONTENT_URI, projection, null, null, Speakers.DEFAULT_SORT);
			if (cursor != null) {
				// Ensure the cursor window is filled
				int count = cursor.getCount();
				Log.v(TAG, "Cursor count: " + count);
			} else {
				Log.v(TAG, "Cursor is null");
			}
		} catch (HandlerException e) {
			throw e;
		} catch (JsonParseException e) {
			throw new HandlerException("Problem parsing local asset: "
					+ assetName, e);
		} catch (IOException e) {
			throw new HandlerException("Problem parsing local asset: "
					+ assetName, e);
		}
	}

}
