package com.example.my17_7;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import android.opengl.GLES20;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

/**
 * �ذ���
 * @author yinchuandong
 *
 */
public class TexFloor {
	int mProgram;
	int muMVPMatrixHandle;
	int muMMatrixHandle;
	int maTexCoorHandle;
	int muTexHandle;
	int maCameraHandle;
	int maPositionHandle;
	
	String mVertexShader;
	String mFragmentShader;
	
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mTextureBuffer;
	
	int vCount;
	float yOffset;
	
	public TexFloor(int mProgram, final float UNIT_SIZE, float yOffset, CollisionShape groundShape, DiscreteDynamicsWorld dynamicsWorld){
		this.mProgram = mProgram;
		this.yOffset = yOffset;
		
		Transform groudTransform = new Transform();
		groudTransform.setIdentity();
		groudTransform.origin.set(new Vector3f(0, yOffset, 0));
		Vector3f localInertia = new Vector3f(0,0,0);
		DefaultMotionState myMotionState = new DefaultMotionState(groudTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo
				(
					0, 
					myMotionState, 
					groundShape, 
					localInertia
				);
		RigidBody body = new RigidBody(rbInfo);
		body.setRestitution(0.4f);
		body.setFriction(0.8f);
		
		dynamicsWorld.addRigidBody(body);
		initVertexData(UNIT_SIZE);
		initShader(mProgram);
	}
	
	public void initVertexData(final float UNIT_SIZE){
		vCount = 6;
		float[] vertices = new float[]
		{
				1*UNIT_SIZE, yOffset, 1*UNIT_SIZE,
				-1*UNIT_SIZE, yOffset, -1*UNIT_SIZE,
				-1*UNIT_SIZE, yOffset, 1*UNIT_SIZE,
				
				1*UNIT_SIZE, yOffset, 1*UNIT_SIZE,
				1*UNIT_SIZE, yOffset, -1*UNIT_SIZE,
				-1*UNIT_SIZE, yOffset, -1*UNIT_SIZE
		};
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		float[] textures = new float[]
		{
				UNIT_SIZE/2, UNIT_SIZE/2,
				0,0,
				0, UNIT_SIZE/2,
				
				UNIT_SIZE/2, UNIT_SIZE/2,
				UNIT_SIZE/2, 0,
				0,0
		};
		
		ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTextureBuffer = tbb.asFloatBuffer();
		mTextureBuffer.put(textures);
		mTextureBuffer.position(0);
		
	}
	
	public void initShader(int mProgram){
		 //��ȡ�����ж���λ����������id  
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //��ȡ�����ж��㾭γ����������id   
        maTexCoorHandle=GLES20.glGetAttribLocation(mProgram, "aTexCoor");  
        //��ȡ�������ܱ任��������id 
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");   
        //��ȡλ�á���ת�任��������id
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");  
        //��ȡ�����������λ������id
        maCameraHandle=GLES20.glGetUniformLocation(mProgram, "uCamera"); 
        muTexHandle=GLES20.glGetUniformLocation(mProgram, "sTexture"); 
	}
	
	public void drawSelf(int texId){
		 //�ƶ�ʹ��ĳ��shader����
   	 	GLES20.glUseProgram(mProgram);
        //�����ձ任������shader����
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0); 
        //��λ�á���ת�任������shader����
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);  
        //�������λ�ô���shader����   
        GLES20.glUniform3fv(maCameraHandle, 1, MatrixState.cameraFB);
        
        //Ϊ����ָ������λ������    
        GLES20.glVertexAttribPointer        
        (
        		maPositionHandle,   
        		3, 
        		GLES20.GL_FLOAT, 
        		false,
               3*4, 
               mVertexBuffer   
        );       
        //Ϊ����ָ������������������
        GLES20.glVertexAttribPointer  
        (  
       		maTexCoorHandle,  
        		2, 
        		GLES20.GL_FLOAT, 
        		false,
               2*4,   
               mTextureBuffer
        );   
        //������λ����������
        GLES20.glEnableVertexAttribArray(maPositionHandle);  
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glUniform1i(muTexHandle, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
