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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TextSizePreference extends Preference implements OnPreferenceClickListener {

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULTVALUE = "defaultValue";
	private static final String ATTR_MAXVALUE = "maxValue";
	private static final String ATTR_MINVALUE = "minValue";
	private static final String ATTR_DIALOGTITLE = "dialogTitle";
	private static final String ATTR_TITLE = "title";
	private static final String ATTR_DIALOGMESSAGE = "dialogMessage";
	private float mDefaultValue = 0;
	private float mValue = 0;
	private float mMax = 24.0f;
	private float mMin = 10.0f;
	private String mTitle = null;
	private String mMessage = null;

	public TextSizePreference(Context context) {
		super(context);
		init(context, null);
	}

	public TextSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public TextSizePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		TextSizeDialog dialog = new TextSizeDialog(getContext(), mMin, mMax, mValue);
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
			persistFloat(restoreValue ? getValue() : (Float) defaultValue);
		}

	}

	private float getValue() {

		try {
			if (isPersistent()) {
				mValue = getPersistedFloat(mDefaultValue);
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

			mDefaultValue = attrs.getAttributeFloatValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
			mMax = attrs.getAttributeFloatValue(null, ATTR_MAXVALUE, 100);
			mMin = attrs.getAttributeFloatValue(null, ATTR_MINVALUE, 0);
		}
		mValue = mDefaultValue;
	}

	private class TextSizeDialog extends AlertDialog implements OnClickListener, OnSeekBarChangeListener {

		private SeekBar mSeekBar;
		private TextView mMessageView, mTextSizeView;
		private float mMin = 0;

		public TextSizeDialog(Context context, float min, float max, float defaultValue) {
			super(context);

			init(context, min, max, defaultValue);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case Dialog.BUTTON_POSITIVE:
					mValue = (float) mSeekBar.getProgress() / 10 + mMin;
					if (isPersistent()) {
						persistFloat(mValue);
					}
					if (getOnPreferenceChangeListener() != null) {
						getOnPreferenceChangeListener().onPreferenceChange(TextSizePreference.this, mValue);
					}
					break;
				case Dialog.BUTTON_NEGATIVE:
					break;
			}

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			mTextSizeView.setTextSize((float) progress / 10 + mMin);
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

		private void init(Context context, float min, float max, float defaultValue) {

			getWindow().setFormat(PixelFormat.RGBA_8888);
			float density = context.getResources().getDisplayMetrics().density;

			LinearLayout mContentView = new LinearLayout(context);
			mContentView.setOrientation(LinearLayout.VERTICAL);
			mMin = min;

			mMessageView = new TextView(context);
			mTextSizeView = new TextView(context);
			mSeekBar = new SeekBar(context);

			mContentView.addView(mMessageView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			mContentView.addView(mTextSizeView, LayoutParams.MATCH_PARENT, (int)(60 * density));
			mContentView.addView(mSeekBar, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			int padding = Math.round(density * 16);
			mContentView.setPadding(padding, padding, padding, padding);
			mSeekBar.setMax((int) (10 * (max - min)));
			mSeekBar.setProgress((int) (10 * (defaultValue - min)));
			mSeekBar.setOnSeekBarChangeListener(this);
			
			mMessageView.setVisibility(View.GONE);
			mMessageView.setTextSize(18.0f);
			mMessageView.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));
			
			mTextSizeView.setSingleLine();
			mTextSizeView.setGravity(Gravity.CENTER);
			mTextSizeView.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));
			mTextSizeView.setText("AaBbCc");
			mTextSizeView.setTextSize((float) mSeekBar.getProgress() / 10 + min);

			setView(mContentView);

			setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
			setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		}
	}
}
