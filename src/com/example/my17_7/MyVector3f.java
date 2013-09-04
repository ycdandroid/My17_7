package com.example.my17_7;


public class MyVector3f {
	float x;
	float y;
	float z;
	
	public MyVector3f(){}
	
	public MyVector3f(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public MyVector3f(float[] v){
		x = v[0];
		y = v[1];
		z = v[2];
	}
	
	/**
	 * 加法
	 * @param v
	 * @return
	 */
	public MyVector3f add(MyVector3f v){
		return new MyVector3f(x+v.x, y+v.y, z+v.z);
	}
	
	/**
	 * 减法
	 * @param v
	 * @return
	 */
	public MyVector3f minus(MyVector3f v){
		return new MyVector3f(x-v.x, y-v.y, z-v.z);
	}
	
	/**
	 * 乘以一个浮点数 float f
	 * @param k
	 * @return
	 */
	public MyVector3f multiK(float k){
		return new MyVector3f(x*k, y*k, z*k);
	}
	
	/**
	 * 规格化
	 */
	public void normalize(){
		float mod = module();
		x /= mod;
		y /= mod;
		z /= mod;
	}
	
	/**
	 * 求模
	 * @return
	 */
	public float module(){
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
}
