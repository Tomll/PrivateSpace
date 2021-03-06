package com.transage.privatespace.gallery.photozoom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.transage.privatespace.R;

import java.util.ArrayList;


/**
 * 适配器：将某一个相册中图片，以GridView的形式进行适配
 *
 * @author Tom
 */
public class AlbumGridViewAdapter extends BaseAdapter {

	private final String TAG = getClass().getSimpleName();
    private ArrayList<ImageItem> dataList;
    private Context context;
    private BitmapCache cache;
    //private DisplayMetrics dm;

    public AlbumGridViewAdapter(Context context, ArrayList<ImageItem> dataList) {
		this.context = context;
		this.dataList = dataList;
        cache = new BitmapCache();
        //dm = new DisplayMetrics();
		//((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
	}

	/**
	 * 数据全选、取消全选 的方法
	 *
	 * @param selectAll
	 */
	public void selectAll(boolean selectAll) {
		Bimp.tempSelectBitmap.clear();
		if (selectAll) {
			Bimp.tempSelectBitmap.addAll(dataList);
		}
		notifyDataSetChanged();
	}

    /**
     * 适配器 获取已选数据集 的方法
     * @return
     */
    public ArrayList<ImageItem> getSelectedData(){
        return Bimp.tempSelectBitmap;
    }

    /**
     * 解密完成后，刷新适配器的方法
     */
    public void refreshDataAfterEncrypt(){
        dataList.removeAll(Bimp.tempSelectBitmap);
        notifyDataSetChanged();
        Bimp.tempSelectBitmap.clear();
    }

	public int getCount() {
		return dataList.size();
	}

	public Object getItem(int position) {
		return dataList.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	BitmapCache.ImageCallback callback = new BitmapCache.ImageCallback() {
        @Override
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
			if (imageView != null && bitmap != null) {
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag())) {
					((ImageView) imageView).setImageBitmap(bitmap);
				} else {
					Log.e(TAG, "callback, bmp not match");
				}
			} else {
				Log.e(TAG, "callback, bmp null");
			}
		}
	};

	/**
	 * 存放列表项控件句柄
	 */
	private class ViewHolder {
		public ImageView imageView;
		public CheckBox checkBox;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.item_album_gridview, parent, false);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String path;
		if (dataList != null && dataList.size() > position)
			path = dataList.get(position).imagePath;
		else
			path = "camera_default";
		if (path.contains("camera_default")) {
			viewHolder.imageView.setImageResource(R.color.greytext);
		} else {
			final ImageItem item = dataList.get(position);
			viewHolder.imageView.setTag(item.imagePath);
			cache.displayBmp(viewHolder.imageView, item.thumbnailPath, item.imagePath,callback);
		}
		viewHolder.checkBox.setTag(position);
		viewHolder.checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (viewHolder.checkBox.isChecked()){
                    Bimp.tempSelectBitmap.add(dataList.get(position));
				}else {
                    Bimp.tempSelectBitmap.remove(dataList.get(position));
				}
			}
		});
		//防止滑动的时候由于控件复用而导致数据错乱，所以控件的适配必须有数据源中的内容决定
		if (Bimp.tempSelectBitmap.contains(dataList.get(position))) {
			viewHolder.checkBox.setChecked(true);
		} else {
			viewHolder.checkBox.setChecked(false);
		}
		//点击viewHolder.imageView 进入Gallery进行单张查看
		viewHolder.imageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				context.startActivity(new Intent(context,Gallery.class).putExtra("position",position).putExtra("isFromPrivateAlbum",false));
			}
		});
		return convertView;
	}

/*	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}*/

}
