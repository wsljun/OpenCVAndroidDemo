package com.example.opencvtest;

import android.graphics.Bitmap;
import android.media.Image;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Lyndon.Li on 2017/6/28.
 *
 */

public class MatchTemplateDemo {
    boolean use_mask = false ;
    Mat img = new Mat();
    Mat templ = new Mat();
    Mat Mask = new Mat();
    int match_method;

     public MatchTemplateDemo(Bitmap bitmap1,Bitmap bitmap0){
         Utils.bitmapToMat(bitmap1,img); // bitmap1 源文件
         Utils.bitmapToMat(bitmap0,templ); // bitmap0 模板文件
     }

    private void startMatch(){
        //! [load_image]
        /// Load image and template
        img = Imgcodecs.imread( "", Imgcodecs.IMREAD_COLOR );
        templ = Imgcodecs.imread( "", Imgcodecs.IMREAD_COLOR );
        matchingMethod();
    }
    public Mat matchingMethod() {

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
        boolean method_accepts_mask = (Imgproc.TM_SQDIFF == match_method ||
                match_method == Imgproc.TM_CCORR_NORMED);
        if (use_mask && method_accepts_mask) {
//            Imgproc.matchTemplate( img, templ, result, match_method, mask);
        } else
        {
            Imgproc.matchTemplate( img, templ, result, match_method);
        }
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
        if( match_method  == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED )
        { matchLoc = mmr.minLoc; }
        else
        { matchLoc = mmr.maxLoc; }
        //! [match_loc]

        //! [imshow]
        /// Show me what you got 显示源图像和结果矩阵。在最高可能的匹配区域周围绘制一个矩形：
        Imgproc.rectangle(img_display, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);
        Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);

         return img_display;


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
