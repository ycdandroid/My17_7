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
	
	final float LIGHT_Y = 70; //��Դ��Y��ľ���
	float mPreviousX;
	float mPreviousY;
	float preNanoTime; //�ϴδ�����ʱ��
	
	boolean isMoveFlag = false;
	float cx = 0;
	float cy = 1;
	float cz = 6;
	float tx = 0;
	float tz = -10;
	
	//���ǻ�����
	double tempRadians = 0;//���ת���ĽǶ�
	double upRadians = 0; //̧����֮�����ת���ĽǶ�
	
	float left, right, top, bottom, near, far;//������Ӿ�������
	
	//���ص�obj�����б�
	ArrayList<LoadedObjectVertexNormal> lovnList = new ArrayList<LoadedObjectVertexNormal>();
	int checkedIndex = -1; //Ĭ��û��ѡ�����壬Ϊ-1;
	float xOffset = 0, yOffset = 0, zOffset = 0; //�����ϵ��ƶ�λ��
	
	//===============����===============================
	static float screenWidth;
	static float screenHeight;
	static enum Area{LU, RU, LD, RD, NONE};
	Area currArea = Area.NONE;
	boolean areaTouch = false;
	AreaTouchThread areaTouchThread;
	
	//===============�����==============================
	Vector3f cameraCircleCenter = new Vector3f(0, 0, 0);
	boolean forward = false;
	
	//================����ģ��===========================
	DiscreteDynamicsWorld dynamicsWorld;
	CollisionShape ballShape;
	CollisionShape planeShape;
	Activity context;
	LoadedObjectVertexNormal[] loadedModels = new LoadedObjectVertexNormal[7];//ֻ������ģ��
	LoadedObjectVertexNormal[] bodyForDraws = new LoadedObjectVertexNormal[BodyPartIndex.BODYPART_COUNT.ordinal()];//11�����֣���������
	
	
	public MySurfaceView(Context context) {
		super(context);
		this.context = (Activity)context;
		this.setEGLContextClientVersion(2);
		initWorld();//��ʼ����������
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
		
		//��������߽�
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		int maxProxies = 1024;
		AxisSweep3 pairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, pairCache, solver, configuration);
		dynamicsWorld.setGravity(new Vector3f(0, -30, 0));
		ballShape = new SphereShape(1);//��������
		planeShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 0);//����ƽ��
	}
	
	public void loadCapsules(){//���ؽ���
		loadedModels[0] = LoadUtil.loadFromFile("head.obj", context.getResources(), MySurfaceView.this);//ͷ
		loadedModels[1] = LoadUtil.loadFromFile("spine.obj", context.getResources(), MySurfaceView.this);//��׵
		loadedModels[2] = LoadUtil.loadFromFile("pelvis.obj", context.getResources(), MySurfaceView.this);//����
		loadedModels[3] = LoadUtil.loadFromFile("upper_arm.obj", context.getResources(), MySurfaceView.this);//���
		loadedModels[4] = LoadUtil.loadFromFile("lower_arm.obj", context.getResources(), MySurfaceView.this);//С��
		loadedModels[5] = LoadUtil.loadFromFile("upper_leg.obj", context.getResources(), MySurfaceView.this);//����
		loadedModels[6] = LoadUtil.loadFromFile("lower_leg.obj", context.getResources(), MySurfaceView.this);//С��
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
	
	//=================�����¼�===========
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
			
			//���������������
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
					//��õ�ǰ��Ե�ؽڵ�λ��
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
//    			//������ת���ı仯
//    			float[] AB=IntersectantUtil.calculateABPosition
//    			(
//    				x, //���ص�X����
//    				y, //���ص�Y����
//    				screenWidth, //��Ļ���
//    				screenHeight, //��Ļ����
//    				left, //�ӽ�left��topֵ
//    				top,
//    				near, //�ӽ�near��farֵ
//    				far
//    			);    			  
//
//    			//����AB
//    			MyVector3f start = new MyVector3f(AB[0], AB[1], AB[2]);//���
//    			MyVector3f end = new MyVector3f(AB[3], AB[4], AB[5]);//�յ�
//    			MyVector3f dir = end.minus(start);//���Ⱥͷ���
//    			/*
//    			 * ����AB�߶���ÿ�������Χ�е���ѽ���(��A������Ľ���)��
//    			 * ����¼����ѽ�����������б��е�����ֵ
//    			 */
//    			//��¼�б���ʱ����С������ֵ
//        		int tmpIndex=-1;//��¼��A�����������������ʱֵ
//        		float minTime=1;//��¼�б�������������AB�ཻ�����ʱ��
//        		for(int i=0;i<lovnList.size();i++){//�����б��е�����
//        			AABB3 box = lovnList.get(i).getCurrBox(); //�������AABB��Χ��   
//    				float t = box.rayIntersect(start, dir, null);//�����ཻʱ��
//        			if (t <= minTime) {
//    					minTime = t;//��¼��Сֵ
//    					tmpIndex = i;//��¼��Сֵ����
//    				}
//        		}
//        		checkedIndex=tmpIndex;//������������checkedIndex��    		
//        		changeObj(checkedIndex);//�ı䱻ѡ������	
//        		
//			break;
//			case MotionEvent.ACTION_MOVE:
//				if(x-mPreviousX>=10.0f||x-mPreviousX<=-10.0f)
//				{//�ƶ�������Ӧ��ֵ������Ϊ���������ƶ�
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
		if (index != -1) { //ѡ�������ʱ��
			LoadedObjectVertexNormal lovo = lovnList.get(index);
			lovo.body.activate();
			lovo.addPickedConstraint();//��ӵ�Ե��Լ��
		}else{
			if (0<x && x<screenWidth/2 && 0<y && y<screenHeight/2) {
				currArea = Area.LU; //���Ͻ�
			}else if (0<x && x<screenWidth/2 && screenHeight/2<y && y<screenHeight) {
				currArea = Area.LD; //���½�
			}else if (screenWidth/2<x && x<screenWidth && 0<y && y<screenHeight/2){
				currArea = Area.RU; //���Ͻ�
			}else if (screenWidth/2<x && x<screenWidth && screenHeight/2<y && y<screenHeight){
				currArea = Area.RD; //���½�
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
			
			//���ù�Դ
			MatrixState.setLightLocation((float)(LIGHT_Y*Math.sin(tempRadians)), 30, (float)(LIGHT_Y*Math.cos(tempRadians)));
			
			cx = (float)(25*Math.sin(tempRadians) + cameraCircleCenter.x);
			cz = (float)(25*Math.cos(tempRadians) + cameraCircleCenter.z);
			tx = (float)(10*Math.sin(tempRadians) + cameraCircleCenter.x);
			tz = (float)(10*Math.cos(tempRadians) + cameraCircleCenter.z);
			MatrixState.setCamera(cx, cy, cz, tx, 0f, tz, 0, 1, 0);
			
			MatrixState.pushMatrix();
			MatrixState.copyMVMatrix();
			doll.drawSelf(checkedIndex);//������ż
			
			MatrixState.pushMatrix();
			floor.drawSelf(floorTextureId);//���Ƶذ�
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
				//���ַ�����ʱ��Ϊ��
				if (currArea == Area.LU) {//���Ͻǣ���ת
					tempRadians = upRadians - Math.toRadians(time*angularV);
				}
				if (currArea == Area.RU) {//���Ͻǣ���ת
					tempRadians = upRadians + Math.toRadians(time*angularV);
				}
				if (currArea == Area.LD) {//���½ǣ�ǰ��
					float moveLength = time*lineV;
					cameraCircleCenter.x = (float)(cameraCircleCenter.x - moveLength*Math.sin(tempRadians));
					cameraCircleCenter.z = (float)(cameraCircleCenter.z - moveLength*Math.cos(tempRadians));
				}
				if (currArea == Area.RD) {//���½ǣ�����
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
