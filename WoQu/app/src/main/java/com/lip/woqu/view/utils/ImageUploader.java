package com.lip.woqu.view.utils;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.lip.woqu.bean.UploadNoteBean;
import com.lip.woqu.utils.CompressPicture;
import com.lip.woqu.utils.DBManager;
import com.lip.woqu.utils.FileManager;
import com.lip.woqu.utils.MidData;
import com.lip.woqu.utils.SynPreferences;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.NetCustomMultiPartEntity;
import com.lip.woqu.utils.net.NetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Hashtable;



/**图片上传类*/
public class ImageUploader {

    private Context ctx=null;
    private int mScreenWidth=0;
    private JSONObject imgSignature=null;
    private JSONObject voiceSignature=null;
    private JSONObject fileSignature=null;
    private NetCustomMultiPartEntity.ProgressListener progressListener;

    public ImageUploader(Context c){
        this.ctx=c;
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        mScreenWidth=dm.widthPixels;
    }
    public void setProgressListener(NetCustomMultiPartEntity.ProgressListener listener){
        this.progressListener=listener;
    }

    /**上传一个图片(方法内已做判断，如果上传过则直接取值返回)，json中值为status,url,media_id*/
    public JSONObject doUploadOneImage(String imagePath) throws JSONException, IOException {
        JSONObject uploadResult=getMediaUploadedInfo(imagePath);
        if(uploadResult!=null){//上传过
            uploadResult.put("status", "1000");
            return uploadResult;
        }
        SynPreferences synPre=SynPreferences.getInstance(ctx);
        JSONObject finalResult=new JSONObject();
        String result_str="";
        if(!TextUtils.isEmpty(synPre.getUID())){//已登录
            boolean isNeedGetSign=true;
            try{
                if(imgSignature!=null&&imgSignature.has("expire_at")){
                    if(imgSignature.getLong("expire_at")> System.currentTimeMillis()/1000){
                        isNeedGetSign=false;
                    }
                }
            }catch(Exception e){
                isNeedGetSign=true;
            }
            if(isNeedGetSign){
                JSONObject signature=getUploadFileSignature(SynPreferences.getInstance(ctx), 1, 1,1);
                imgSignature=signature.has("imagesignature")?signature.getJSONObject("imagesignature"):null;
                voiceSignature=signature.has("voicesignature")?signature.getJSONObject("voicesignature"):null;
                fileSignature=signature.has("filesignature")?signature.getJSONObject("filesignature"):null;
            }
            String path = "";
            String policy = "";
            String signature = "";
            if (imgSignature != null){
                if (imgSignature.has("path")){
                    path = imgSignature.getString("path");
                }
                if (imgSignature.has("policy")){
                    policy = imgSignature.getString("policy");
                }
                if (imgSignature.has("signature")){
                    signature = imgSignature.getString("signature");
                }
            }
            result_str= NetManager.getInstance().updateFileToUpYun(path,policy,signature,imagePath,progressListener);
            JSONObject result=new JSONObject(result_str);
            if(result.optInt("status",0)==1000){
                JSONObject data = result.optJSONObject("data");
                String neturl = data.optString("url","");
                String mid = data.optString("media_id","");
                finalResult.put("status", "1000");
                finalResult.put("url", neturl);
                finalResult.put("media_id", mid);
                inserOneDataToUploadInfo(imagePath,neturl,new File(imagePath).length()+"",mid);
                uploadImageSuccess(imagePath, neturl);//重命名图片文件
            }else{
                finalResult.put("status", "1002");
            }
        }else{//未登录上传图片
            result_str=NetManager.getInstance().updateFileToUpYun(imagePath,progressListener);
            JSONObject result=new JSONObject(result_str);
            if(result.optInt("status",0)==1000){
                JSONObject data = result.optJSONObject("data");
                String neturl = data.optString("url","");
                String mid = "";
                finalResult.put("status", "1000");
                finalResult.put("url", neturl);
                finalResult.put("media_id", mid);
                inserOneDataToUploadInfo(imagePath,neturl,new File(imagePath).length()+"",mid);
                uploadImageSuccess(imagePath, neturl);//重命名图片文件
            }else{
                finalResult.put("status", "1002");
            }
        }
        UtilsManager.println(result_str);
        return finalResult;
    }

    /**取得上传文件的签名
     * @throws org.json.JSONException */
    private JSONObject getUploadFileSignature(SynPreferences pre,int imageNum,int voiceNum,int fileNum) throws JSONException {
        JSONObject signature=new JSONObject();
        Hashtable<String, String> table=new Hashtable<String, String>();
//        table.put("app_key", SysParams.appkey);
//        table.put("uid", pre.getUID());
//        table.put("acctk", pre.getAcctk());
//        table.put("up",SysParams.UserPlatform.android);
//        table.put("device", pre.getloginDeviceNumber());
//        table.put("image_req",imageNum>0?"1":"0");
//        table.put("audio_req", voiceNum>0?"1":"0");
//        table.put("file_req", fileNum>0?"1":"0");
//        String result= NetManager.getInstance().doGetAsString(SysParams.getUploadFileSignature, table);
        JSONObject rootObj=new JSONObject("");
        if(rootObj.getInt("status")==1000){
            JSONObject data=rootObj.getJSONObject("data");
            if(data.has("IMAGE")){
                signature.put("imagesignature", data.getJSONObject("IMAGE"));
            }
            if(data.has("VOICE")){
                signature.put("voicesignature", data.getJSONObject("VOICE"));
            }
            if(data.has("FILE")){
                signature.put("filesignature", data.getJSONObject("FILE"));
            }
        }
        //UtilsManager.println(result);
        return signature;
    }


    /**
     * media上传成功插入该条记录到数据库中
     * @param path
     * @param neturl
     * @param size
     * @param mid
     */
    private void inserOneDataToUploadInfo(String path, String neturl, String size, String mid) {
        DBManager db = DBManager.open(ctx);
        UploadNoteBean upload = new UploadNoteBean();
        upload.flag = 1;
        upload.imagePath = path;
        upload.neturl = neturl;
        upload.size = size;
        upload.mid = mid;
        db.inserOneDataToUploadImage(upload);
    }
    /**判断图片是否已经上传过，如果是则返回上传结果json(url和media_id)，如果没有则返回null*/
    private JSONObject getMediaUploadedInfo(String path) {
        DBManager db = DBManager.open(ctx);
        Cursor cur = db.getOneDataFromUploadImage(path);
        if (cur != null) {
            if (cur.getCount() > 0 && cur.moveToFirst()) {
                int flag = cur.getInt(cur.getColumnIndex(DBManager.UploadImage.KEY_flag));
                String neturl = cur.getString(cur.getColumnIndex(DBManager.UploadImage.KEY_neturl));
                String mid = cur.getString(cur.getColumnIndex(DBManager.UploadImage.KEY_mid));
                if (flag == 1) {// 下载成功 返回neturl
                    JSONObject obj=new JSONObject();
                    try {
                        obj.put("url", neturl);
                        obj.put("media_id", mid);
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                    cur.close();
                    return obj;
                }
            }
            cur.close();
        }
        return null;
    }
    /** 图片上传成功之后修改本地图片的名字 */
    private void uploadImageSuccess(String path, String neturl) {
        try{
            String temp = path;
            if (temp.contains("?")) {
                temp = temp.substring(0, temp.indexOf("?"));
            }
            temp = URLDecoder.decode(temp, "utf-8");
            final String name = temp.substring(temp.lastIndexOf("/") + 1);

            StringBuffer sb = new StringBuffer();
            sb.append("!w").append(UtilsManager.getImageWidth(mScreenWidth)).append(".jpg");
            File file = new File(MidData.notebookPicturePath + name + sb.toString());
            if (file.exists()) {//重命名屏幕尺寸的图
                FileManager.renameFile(MidData.notebookPicturePath + name + sb.toString(), neturl + sb.toString());
            } else {
                new CompressPicture().copyImageWithWidth(path, neturl, mScreenWidth);
            }
            //拷贝一个屏幕尺寸1/3的图
            new CompressPicture().copyImageWithWidth(path, neturl, mScreenWidth/3);
            //重命名原图
            file=new File(MidData.notebookPicturePath + name);
            if(file.exists()){
                FileManager.renameFile(path, neturl);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
