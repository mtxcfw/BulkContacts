package com.bulk.contactcreate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	public static String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		Button mCreateBtn;
		Button mDeleteBtn;
		EditText mNumberEdit;
		EditText mOrgEditText;

		public ProgressDialog mProgressDialog;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			mNumberEdit = (EditText) rootView.findViewById(R.id.number);
			mOrgEditText = (EditText) rootView.findViewById(R.id.org_name);
			mCreateBtn = (Button) rootView.findViewById(R.id.finish);
			mDeleteBtn = (Button) rootView.findViewById(R.id.delete);

			mOrgEditText.setText("ejiahe");

			mCreateBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String input = mNumberEdit.getText().toString();
					if (TextUtils.isEmpty(input)) {
						return;
					}

					final String orgName = mOrgEditText.getText().toString();
					if (TextUtils.isEmpty(orgName)) {
						return;
					}

					final int num = Integer.parseInt(input);
					Log.d(TAG, "number is : " + num);
					if (mProgressDialog != null && mProgressDialog.isShowing()) {
						return;
					}

					new CreateContactTask(getActivity(), orgName, num).execute(null, null, null);
				}
			});

			mDeleteBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String orgName = mOrgEditText.getText().toString();
					if (TextUtils.isEmpty(orgName)) {
						return;
					}

					Log.d(TAG, "orgName is : " + orgName);
					new DeleteContactTask(getActivity(), orgName).execute(null, null, null);
				}
			});

			return rootView;
		}

		class CreateContactTask extends AsyncTask<Void, Void, Integer> {
			private Context mContext;
			private String mOrgName;
			private int mNumber;

			public CreateContactTask(Context context, String orgName, int number) {
				mContext = context;
				mOrgName = orgName;
				mNumber = number;
			}

			@Override
			protected Integer doInBackground(Void... params) {
				int result = ContactUtils.insertContacts(getActivity(), ContactUtils.createContacts(getActivity(),mOrgName, mNumber));
//				SystemClock.sleep(10000);
				return result;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setTitle("提示");
				mProgressDialog.setMessage("正在添加...");
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				
				String message = String.format("成功创建%s个联系人，失败%s个", result, mNumber-result);
				new AlertDialog.Builder(mContext).setTitle("提示").setMessage(message).setPositiveButton("确定", null).show();
			}
		}
		
		class DeleteContactTask extends AsyncTask<Void, Void, Integer> {
			private Context mContext;
			private String mOrgName;

			public DeleteContactTask(Context context, String orgName) {
				mContext = context;
				mOrgName = orgName;
			}

			@Override
			protected Integer doInBackground(Void... params) {
				int result = ContactUtils.deleteContacts(getActivity(), mOrgName);
				return result;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setTitle("提示");
				mProgressDialog.setMessage("正在删除...");
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				
				String message = String.format("从%s组删除%s个联系人", mOrgName, result);
				new AlertDialog.Builder(mContext).setTitle("提示").setMessage(message).setPositiveButton("确定", null).show();
			}
		}
	}
}
