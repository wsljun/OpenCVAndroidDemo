package com.example.opencvtest;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Recognizer {
	private final String				TAG			= "Recognizer";
	private static Recognizer			recognizer	= null;

	private String[]					TV_list;
	private Mat[]						TV_templates;

	public Recognizer (){

	}
	private Recognizer(String[] TV_l, Mat[] TV_t) {
		initiate(TV_l, TV_t);
	}

	public static Recognizer create(String[] TV_l, Mat[] TV_t) {
		if (recognizer == null) {
			recognizer = new Recognizer(TV_l, TV_t);
		}
		return recognizer;
	}

	private void initiate(String[] TV_l, Mat[] TV_t) {
		TV_list = TV_l;
		TV_templates = TV_t;		
		Log.i(TAG, "========= recognizer initiated ========");
	}
	
	// 计算梯度
	public Mat getGradient(Mat in) {
		Mat prewitt_x = new Mat(3, 3, CvType.CV_8SC1) {
			{
				put(0, 0, -1);
				put(0, 1, 0);
				put(0, 2, 1);
				put(1, 0, -1);
				put(1, 1, 0);
				put(1, 2, 1);
				put(2, 0, -1);
				put(2, 1, 0);
				put(2, 2, 1);
			}
		};
		Mat prewitt_y = new Mat(3, 3, CvType.CV_8SC1) {
			{
				put(0, 0, -1);
				put(0, 1, -1);
				put(0, 2, -1);
				put(1, 0, 0);
				put(1, 1, 0);
				put(1, 2, 0);
				put(2, 0, 1);
				put(2, 1, 1);
				put(2, 2, 1);
			}
		};

		Mat Gx = in.clone();
		Mat Gy = in.clone();
		Imgproc.filter2D(in, Gx, -1, prewitt_x);
		Imgproc.filter2D(in, Gy, -1, prewitt_y);

		Gx.convertTo(Gx, CvType.CV_32FC1);
		Gy.convertTo(Gy, CvType.CV_32FC1);

		Mat Gx2 = new Mat(Gx.size(), Gx.type());
		Mat Gy2 = new Mat(Gy.size(), Gy.type());
		Mat S = new Mat(Gx.size(), Gx.type());
		Mat G = new Mat(Gx.size(), Gx.type());

		Core.multiply(Gx, Gx, Gx2);
		Core.multiply(Gy, Gy, Gy2);
		Core.add(Gx2, Gy2, S);
		Core.sqrt(S, G);

		G.convertTo(G, CvType.CV_8UC1);

		Log.i(TAG, "Gradient returned");
		return G;
	}

	// 灰度化
	public Mat toGray(Mat in) {
		Mat out = in.clone();
		Imgproc.cvtColor(in, out, Imgproc.COLOR_RGB2GRAY);
		return out;
	}

	// 截取左侧台标
	public Mat cropLeft(Mat frame) {
		Mat cornerL;
		cornerL = new Mat(frame, new Rect(60, 60, 60, 60));
		return cornerL;
	}

	// 比对台标
	private String matchTV(Mat queryL) {
		String TVL = "unknown";
		double th = 0.8;
		double best_matchL = 10.0;

		Mat queryGL = getGradient(queryL);

		for (int i = 0; i < TV_list.length; i++) {
			Mat result = new Mat();
			double minValL = 1.0;
			try {
				//http://docs.opencv.org/master/de/da9/tutorial_template_matching.html
				Imgproc.matchTemplate(queryGL, TV_templates[i], result,
						Imgproc.TM_SQDIFF_NORMED); //OpenCV在函数matchTemplate（）中实现模板匹配
				Core.MinMaxLocResult mML = Core.minMaxLoc(result);//查找数组中的全局最小值和最大值
				minValL = mML.minVal;
				Log.i(TAG, "tv_name = " + TV_list[i] );
				Log.i(TAG, "minLoc  = " + mML.minLoc );
				Log.i(TAG, "minValL = " + minValL );
				Log.i(TAG, "maxVal  = " + mML.maxVal );
				if (minValL < th) {
					if (best_matchL > minValL) {
						best_matchL = minValL;
						TVL = TV_list[i];
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());

			}
		}
		return TVL;
	}
	
	// 识别图像
	public String recognize(Mat query) {
		Mat queryLeft = toGray(query);
		String result = matchTV(queryLeft);
		Log.i(TAG, "========== " + result + " =============");
		if (!(result.equals("unknown"))) {
			return result;
		} 
		return "unknown";
	}
	/**
	 * 比较来个矩阵的相似度 ,todo 不可用
	 * @param srcMat
	 * @param desMat
	 */
	public void comPareHist(Mat srcMat,Mat desMat){

		srcMat.convertTo(srcMat, CvType.CV_32F);
		desMat.convertTo(desMat, CvType.CV_32F);
		double target = Imgproc.compareHist(srcMat, desMat, Imgproc.CV_COMP_CORREL);
		Log.e(TAG, "相似度 : == " + target);
	}


    Mat img = new Mat();
    Mat templ = new Mat();
    /* sample match*/
    public void matchingMethod(Bitmap bitmap1, Bitmap bitmap0) {
        Utils.bitmapToMat(bitmap1,img); // bitmap1 源文件
        Utils.bitmapToMat(bitmap0,templ); // bitmap0 模板文件

        Mat result = new Mat();
        //! [copy_source]
        /// Source image to display
        Mat img_display = new Mat();
        img.copyTo( img_display );
        //! [copy_source]

        //! [create_result_matrix]
        /// Create the result matrix
        int result_cols =  img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;

        result.create( result_rows, result_cols, CvType.CV_32FC1 );
        //! [create_result_matrix]

        //! [match_template]
        /// Do the Matching and Normalize
//        boolean method_accepts_mask = (Imgproc.TM_SQDIFF == match_method ||
//                match_method == Imgproc.TM_CCORR_NORMED);
//        if (use_mask && method_accepts_mask) {
//            Imgproc.matchTemplate( img, templ, result, match_method, mask);
//        } else
//        {
            Imgproc.matchTemplate( img, templ, result, Imgproc.TM_SQDIFF_NORMED); // Imgproc.TM_SQDIFF ,Imgproc.TM_SQDIFF_NORMED
//        }
        //! [match_template]

        //! [normalize]
        Core.normalize( result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat() );
        //! [normalize]

        //! [best_match]
        /// Localizing the best match with minMaxLoc
        double minVal; double maxVal;
        Point matchLoc;

        Core.MinMaxLocResult mmr = Core.minMaxLoc( result );
        //! [best_match]

        //! [match_loc]
        /// For SQDIFF and SQDIFF_NORMED, the best matches are lower values.
        //  For all the other methods, the higher the better
//        if( match_method  == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED )
//        {
            matchLoc = mmr.minLoc;
        Log.d("Recognizer", "matchLoc = "+matchLoc);
		Log.d("Recognizer", "minVal   = "+mmr.minVal);
		Log.d("Recognizer", "maxVal   = "+mmr.maxVal);
//        }
//        else
//        { matchLoc = mmr.maxLoc; }
        //! [match_loc]

        //! [imshow]
        /// Show me what you got 显示源图像和结果矩阵。在最高可能的匹配区域周围绘制一个矩形：
        Imgproc.rectangle(img_display, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);
        Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);

		result.convertTo(result, CvType.CV_8UC1, 255.0);

//        Image tmpImg = toBufferedImage(img_display);
//        ImageIcon icon = new ImageIcon(tmpImg);
//        imgDisplay.setIcon(icon);
//
//        result.convertTo(result, CvType.CV_8UC1, 255.0);
//        tmpImg = toBufferedImage(result);
//        icon = new ImageIcon(tmpImg);
//        resultDisplay.setIcon(icon);
        //! [imshow]

    }




}
