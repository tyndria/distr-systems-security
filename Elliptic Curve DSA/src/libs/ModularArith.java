package libs;

import java.io.*;
import java.math.*;

/**
 * Modular Arithmetic
 * This program calculates the GCD, Extended GCD Inverse Modulus and sqare root mod p
 * (a^(-1) mod b) for long integers.<p>
 * Classes used: Tools, LabMenu
 * @author Moshe
 * @author Denis Berger Edited Feb 2006
 * @version 0.1  Feb. 26, 2002
 * http://faculty.washington.edu/moishe/tcss581/modulararith/ModularArith.java
 */
public class ModularArith {
    // private constructor to prevent javadoc generation
    public ModularArith(){};

    /**
     * Calculates the GCD of two integers.
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of a & b
     */
    public static BigInteger GCD(BigInteger a, BigInteger b) {
       BigInteger zero = BigInteger.valueOf(0);
       if (a.multiply(b).compareTo(zero) == 0 )
           return a.add(b);  //EXIT condition
       else
           return GCD(b, a.mod(b));  //the recursive call
    }

    /**
     * Calculates the extended GCD.
     * @param aux the helper array
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of the a & b. GCD(a,b) = m*a + n*b. Stores m and n in the helper array.
     *
     */
    public static  BigInteger  extGCD(BigInteger[] aux, BigInteger a, BigInteger b) {
    	BigInteger  tempo;
    	BigInteger zero = BigInteger.valueOf(0);
    	if (a.multiply(b).compareTo(zero) == 0 ){	//EXIT condition
            tempo =  a.add(b);
        }
        else {
            tempo = extGCD(aux, b, a.mod(b));
            BigInteger temp = aux[0];
            aux[0] = aux[1];
            aux[1] = temp.subtract(aux[1].multiply(a.divide(b)));
        }
        return tempo;
    }

    /**
     * Calculates the inverse modulus: a^(-1) mod b
     * @param aux the helper array
     * @param a the base
     * @param b the the modulus
     * @return a^(-1) mod b, or -1 if the inverse does not exist.
     */
    public static  BigInteger  inverseMod(BigInteger[] aux, BigInteger a, BigInteger b) {
    	BigInteger zero = BigInteger.valueOf(0);
    	BigInteger one = BigInteger.valueOf(1);
    	if (GCD(a,b).compareTo(one) == 1)  //gcd a,b > 1
            return null;
        else {
        	BigInteger tempo = extGCD(aux, a, b);
        		//aux[0] > 0
                return (aux[0].compareTo(zero) == 1 ? aux[0].mod(b) : ((aux[0].mod(b)).add(b)));
        }
    }
    /**
	     * Calculates power of a^exp % m.
	     * @param m the mod
	     * @param exp the exponent
	     * @param a   the base
	     * @return a^exp % m.
     */
    public static BigInteger modPower(BigInteger m, BigInteger exp, BigInteger a)
    {
    	BigInteger zero = BigInteger.valueOf(0);
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two = BigInteger.valueOf(2);
    	if (exp.compareTo(one) == 0) return a.mod(m);
    	if (exp.compareTo(zero) == 0) return one;
    	if(exp.mod(two).compareTo(zero) == 0)
		{
			BigInteger x = modPower(m,exp.divide(two),a);
				return (x.multiply(x)).mod(m) ;
		}
		else
		{
			BigInteger x = modPower(m,exp.subtract(one).divide(two),a);
				return (x.multiply(x).multiply(a)).mod(m);
		}
	}
    /**
     * Calculates square root of res mod p.
     * @param res the residue
     * @param p the prime number
     * @return square root of res mod p or null if none can be found
    */
    public static BigInteger sqrtP(BigInteger res, BigInteger p)
    {
    	BigInteger zero = BigInteger.valueOf(0);
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two = BigInteger.valueOf(2);
    	BigInteger three = BigInteger.valueOf(3);
    	BigInteger four = BigInteger.valueOf(4);
    	if (p.mod(two).compareTo(zero) == 0) return null; //p not prime odd prime
    	BigInteger q = (p.subtract(one)).divide(two);
    	//make sure res is a residue mod p by checking that res^q mod p == 1
    	if (modPower(p,q,res).compareTo(one)!=0) return null;

    	while (q.mod(two).compareTo(zero) == 0)
    	{
    		q = q.divide(two);
    		//if res^q mod p != 1 run the complicated root find
    		if (modPower(p,q,res).compareTo(one)!=0)
    		{
    			return complexSqrtP(res, q, p) ;
    		}

    	}
    	//Code gets here if res^q mod p were all 1's and now q is odd
    	//then root = res^((q+1)/2) mod p
    	q = (q.add(one)).divide(two);
    	return modPower(p,q,res);
    }
    /**
     * Calculates square root of res mod p using a start exponent q.
     * @param res the residue
     * @param q the prime number
     * @param p the prime number
     * @return square root of res mod p or null if none can be found
    */
    private static BigInteger complexSqrtP(BigInteger res, BigInteger q,BigInteger p )
    {
    	BigInteger a = findNonResidue(p);
    	if (a == null) return null;
    	BigInteger zero = BigInteger.valueOf(0);
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two = BigInteger.valueOf(2);
    	BigInteger t = (p.subtract(one)).divide(two);
    	BigInteger negativePower = t; // a^negativePower mod p = -1 mod p this will be used to get the right power
    	//res^q mod p = a^((p-1)/2) mod p

    	while (q.mod(two).compareTo(zero) == 0)
    	{
    		q = q.divide(two);
    		t = t.divide(two);
    		//check to make sure that the right power was gonnen
    		if (modPower(p,q,res).compareTo(modPower(p,t,a))!=0)
    		{
    			//-(a^t mod p) = a^t*a^negativePower mod p = a^t+(negativePower) mod p
    			t = t.add(negativePower);
    		}
    	}
    	BigInteger helper[] = {one,one};
    	BigInteger inverceRes = inverseMod(helper,res,p);
    	//	inverceRes^((q-1)/2)
    	q = (q.subtract(one)).divide(two);
    	//System.out.println("p:"+p+" q:"+q+"invres: "+inverceRes);
    	BigInteger partone = modPower(p,q,inverceRes);
    	//  a^(t/2)
    	t = t.divide(two);
    	BigInteger parttwo = modPower(p,t,a);
    	BigInteger root;
    	root = partone.multiply(parttwo);
    	root = root.mod(p);
    	return root;
    }
    /**
     * Finds the non residue of the prime p
     * @param q the prime number
     * @return square root of res mod p or null if none can be found
    */
    private static BigInteger findNonResidue(BigInteger p)
    {
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two = BigInteger.valueOf(2);
    	//pick numbers till a^((p-1)/2) = -1;
    	int a = 2;
    	BigInteger q = (p.subtract(one)).divide(two);
    	while(true)
    	{
    		if (modPower(p,q,BigInteger.valueOf(a)).compareTo(one)!=0)
    		{
    			return BigInteger.valueOf(a);
    		}
    		//If i tried all the numbers in an int and got nothing somthing is wrong... this is taking too long.
    		if (a == 0) return null;
    		a++;
    	}

    }
    /**
     * Calculates square root of res mod pq.
     * @param roots, array[4] to store the 4 roots
     * @param res the residue
     * @param p first prime number
     * @param q second prime number
     * @return square root of res mod p or null if none can be found
    */
    public static boolean sqrtPQ(BigInteger[] roots, BigInteger res, BigInteger p, BigInteger q)
    {
    	BigInteger zero = BigInteger.valueOf(0);
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two  = BigInteger.valueOf(2);
    	if (p.mod(two).compareTo(zero) == 0) return false;
    	if (q.mod(two).compareTo(zero) == 0) return false;

    	BigInteger temp[] = {one,one};
    	BigInteger n = p.multiply(q);
    	extGCD(temp,p,q);
    	BigInteger a = temp[0];
    	BigInteger b = temp[1];
    	BigInteger x1 = sqrtP(res,p);
    	BigInteger x2 = sqrtP(res,q);
    	if (x1 == null || x2 == null) return false;
    	BigInteger s1 = (x2.multiply(a)).multiply(p);
    	BigInteger s2 = (x1.multiply(b)).multiply(q);
    	roots[0]      =	(s1.add(s2)).mod(n);			     //x2*a*p + x1+b*q;
    	roots[1] 	  = (s1.subtract(s2)).mod(n);			 //x2*a*p - x1+b*q;
    	roots[2] 	  = (s2.subtract(s1)).mod(n);			 //-x2*a*p + x1+b*q;
    	roots[3]      = ((s1.negate()).subtract(s2)).mod(n); //-x2*a*p - x1+b*q;
    	return true;
    }
    /**
     * attempts to find the factors of a compiste n = pq where p and q are primes
     * @param factors, array[2] to store the 2 factors
     * @param n is the composite number
     * @param runLenght is the number of attempts
     * @return true if found factors false if didnt
    */
    public static boolean pollardRho(BigInteger[] factors, BigInteger n, int runLenght)
    {
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two = BigInteger.valueOf(2);
    	BigInteger five = BigInteger.valueOf(5);

    	BigInteger a =BigInteger.valueOf(2);
    	BigInteger b =BigInteger.valueOf(5);
    	for(int i=1; i < runLenght; i++)
    	{
    		a = ((a.multiply(a)).add(one)).mod(n);
    		BigInteger btemp = ((b.multiply(b)).add(one));
    		b = ((btemp.multiply(btemp)).add(one)).mod(n);
    		BigInteger factor = GCD(a.subtract(b),n);
    		if (factor.compareTo(one) == 1)
    		{
    			factors[0] = factor;
    			factors[1] = n.divide(factor);
    			return true;
    		}
    	}
    	return false;
    }
    /**
     * Checks in an integer is prime
     * @param inetger to check
     * @param size amount of numbers to check
     * @return true if found factors false if didnt
    */
    public static boolean millerRabinPrimeCheck(BigInteger p,int  size)
    {
    	BigInteger zero = BigInteger.valueOf(0);
    	BigInteger one = BigInteger.valueOf(1);
    	BigInteger two = BigInteger.valueOf(2);
    	BigInteger minusOne = p.subtract(one);
    	BigInteger q = p.subtract(one);
    	BigInteger t = q;
    	for(int i = 2; i < size; i++)
    	{
    		//make sure i = p^x because then all powers of i are zero
    		if (BigInteger.valueOf(i).mod(p).compareTo(zero)!=0)
    		{
    			t = p.subtract(one);
    			BigInteger check = modPower(p, t, BigInteger.valueOf(i));
	    		if (check.compareTo(one)!=0 ) return false;
	    		while(t.mod(two).compareTo(zero)==0)
	    		{
	    			t = t.divide(two);
	    			//bool check = i^q mod p;
	    			check = modPower(p, t, BigInteger.valueOf(i));
	    			if (check.compareTo(one)!=0 && check.compareTo(minusOne)!=0)
	    			{
	    				return false;
	    			}
	    			if (check.compareTo(minusOne)==0)
	    			{
	    				break;
	    			}
	    		}
    		}
    	}
    	return true;
    }
 }

