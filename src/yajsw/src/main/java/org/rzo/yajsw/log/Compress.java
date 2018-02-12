package org.rzo.yajsw.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compress {

	public static void compress(List<String> files, String zipFile) {
		if (files == null || files.size() == 0)
			return;

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String file : files) {
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);
				FileInputStream in = new FileInputStream(file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
				zos.closeEntry();
				new File(file).delete();

			}

			zos.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void compress(String file, String zipFile) {
		if (file == null || !new File(file).exists())
			return;

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry(new File(file).getName());
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(file);

			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}

			in.close();
			zos.closeEntry();
			zos.flush();

			zos.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
