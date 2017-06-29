package com.example.opencvtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img;
    private final String[]	CH_list	= {
            "beijing", "cctv1", "cctv3", "cctv6", "diyicaijing", "dongfang",
            "guangdong", "heilongjiang", "hubei", "hunan", "jiangsu",
            "shandong", "shangshixinwen", "shangshiyule", "shenzhen",
            "tianjin", "zhejiang"									};

    private final HashMap<String, String> NameRef	= new HashMap<String, String>();
    private Recognizer	recognizer;
    private  Bitmap srcBitmap , newBitmap ;
    private Mat  newMat ;
    private  Mat rgbMat ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = (ImageView) findViewById(R.id.img);
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hubei1);
        newBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStart:
                recognize10f(srcBitmap);
                break;
            case R.id.btnCropLeft: //截取图标
//                Mat mat = new Mat(srcBitmap.getHeight(), srcBitmap.getWidth(),
//                        CvType.CV_8UC1);
//                // 将Bitmap转化为Mat
//                Bitmap bmp32 = srcBitmap.copy(Bitmap.Config.ARGB_8888, true);
//                Utils.bitmapToMat(bmp32, mat);
//                newMat = recognizer.cropLeft(mat); //获取新的mat
//
//                Utils.matToBitmap(newMat,newBitmap);  // 根据新的mat ,转出新的bitmap
//                img.setImageBitmap(newBitmap);
                break;
            case R.id.btnGray: //灰度
//                grayImageForJava();
                newMat = recognizer.toGray(rgbMat); //获取新的mat
                Utils.matToBitmap(newMat,newBitmap);  // 根据新的mat ,转出新的bitmap
                img.setImageBitmap(newBitmap);
                break;
            case R.id.btnGradient: //计算梯度
                newMat = recognizer.getGradient(rgbMat); //获取新的mat
                Utils.matToBitmap(newMat,newBitmap);  // 根据新的mat ,转出新的bitmap
                img.setImageBitmap(newBitmap);
                break;
            case R.id.btnLoadImage:
                OpenCVLoader.initDebug();
                loadTemplates();
                newMat = new Mat();
                rgbMat = new Mat(srcBitmap.getHeight(), srcBitmap.getWidth(),
                        CvType.CV_8UC1);
                Utils.bitmapToMat(srcBitmap, rgbMat); //将原始图片转成 mat

                img.setImageBitmap(srcBitmap);
//                img.setImageDrawable(getResources().getDrawable(R.drawable.cctv1));
                break;
        }
    }

    // java 层灰度化
    private void grayImageForJava() {
//        OpenCVLoader.initDebug();
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic);
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
        img.setImageBitmap(grayBitmap);
    }

    // jni 层灰度化
    private void grayImage() {
//		Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(
//				R.drawable.ic)).getBitmap();
//		int w = bitmap.getWidth(), h = bitmap.getHeight();
//		int[] pix = new int[w * h];
//		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
//		int [] resultPixes=OpenCVHelper.gray(pix,w,h);
//		Bitmap result = Bitmap.createBitmap(w,h, Bitmap.Config.RGB_565);
//		result.setPixels(resultPixes, 0, w, 0, 0,w, h);
//		img.setImageBitmap(result);
    }





    // 识别帧画面
    private String recognize10f(Bitmap queryBit) {
        Mat mat = new Mat(queryBit.getHeight(), queryBit.getWidth(),
                CvType.CV_8UC1);
        // 将Bitmap转化为Mat
        Bitmap bmp32 = queryBit.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        String result = recognizer.recognize(mat);
        Toast.makeText(this, "recognize10f= "+result, Toast.LENGTH_SHORT).show();
        return result;
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
        NameRef.put("heilongjiang", "黑龙江卫视");
        NameRef.put("hubei", "湖北卫视");
        NameRef.put("hunan", "湖南卫视");
        NameRef.put("jiangsu", "江苏卫视");
        NameRef.put("shandong", "山东卫视");
        NameRef.put("shangshixinwen", "上视新闻");
        NameRef.put("shangshiyule", "上视娱乐");
        NameRef.put("shenzhen", "深圳卫视");
        NameRef.put("tianjin", "天津卫视");
        NameRef.put("zhejiang", "浙江卫视");
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
        Mat img = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), 0);
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
