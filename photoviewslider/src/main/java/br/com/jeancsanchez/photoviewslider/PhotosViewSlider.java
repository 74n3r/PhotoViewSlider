package br.com.jeancsanchez.photoviewslider;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PhotosViewSlider extends LinearLayout implements View.OnClickListener, View.OnTouchListener {
    private TextView txtPhotoGalleryTitle;
    private TextView txtPhotoGalleryDescription;
    private PhotosViewAdapter photoAdapter;
    private RecyclerView recyclerView;
    private int currentPosition = 0;
    private float dx, dy, ux, uy;
    private final int MIN_DISTANCE = 100;
    private Dialog builder;
    private View viewDialog;
    private ImageButton btnShare;
    private TextView txtPhotosTotal;
    private TextView txtCurrentPosition;
    private TextView txtDescriptionGallery;
    private ImageView imgPhoto;
    private List<Photo> photos;
    private int gridColumns = 2;


    public PhotosViewSlider(Context context){
        super(context);
        init();
    }

    public PhotosViewSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotosViewSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(){
        inflate(getContext(), R.layout.photos_view, this);
        recyclerView = (RecyclerView) findViewById(R.id.photosList);
    }

    /**
     *
     * @param photos List of photos that will to be show on grid.
     * @param gridColumns Number of columns that photos grid will have.
     */
    public void initializePhotos(List<Photo> photos, int gridColumns) {
        this.photos = photos;
        this.gridColumns = gridColumns;
        adapterSetup();
    }

    private void adapterSetup() {
        photoAdapter = new PhotosViewAdapter(getContext());
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(gridColumns, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoAdapter);
        photoAdapter.setupItems(photos);
    }

    private void generatePhotoDetail() {
        viewDialog = inflate(getContext(), R.layout.photo_detail, null);
        imgPhoto = (ImageView) viewDialog.findViewById(R.id.img_photo_gallery_detail);
        txtDescriptionGallery = (TextView) viewDialog.findViewById(R.id.txt_photo_gallery_description);
        txtCurrentPosition = (TextView) viewDialog.findViewById(R.id.txt_photo_current_position);
        txtPhotosTotal = (TextView) viewDialog.findViewById(R.id.txt_photo_total);
        btnShare = (ImageButton) viewDialog.findViewById(R.id.btn_share);

        builder = new Dialog(getContext());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    private void photoClicked(Photo photo) {
        generatePhotoDetail();

        for (int i = 0; i < photos.size(); i++)
            if (photos.get(i).getImage().equals(photo.getImage()))
                currentPosition = i;

        showImage(photo.getImage(), photo.getDescription(), currentPosition);
    }


    private void showImage(String url, String description, int currentPosition) {
        txtDescriptionGallery.setText(description);
        txtCurrentPosition.setText(String.valueOf(currentPosition+1));
        txtPhotosTotal.setText(String.valueOf(photos.size()));
        Picasso.with(getContext()).load(url).fit()
                .placeholder(R.drawable.photodefault)
                .into(imgPhoto);

        btnShare.setOnClickListener(this);
        viewDialog.setOnTouchListener(this);

        builder.setContentView(viewDialog);
        builder.show();
    }


    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btn_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            getContext().startActivity(Intent.createChooser(intent, "Share"));
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN){
                dx = event.getX();
                dy = event.getY();
                return true;

        }else if (action == MotionEvent.ACTION_UP) {
            ux = event.getX();
            uy = event.getY();

            float deltaX = dx - ux;
            float deltaY = dy - uy;

            if (Math.abs(deltaX) > Math.abs(deltaY))
                if (Math.abs(deltaX) > MIN_DISTANCE)
                        // Before
                    if (deltaX < 0) {
                        if(currentPosition == 0)
                            currentPosition = photos.size() -1;
                        else
                            currentPosition --;

                        Photo photo = photos.get(currentPosition);
                        showImage(photo.getImage(), photo.getDescription(), currentPosition);
                        return true;
                    }

                    // Next
                    if (deltaX > 0) {
                        if(currentPosition == photos.size()-1)
                            currentPosition = 0;
                        else
                            currentPosition ++;

                        Photo photo = photos.get(currentPosition);
                        showImage(photo.getImage(), photo.getDescription(), currentPosition);
                        return true;
                    }
            }
        return false;
    }


    private class PhotosViewAdapter extends RecyclerView.Adapter<PhotosViewAdapter.PhotosAdapterViewHolder> implements View.OnClickListener {
        private Context context;
        private List<Photo> photoList;

        public PhotosViewAdapter(Context context) {
            this.context = context;
        }


        @Override
        public PhotosAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
            PhotosAdapterViewHolder photosViewHolder = new PhotosAdapterViewHolder(view);
            photosViewHolder.itemView.setOnClickListener(this);
            return photosViewHolder;
        }

        @Override
        public void onBindViewHolder(final PhotosAdapterViewHolder holder, int position) {
            holder.itemView.setTag(photoList.get(position));
            Picasso.with(context).load(photoList.get(position).getImage()).fit().into(holder.photo);
        }

        @Override
        public int getItemCount() {
            return photoList == null ? 0 : photoList.size();
        }

        public void setupItems(List<Photo> photoList) {
            this.photoList = photoList;
            notifyDataSetChanged();
        }

        @Override
        public void onClick(View view) {
            Photo photo = (Photo) view.getTag();
            photoClicked(photo);
        }

        class PhotosAdapterViewHolder extends RecyclerView.ViewHolder{
            private ImageView photo;

            public PhotosAdapterViewHolder(View view){
                super(view);
                this.photo = (ImageView) view.findViewById(R.id.photo_item);
            }
        }
    }
}