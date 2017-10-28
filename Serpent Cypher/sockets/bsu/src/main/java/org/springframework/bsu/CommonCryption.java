package org.springframework.bsu;

import java.nio.ByteBuffer;

public class CommonCryption implements Table{
	public byte[] initPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0;  i < 128; i++) {
            int bit = (data[(ipTable[i]) / 8] >>> ((ipTable[i]) % 8)) & 0x01;
            if ((bit & 0x01) == 1) {
            	output[15 - (i / 8)] |= 1 << (i % 8);
            } else {
            	output[15 - (i / 8)] &= ~ (1 << (i % 8));
            }
        }
        return output; 
    }

    public byte[] finalPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0;  i < 128; i++) {
            int bit = (data[15 - fpTable[i] / 8] >>> (fpTable[i] % 8)) & 0x01;
            if ((bit & 0x01) == 1) {
            	output[(i / 8)] |= 1 << (i % 8);
            } else {
            	output[(i / 8)] &= ~ (1 << (i % 8));
            }
        }
        return output; 
    }
    
    public int[] getIntegers(byte[] b) {
   	 ByteBuffer buffer = ByteBuffer.wrap(b);
   	 int[] integers = new int[b.length / 4];
   	 for (int i = 0; i < b.length / 4; i ++) {
   		 integers[i] = buffer.getInt();
   	 }
   	 return integers;
   }
   
   public byte[] getBytes(int[] n) {
   	 ByteBuffer buffer = ByteBuffer.allocate(n.length * 4);
   	 for (int i = 0; i < n.length; i ++) {
   		 buffer.putInt(n[i]);
   	 }
   	 return buffer.array();
   }
}
