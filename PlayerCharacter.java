
public class PlayerCharacter extends Character {

	String name;
	
	public PlayerCharacter(String name){
		this.name = name;
		
	}
	public PlayerCharacter(String name, int hp, int atk) {
		super(name, hp, atk);
		// 持失切人 搾汁廃 奄管. 採乞拭惟辞 雌紗閤精 依
	}
	public void heal(){
		hp += 20;
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println(name + "HEAL!!!");
		System.out.println(name + " HP : "+hp);
		System.out.println("@ぃぃぃぃぃぃぃぃぃぃぃぃぃぃぃぃぃぃぃ@");
	}
}
