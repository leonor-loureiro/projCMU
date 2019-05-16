package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import pt.ulisboa.tecnico.cmov.p2photo.R;


import java.util.List;

public class PhotoAdapter extends BaseAdapter {

    private Context mContext;
    private List<Photo> photos;

    public PhotoAdapter(Context mContext, List<Photo> photos) {
        this.mContext = mContext;
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int i) {
        return photos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void addPhoto(Photo photo){
        photos.add(photo);
        notifyDataSetChanged();

    }

    public void clear(){
        photos.clear();
    }

    public void addAllPhotos(List<Photo> allPhotos){
        photos.addAll(allPhotos);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Photo photo = (Photo) getItem(i);
        Log.i("My Photo", photo.getUrl());

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.photo_item, null);
        }

        final ImageView imageView = convertView.findViewById(R.id.image);
        imageView.setImageBitmap(photo.getBitmap());
        //imageView.setImageResource(android.R.drawable.ic_menu_gallery);

        return convertView;
    }
}
