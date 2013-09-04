package com.example.my17_7;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Stack;
import android.opengl.Matrix;

//�洢ϵͳ����״̬����
public class MatrixState 
{
	private static float[] mProjMatrix = new float[16];//4x4���� ͶӰ��
    static float[] mVMatrix = new float[16];//�����λ�ó���9��������
    static float[] mMVPMatrix;//��������õ��ܱ任����
    public static float[] lightLocationRed=new float[]{0,0,0};//��ɫ��λ���Դλ��
    public static float[] lightLocationGreenBlue=new float[]{0,0,0};//����ɫ��λ���Դλ��
    public static float[] lightLocation = new float[]{0,0,0};
    public static FloatBuffer cameraFB;
    public static FloatBuffer lightPositionFBRed;
    public static FloatBuffer lightPositionFBGreenBlue;
    public static FloatBuffer lightPositionFB;
    
    //���������
    public static void setCamera
    (
    		float cx,	//�����λ��x
    		float cy,   //�����λ��y
    		float cz,   //�����λ��z
    		float tx,   //�����Ŀ���x
    		float ty,   //�����Ŀ���y
    		float tz,   //�����Ŀ���z
    		float upx,  //�����UP����X����
    		float upy,  //�����UP����Y����
    		float upz   //�����UP����Z����		
    )
    {
    	Matrix.setLookAtM
        (
        		mVMatrix, 
        		0, 
        		cx,
        		cy,
        		cz,
        		tx,
        		ty,
        		tz,
        		upx,
        		upy,
        		upz
        );
    	
    	float[] cameraLocation=new float[3];//�����λ��
    	cameraLocation[0]=cx;
    	cameraLocation[1]=cy;
    	cameraLocation[2]=cz;
    	//�����λ�þ���
    	ByteBuffer llbb = ByteBuffer.allocateDirect(3*4);
        llbb.order(ByteOrder.nativeOrder());//�����ֽ�˳��
        cameraFB=llbb.asFloatBuffer();
        cameraFB.put(cameraLocation);
        cameraFB.position(0);  
    }
    
    /**
     * ��ȡ���������������ķ���
     * @return float[]
     */
   	public static float[] getInvertMvMatrix(){
   		float[] invM = new float[16];
   		Matrix.invertM(invM, 0, mVMatrix, 0);//�������
   		return invM;
   	}
   	
   	/**
   	 * ͨ��������任��ĵ���任ǰ�ĵ�ķ������������������������
   	 * ��������������ϵ�е�����
   	 * @param p
   	 * @return
   	 */
   	public static float[] fromPtoPreP(float[] p){
   		//ͨ����任���õ��任֮ǰ�ĵ�
   		float[] inverM = getInvertMvMatrix();//��ȡ��任����
   		float[] preP = new float[4];
   		Matrix.multiplyMV(preP, 0, inverM, 0, new float[]{p[0],p[1],p[2],1}, 0);//��任ǰ�ĵ�
   		return new float[]{preP[0],preP[1],preP[2]};//�任ǰ�ĵ���Ǳ任֮ǰ�ķ�����
   	}
    
    //����͸��ͶӰ����
    public static void setProjectFrustum
    (
    	float left,		//near���left
    	float right,    //near���right
    	float bottom,   //near���bottom
    	float top,      //near���top
    	float near,		//near�����
    	float far       //far�����
    )
    {
    	Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);    	
    }
    
    //��������ͶӰ����
    public static void setProjectOrtho
    (
    	float left,		//near���left
    	float right,    //near���right
    	float bottom,   //near���bottom
    	float top,      //near���top
    	float near,		//near�����
    	float far       //far�����
    )
    {    	
    	Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }   
   
    //��ȡ����������ܱ任����
    public static float[] getFinalMatrix()
    {
    	float[] mMVPMatrix=new float[16];
    	Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);        
        return mMVPMatrix;
    }
    
    /**
     * ���õƹ�λ�õķ���
     */
    public static void setLightLocation(float x,float y,float z)
    {
    	lightLocation[0]=x;
    	lightLocation[1]=y;
    	lightLocation[2]=z;
    	ByteBuffer llbbL = ByteBuffer.allocateDirect(3*4);
        llbbL.order(ByteOrder.nativeOrder());//�����ֽ�˳��
        lightPositionFB=llbbL.asFloatBuffer();
        lightPositionFB.put(lightLocation);
        lightPositionFB.position(0);
    }
    
    /**
     * ���ú�ɫ�ƹ�λ�õķ���
     * @param x
     * @param y
     * @param z
     */
    public static void setLightLocationRed(float x,float y,float z)
    {
    	lightLocationRed[0]=x;
    	lightLocationRed[1]=y;
    	lightLocationRed[2]=z;
    	ByteBuffer llbb = ByteBuffer.allocateDirect(3*4);
        llbb.order(ByteOrder.nativeOrder());//�����ֽ�˳��
        lightPositionFBRed=llbb.asFloatBuffer();
        lightPositionFBRed.put(lightLocationRed);
        lightPositionFBRed.position(0);
    }
    
    /**
     * ��������ɫ�ƹ�λ�õķ���
     * @param x
     * @param y
     * @param z
     */
    public static void setLightLocationGreenBlue(float x,float y,float z)
    {
    	lightLocationGreenBlue[0]=x;
    	lightLocationGreenBlue[1]=y;
    	lightLocationGreenBlue[2]=z;
    	ByteBuffer llbb = ByteBuffer.allocateDirect(3*4);
        llbb.order(ByteOrder.nativeOrder());//�����ֽ�˳��
        lightPositionFBGreenBlue=llbb.asFloatBuffer();
        lightPositionFBGreenBlue.put(lightLocationGreenBlue);
        lightPositionFBGreenBlue.position(0);
    }
    
    public static Stack<float[]> mStack=new Stack<float[]>();//�����任�����ջ
    static float[] currMatrix;//��ǰ�任����
 
    public static void setInitStack()//��ȡ���任��ʼ����
    {
    	currMatrix=new float[16];
    	Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }
    
    public static void pushMatrix()//�����任����
    {
    	mStack.push(currMatrix.clone());
    }
    
    public static void popMatrix()//�ָ��任����
    {
    	currMatrix=mStack.pop();
    }
    
    public static void translate(float x,float y,float z)//������xyz���ƶ�
    {
    	Matrix.translateM(currMatrix, 0, x, y, z);
    }
    
    public static void rotate(float angle,float x,float y,float z)//������xyz���ƶ�
    {
    	Matrix.rotateM(currMatrix,0,angle,x,y,z);
    }
    
    public static void scale(float x,float y,float z){
    	Matrix.scaleM(currMatrix, 0, x, y, z);
    }
    
    //�����Դ�����
    public static void matrix(float[] self)
    {
    	float[] result=new float[16];
    	Matrix.multiplyMM(result,0,currMatrix,0,self,0);
    	currMatrix=result;
    }

	public static float[] getMMatrix() {
		return currMatrix;
	}
	
	//����һ֡�ڵ����������
    private static float[] mVMatrixForSpecFrame = new float[16];//�����λ�ó���9��������   
    /**
     * �������������
     */
    public static void copyMVMatrix()
    {
		for(int i=0;i<16;i++)
    	{
    		mVMatrixForSpecFrame[i]=mVMatrix[i];        		
    	}
    }
}
