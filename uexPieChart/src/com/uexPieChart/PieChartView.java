package com.uexPieChart;

import java.util.List;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import com.uexPieChart.bean.PieChartBean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PieChartView extends View {
	public static final String TAG = "PieChartView";

	public static final int ANIMATION_DURATION = 800;
	public static final int ANIMATION_STATE_RUNNING = 1;
	public static final int ANIMATION_STATE_DOWN = 2;
	private static RectF OVAL;
	private List<PieChartBean> pieChartList;

	private Paint paint;// 绘制饼图

	private Paint maskPaint;// 图片

	private Paint textPaint;// 绘制文字
	private boolean isDraw = true;

	private Point lastEventPoint;// 最后point

	private int currentTargetIndex = -1;

	private static int lastTargetIndex = -1;

	private Point mCenterPoint; // 这个是饼图的中心位置
	private int MR = 0;// 半径
	private int backR = 491;// 背景图内圆直径

	private int eventRadius = 0; // 事件距离饼图中心的距离

	private int startDegree = 90; // 让初始的时候，圆饼是从箭头位置开始画出的

	private int animState = ANIMATION_STATE_DOWN;

	private boolean animEnabled = false;

	private long animStartTime;
	private Bitmap backBitmap;

	private Context context;

	private int screenWidth = 480;
	private int screenHeight = 800;

	public PieChartView(Context context, List<PieChartBean> list,
			int screenWidth, int screenHeight) {
		super(context);
		this.context = context;
		pieChartList = list;
		if (list.size() > 0) {
			int firstAngle = pieChartList.get(0).getJiaodu();
			startDegree = 90 - (firstAngle / 2);
			if (startDegree < 0) {
				startDegree = startDegree + 360;
			}
		}
		mCenterPoint = new Point(screenWidth / 2, screenHeight / 2);
		if (screenWidth <= screenHeight) {
			MR = screenWidth / 2 - 35;
		} else {
			MR = screenHeight / 2 - 35;
		}
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		init();
	}

	public PieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	private void init() {
		OVAL = new RectF(mCenterPoint.x - MR, mCenterPoint.y - MR,
				mCenterPoint.x + MR, mCenterPoint.y + MR);
		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);

		maskPaint = new Paint();
		maskPaint.setAntiAlias(true);

		textPaint = new Paint();
		textPaint.setAntiAlias(true);

		// 获取初始位置的时候，下方箭头所在的区域
		animEnabled = true; // 同时，启动动画

		backBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				EUExUtil.getResDrawableID("plugin_uexpiechart_back")))
				.getBitmap();
	}

	/**
	 * 重写这个方法来画出整个界面
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (animEnabled) {
			/**
			 * 说明是启动的时候，需要旋转着画出饼图
			 */
			// Log.e(TAG, "anim enabled");
			if (animState == ANIMATION_STATE_DOWN) {
				animStartTime = SystemClock.uptimeMillis();
				animState = ANIMATION_STATE_RUNNING;
			}

			final long currentTimeDiff = SystemClock.uptimeMillis()
					- animStartTime;
			int currentMaxDegree = (int) ((float) currentTimeDiff
					/ ANIMATION_DURATION * 360f);

			if (currentMaxDegree >= 360) {
				// 动画结束状态,停止绘制
				currentMaxDegree = 360;
				animState = ANIMATION_STATE_DOWN;
				animEnabled = false;
			}

			int startAngle = this.startDegree;
			int tempAngle = startAngle;

			// 获取当前时刻最大可以旋转的角度所位于的区域
			int maxIndex = getEventPart(currentMaxDegree);

			// 根据不同的颜色画饼图
			for (int i = 0; i <= maxIndex; i++) {
				PieChartBean bean = pieChartList.get(i);
				int currentDegree = bean.getJiaodu();

				if (i == maxIndex) {
					// 对于当前最后一个绘制区域，可能只是一部分，需要获取其偏移量
					currentDegree = getOffsetOfPartStart(currentMaxDegree,
							maxIndex);
				}

				if (i > 0) {
					// 注意，每次画饼图，记得计算startAngle
					startAngle += pieChartList.get(i - 1).getJiaodu();
				}

				paint.setColor(PieChartUtility.parseColor(bean.getColor()));
				canvas.drawArc(OVAL, startAngle, currentDegree, true, paint);
			}
			for (int i = 0; i < pieChartList.size(); i++) {
				PieChartBean bean = pieChartList.get(i);
				String value = bean.getPercent()+"%";
				if (i > 0) {
					tempAngle += pieChartList.get(i - 1).getJiaodu();
				}
				// //写文字
				textPaint.setTextSize(22);
				Rect rect = new Rect();
				textPaint.getTextBounds(value, 0, value.length(), rect);
				textPaint.setColor(PieChartUtility.parseColor("#FFFFFF"));
				if (bean.getPercent() > 5) {
					Point middlePoint = parseAngle2(tempAngle, bean.getJiaodu());
					canvas.drawText(value, middlePoint.x-rect.width()/2,
							middlePoint.y, textPaint);
				}
			}

			if (animState == ANIMATION_STATE_DOWN) {

				// 如果动画结束了，则调整当前箭头位于所在区域的中心方向
				// onStop();

			} else {
				postInvalidate();
			}

		} else {

			int startAngle = this.startDegree;
			/**
			 * 每个区域的颜色不同，但是这里只要控制好每个区域的角度就可以了，整个是个圆
			 */
			int tempAngle = startAngle;
			for (int i = 0; i < pieChartList.size(); i++) {
				PieChartBean bean = pieChartList.get(i);
				paint.setColor(PieChartUtility.parseColor(bean.getColor()));
				if (i > 0) {
					startAngle += pieChartList.get(i - 1).getJiaodu();
				}
				canvas.drawArc(OVAL, startAngle, bean.getJiaodu(), true, paint);
			}
			for (int i = 0; i < pieChartList.size(); i++) {
				PieChartBean bean = pieChartList.get(i);
				String value = bean.getPercent()+"%";
				if (i > 0) {
					tempAngle += pieChartList.get(i - 1).getJiaodu();
				}
				// //写文字
				textPaint.setTextSize(22);
				Rect rect = new Rect();
				textPaint.getTextBounds(value, 0, value.length(), rect);
				textPaint.setColor(PieChartUtility.parseColor("#FFFFFF"));
				if (bean.getPercent() > 5) {
					Point middlePoint = parseAngle2(tempAngle, bean.getJiaodu());
					canvas.drawText(value, middlePoint.x-rect.width()/2,
							middlePoint.y, textPaint);
				}
			}
		}

		/**
		 * 画出饼图之后，画遮罩图片，这样图片就位于饼图之上了，形成了遮罩的效果
		 */
		Matrix backMatrix = new Matrix();
		backR = backBitmap.getWidth() * 491 / 556;
		float backWdithSale = (float) (MR * 2 - 2) / (float) backR;
		backMatrix.reset();
		backMatrix.postScale(backWdithSale, backWdithSale);
		int back2R = backBitmap.getWidth() * MR / backR;
		Bitmap tempBackBitmap = Bitmap
				.createBitmap(backBitmap, 0, 0, backBitmap.getWidth(),
						backBitmap.getHeight(), backMatrix, true);
		canvas.drawBitmap(tempBackBitmap, mCenterPoint.x - back2R,
				mCenterPoint.y - back2R + 1, maskPaint);

		/**
		 * 根据当前计算得到的箭头所在区域显示该区域代表的信息
		 */

	}

	/*
	 * 根据起始角度和偏移角度算三分之一的一个点
	 */
	public Point parseAngle(int startAngle, int valueAngle) {
		Point point = null;
		if (startAngle >= 360) {
			startAngle -= 360;
		} else if (startAngle <= -360) {
			startAngle += 360;
		}
		int middleAngle = startAngle + valueAngle / 2;
		if (middleAngle >= 360) {
			middleAngle = middleAngle - 360;
		} else if (middleAngle < 0) {
			middleAngle = middleAngle + 360;
		}
		if (middleAngle >= 0 && middleAngle < 90) {
			point = new Point(mCenterPoint.x
					+ (int) (MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					+ (int) (MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))));
		} else if (middleAngle >= 90 && middleAngle < 180) {
			middleAngle = middleAngle - 90;
			point = new Point(mCenterPoint.x
					- (int) (MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					+ (int) (MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))));
		} else if (middleAngle >= 180 && middleAngle < 270) {
			middleAngle = middleAngle - 180;
			point = new Point(mCenterPoint.x
					- (int) (MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					- (int) (MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))));
		} else if (middleAngle >= 270 && middleAngle < 360) {
			middleAngle = middleAngle - 270;
			point = new Point(mCenterPoint.x
					+ (int) (MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					- (int) (MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))));
		}
		return point;
	}

	/*
	 * 根据起始角度和偏移角度算三分之2的一个点
	 */
	public Point parseAngle2(int startAngle, int valueAngle) {
		Point point = null;
		if (startAngle >= 360) {
			startAngle -= 360;
		} else if (startAngle <= -360) {
			startAngle += 360;
		}
		int middleAngle = startAngle + valueAngle / 2;
		if (middleAngle >= 360) {
			middleAngle = middleAngle - 360;
		} else if (middleAngle < 0) {
			middleAngle = middleAngle + 360;
		}
		if (middleAngle >= 0 && middleAngle < 90) {
			point = new Point(mCenterPoint.x
					+ (int) (2 * MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					+ (int) (2 * MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))));
		} else if (middleAngle >= 90 && middleAngle < 180) {
			middleAngle = middleAngle - 90;
			point = new Point(mCenterPoint.x
					- (int) (2 * MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					+ (int) (2 * MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))));
		} else if (middleAngle >= 180 && middleAngle < 270) {
			middleAngle = middleAngle - 180;
			point = new Point(mCenterPoint.x
					- (int) (2 * MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					- (int) (2 * MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))));
		} else if (middleAngle >= 270 && middleAngle < 360) {
			middleAngle = middleAngle - 270;
			point = new Point(mCenterPoint.x
					+ (int) (2 * MR / 3 * Math.abs(Math.sin(Math.PI / 180
							* middleAngle))), mCenterPoint.y
					- (int) (2 * MR / 3 * Math.abs(Math.cos(Math.PI / 180
							* middleAngle))));
		}
		return point;
	}

	/**
	 * 处理饼图的转动
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (animEnabled && animState == ANIMATION_STATE_RUNNING) {
			return super.onTouchEvent(event);
		}

		Point eventPoint = new Point((int) event.getX(), (int) event.getY());
		// computeCenter(); // 计算中心坐标
		// 计算当前位置相对于x轴正方向的角度
		// 在下面这个方法中计算了eventRadius的
		int newAngle = getEventAngle(eventPoint, mCenterPoint);
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			isDraw = false;
			lastEventPoint = eventPoint;
			if (eventRadius > MR) {
				/**
				 * 只有点在饼图内部才需要处理转动,否则直接返回
				 */
				return super.onTouchEvent(event);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			int tt = getTargetDegree();
			int index = getEventPart(tt);
			PieChartActivity.getDataListener.onPieChartMove(PieChartActivity
					.getOpid(), 0, pieChartList.get(index).getJsonData());
			// 这里处理滑动
			rotate(eventPoint, newAngle);
			// 处理之后，记得更新lastEventPoint
			lastEventPoint = eventPoint;
			break;
		case MotionEvent.ACTION_UP:
			// onStop();
			int stopDegree = getTargetDegree();
			int stopIndex = getEventPart(stopDegree);
			PieChartActivity.getDataListener.onPieChartStop(PieChartActivity
					.getOpid(), 0, pieChartList.get(stopIndex).getJsonData());
			isDraw = true;
			break;
		default:
			break;
		}
		return true;
	}

	private void rotate(Point eventPoint, int newDegree) {
		// 计算上一个位置相对于x轴正方向的角度
		int lastDegree = getEventAngle(lastEventPoint, mCenterPoint);
		/**
		 * 其实转动就是不断的更新画圆弧时候的起始角度， 这样，每次从新的起始角度重画圆弧就形成了转动的效果
		 */
		startDegree += newDegree - lastDegree;
		// 转多圈的时候，限定startAngle始终在-360-360度之间
		if (startDegree >= 360) {
			startDegree -= 360;
		} else if (startDegree <= -360) {
			startDegree += 360;
		}
		// 获取当前下方箭头所在的区域，这样在onDraw的时候就会转到不同区域显示的是当前区域对应的信息
		int targetDegree = getTargetDegree();
		currentTargetIndex = getEventPart(targetDegree);
		// 请求重新绘制界面，调用onDraw方法
		postInvalidate();

	}

	/**
	 * 获取当前饼图的中心坐标，相对于屏幕左上角
	 */
	protected void computeCenter() {
		if (mCenterPoint == null) {
			int x = (int) OVAL.left + (int) ((OVAL.right - OVAL.left) / 2f);
			int y = (int) OVAL.top + (int) ((OVAL.bottom - OVAL.top) / 2f) + 50; // 状态栏的高度是50
			mCenterPoint = new Point(x, y);
		}
	}

	/**
	 * 获取半径
	 */
	protected int getRadius() {
		int radius = (int) ((OVAL.right - OVAL.left) / 2f);
		return radius;
	}

	/**
	 * 获取事件坐标相对于饼图的中心x轴正方向的角度
	 * 这里就是坐标系的转换，本例中使用饼图的中心作为坐标中心，就是我们从初中到大学一直使用的"正常"坐标系。
	 * 但是涉及到圆的转动，本例中一律相对于x正方向顺时针来计算某个事件在坐标系中的位置
	 * 
	 * @param eventPoint
	 * @param center
	 * @return
	 */
	protected int getEventAngle(Point eventPoint, Point center) {
		int x = eventPoint.x - center.x;// x轴方向的偏移量
		int y = eventPoint.y - center.y; // y轴方向的偏移量


		double z = Math.hypot(Math.abs(x), Math.abs(y)); // 求直角三角形斜边的长度

		eventRadius = (int) z;
		double sinA = Math.abs(y) / z;

		double asin = Math.asin(sinA); // 求反正玄，得到当前点和x轴的角度,是最小的那个

		int degree = (int) (asin / 3.14f * 180f);

		// 下面就需要根据x,y的正负，来判断当前点和x轴的正方向的夹角
		int realDegree = 0;
		if (x <= 0 && y <= 0) {
			// 左上方，返回180+angle
			realDegree = 180 + degree;

		} else if (x >= 0 && y <= 0) {
			// 右上方，返回360-angle
			realDegree = 360 - degree;
		} else if (x <= 0 && y >= 0) {
			// 左下方，返回180-angle
			realDegree = 180 - degree;
		} else {
			// 右下方,直接返回
			realDegree = degree;
		}

		return realDegree;

	}

	/**
	 * 获取当前下方箭头位置相对于startDegree的角度值 注意，下方箭头相对于x轴正方向是90度
	 * 
	 * @return
	 */
	protected int getTargetDegree() {
		int targetDegree = -1;
		int tmpStart = startDegree;

		/**
		 * 如果当前startAngle为负数，则直接+360，转换为正值
		 */
		if (tmpStart < 0) {
			tmpStart += 360;
		}
		if (tmpStart < 90) {
			/**
			 * 如果startAngle小于90度（可能为负数）
			 */
			targetDegree = 90 - tmpStart;
		} else {
			/**
			 * 如果startAngle大于90，由于在每次计算startAngle的时候，限定了其最大为360度，所以 直接可以按照如下公式计算
			 */
			targetDegree = 360 + 90 - tmpStart;
		}

		return targetDegree;
	}

	/**
	 * 判断角度为degree坐落在饼图的哪个部分 注意，这里的角度一定是正值，而且不是相对于x轴正方向，而是相对于startAngle
	 * 返回当前部分的索引
	 * 
	 * @param degree
	 * @return
	 */
	protected int getEventPart(int degree) {
		int currentSum = 0;

		for (int i = 0; i < pieChartList.size(); i++) {
			currentSum += pieChartList.get(i).getJiaodu();
			if (currentSum >= degree) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 在已经得知了当前degree位于targetIndex区域的情况下，计算angle相对于区域targetIndex起始位置的偏移量
	 * 
	 * @param degree
	 * @param targetIndex
	 * @return
	 */
	protected int getOffsetOfPartStart(int degree, int targetIndex) {
		int currentSum = 0;
		for (int i = 0; i < targetIndex; i++) {
			currentSum += pieChartList.get(i).getJiaodu();
		}

		int offset = degree - currentSum;

		return offset;
	}
}