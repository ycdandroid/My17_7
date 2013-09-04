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
	 * �ӷ�
	 * @param v
	 * @return
	 */
	public MyVector3f add(MyVector3f v){
		return new MyVector3f(x+v.x, y+v.y, z+v.z);
	}
	
	/**
	 * ����
	 * @param v
	 * @return
	 */
	public MyVector3f minus(MyVector3f v){
		return new MyVector3f(x-v.x, y-v.y, z-v.z);
	}
	
	/**
	 * ����һ�������� float f
	 * @param k
	 * @return
	 */
	public MyVector3f multiK(float k){
		return new MyVector3f(x*k, y*k, z*k);
	}
	
	/**
	 * ���
	 */
	public void normalize(){
		float mod = module();
		x /= mod;
		y /= mod;
		z /= mod;
	}
	
	/**
	 * ��ģ
	 * @return
	 */
	public float module(){
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
}
