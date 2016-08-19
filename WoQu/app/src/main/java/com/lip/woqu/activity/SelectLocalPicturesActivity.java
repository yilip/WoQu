package com.lip.woqu.activity;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.utils.MidData;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.view.ETNetworkImageView;
import com.lip.woqu.view.LoadingView;
import com.lip.woqu.view.TuKuImageLoader;
import com.lip.woqu.view.TuKuImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



/**
 * 从本地选择图片 页面
 * */
public class SelectLocalPicturesActivity extends EFragmentActivity implements View.OnClickListener {

    private Button btn_back;
    private TextView tv_title;
    private ImageView iv_arrow;
    private GridView gridView = null;
    private LoadingView loadingView=null;
    private ArrayList<MyImageBean> imageList = new ArrayList<MyImageBean>();
    private ArrayList<ImageFolderBean> folderList = new ArrayList<ImageFolderBean>();
    private HashMap<Integer, ArrayList<MyImageBean>> imageListMap = new HashMap<Integer, ArrayList<MyImageBean>>();
    private MyImageAdapter simpleAdapter = null;
    private ImageFolderAdapter folderAdapter;
    private RelativeLayout layout_root;
    /** 可以选的图片总数 */
    private int canSelectNum = 9;
    /** 已选择的图片列表 */
    private ArrayList<String> selectList = new ArrayList<String>();
    private ArrayList<Integer> orientationList = new ArrayList<Integer>();

    private int imageWidth = 0;
    private boolean loadImageFlag = true;
    private TuKuImageLoader tuKuImageLoader;

    /** 从分类界面跳入只能选择一张图片 */
    private boolean only = false;
    private static final String[] STORE_IMAGES = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION };
    LayoutInflater inflater;
    ContentResolver contentResolver;
    /** 查询含有图片的文件夹的游标 */
    private Cursor cursor;
    /** 被选中的文件夹名称 */
    private String folderName = "";
    /** 被选中的文件夹的位置 */
    private int selectFolder = 0;

    private LinearLayout layout_complete;
    private LinearLayout layout_head_category;
    private ListView lv_category;
    private int category_height = 480;
    private FrameLayout layout_listview;
    private TextView tv_category;
    private TextView tv_sure;
    private String photoUrl="";
    private int imagesNum = 0;
    private boolean isFromTab = false;
    private boolean isNeedShowText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlocalpictures);
        canSelectNum = getIntent().getIntExtra("canSelectPicnums", canSelectNum);
        only = getIntent().getBooleanExtra("only", false);
        imagesNum = getIntent().getIntExtra("imagesNum", 0);
        isFromTab = getIntent().getBooleanExtra("isFromTab",isFromTab);
        isNeedShowText = getIntent().getBooleanExtra("isNeedShowText",isNeedShowText);
        contentResolver = getContentResolver();
        inflater = LayoutInflater.from(this);
        // 判断屏幕分辨率并设置imageLoader缩略图的精度
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        imageWidth = (MidData.main_screenWidth- UtilsManager.dip2px(getApplicationContext(), 24))/3;
        category_height = (int) ((metric.heightPixels - metric.density * 20) * 2 / 3);
        tuKuImageLoader=TuKuImageLoader.getInstance(this.getApplicationContext());
        Init();
    }// end onCreate

    private void Init() {
        layout_root = (RelativeLayout) findViewById(R.id.rl_root);
//        setTheme(layout_root);
        tv_title = (TextView) findViewById(R.id.textView1);
        tv_sure = (TextView) findViewById(R.id.textView_title);
        btn_back = (Button) findViewById(R.id.button_back);
        layout_complete = (LinearLayout) findViewById(R.id.layout_complete);
        layout_head_category = (LinearLayout) findViewById(R.id.layout_head_category);
        layout_head_category.setOnClickListener(this);
        iv_arrow=(ImageView) findViewById(R.id.iv_arrow);
        lv_category = (ListView) findViewById(R.id.listView1);
        lv_category.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                folderName = folderList.get(arg2).name;
                selectFolder = arg2;
                if (folderAdapter != null) {
                    folderAdapter.notifyDataSetChanged();
                }
                if (selectFolder == 0) {
                    tv_category.setText("全部");
                } else {
                    tv_category.setText(folderName);
                }
                getImageListData();
                layout_listview.setVisibility(View.GONE);
                gridView.setEnabled(true);
                iv_arrow.setImageResource(R.drawable.ic_arrow_down);
            }
        });
        layout_listview = (FrameLayout) findViewById(R.id.layout_listview);
        tv_category = (TextView) findViewById(R.id.textView2);
        btn_back.setOnClickListener(this);
        gridView = (GridView) findViewById(R.id.gridView1);
        if (imagesNum > 0) {
            tv_title.setVisibility(View.VISIBLE);
            tv_title.setText((imagesNum ) + "/"+canSelectNum);
            tv_sure.setTextColor(getResources().getColor(R.color.white));
        } else {
            tv_title.setVisibility(View.GONE);
            tv_sure.setTextColor(getResources().getColor(R.color.white));
        }
        loadingView=(LoadingView)findViewById(R.id.loadingView1);

        layout_complete.setOnClickListener(this);
        layout_complete.setEnabled(false);
        layout_listview.setVisibility(View.GONE);
        layout_listview.setFocusable(false);
        layout_listview.setClickable(false);
        layout_listview.setOnClickListener(this);

        InitThreadSelectPic();
    }// end init
    

    /**** 初始化发帖页面选择图库 */
    private void InitThreadSelectPic() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, category_height);
        lv_category.setLayoutParams(params);
        gridView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {
					loadImageFlag = true;
					simpleAdapter.notifyDataSetChanged();
				} else if (scrollState == SCROLL_STATE_FLING) {
					loadImageFlag = false;
				} else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
					loadImageFlag = true;
				}
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            	
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final MyImageBean mb = imageList.get(arg2);
                if (selectFolder == 0 && arg2 == 0) {
                    takePhoto();
                    return;
                }
                if(selectFolder == 0 && arg2 == 1 && isNeedShowText){
                    Intent intent = new Intent(getApplicationContext(), MyPublishActivity.class);
                    startActivity(intent);
                    SelectLocalPicturesActivity.this.close();
                    return;
                }
                if (only) {
                    selectList.add(mb.localPath);
                    orientationList.add(mb.orientation);
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("pictures", selectList);
                    intent.putIntegerArrayListExtra("orientation", orientationList);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    mb.isSelect = !mb.isSelect;
                    simpleAdapter.notifyDataSetChanged();
                    if (mb.isSelect) {// 当前以选中
                        if (canSelectNum > (selectList.size()+imagesNum) ) {
                            selectList.add(mb.localPath);
                            orientationList.add(mb.orientation);
                        } else {
                            mb.isSelect = !mb.isSelect;
                            UtilsManager.Toast(getApplicationContext(), "图片已选满");
                        }
                    } else {// 取消选中
                        for (int i = 0; i < selectList.size(); i++) {
                            if (selectList.get(i).equals(mb.localPath)) {
                                selectList.remove(i);
                                orientationList.remove(i);
                                break;
                            }
                        }
                    }
                    if (selectList.size() != 0) {
                        layout_complete.setEnabled(true);
                        tv_title.setVisibility(View.VISIBLE);
                        tv_title.setText((selectList.size()+imagesNum ) + "/"+canSelectNum);
                        tv_sure.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        layout_complete.setEnabled(false);
                        if(imagesNum>0){
                            tv_title.setVisibility(View.VISIBLE);
                            tv_title.setText((selectList.size()+imagesNum ) + "/"+canSelectNum);
                        }else {
                            tv_title.setVisibility(View.GONE);
                        }
                        tv_sure.setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }
        });
        getImageFolderData();
    }
    /** 相机拍照 */
    private void takePhoto() {
        try{
            File file = new File(MidData.notebookPicturePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            photoUrl = MidData.notebookPicturePath +new Date().getTime()+ ".jpg";
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (!file.exists()){
                photoUrl = MidData.tempDir +new Date().getTime()+ ".jpg";
                File file1 = new File(MidData.tempDir);
                if (!file1.exists()){
                    handler.sendEmptyMessage(5);
                    return;
                }
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoUrl)));
            startActivityForResult(cameraIntent, 0);
        }catch(ActivityNotFoundException e){
            e.printStackTrace();
            handler.sendEmptyMessage(5);
        }
    }
    /** 取消 */
    private void cancelPicture() {
        if (photoUrl != null && !photoUrl.equals("")) {
            if (photoUrl.contains(MidData.notebookPicturePath)) {
                File file = new File(photoUrl);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        handler.sendEmptyMessage(22);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
			if (requestCode == 100) {
                setResult(RESULT_OK);
                finish();
            }else if(requestCode==0){
				selectList.clear();
				selectList.add(photoUrl);
				orientationList.clear();
				orientationList.add(getExifOrientation(photoUrl));
                if (isFromTab){
//                    Intent intent=new Intent(SelectLocalPicturesActivity.this,LifePublishActivity.class);
//                    intent.putStringArrayListExtra("pictures", selectList);
//                    intent.putIntegerArrayListExtra("orientation", orientationList);
//                    startActivity(intent);
                }else {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("pictures", selectList);
                    intent.putIntegerArrayListExtra("orientation", orientationList);
                    setResult(RESULT_OK, intent);
                }
				finish();
            }
        } else {
            cancelPicture();
        }

    }
    /**用于返回jpg的旋转角度信息*/
    public int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    @Override
    public void onClick(View v) {
        if (v == btn_back) {
			if (layout_listview.getVisibility() == View.VISIBLE) {
				layout_listview.setVisibility(View.GONE);
				gridView.setEnabled(true);
			} else {
				setResult(RESULT_CANCELED);
				close();
			}
        } else if (v == layout_complete) {
            if (selectList.size() > 0) {
                if (isFromTab){
                    Intent intent=new Intent(SelectLocalPicturesActivity.this,MyPublishActivity.class);
                    intent.putStringArrayListExtra("pictures", selectList);
                    intent.putIntegerArrayListExtra("orientation", orientationList);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("pictures", selectList);
                    intent.putIntegerArrayListExtra("orientation", orientationList);
                    setResult(RESULT_OK, intent);
                }
            }
            close();
        } else if (v == layout_head_category||v==layout_listview) {
            if (layout_listview.getVisibility() == View.VISIBLE) {
                gridView.setEnabled(true);
                layout_listview.setVisibility(View.GONE);
                iv_arrow.setImageResource(R.drawable.ic_arrow_down);
            } else {
                layout_listview.setVisibility(View.VISIBLE);
                gridView.setEnabled(false);
                iv_arrow.setImageResource(R.drawable.ic_arrow_up);
            }
        }
    }
    
    /**
     * 获取外存储中含有图片的文件夹列表
     */
    private void getImageFolderData() {
        folderList.clear();
        new Thread(new Runnable() {

            @Override
            public void run() {
                ContentResolver resolver = getContentResolver();
                ImageFolderBean firstBean = new ImageFolderBean();
                firstBean.name = "全部图片";
                folderList.add(firstBean);
                try {
                    cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                            "COUNT(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ")" }, "0==0) GROUP BY (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            ImageFolderBean bean = new ImageFolderBean();
                            bean.name = cursor.getString(0);
                            bean.count = cursor.getInt(1);
                            getFolderCover(bean);
                            folderList.add(bean);
                        }
                        handler.sendEmptyMessage(6);
                        handler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {

                } finally {
                    if (cursor != null) {
                        cursor.close();
                        cursor = null;
                    }
                }
            }
        }).start();
    }

    /**
     * 获取文件夹的封面照片
     */
    private void getFolderCover(ImageFolderBean bean) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION },
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[] { bean.name }, MediaStore.Images.Media.DATE_ADDED + " DESC");
        if (cursor != null) {
            cursor.moveToFirst();
            bean.path = cursor.getString(0);
            bean.id = cursor.getLong(1);
            bean.orientation = cursor.getInt(2);
            cursor.close();
            cursor = null;
        }
    }

    @Override
    protected void prepareDestroy() {
        super.prepareDestroy();
        tuKuImageLoader.clearCache();
        tuKuImageLoader.shutDown();
    }
    @Override
    protected void onDestroy() {
        if (imageListMap != null) {
            imageListMap.clear();
        }
        System.gc();
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
			if (layout_listview.getVisibility() == View.VISIBLE) {
				layout_listview.setVisibility(View.GONE);
				gridView.setEnabled(true);
			} else {
				setResult(RESULT_CANCELED);
				close();
			}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** 获取文件夹内的图片列表 category=-1为获取所有图片 */
    private void getImageListData() {
        new Thread() {
            public void run() {
                handler.sendEmptyMessage(14);
                if (selectFolder == 0) {//获取全部图片
                    ArrayList<MyImageBean> tempList = new ArrayList<MyImageBean>();
                    MyImageBean camera = new MyImageBean();
                    camera.isSelect = false;
                    tempList.add(camera);
                    if (isNeedShowText){
                        MyImageBean text = new MyImageBean();
                        text.isSelect = false;
                        tempList.add(text);
                    }

                    ArrayList<MyImageBean> list = imageListMap.get(selectFolder);
                    if (list != null) {
                        tempList.addAll(list);
                    } else {
                        ImageFolderBean firstBean = folderList.get(0);
                        Cursor arg1 = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES, null,
                                null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                        list = new ArrayList<MyImageBean>();
                        int i=0;
                        if (arg1 != null && arg1.moveToFirst()) {
                            do {
                                MyImageBean mb = new MyImageBean();
                                mb.localPath = arg1.getString(0);
                                mb.id = arg1.getLong(4);
                                mb.orientation = arg1.getInt(5);
                                File file=new File(mb.localPath);
                                if(file.exists()){
                                    list.add(mb);
                                }
                                if (i==0&& TextUtils.isEmpty(firstBean.path)) {
                                    firstBean.path = mb.localPath;
                                }
                                i++;
                            } while (arg1.moveToNext());
                        }
                        tempList.addAll(list);
                        if (arg1 != null) {
                            arg1.close();
                        }
                        imageListMap.put(selectFolder, list);
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = tempList;
                    msg.what = 15;
                    handler.sendMessage(msg);
                } else {//获取某个指定文件夹的图片
                    ArrayList<MyImageBean> list = imageListMap.get(selectFolder);
                    if (list == null) {
                        Cursor arg1 = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " LIKE ?",
                                new String[] { folderName }, MediaStore.Images.Media.DATE_ADDED + " DESC");
                        list = new ArrayList<MyImageBean>();
                        if (arg1 != null && arg1.moveToFirst()) {
                            do {
                                MyImageBean mb = new MyImageBean();
                                mb.localPath = arg1.getString(0);
                                mb.id = arg1.getLong(4);
                                mb.orientation = arg1.getInt(5);
                                list.add(mb);
                            } while (arg1.moveToNext());
                        }
                        if (arg1 != null) {
                            arg1.close();
                        }
                        imageListMap.put(selectFolder, list);
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = list;
                    msg.what = 15;
                    handler.sendMessage(msg);
                }
            }

        }.start();
    }

    private class MyImageAdapter extends BaseAdapter {
        private Holder holder = null;
        private ArrayList<MyImageBean> imageList = new ArrayList<MyImageBean>();

        public void setImageList(ArrayList<MyImageBean> imageList) {
            this.imageList = imageList;
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.select_pictures_from_device_activity_item, null);
                holder = new Holder();
                holder.imageView = (TuKuImageView) convertView.findViewById(R.id.imageView1);
                holder.imageView.setCustomBitmapWidth(imageWidth);
                holder.ll_select = (LinearLayout) convertView.findViewById(R.id.linearLayout_select);
                holder.iv_select = (ImageView) convertView.findViewById(R.id.imageView2);
                holder.linearLayout_root = (LinearLayout) convertView.findViewById(R.id.linearLayout_root);
                RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)holder.linearLayout_root.getLayoutParams();
                params.height=imageWidth;
                holder.linearLayout_root.setLayoutParams(params);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            final MyImageBean mb = imageList.get(position);
            if (selectFolder == 0 && position == 0) {
                holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.imageView.setImageResource(R.drawable.ic_publish_photo);
                holder.ll_select.setVisibility(View.GONE);
            }else if (selectFolder == 0 && position == 1 && isNeedShowText){
                holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.imageView.setImageResource(R.drawable.ic_publish_word);
                holder.ll_select.setVisibility(View.GONE);
            } else {
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                tuKuImageLoader.displayImage(holder.imageView, mb.localPath, R.drawable.blank, mb.id, !loadImageFlag);
                holder.iv_select.setImageResource(mb.isSelect?R.drawable.btn_ic_done:R.drawable.ic_publish_default);
                holder.ll_select.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
    class Holder {
        TuKuImageView imageView;
        ImageView iv_select;
        LinearLayout ll_select;
        LinearLayout linearLayout_root;
    }

    /**
     * 文件夹选择界面适配器
     *
     * @author zbl
     *
     */
    private class ImageFolderAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return folderList.size();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FolderItemHolder holder = null;
            if (convertView == null) {
                View view = inflater.inflate(R.layout.folder_select_item, null);
                holder = new FolderItemHolder();
                holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
                holder.tv_count = (TextView) view.findViewById(R.id.tv_count);
                holder.imageView = (ETNetworkImageView) view.findViewById(R.id.imageView1);
                convertView = view;
                convertView.setTag(holder);
            } else {
                holder = (FolderItemHolder) convertView.getTag();
            }
            ImageFolderBean bean = folderList.get(position);
            holder.tv_name.setText(bean.name);
            if (position == 0) {
                holder.tv_count.setVisibility(View.INVISIBLE);
            } else {
                holder.tv_count.setVisibility(View.VISIBLE);
                holder.tv_count.setText("("+bean.count + "张)");
            }
            holder.imageView.setImageUrl(bean.path, -1);
            return convertView;
        }
    }
    class FolderItemHolder {
        TextView tv_name;
        TextView tv_count;
        ETNetworkImageView imageView;
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (folderAdapter == null) {
                        folderAdapter = new ImageFolderAdapter();
                        lv_category.setAdapter(folderAdapter);
                    } else {
                        folderAdapter.notifyDataSetChanged();
                    }
                    break;
                case 3:
                    if (folderAdapter != null) {
                        folderAdapter.notifyDataSetChanged();
                    }
                    break;
                case 5:
                    UtilsManager.Toast(SelectLocalPicturesActivity.this, getResources().getString(R.string.not_found_capture));
                    break;
                case 6:
                    getImageListData();
                    break;
                case 14:
                    loadingView.setVisibility(View.VISIBLE);
                    break;
                case 15:
                    loadingView.setVisibility(View.GONE);
                    imageList = (ArrayList<MyImageBean>) msg.obj;
                    if (simpleAdapter == null) {
                        simpleAdapter = new MyImageAdapter();
                        simpleAdapter.setImageList(imageList);
                        gridView.setAdapter(simpleAdapter);
                    } else {
                        simpleAdapter.setImageList(imageList);
                        simpleAdapter.notifyDataSetChanged();
                    }
                    break;
                case 22:
                    setResult(RESULT_CANCELED);
                    break;
            }
        }
    };

    class MyImageBean {
        String localPath = "";
        int orientation = 0;
        long id;
        boolean isSelect = false;
    }

    class ImageFolderBean {
        String name = "";
        int count;
        String path;
        long id;
        int orientation;
    }

}
