package com.lip.woqu.bean;

public class UploadNoteBean {
	public int id;
	// 记事的id
	public String noteId = "";
	// 源图片或音频的路径
	public String imagePath = "";
	// 上传类型：1图片或音频2
	public int type;
	// 上传成功的标志：0添加 ，1成功，2 失败
	public int flag;
	// 上传成功的网络地址
	public String neturl = "";
	// 上传成功的size
	public String size = "";
	// 上传成功的mid
	public String mid = "";

}
