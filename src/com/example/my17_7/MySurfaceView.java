package com.example.my17_7;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector3f;


import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

public class MySurfaceView extends GLSurfaceView{
	
	private SceneRenderer mRenderer;
	
	final float LIGHT_Y = 70; //光源离Y轴的距离
	float mPreviousX;
	float mPreviousY;
	float preNanoTime; //上次触碰的时间
	
	boolean isMoveFlag = false;
	float cx = 0;
	float cy = 1;
	float cz = 6;
	float tx = 0;
	float tz = -10;
	
	//都是弧度制
	double tempRadians = 0;//相机转动的角度
	double upRadians = 0; //抬起手之后相机转动的角度
	
	float left, right, top, bottom, near, far;//摄像机视觉立方体
	
	//加载的obj物体列表
	ArrayList<LoadedObjectVertexNormal> lovnList = new ArrayList<LoadedObjectVertexNormal>();
	int checkedIndex = -1; //默认没有选中物体，为-1;
	float xOffset = 0, yOffset = 0, zOffset = 0; //在轴上的移动位置
	
	//===============分屏===============================
	static float screenWidth;
	static float screenHeight;
	static enum Area{LU, RU, LD, RD, NONE};
	Area currArea = Area.NONE;
	boolean areaTouch = false;
	AreaTouchThread areaTouchThread;
	
	//===============摄像机==============================
	Vector3f cameraCircleCenter = new Vector3f(0, 0, 0);
	boolean forward = false;
	
	//================物理模拟===========================
	DiscreteDynamicsWorld dynamicsWorld;
	CollisionShape ballShape;
	CollisionShape planeShape;
	Activity context;
	LoadedObjectVertexNormal[] loadedModels = new LoadedObjectVertexNormal[7];//只有其中模型
	LoadedObjectVertexNormal[] bodyForDraws = new LoadedObjectVertexNormal[BodyPartIndex.BODYPART_COUNT.ordinal()];//11个部分，包括左右
	
	
	public MySurfaceView(Context context) {
		super(context);
		this.context = (Activity)context;
		this.setEGLContextClientVersion(2);
		initWorld();//初始化物理世界
		mRenderer = new SceneRenderer();
		this.setRenderer(mRenderer);
		this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		DisplayMetrics dm = new DisplayMetrics();
		this.context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}
	
	public void initWorld(){
		CollisionConfiguration configuration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(configuration);
		
		//物理世界边界
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		int maxProxies = 1024;
		AxisSweep3 pairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, pairCache, solver, configuration);
		dynamicsWorld.setGravity(new Vector3f(0, -30, 0));
		ballShape = new SphereShape(1);//创建球体
		planeShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 0);//创建平面
	}
	
	public void loadCapsules(){//加载胶囊
		loadedModels[0] = LoadUtil.loadFromFile("head.obj", context.getResources(), MySurfaceView.this);//头
		loadedModels[1] = LoadUtil.loadFromFile("spine.obj", context.getResources(), MySurfaceView.this);//脊椎
		loadedModels[2] = LoadUtil.loadFromFile("pelvis.obj", context.getResources(), MySurfaceView.this);//骨盆
		loadedModels[3] = LoadUtil.loadFromFile("upper_arm.obj", context.getResources(), MySurfaceView.this);//大臂
		loadedModels[4] = LoadUtil.loadFromFile("lower_arm.obj", context.getResources(), MySurfaceView.this);//小臂
		loadedModels[5] = LoadUtil.loadFromFile("upper_leg.obj", context.getResources(), MySurfaceView.this);//大腿
		loadedModels[6] = LoadUtil.loadFromFile("lower_leg.obj", context.getResources(), MySurfaceView.this);//小腿
	}
	
	public void initBodyForDraws(){
		bodyForDraws[BodyPartIndex.BODYPART_HEAD.ordinal()]=loadedModels[0];
		bodyForDraws[BodyPartIndex.BODYPART_SPINE.ordinal()]=loadedModels[1];
		bodyForDraws[BodyPartIndex.BODYPART_PELVIS.ordinal()]=loadedModels[2];
		bodyForDraws[BodyPartIndex.BODYPART_RIGHT_UPPER_ARM.ordinal()]=loadedModels[3];
		bodyForDraws[BodyPartIndex.BODYPART_LEFT_UPPER_ARM.ordinal()]=loadedModels[3].clone();
		bodyForDraws[BodyPartIndex.BODYPART_LEFT_LOWER_ARM.ordinal()]=loadedModels[4];
		bodyForDraws[BodyPartIndex.BODYPART_RIGHT_LOWER_ARM.ordinal()]=loadedModels[4].clone();
		bodyForDraws[BodyPartIndex.BODYPART_RIGHT_UPPER_LEG.ordinal()]=loadedModels[5];
		bodyForDraws[BodyPartIndex.BODYPART_LEFT_UPPER_LEG.ordinal()]=loadedModels[5].clone();
		bodyForDraws[BodyPartIndex.BODYPART_RIGHT_LOWER_LEG.ordinal()]=loadedModels[6];
		bodyForDraws[BodyPartIndex.BODYPART_LEFT_LOWER_LEG.ordinal()]=loadedModels[6].clone();
		for(int i=0;i<bodyForDraws.length;i++){
			lovnList.add(bodyForDraws[i]);
		}
	}
	
	//=================触摸事件===========
	float x;
	float y;
	@Override
	public boolean onTouchEvent(MotionEvent e){
		x = e.getX();
		y = e.getY();
		
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPreviousX = e.getX();
			mPreviousY = e.getY();
			preNanoTime = System.nanoTime();
			checkedIndex = -1;
			
			float[] AB = IntersectantUtil.calculateABPosition(
					x, y, 
					screenWidth, screenHeight, 
					left, top,
					near, far
				);
			
			//求出触摸的摄射线
			MyVector3f start = new MyVector3f(AB[0], AB[1], AB[2]);
			MyVector3f end = new MyVector3f(AB[3], AB[4], AB[5]);
			MyVector3f dir = end.minus(start);
			Log.i("dir", dir.x + "/" + dir.y + "/" + dir.z);
			
			int tempIndex = -1;
			float minTime = 1;
			for(int i=0; i<lovnList.size(); i++){
				AABB3 box = lovnList.get(i).getCurrBox();
				float t = box.rayIntersect(start, dir, null);
				Log.w("t", t+"");
				if (t<=minTime) {
					minTime = t;
					tempIndex = i;
				}
			}
			checkedIndex = tempIndex;
			changeObj(checkedIndex);
//			Log.i("action_down", checkedIndex+"");
			break;
		
		case MotionEvent.ACTION_MOVE:		
			if(x - mPreviousX >= 10.0f || x - mPreviousX <=-10.0f){
				isMoveFlag = true;
			}
			
			if (isMoveFlag) {
				if (checkedIndex != -1) {
					LoadedObjectVertexNormal lovo = lovnList.get(checkedIndex);
					lovo.isPicked = true;
					float[] nearXY = IntersectantUtil.calculateABPosition(
							x, y, screenWidth, screenHeight, left, top, near, far );
					float[] preNearXY = IntersectantUtil.calculateABPosition(
							mPreviousX, mPreviousY, screenWidth, screenHeight, left, top, near, far );
					//获得当前点对点关节的位置
					Vector3f curPivot = lovo.p2p.getPivotInB(new Vector3f());
					Vector3f dir1 = new Vector3f(
							nearXY[0]-preNearXY[0], nearXY[1] - preNearXY[1], nearXY[2]- preNearXY[2]);
					float factor = 0.5f;
					dir1.set(dir1.x*factor, dir1.y*factor, dir1.z*factor);
					curPivot.add(dir1);
					lovo.p2p.setPivotB(curPivot);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			isMoveFlag = false;
			if (checkedIndex != -1) {
				LoadedObjectVertexNormal lovo = lovnList.get(checkedIndex);
				lovo.removePickedConstraint();
				checkedIndex = -1;
			}
			
			if (checkedIndex == -1 && currArea != Area.NONE) {
				currArea = Area.NONE;
				areaTouch = false;
				upRadians = tempRadians;
			}
			break;
		}
		
		return true;
	}
	
//	public boolean onTouchEvent(MotionEvent event)
//	{
//		x=event.getX();
//		y=event.getY();
//		switch(event.getAction())
//		{
//			case MotionEvent.ACTION_DOWN:
//				mPreviousX=event.getX();
//				mPreviousY=event.getY();
//				preNanoTime=System.nanoTime();
//    			checkedIndex=-1;
//    			
//    			//计算旋转后点的变化
//    			float[] AB=IntersectantUtil.calculateABPosition
//    			(
//    				x, //触控点X坐标
//    				y, //触控点Y坐标
//    				screenWidth, //屏幕宽度
//    				screenHeight, //屏幕长度
//    				left, //视角left、top值
//    				top,
//    				near, //视角near、far值
//    				far
//    			);    			  
//
//    			//射线AB
//    			MyVector3f start = new MyVector3f(AB[0], AB[1], AB[2]);//起点
//    			MyVector3f end = new MyVector3f(AB[3], AB[4], AB[5]);//终点
//    			MyVector3f dir = end.minus(start);//长度和方向
//    			/*
//    			 * 计算AB线段与每个物体包围盒的最佳交点(与A点最近的交点)，
//    			 * 并记录有最佳交点的物体在列表中的索引值
//    			 */
//    			//记录列表中时间最小的索引值
//        		int tmpIndex=-1;//记录与A点最近物体索引的临时值
//        		float minTime=1;//记录列表中所有物体与AB相交的最短时间
//        		for(int i=0;i<lovnList.size();i++){//遍历列表中的物体
//        			AABB3 box = lovnList.get(i).getCurrBox(); //获得物体AABB包围盒   
//    				float t = box.rayIntersect(start, dir, null);//计算相交时间
//        			if (t <= minTime) {
//    					minTime = t;//记录最小值
//    					tmpIndex = i;//记录最小值索引
//    				}
//        		}
//        		checkedIndex=tmpIndex;//将索引保存在checkedIndex中    		
//        		changeObj(checkedIndex);//改变被选中物体	
//        		
//			break;
//			case MotionEvent.ACTION_MOVE:
//				if(x-mPreviousX>=10.0f||x-mPreviousX<=-10.0f)
//				{//移动大于相应的值，则认为其真正的移动
//					isMoveFlag=true;
//				}
//				if(isMoveFlag)
//				{
//					if(checkedIndex!=-1){
//						LoadedObjectVertexNormal lovo = lovnList.get(checkedIndex);
//						lovo.isPicked=true;
//						float[] nearXY=IntersectantUtil.calculateABPosition(
//								x,y,
//								screenWidth,screenHeight,
//								left,top,
//								near,far
//								);
//						float[] nearPreXY = IntersectantUtil.calculateABPosition(
//								mPreviousX,mPreviousY,
//								screenWidth,screenHeight,
//								left,top,
//								near,far
//								);
//						Vector3f currPivot = lovo.p2p.getPivotInB(new Vector3f());
//						Vector3f dir1 = new Vector3f(nearXY[0]-nearPreXY[0],nearXY[1]-nearPreXY[1],nearXY[2]-nearPreXY[2]);
//						float vFactor = 0.5f;
//						dir1.set(dir1.x*vFactor,dir1.y*vFactor,dir1.z*vFactor);
//						currPivot.add(dir1);
//						lovo.p2p.setPivotB(currPivot);
//					}
//				}
//			break;
//			case MotionEvent.ACTION_UP:
//				
//				isMoveFlag=false;
//				if(checkedIndex!=-1){
//					LoadedObjectVertexNormal lovo = lovnList.get(checkedIndex);
//	    			lovo.removePickedConstraint();
//	    			checkedIndex=-1;
//				}
//				if(checkedIndex==-1 && currArea!=Area.NONE){
//					currArea=Area.NONE;
//					areaTouch=false;
//					upRadians=tempRadians;
//				}
//			break;
//		}
//		return true;
//	}
	
	public void changeObj(int index) {
		if (index != -1) { //选中物体的时候
			LoadedObjectVertexNormal lovo = lovnList.get(index);
			lovo.body.activate();
			lovo.addPickedConstraint();//添加点对点的约束
		}else{
			if (0<x && x<screenWidth/2 && 0<y && y<screenHeight/2) {
				currArea = Area.LU; //左上角
			}else if (0<x && x<screenWidth/2 && screenHeight/2<y && y<screenHeight) {
				currArea = Area.LD; //左下角
			}else if (screenWidth/2<x && x<screenWidth && 0<y && y<screenHeight/2){
				currArea = Area.RU; //右上角
			}else if (screenWidth/2<x && x<screenWidth && screenHeight/2<y && y<screenHeight){
				currArea = Area.RD; //右下角
			}
			
			areaTouch = true;
			if (areaTouchThread == null || !areaTouchThread.isAlive()) {
				areaTouchThread = new AreaTouchThread();
				areaTouchThread.start();
			}
		}
	}

	class SceneRenderer implements GLSurfaceView.Renderer{
		int floorTextureId;
		TexFloor floor;
		Doll doll;
		
		
		@Override
		public void onDrawFrame(GL10 gl) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			
			//设置光源
			MatrixState.setLightLocation((float)(LIGHT_Y*Math.sin(tempRadians)), 30, (float)(LIGHT_Y*Math.cos(tempRadians)));
			
			cx = (float)(25*Math.sin(tempRadians) + cameraCircleCenter.x);
			cz = (float)(25*Math.cos(tempRadians) + cameraCircleCenter.z);
			tx = (float)(10*Math.sin(tempRadians) + cameraCircleCenter.x);
			tz = (float)(10*Math.cos(tempRadians) + cameraCircleCenter.z);
			MatrixState.setCamera(cx, cy, cz, tx, 0f, tz, 0, 1, 0);
			
			MatrixState.pushMatrix();
			MatrixState.copyMVMatrix();
			doll.drawSelf(checkedIndex);//绘制人偶
			
			MatrixState.pushMatrix();
			floor.drawSelf(floorTextureId);//绘制地板
			MatrixState.popMatrix();
			
			MatrixState.popMatrix();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
			float ratio = (float)width/height;
			left = right = ratio;
			top = bottom = 1;
			near = 2;
			far = 100;
			MatrixState.setProjectFrustum(-left, right, -bottom, top, near, far);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			GLES20.glClearColor(0, 0, 0, 0);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			
			MatrixState.setInitStack();
			ShaderManager.loadCodeFromFile(context.getResources());
			ShaderManager.compileShader();
			loadCapsules();
			initBodyForDraws();
			
			floorTextureId = ShaderUtil.initTexture(MySurfaceView.this, 1, R.drawable.f6);
			
			doll = new Doll(MySurfaceView.this, dynamicsWorld, bodyForDraws);
			
			floor = new TexFloor(
					ShaderManager.getTextureShaderProgram(), 
					80*Constant.UNIT_SIZE, 
					-Constant.UNIT_SIZE, 
					planeShape, 
					dynamicsWorld);
			
			new Thread(){
				public void run(){
					while(true){
						try {
							dynamicsWorld.stepSimulation(Constant.TIME_STEP, Constant.MAX_SUB_STEPS);
							Thread.sleep(20);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		
	}
	
	
	class AreaTouchThread extends Thread{
		float time = 0;
		float lineV = 0.1f;
		float angularV = 2;
		
		@Override
		public void run(){
			while(areaTouch){
				time ++ ;
				//右手法则，逆时针为正
				if (currArea == Area.LU) {//左上角，左转
					tempRadians = upRadians - Math.toRadians(time*angularV);
				}
				if (currArea == Area.RU) {//右上角，右转
					tempRadians = upRadians + Math.toRadians(time*angularV);
				}
				if (currArea == Area.LD) {//左下角，前进
					float moveLength = time*lineV;
					cameraCircleCenter.x = (float)(cameraCircleCenter.x - moveLength*Math.sin(tempRadians));
					cameraCircleCenter.z = (float)(cameraCircleCenter.z - moveLength*Math.cos(tempRadians));
				}
				if (currArea == Area.RD) {//右下角，后退
					float moveLength = time*lineV;
					cameraCircleCenter.x = (float)(cameraCircleCenter.x + moveLength*Math.sin(tempRadians));
					cameraCircleCenter.z = (float)(cameraCircleCenter.z + moveLength*Math.cos(tempRadians));
				}
				
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	
	
}
