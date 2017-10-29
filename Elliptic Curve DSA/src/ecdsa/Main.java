package ecdsa;

public class Main {

	public static void main(String[] args) {
		int M = 71;
		EllipticGroup eGroup = new EllipticGroup(M);
		eGroup.generatedGroupElements();
		System.out.println(eGroup.getPoints());
	}

}
