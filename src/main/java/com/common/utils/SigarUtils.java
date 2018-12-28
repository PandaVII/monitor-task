package com.common.utils;

import java.io.File;

import org.hyperic.sigar.Sigar;

public class SigarUtils {
    //public final static Sigar sigar = initSigar();
    public static Sigar initSigar() {
        try {
            //此处只为得到依赖库文件的目录，可根据实际项目自定义
            //String file = Paths.get(PathKit.getWebRootPath(),  "files", "sigar",".sigar_shellrc").toString();
            //File classPath = new File(file).getParentFile();
            //String filePath = SigarUtils.class.getClassLoader().getResource("").toURI().getPath();
            //System.out.println(filePath);
            File classPath = new File("lib");//这里我直接把文件路径写死了

            String path = System.getProperty("java.library.path");
            String sigarLibPath = classPath.getCanonicalPath();
            //为防止java.library.path重复加，此处判断了一下
            if (!path.contains(sigarLibPath)) {
                if (isOSWin()) {
                    path += ";" + sigarLibPath;
                } else {
                    path += ":" + sigarLibPath;
                }
                System.setProperty("java.library.path", path);
            }
            return new Sigar();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isOSWin(){//OS 版本判断
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            return true;
        } else return false;
    }
    
    public static void main(String[] args) {
    	SigarUtils.initSigar();
	}
}
