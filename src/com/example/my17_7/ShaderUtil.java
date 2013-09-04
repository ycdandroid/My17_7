package com.example.my17_7;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.View;

/**
 * ���ض���Shader��ƬԪShader�Ĺ�����
 * @author yinchuandong
 *
 */
public class ShaderUtil 
{
   /**
    * �����ƶ�shader�ķ���
    * @param shaderType
    * @param source
    * @return
    */
   public static int loadShader
   (
		 int shaderType, //shader������  GLES20.GL_VERTEX_SHADER   GLES20.GL_FRAGMENT_SHADER
		 String source   //shader�Ľű��ַ���
   ) 
   {
	    //����һ����shader
        int shader = GLES20.glCreateShader(shaderType);
        //�������ɹ������shader
        if (shader != 0) 
        {
        	//����shader��Դ����
            GLES20.glShaderSource(shader, source);
            //����shader
            GLES20.glCompileShader(shader);
            //��ű���ɹ�shader����������
            int[] compiled = new int[1];
            //��ȡShader�ı������
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) 
            {//������ʧ������ʾ������־��ɾ����shader
            	String tips = "";
            	switch (shaderType) {
				case GLES20.GL_VERTEX_SHADER:
					tips = "===" + shaderType + " vertex.sh";
					break;
				case GLES20.GL_FRAGMENT_SHADER:
					tips = "===" + shaderType + " frag.sh";
					break;
				default:
					tips = "===" + shaderType + "unknown shader error";
					break;
				}
                Log.e("ES20_ERROR", "Could not compile shader " + tips + ":");
                Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;      
            }  
        }
        return shader;
    }
   
   /**
    * ����shader����ķ���
    * @param vertexSource
    * @param fragmentSource
    * @return
    */
   public static int createProgram(String vertexSource, String fragmentSource) 
   {
	    //���ض�����ɫ��
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) 
        {
            return 0;
        }
        //����ƬԪ��ɫ��
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) 
        {
            return 0;
        }
        //��������
        int program = GLES20.glCreateProgram();
        //�����򴴽��ɹ���������м��붥����ɫ����ƬԪ��ɫ��
        if (program != 0) 
        {
        	//������м��붥����ɫ��
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            //������м���ƬԪ��ɫ��
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            //���ӳ���
            GLES20.glLinkProgram(program);
            //������ӳɹ�program����������
            int[] linkStatus = new int[1];
            //��ȡprogram���������
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            //������ʧ���򱨴�ɾ������
            if (linkStatus[0] != GLES20.GL_TRUE) 
            {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
   
   /**
    * ���ÿһ�������Ƿ��д���ķ��� 
    * @param op
    */
   public static void checkGlError(String op) 
   {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) 
        {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
   }
   
   /**
    * ��sh�ű��м���shader���ݵķ���
    * @param fname
    * @param r
    * @return
    */
   public static String loadFromAssetsFile(String fname,Resources r)
   {
   	String result=null;    	
   	try
   	{
   		InputStream in=r.getAssets().open(fname);
			int ch=0;
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    while((ch=in.read())!=-1)
		    {
		      	baos.write(ch);
		    }      
		    byte[] buff=baos.toByteArray();
		    baos.close();
		    in.close();
   		result=new String(buff,"UTF-8"); 
   		result=result.replaceAll("\\r\\n","\n");
   	}
   	catch(Exception e)
   	{
   		e.printStackTrace();
   	}    	
   	return result;
   }

   
   /**
    * ��������
    * @param view
    * @param type 0ΪGL_CLAMP_TO_EDGE, 1��Ĭ�϶�ΪGL_REPEAT
    * @param drawableId
    * @return
    */
   public static int initTexture(View view, int type, int drawableId)//textureId
   {
		//��������ID
		int[] textures = new int[1];
		GLES20.glGenTextures
		(
				1,          //����������id������
				textures,   //����id������
				0           //ƫ����
		);    
		int textureId=textures[0];    
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
		
		switch (type) {
		case 0:
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
			break;
		case 1:
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
			break;
		default:
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
			Log.e("shaderUtil", "initTexture type is illegal,limit between 0 and 1");
			break;
		}
		
		
	   //ͨ������������ͼƬ===============begin===================
	   InputStream is = view.getResources().openRawResource(drawableId);
	   Bitmap bitmapTmp;
	   try 
	   {
	   	bitmapTmp = BitmapFactory.decodeStream(is);
	   } 
	   finally 
	   {
	       try 
	       {
	           is.close();
	       } 
	       catch(IOException e) 
	       {
	           e.printStackTrace();
	       }
	   }
	   //ͨ������������ͼƬ===============end=====================  
	   
	   //ʵ�ʼ�������
	   GLUtils.texImage2D
	   (
	   		GLES20.GL_TEXTURE_2D,   //�������ͣ���OpenGL ES�б���ΪGL10.GL_TEXTURE_2D
			0, 					  //����Ĳ�Σ�0��ʾ����ͼ��㣬�������Ϊֱ����ͼ
			bitmapTmp, 			  //����ͼ��
			0					  //����߿�ߴ�
	   );
	   bitmapTmp.recycle(); 		  //������سɹ����ͷ�ͼƬ
	   
	   return textureId;
	}
}
