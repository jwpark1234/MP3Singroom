package com.project.mp3singroom;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class List_utils {
	private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

	public static Bitmap getBitmapImage(Context mContext, int id, int w, int h) {
		ContentResolver res = mContext.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, id);
		if (uri != null) {
			ParcelFileDescriptor fd = null;
			try {
				fd = res.openFileDescriptor(uri, "r");
				int sampleSize = 1;
     
				sBitmapOptionsCache.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, sBitmapOptionsCache);
				int nextWidth = sBitmapOptionsCache.outWidth >> 1;
				int nextHeight = sBitmapOptionsCache.outHeight >> 1;
				while (nextWidth>w && nextHeight>h) {
					sampleSize <<= 1;
					nextWidth >>= 1;
					nextHeight >>= 1;
				}
				sBitmapOptionsCache.inSampleSize = sampleSize;
				sBitmapOptionsCache.inJustDecodeBounds = false;
				Bitmap b = BitmapFactory.decodeFileDescriptor(
						fd.getFileDescriptor(), null, sBitmapOptionsCache);
				if (b != null) {
					if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
						Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
						b.recycle();
						b = tmp;
					}
				}
				return b;
			} catch (FileNotFoundException e) { return null;
			} catch (Exception e) { return null;
			} finally {
				try {
					if (fd != null)
						fd.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
}
