package com.martin.dailyselfie;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

final class SampleGridViewAdapter extends BaseAdapter {
  private final Context context;
  private final List<File> files = new ArrayList<File>();
  private final String TAG = "SampleGridViewAdapter";

  public SampleGridViewAdapter(Context context, File albumDir) {
    this.context = context;
    
    files.clear();

    for (final File fileEntry : albumDir.listFiles()) {
        if (!fileEntry.isDirectory()) {
        	 Log.d(TAG, fileEntry.getAbsolutePath());
             files.add(fileEntry);
        } 
    }
    
    // Ensure we get a different ordering of images on each run.
    //Collections.addAll(urls, Data.URLS);
    //Collections.shuffle(files);

  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    SquaredImageView view = (SquaredImageView) convertView;
    if (view == null) {
      view = new SquaredImageView(context);
      view.setScaleType(CENTER_CROP);
      view.setPadding(5, 5, 5, 5);
    }

    // Get the image URL for the current position.
    File file = getItem(position);

    // Trigger the download of the URL asynchronously into the image view.
    Picasso.with(context) //
        .load(file) //
        .placeholder(R.drawable.placeholder) //
        .error(R.drawable.error) //
        .fit() //
        .tag(context) //
        .into(view);

    return view;
  }

  @Override public int getCount() {
    return files.size();
  }

  @Override public File getItem(int position) {
    return files.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }
}
