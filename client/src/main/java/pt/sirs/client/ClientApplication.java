package pt.sirs.client;
public class ClientApplication {
    private String myIban;
	private int myMoney; //Vamos comecar com ints e depois se quisermos mudamos para floats/doubles
	
	public ClientApplication(String mib, int mm) {
		myIban=mib;
		myMoney=mm;
	}
	
	public void setMyIban(String mib){
		myIban=mib;
	}
	
	public String getMyIban(){
		return myIban;
	}
	
	public void setMyMoney(int mm){
		myMoney=mm;
	}
	
	public int getMyMoney(){
		return myMoney;
	}
	
    public static void main(String[] args) {
    	
    }
    
}
