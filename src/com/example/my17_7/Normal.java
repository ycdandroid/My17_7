package com.example.my17_7;

import java.util.Set;

public class Normal {
	public static final float DIFF = 0.0000001f;
	float nx;
	float ny;
	float nz;
	
	public Normal(float nx, float ny, float nz){
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
	}
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof Normal)) {
			return false;
		}
		Normal tn = (Normal)o;
		if (Math.abs(nx - tn.nx) <= DIFF &&
			Math.abs(ny - tn.ny) <= DIFF &&
			Math.abs(nz - tn.nz) <= DIFF
		) {
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return 1;
	}
	
	/**
	 * 求法向量的平均向量
	 * @param sn
	 * @return
	 */
	public static float[] getAverage(Set<Normal> sn){
		float[] result = new float[3];
		for (Normal n : sn) {
			result[0] += n.nx;
			result[1] += n.ny;
			result[2] += n.nz;
		}
		return LoadUtil.vectorNormal(result);
	}
	
	
}
