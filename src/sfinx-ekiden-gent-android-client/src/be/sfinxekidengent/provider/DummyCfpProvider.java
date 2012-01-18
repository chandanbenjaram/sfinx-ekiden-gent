package be.sfinxekidengent.provider;

import java.util.ArrayList;

import be.sfinxekidengent.provider.CfpContract.Blocks;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class DummyCfpProvider extends CfpProvider {
	private static final String TAG = "DummyCfpProvider";
	@Override
	public boolean onCreate() {
		Log.v(TAG, "onCreate");
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		Log.v(TAG, "getType: " + uri.toString());
		return Blocks.CONTENT_TYPE;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
    	Log.v(TAG, "query(URI="+uri+";Projection="+projection+
    			";Selection="+selection+";SelectionArgs="+selectionArgs+
    			";SortOrder="+sortOrder+")");
		final Cursor c = new Cursor() {
			
			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void unregisterContentObserver(ContentObserver observer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNotificationUri(ContentResolver cr, Uri uri) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Bundle respond(Bundle extras) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean requery() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void registerContentObserver(ContentObserver observer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean moveToPrevious() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean moveToPosition(int position) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean moveToNext() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean moveToLast() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean moveToFirst() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean move(int offset) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isNull(int columnIndex) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isLast() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isFirst() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isClosed() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isBeforeFirst() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isAfterLast() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean getWantsAllOnMoveCalls() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getType(int columnIndex) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public String getString(int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public short getShort(int columnIndex) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getPosition() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public long getLong(int columnIndex) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getInt(int columnIndex) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public float getFloat(int columnIndex) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Bundle getExtras() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public double getDouble(int columnIndex) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public String[] getColumnNames() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getColumnName(int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getColumnIndexOrThrow(String columnName)
					throws IllegalArgumentException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getColumnIndex(String columnName) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getColumnCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public byte[] getBlob(int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void deactivate() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
		};
		return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
    	Log.v(TAG, "insert(URI="+uri+"; values="+values.toString()+")");
		return Uri.parse("content://be.sfinxekidengent");
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
    	Log.v(TAG, "update(URI="+uri+"; values="+values.toString()+
    			";selection="+selection+";selectionArgs="+selectionArgs+")");
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
    	Log.v(TAG, "delete(URI="+uri+"; "+
    			";selection="+selection+";selectionArgs="+selectionArgs+")");
		return 0;
	}
	
	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		Log.v(TAG, "applyBatch");
		return new ContentProviderResult[] {};
	}

}
