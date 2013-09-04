package com.example.my17_7;

import android.content.res.Resources;

/**
 * 统一管理shader program
 * @author yinchuandong
 *
 */
public class ShaderManager {

	final static int shaderCount = 1;
	final static String[][] shaderName = 
	{
		{"vertex.sh","frag.sh"}
	};
	static String[] mVertexShader = new String[shaderCount];
	static String[] mFragmentShader = new String[shaderCount];
	static int[] program = new int[shaderCount];
	
	public static void loadCodeFromFile(Resources r){
		for (int i = 0; i < shaderCount; i++) {
			mVertexShader[i] = ShaderUtil.loadFromAssetsFile(shaderName[i][0], r);
			mFragmentShader[i] = ShaderUtil.loadFromAssetsFile(shaderName[i][1], r);
		}
	}
	
	/**
	 * 统一编译3d物体的程序
	 */
	public static void compileShader(){
		for (int i = 0; i < shaderCount; i++) {
			program[i] = ShaderUtil.createProgram(mVertexShader[i], mFragmentShader[i]);
		}
	}
	
	/**
	 * 获取编译的程序
	 * @return int
	 */
	public static int getTextureShaderProgram(){
		return program[0];
	}
	
}
