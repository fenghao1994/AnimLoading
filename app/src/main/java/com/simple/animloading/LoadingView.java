package com.simple.animloading;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * 原点放置的高度为屏幕高度的一半，宽度为屏幕宽度的一半，并且将屏幕宽度分为三份，点占据中间的那一份
 */
public class LoadingView extends View {

    private Context mContext;
    private Paint mPaint;

    //屏幕宽度
    private int windowWidth;
    //屏幕高度
    private int windowHeight;
    //宽度的1/3
    private int width1_3;
    //高度的1/2
    private int height1_2;
    //宽度的1/12
    private int width1_4;

    private Point mPoint;
    //标记是否第一次进入绘画
    private boolean flag;
    //将其中的某一个点作为动画的起始点
    private int n;
    //原的半径
    private int RADIUS;

    //点的集合
    private ArrayList<Point> mList;

    public LoadingView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        RADIUS = LoadingView.dip2px(mContext , 4);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getWindowWidthAndHeight();
        Point point1 = new Point(width1_3, height1_2, RADIUS, 0);
        Point point2 = new Point(width1_3 + width1_4, height1_2, RADIUS ,1);
        Point point3 = new Point(width1_3 + width1_4 * 2, height1_2, RADIUS, 2);
        Point point4 = new Point(width1_3 + width1_4 * 3, height1_2, RADIUS, 3);
        mList = new ArrayList<>();
        mList.add( point1);
        mList.add( point2);
        mList.add( point3);
        mList.add( point4);
    }

    /**
     * 屏幕的宽和高
     */
    public void getWindowWidthAndHeight(){
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        windowHeight = wm.getDefaultDisplay().getHeight();

        width1_3 = windowWidth / 3;
        width1_4 = width1_3 / 4;
        height1_2 = windowHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (flag == false){
            drawCircle(canvas);
            startAnim();
            flag = true;
        }else {
            drawCircle(canvas);
        }
    }

    /**
     * 绘制圆点
     * 加RADIUS / 2 是为了让画圆时的坐标变成圆心
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        for (int i = 0 ; i < mList.size() ; i++){
            canvas.drawCircle((float) mList.get(i).getX() + RADIUS / 2, (float)mList.get(i).getY() + RADIUS / 2, (float)mList.get(i).getRadius(), mPaint);
        }
    }

    public void startAnim(){
        final Point start = new Point( mList.get( n).getX(), mList.get( n).getY(), mList.get( n).getRadius(), mList.get( n).getIndex());
        Point end = null;
        int m = 0;
        //当前原点所在的位置没有到达最后
        if ( start.getIndex() != mList.size() - 1){
            for ( int i = 0 ;i < mList.size() ;i ++){
                //找到start原点所在位置的下一个位置
                if (mList.get(i).getIndex() == start.getIndex() + 1){
                    end = getNextPoint(i);
                    m = i;
                    break;
                }
            }
        } else {
            //当最开始的圆点位于最后的位置时，则去寻找位置为0的圆点实现循环
            for (int i = 0; i < mList.size() ;i++){
                if ( mList.get(i).getIndex() == 0){
                    end = getNextPoint(i);
                    m = i;
                    break;
                }
            }
        }
        ValueAnimator animator = ValueAnimator.ofObject(new CircleTypeEvaluator(), Math.PI, 0.0d);

        final Point finalEnd = end;
        final int finalM = m;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //得到当前的角度
                double angle = (double) animation.getAnimatedValue();
                //得到半径（通过两个点之间的距离）
                double radius = Math.abs((finalEnd.getX() - start.getX()) / 2);
                //进行1,2或者2,3或者3,4位置的旋转
                if (start.getIndex() != mList.size() - 1){
                    mList.get( n).setX( start.getX() + radius * (1 + Math.cos( angle)));
                    mList.get( n).setY( start.getY() - radius * Math.sin( angle));
                    mList.get(finalM).setX( finalEnd.getX() - radius * (1 + Math.cos( angle)));
                    mList.get(finalM).setY( finalEnd.getY() + radius * Math.sin( angle));
                    //进行1，4位置的旋转
                }else {
                    mList.get( n).setX( start.getX() - radius * (1 + Math.cos( angle)));
                    mList.get( n).setY( start.getY() + radius * Math.sin( angle));
                    mList.get(finalM).setX( finalEnd.getX() + radius * (1 + Math.cos( angle)));
                    mList.get(finalM).setY( finalEnd.getY() - radius * Math.sin( angle));
                }
                //一次旋转完成
                if ( Math.abs( angle - 0.0d) < 0.00000001){
                    startAnim();
                }
                invalidate();
            }
        });
        animator.setDuration( 3000);
        //匀速进行
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    /**
     * 找到下一个圆点
     * @param i
     * @return
     */
    public Point getNextPoint(int i){
        Point end = new Point( mList.get(i).getX(), mList.get(i).getY(), mList.get(i).getRadius(), mList.get(i).getIndex());
        int index = mList.get(n).getIndex();
        mList.get( n).setIndex( mList.get( i).getIndex());
        mList.get( i).setIndex( index);
        return end;
    }

    class Point{
        double x, y, radius;
        int index;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Point(double x, double y, double radius, int index) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.index = index;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setY(double y) {
            this.y = y;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    /**
     * 自定义的TypeEvaluator
     */
    class CircleTypeEvaluator implements TypeEvaluator{

        /**
         * 角度从π到0，
         * @param fraction  变化率
         * @param startValue π
         * @param endValue 0
         * @return  当前变化率的情况下，所要旋转的角度
         */
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            double angle = (double) startValue - fraction * ( (double)startValue - (double)endValue);
            return angle;
        }
    }

    /**
     * dp 转 px
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
}
