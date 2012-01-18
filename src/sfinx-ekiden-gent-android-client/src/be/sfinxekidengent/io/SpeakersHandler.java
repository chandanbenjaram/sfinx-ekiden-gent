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

import static org.codehaus.jackson.JsonToken.END_ARRAY;
import static org.codehaus.jackson.JsonToken.END_OBJECT;
import static org.codehaus.jackson.JsonToken.FIELD_NAME;
import static org.codehaus.jackson.JsonToken.START_ARRAY;
import static org.codehaus.jackson.JsonToken.START_OBJECT;
import static org.codehaus.jackson.JsonToken.VALUE_NUMBER_INT;
import static org.codehaus.jackson.JsonToken.VALUE_STRING;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import be.sfinxekidengent.provider.CfpContract;
import be.sfinxekidengent.provider.CfpContract.Blocks;
import be.sfinxekidengent.provider.CfpContract.News;
import be.sfinxekidengent.provider.CfpContract.Rooms;
import be.sfinxekidengent.provider.CfpContract.Speakers;
import be.sfinxekidengent.provider.CfpContract.SyncColumns;
import be.sfinxekidengent.util.Lists;
import be.sfinxekidengent.util.ParserUtils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Handle a local {@link JsonHandler} that defines a set of {@link Speakers}
 * entries.
 */
public class SpeakersHandler extends JsonHandler {

	protected static final String TAG = "SpeakersHandler";

	public SpeakersHandler() {
		super(CfpContract.CONTENT_AUTHORITY);
	}

	/** {@inheritDoc} */
	@Override
	public ArrayList<ContentProviderOperation> parse(JsonParser parser,
			ContentResolver resolver) throws JsonParseException, IOException {
		Log.d(TAG, "Parse JSON speaker: " + parser.toString());
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

		batch.add(ContentProviderOperation.newUpdate(Speakers.CONTENT_URI)
				.withValue(Speakers.DELETED, CfpContract.MARK_AS_DELETED)
				.build());

		JsonToken token;
		int speakerCount = 0;
		while ((token = parser.nextToken()) != END_ARRAY) {
			if (token == START_OBJECT) {
				parseSpeaker(parser, batch, resolver);
				speakerCount++;
			}
		}
		Log.d(TAG, speakerCount + " speakers parsed.");

		/*
		batch.add(ContentProviderOperation
				.newDelete(Speakers.CONTENT_URI)
				.withSelection(
						Speakers.DELETED + "=?",
						new String[] { String
								.valueOf(CfpContract.MARK_AS_DELETED) })
				.build());
		*/

		Log.v(TAG, "Returning batch: " + batch);
		return batch;
	}

	/**
	 * Parse a given {@link Rooms} entry, building
	 * {@link ContentProviderOperation} to define it locally.
	 */
	private static void parseSpeaker(JsonParser parser,
			ArrayList<ContentProviderOperation> batch, ContentResolver resolver)
			throws JsonParseException, IOException {
		Log.v(TAG, "Create builder for " + Speakers.CONTENT_URI);

		// final ContentValues values = new ContentValues();
		String speakerId = null;
		String speakerBio = null;
		String speakerCompany = null;
		String speakerFirstname = null;
		String speakerLastName = null;
		String speakerImageUri = null;

		int depth = 0;
		String fieldName = null;
		JsonToken token;
		while ((token = parser.nextToken()) != END_OBJECT || depth > 0) {
			if (token == START_OBJECT || token == START_ARRAY) {
				depth++;
			} else if (token == END_OBJECT || token == END_ARRAY) {
				depth--;
			} else if (token == FIELD_NAME) {
				fieldName = parser.getCurrentName();
			} else if (token == VALUE_NUMBER_INT) {
				final int value = parser.getIntValue();
				if (Fields.ID.equals(fieldName)) {
					speakerId = Speakers.generateSpeakerId(value);
					// values.put(Speakers.SPEAKER_ID, speakerId);
				}
			} else if (token == VALUE_STRING) {
				final String text = parser.getText();
				if (Fields.BIO.equals(fieldName)) {
					speakerBio = text;
					// values.put(Speakers.SPEAKER_BIO, text);
				} else if (Fields.COMPANY.equals(fieldName)) {
					speakerCompany = text;
					// values.put(Speakers.SPEAKER_COMPANY, text);
				} else if (Fields.FIRSTNAME.equals(fieldName)) {
					speakerFirstname = text;
					// values.put(Speakers.SPEAKER_FIRSTNAME, text);
					Log.v(TAG, "Add speaker " + text);
				} else if (Fields.IMAGE.equals(fieldName)) {
					speakerImageUri = text;
					// values.put(Speakers.SPEAKER_IMAGE_URL, text);
				} else if (Fields.LASTNAME.equals(fieldName)) {
					speakerLastName = text;
					// values.put(Speakers.SPEAKER_LASTNAME, text);
				}
			}
		}

		final String theSpeakerId = findOrCreateSpeaker(speakerId, speakerBio,
				speakerCompany, speakerFirstname, speakerLastName,
				speakerImageUri, batch, resolver);

		/*
		 * final Uri speakerUri = Speakers.buildSpeakerUri(speakerId);
		 * 
		 * ContentProviderOperation.Builder speakerBuilder = null; Log.d(TAG,
		 * "Query old values on " + speakerUri); final ContentValues oldValues =
		 * querySpeakerDetails(speakerUri, resolver); Log.d(TAG, "old values: "
		 * + oldValues.size() + " => " + oldValues.toString()); final long
		 * localUpdated = oldValues.getAsLong(SyncColumns.UPDATED); Log.d(TAG,
		 * "Going to install speaker values with localUpdated == CfpContract.UPDATED_NEVER: "
		 * + (localUpdated == CfpContract.UPDATED_NEVER)); if (localUpdated ==
		 * CfpContract.UPDATED_NEVER) { Log.d(TAG, "New insert: " +
		 * Speakers.CONTENT_URI); speakerBuilder = ContentProviderOperation
		 * .newInsert(Speakers.CONTENT_URI); } else { Log.d(TAG, "New insert: "
		 * + speakerUri); speakerBuilder =
		 * ContentProviderOperation.newUpdate(speakerUri); }
		 * 
		 * values.put(SyncColumns.UPDATED, System.currentTimeMillis());
		 * values.put(SyncColumns.DELETED, CfpContract.NOT_DELETED); Log.d(TAG,
		 * "Adding speaker to batch: " + values);
		 * batch.add(speakerBuilder.withValues(values).build());
		 */
	}

	/**
	 * Return a {@link Blocks#BLOCK_ID} matching the requested arguments,
	 * inserting a new {@link Blocks} entry as a
	 * {@link ContentProviderOperation} when none already exists.
	 */
	private static String findOrCreateSpeaker(String speakerId,
			String speakerBio, String speakerCompany, String speakerFirstname,
			String speakerLastname, String speakerImageUri,
			ArrayList<ContentProviderOperation> batch, ContentResolver resolver) {
		Log.v(TAG, "Find or create speaker " + speakerId + " " + speakerFirstname);
		boolean isSpeakerExisting = isSpeakerExisting(speakerId, resolver);
		Log.v(TAG, "Speaker is " + (isSpeakerExisting?"":"not ") + "existing.");
		if (!isSpeakerExisting) {
			final ContentProviderOperation.Builder builder = ContentProviderOperation
					.newInsert(Speakers.CONTENT_URI);
			builder.withValue(Speakers.SPEAKER_ID, speakerId);
			builder.withValue(Speakers.SPEAKER_BIO, speakerBio);
			builder.withValue(Speakers.SPEAKER_COMPANY, speakerCompany);
			builder.withValue(Speakers.SPEAKER_FIRSTNAME, speakerFirstname);
			builder.withValue(Speakers.SPEAKER_LASTNAME, speakerLastname);
			builder.withValue(Speakers.SPEAKER_IMAGE_URL, speakerImageUri);
			builder.withValue(Speakers.UPDATED, System.currentTimeMillis());
			builder.withValue(Speakers.DELETED, CfpContract.NOT_DELETED);
			Log.v(TAG, "Add speaker " + speakerId);
			batch.add(builder.build());
		} else {
			Log.v(TAG, "Update speaker (SKIP!!!)" + speakerId);
			/*
			final ContentProviderOperation.Builder builder = ContentProviderOperation
					.newUpdate(Speakers.buildSpeakerUri(speakerId));
			builder.withValue(Speakers.UPDATED, System.currentTimeMillis());
			builder.withValue(Speakers.DELETED, CfpContract.NOT_DELETED);
			batch.add(builder.build());
			*/
		}

		return speakerId;
	}

	private static boolean isSpeakerExisting(String speakerId,
			ContentResolver resolver) {
		boolean result = false;
		final Uri uri = Speakers.buildSpeakerUri(speakerId);
		final Cursor cursor = resolver.query(uri, SpeakersQuery.PROJECTION,
				null, null, null);
		try {
			result = cursor.moveToFirst();
			return result;
		} finally {
			cursor.close();
		}

	}

	private static ContentValues querySpeakerDetails(Uri uri,
			ContentResolver resolver) {
		final ContentValues values = new ContentValues();
		final Cursor cursor = resolver.query(uri, SpeakersQuery.PROJECTION,
				null, null, null);
		try {
			if (cursor.moveToFirst()) {
				values.put(SyncColumns.UPDATED,
						cursor.getLong(SpeakersQuery.UPDATED));
			} else {
				values.put(SyncColumns.UPDATED, CfpContract.UPDATED_NEVER);
			}
		} finally {
			cursor.close();
		}
		return values;
	}

	/**
	 * {@link Speakers} query parameters.
	 */
	private interface SpeakersQuery {
		String[] PROJECTION = { SyncColumns.UPDATED };

		int UPDATED = 0;
	}

	private interface Fields {
		String BIO = "bio";
		String COMPANY = "company";
		String FIRSTNAME = "firstName";
		String ID = "id";
		String IMAGE = "imageURI";
		String LASTNAME = "lastName";
	}

}
