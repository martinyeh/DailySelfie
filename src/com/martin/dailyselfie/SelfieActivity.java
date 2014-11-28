package com.martin.dailyselfie;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class SelfieActivity extends FragmentActivity {

	private final int ACTION_TAKE_PHOTO_B = 1;

	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	private AlarmManager mAlarmManager;

	private Intent mNotificationReceiverIntent, mLoggerReceiverIntent;
	private PendingIntent mNotificationReceiverPendingIntent;

	private static final long INITIAL_ALARM_DELAY = 2 * 60 * 1000L;
	protected static final long JITTER = 5000L;
	
	SampleGridViewAdapter mAdapter;
	
	private String TAG = "SelfieActivity";

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	void showDetails(File file) {

		getFragmentManager().beginTransaction()
				.replace(R.id.sample_content, DetailFragment.newInstance(file))
				.addToBackStack("detail").commit();
	}
	
	public SampleGridViewAdapter getAdapter(){
		return mAdapter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selfie);
		
		mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		
		 mAdapter = new SampleGridViewAdapter(this, this.getAlbumDir());
		
	    if (savedInstanceState == null) {
	        getFragmentManager().beginTransaction()
	            .add(R.id.sample_content, GridFragment.newInstance())
	            .commit();
	      }
		

		setAlarm();

	}
	
	@Override
	public void onBackPressed(){
	    FragmentManager fm = getFragmentManager();
	    if (fm.getBackStackEntryCount() > 0) {
	        Log.i("MainActivity", "popping backstack");
	        fm.popBackStack();
	    } else {
	        Log.i("MainActivity", "nothing on backstack, calling super");
	        super.onBackPressed();  
	    }
	}

	// @Override
	// protected void onPause(){
	// mAlarmManager.cancel(mNotificationReceiverPendingIntent);
	// }

	private void setAlarm() {

		// Get the AlarmManager Service
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		// Create an Intent to broadcast to the AlarmNotificationReceiver
		mNotificationReceiverIntent = new Intent(SelfieActivity.this,
				AlarmNotificationReceiver.class);

		// Create an PendingIntent that holds the NotificationReceiverIntent
		mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
				SelfieActivity.this, 0, mNotificationReceiverIntent, 0);
		// Set repeating alarm
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 10 * 1000,
				AlarmManager.INTERVAL_HOUR, mNotificationReceiverPendingIntent);

		// Show Toast message
		Toast.makeText(getApplicationContext(), "Repeating Alarm Set",
				Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selfie, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_camera) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {

		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();

		return f;
	}

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		switch (actionCode) {
		case ACTION_TAKE_PHOTO_B:
			File f = null;

			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(f));
				takePictureIntent.putExtra(
						"android.intent.extras.CAMERA_FACING",
						Camera.CameraInfo.CAMERA_FACING_FRONT);
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;

		default:
			break;
		} // switch

		startActivityForResult(takePictureIntent, actionCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == ACTION_TAKE_PHOTO_B) {
            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
            	mAdapter.notifyDataSetChanged();
            	Log.d(TAG,"notifyDataSetChanged");
            }
        }
    }
	
	public static class GridFragment extends Fragment {
	    public static GridFragment newInstance() {
	      return new GridFragment();
	    }

	    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	      final SelfieActivity activity = (SelfieActivity) getActivity();
	      //mAlbumStorageDirFactory = new FroyoAlbumDirFactory();

		  final SampleGridViewAdapter adapter = activity.getAdapter();
		  
		  GridView listView = (GridView) LayoutInflater.from(activity)
	          .inflate(R.layout.grid, container, false);
	      listView.setAdapter(adapter);
	      listView.setOnScrollListener(new SampleScrollListener(activity));
	      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
	          File file = adapter.getItem(position);
	          activity.showDetails(file);
	        }
	      });
	      return listView;
	    }
	  }


	public static class DetailFragment extends Fragment {
		private static final String KEY_URL = "picasso:file";

		public static DetailFragment newInstance(File file) {
			Bundle arguments = new Bundle();
			arguments.putSerializable(KEY_URL, file);

			DetailFragment fragment = new DetailFragment();
			fragment.setArguments(arguments);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Activity activity = getActivity();

			View view = LayoutInflater.from(activity).inflate(
					R.layout.grid_detail, container, false);

			TextView descView = (TextView) view.findViewById(R.id.desc);
			ImageView imageView = (ImageView) view.findViewById(R.id.photo);

			Bundle arguments = getArguments();
			File file = (File) arguments.getSerializable(KEY_URL);

			descView.setText(file.getAbsolutePath());
			Picasso.with(activity).load(file).fit().tag(activity)
					.into(imageView);

			return view;
		}
	}
}
