package com.bulk.contactcreate;

import java.util.ArrayList;
import java.util.Random;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class ContactUtils {
	private static final String TAG = ContactUtils.class.getSimpleName();

	private static Random mRandom = new Random();

	public static ArrayList<ContentProviderOperation> createContacts() {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		int rawContactInsertIndex = ops.size();
		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).withValue(RawContacts.ACCOUNT_TYPE, null).withValue(RawContacts.ACCOUNT_NAME, null).build());

		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex).withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
				.withValue(StructuredName.DISPLAY_NAME, "良辰").build());

		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex).withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				.withValue(Phone.NUMBER, "1234567890").withValue(Phone.TYPE, Phone.TYPE_MOBILE).withValue(Phone.LABEL, "手机号").build());

		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex).withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
				.withValue(Email.DATA, "liangchen@itcast.cn").withValue(Email.TYPE, Email.TYPE_WORK).build());

		return ops;
	}

	public static ArrayList<ContentProviderOperation> createContacts(Context context, String companyName, int number) {

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		for (int i = 0; i < number; i++) {
			int rawContactInsertIndex = ops.size();
			ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
					.withValue(RawContacts.ACCOUNT_TYPE, null)
					.withValue(RawContacts.ACCOUNT_NAME, null)
					.build());

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
					.withValue(Organization.COMPANY, companyName)
					.build());

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
					.withValue(StructuredName.DISPLAY_NAME, createName(context))
					.build());

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, createPhoneNum(Phone.TYPE_MOBILE))
					.withValue(Phone.TYPE, Phone.TYPE_MOBILE)
					.build());
		}

		return ops;

	}

	public static int insertContacts(Context context, ArrayList<ContentProviderOperation> ops) {

		ContentProviderResult[] results = null;
		int cnt = 0;
		try {
			results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//			for (ContentProviderResult result : results) {
//				Log.i(TAG, result.uri.toString());
//			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		
		cnt = results == null ? 0 : results.length / 4;
		Log.d(TAG, "success: " + cnt);
		return cnt;
	}

	public static int deleteContacts(Context context, String companyName) {
		String[] projection = new String[] { Data.RAW_CONTACT_ID };
		String selection = Organization.COMPANY + "=?";
		String[] selectionArgs = new String[] { companyName };
		
		Log.d(TAG, "to delete contacts , companyName : " + companyName);
		Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, projection, selection, selectionArgs, null);
		
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int succ = 0;
		int fail = 0;
		if (cursor.moveToFirst()) {
			do {
				long Id = cursor.getLong(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
				ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, Id)).build());
				try {
					context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
					succ ++;
				} catch (Exception e) {
					fail ++;
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
			cursor.close();
		}
		Log.d(TAG, "success: " + succ + ", failed: " + fail);
		return succ;
	}

	public static String createCompany(Context context) {
		return "ejiahe";
	}

	public static String createName(Context context) {
		String prefix[] = context.getResources().getStringArray(R.array.prefix);
		String suffix[] = context.getResources().getStringArray(R.array.suffix);
		String name = "";
		int cnt = 1 + mRandom.nextInt(2);
		name = prefix[mRandom.nextInt(prefix.length)];
		for (int i = 0; i < cnt; i++) {
			name += suffix[mRandom.nextInt(prefix.length)];
		}
		return name;
	}

	public static String createPhoneNum(int type) {
		String phone = "";
		String[] array = new String[] {"13", "15", "18"};
		phone = array[mRandom.nextInt(3)]+ mRandom.nextInt(10) + convertString(4, mRandom.nextInt(10000)) + convertString(4, mRandom.nextInt(10000));
		return phone;
	}

	static String convertString(int len, int number) {
		String result = "";
		result = String.format(String.format("%%1$0%sd", len), number);
		return result;
	}
}
