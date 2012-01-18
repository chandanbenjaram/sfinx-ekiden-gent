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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;
import be.sfinxekidengent.io.BaseHandler.HandlerException;
import be.sfinxekidengent.provider.CfpContract;
import be.sfinxekidengent.provider.CfpContract.Speakers;
import be.sfinxekidengent.util.JsonHandlerUtils;

/**
 * Executes an {@link HttpUriRequest} and passes the result as an
 * {@link JsonParser} to the given {@link JsonHandler}.
 */
public class RemoteExecutor {
	private static final String TAG = "RemoteExecutor";
	private final HttpClient mHttpClient;
	private final ContentResolver mResolver;

	public RemoteExecutor(HttpClient httpClient, ContentResolver resolver) {
		mHttpClient = httpClient;
		mResolver = resolver;
	}

	/**
	 * Execute a {@link HttpGet} request, passing a valid response through
	 * {@link JsonHandler#parseAndApply(JsonParser, ContentResolver)}.
	 */
	public void executeGet(String url, JsonHandler handler)
			throws HandlerException {
		final HttpUriRequest request = new HttpGet(url);
		execute(request, handler);
	}

	/**
	 * Execute this {@link HttpUriRequest}, passing a valid response through
	 * {@link JsonHandler#parseAndApply(JsonParser, ContentResolver)}.
	 */
	public void execute(HttpUriRequest request, JsonHandler handler)
			throws HandlerException {
		try {
			final HttpResponse resp = mHttpClient.execute(request);
			final int status = resp.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				throw new HandlerException("Unexpected server response "
						+ resp.getStatusLine() + " for "
						+ request.getRequestLine());
			}

			final InputStream input = resp.getEntity().getContent();
			try {
				final JsonParser parser = JsonHandlerUtils.newJsonParser(input);
				Log.v(TAG, "Parse and reply...");
				handler.parseAndApply(parser, mResolver);
			} catch (JsonParseException e) {
				throw new HandlerException("Malformed response for "
						+ request.getRequestLine(), e);
			} finally {
				if (input != null)
					input.close();
			}
			// TODO: removeme
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
				cursor = mResolver.query(Speakers.CONTENT_URI, projection,
						null, null, Speakers.DEFAULT_SORT);
				Log.v(TAG, "ContentResolver: " + mResolver.toString());
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

		} catch (HandlerException e) {
			throw e;
		} catch (IOException e) {
			throw new HandlerException("Problem reading remote response for "
					+ request.getRequestLine(), e);
		}
	}
}
