package org.mariotaku.preference;

import android.graphics.Paint;
import android.os.Build;
import android.view.View;

class MethodsCompat {

	public void setLayerType(View view, int layerType, Paint paint) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setLayerType(layerType, paint);
		}
	}
}
