package com.example.opencvtest;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by Lyndon.Li on 2017/7/18.
 */

public class ScreenDiscernService extends Service {
    private final  String TAG = "ScreenDiscernService";

    private volatile static Handler mHandler;
    private volatile static Runnable mRunable;
    private int imageNum = 0;
    private Bitmap	query = null;
    private final String[]	CH_list	= {
            "beijing", "cctv1", "cctv3", "cctv6", "diyicaijing", "dongfang",
            "guangdong","guangdongtb", "heilongjiang", "hubei","hubei0","hubeitb", "hunan", "jiangsu",
            "shandong", "shangshixinwen", "shangshiyule", "shenzhen","shenzhentb",
            "tianjin","tianjintb", "zhejiang"									};

    private final HashMap<String, String> NameRef	= new HashMap<String, String>();
    private Recognizer	recognizer;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 加在OpenCV库
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCVLoader", "Opencv not loaded.");
        }
    }


    // 加载样本
    public void loadTemplates() {
        Mat[] CH_templates = new Mat[CH_list.length];
        for (int i = 0; i < CH_list.length; i++) {
            CH_templates[i] = loadImg(CH_list[i]);
        }
        recognizer = Recognizer.create(CH_list, CH_templates);
        NameRef.put("beijing", "北京卫视");
        NameRef.put("cctv1", "CCTV-1 综合");
        NameRef.put("cctv3", "CCTV-3 综艺");
        NameRef.put("cctv6", "CCTV-6 电影");
        NameRef.put("cctv8", "CCTV-8 电视剧");
        NameRef.put("diyicaijing", "第一财经");
        NameRef.put("dongfang", "东方卫视");
        NameRef.put("guangdong", "广东卫视");
        NameRef.put("guangdongtb", "广东卫视tb");
        NameRef.put("heilongjiang", "黑龙江卫视");
        NameRef.put("hubei", "湖北卫视");
        NameRef.put("hubei0", "湖北卫视0");
        NameRef.put("hubeitb", "湖北卫视台标");
        NameRef.put("hunan", "湖南卫视");
        NameRef.put("jiangsu", "江苏卫视");
        NameRef.put("shandong", "山东卫视");
        NameRef.put("shangshixinwen", "上视新闻");
        NameRef.put("shangshiyule", "上视娱乐");
        NameRef.put("shenzhen", "深圳卫视");
        NameRef.put("shenzhentb", "深圳卫视tb");
        NameRef.put("tianjin", "天津卫视");
        NameRef.put("tianjintb", "天津卫视tb");
        NameRef.put("zhejiang", "浙江卫视");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        loadTemplates();
        startDiscern();
//        initHandler();
    }

    private void startDiscern(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 主循环，截屏识别
                while (true){
                    try {
                        // 执行截屏
                        startCapture();
                        Log.i(TAG, "startCapture over");
//                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.hubei2);
//                        query = compressImage(bitmap);
//                        query = readBitMap(MainActivity.srcId,"");
                        if (query != null) {
                            String result = recognize10f(query);
                            Log.i(TAG, "Image recognize10f result = "+result);
                        }


                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "startDiscern "+e.getMessage());
                    }
                }

            }
        }).start();
    }

    private void startCapture() {
        try {
            imageNum++;
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "tvlogo");
            if (!file.exists()) {
                file.mkdirs();
            }
//            String fname = imageNum+"screenshot.jpg";//imageNum+
            String fname = "screenshot.jpg";
//			fname = file.getAbsolutePath() + "/"+fname;
            // /storage/emulated/0//storage/emulated/0/tvlogo/48screenshot.png

//			String cmd="screencap -p /sdcard/"+fname+".png";
            String cmd="screencap -p /sdcard/tvlogo/"+fname;

            // 权限设置
            Process p = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            // 将命令写入
            dataOutputStream.writeBytes(cmd);
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            outputStream.close();

            Thread.sleep(10000);

            String path = Environment.getExternalStorageDirectory().getPath()+"/tvlogo/"+fname;
            Log.e(TAG, "startCapture: img_path= "+path );
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            if (bitmap != null) {
            query = readBitMap(0,path);
//					saveImage(bitmap);
            Log.i(TAG, "Screen image added to queue.");
//            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     * 以最省内存的方式读取本地资源的图片
     */
    public  Bitmap readBitMap( int resId,String path){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = null;
        if(resId != 0){
            is = getApplicationContext().getResources().openRawResource(resId);
        }
        if(!path.equals("")){
            try {
                is = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return BitmapFactory.decodeStream(is,null,opt);
    }

    // 识别帧画面
    private String recognize10f(Bitmap queryBit) {
        Mat mat = new Mat(queryBit.getHeight(), queryBit.getWidth(),
                CvType.CV_8UC1);  // CV_8U 8位无符号整数
        // 将Bitmap转化为Mat
        Bitmap bmp32 = queryBit.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        String result = recognizer.recognize(mat);
//        Toast.makeText(this, "recognize10f= "+result, Toast.LENGTH_SHORT).show();
        return result;
    }

    // 加在图像 ,从raw文件中读取图片，并转成 Mat
    public Mat loadImg(String fname) {
        InputStream is = getResources().openRawResource(
                getResources().getIdentifier(fname, "raw", getPackageName()));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] temporaryImageInMemory = buffer.toByteArray();
        try {
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat img = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), 0); // flag 0
        data = null;
        buffer = null;
        is = null;
        temporaryImageInMemory = null;
        return img;
    }



    // 质量压缩
    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            if(options>10){
                options -= 10;//每次都减少10
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


}
