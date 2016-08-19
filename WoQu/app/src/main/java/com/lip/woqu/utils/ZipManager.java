package com.lip.woqu.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipManager {
	public static boolean isZipExtractSuccess=true;
	/** 解压文件到指定文件夹 */
	public static void extnativeZipFileList(InputStream in, String filepath)
			throws Exception {
		isZipExtractSuccess=false;
		ZipInputStream zin = new ZipInputStream(in);
		ZipEntry entry;
		// 解压
		File f = new File(filepath);
		f.mkdirs();
		while ((entry = zin.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				File directory = new File(filepath, entry.getName());
				if (!directory.exists())
					if (!directory.mkdirs())
						System.exit(0);
				zin.closeEntry();
			}
			if (!entry.isDirectory()) {
				File myFile = new File(entry.getName());
				FileOutputStream fout = new FileOutputStream(filepath
						+ myFile.getPath());
				BufferedOutputStream dout = new BufferedOutputStream(fout);
				byte[] b = new byte[1024];
				int len = 0;
				while ((len = zin.read(b)) != -1) {
					dout.write(b, 0, len);
				}
				b=null;
				dout.close();
				fout.close();
				zin.closeEntry();
			}
		}
		isZipExtractSuccess=true;
	}

	/** 监测黄历数据是否存在 */
	public static boolean checkFileAlmanacExists() {
		File file = new File(MidData.appDir + "etouch_ecalendar.db");
		if (!file.exists()) {
			return false;
		} else {
			return true;
		}
	}// end checkFileAlmanacExists


    public static boolean checkFileWongTaiSinExists() {
        File fileQian = new File(MidData.appDir + "huangdaxianqian.db");
        if (!fileQian.exists()) {
            return false;
        } else {
            return true;
        }
    }
}
