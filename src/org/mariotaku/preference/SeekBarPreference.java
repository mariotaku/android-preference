package org.mariotaku.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources.NotFoundException;
import android.graphics.PixelFormat;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnPreferenceClickListener {

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULTVALUE = "defaultValue";
	private static final String ATTR_MAXVALUE = "maxValue";
	private static final String ATTR_MINVALUE = "minValue";
	private static final String ATTR_DIALOGTITLE = "dialogTitle";
	private static final String ATTR_TITLE = "title";
	private static final String ATTR_DIALOGMESSAGE = "dialogMessage";
	private int mDefaultValue = 0;
	private int mValue = 0;
	private int mMax = 100;
	private int mMin = 0;
	private String mTitle = null;
	private String mMessage = null;

	public SeekBarPreference(Context context) {
		super(context);
		init(context, null);
	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		SeekBarDialog dialog = new SeekBarDialog(getContext(), mMin, mMax, mValue);
		if (mTitle != null) {
			dialog.setTitle(mTitle);
		}
		if (mMessage != null) {
			dialog.setMessage(mMessage);
		}
		dialog.show();

		return false;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if (isPersistent()) {
			persistInt(restoreValue ? getValue() : (Integer) defaultValue);
		}

	}

	private int getValue() {

		try {
			if (isPersistent()) {
				mValue = getPersistedInt(mDefaultValue);
			}
		} catch (ClassCastException e) {
			mValue = mDefaultValue;
		}

		return mValue;
	}

	private void init(Context context, AttributeSet attrs) {

		setOnPreferenceClickListener(this);
		if (attrs != null) {
			try {
				mTitle = context.getString(attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DIALOGTITLE, -1));
			} catch (NotFoundException e) {
				mTitle = attrs.getAttributeValue(ANDROID_NS, ATTR_DIALOGTITLE);
			}

			if (mTitle == null) {
				try {
					mTitle = context.getString(attrs.getAttributeResourceValue(ANDROID_NS, ATTR_TITLE, -1));
				} catch (NotFoundException e) {
					mTitle = attrs.getAttributeValue(ANDROID_NS, ATTR_TITLE);
				}
			}

			try {
				mMessage = context.getString(attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DIALOGMESSAGE, -1));
			} catch (NotFoundException e) {
				mMessage = attrs.getAttributeValue(ANDROID_NS, ATTR_DIALOGMESSAGE);
			}

			String defaultValue = attrs.getAttributeValue(ANDROID_NS, ATTR_DEFAULTVALUE);
			if (defaultValue != null && defaultValue.startsWith("@")) {
				int resourceId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
				if (resourceId != 0) {
					mDefaultValue = context.getResources().getInteger(resourceId);
				}

			} else {
				mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
			}
			mMax = attrs.getAttributeIntValue(null, ATTR_MAXVALUE, 100);
			mMin = attrs.getAttributeIntValue(null, ATTR_MINVALUE, 0);
		}
		mValue = mDefaultValue;
	}

	private class SeekBarDialog extends AlertDialog implements OnClickListener, OnSeekBarChangeListener {

		private SeekBar mSeekBar;
		private TextView mMessageView, mProgressView;
		private int mMin = 0;

		public SeekBarDialog(Context context, int min, int max, int defaultValue) {
			super(context);

			init(context, min, max, defaultValue);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case Dialog.BUTTON_POSITIVE:
					mValue = mSeekBar.getProgress() + mMin;
					if (isPersistent()) {
						persistInt(mValue);
					}
					if (getOnPreferenceChangeListener() != null) {
						getOnPreferenceChangeListener().onPreferenceChange(SeekBarPreference.this, mValue);
					}
					break;
				case Dialog.BUTTON_NEGATIVE:
					break;
			}

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			mProgressView.setText(String.valueOf(progress + mMin));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void setMessage(CharSequence message) {
			mMessageView.setText(message);
			mMessageView.setVisibility(message != null ? View.VISIBLE : View.GONE);
		}

		private void init(Context context, int min, int max, int defaultValue) {

			getWindow().setFormat(PixelFormat.RGBA_8888);
			float density = context.getResources().getDisplayMetrics().density;

			LinearLayout mContentView = new LinearLayout(context);
			mContentView.setOrientation(LinearLayout.VERTICAL);
			mMin = min;

			mMessageView = new TextView(context);
			mProgressView = new TextView(context);
			mSeekBar = new SeekBar(context);

			mContentView.addView(mMessageView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			mContentView.addView(mProgressView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			mContentView.addView(mSeekBar, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			int padding = Math.round(density * 16);
			mContentView.setPadding(padding, padding, padding, padding);

			mMessageView.setVisibility(View.GONE);
			mMessageView.setTextSize(18.0f);
			mMessageView.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));
			mProgressView.setTextSize(18.0f);
			mProgressView.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));
			mSeekBar.setMax(max - min);
			mSeekBar.setProgress(defaultValue - min);
			mProgressView.setText(String.valueOf(mSeekBar.getProgress() + min));
			mSeekBar.setOnSeekBarChangeListener(this);

			setView(mContentView);

			setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
			setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		}
	}
}
