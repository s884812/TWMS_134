//cygus quest skill

var status = 0;  

function start() {  
    status = -1;  
    action(1, 0, 0);  
}  

function action(mode, type, selection) {  
       
    if (mode == -1) {  
        cm.dispose();  
    }  
    else {   
        if (status >= 2 && mode == 0) {   
            cm.sendOk("Goodbye");   
            cm.dispose();   
            return;   
        }   
          
        if (mode == 1) {  
            status++;  
        }      
        else {  
            status--;  
        }  
          
        if (status == 0) {
/*jobs
"hero", 112,
"Paladin", 122,
"Dark Knight", 132,

"Fire/Poison Archmage", 212,
"Ice/Lightning Archmage", 222,
"BIishop", 232,

"Bow Master", 312,
"Crossbow Master", 322,

"Night Lord", 412,
"Shadower", 422,

"Buccaneer", 512,
"Corsair", 522,
*/

		
		   // paladin
		   		   
		   if (cm.getJob() ==  122 && cm.getSkillLevel(1221000) > 0) {
		   cm.sendNext("You already know your skills."); 
		   		   cm.dispose();
		   } 
		   else if (cm.getJob() ==  122 && cm.getSkillLevel(1221001) == 0){
		   cm.teachSkill(1221000,0,15); //Maple Warrior
cm.teachSkill(1221001,1,5); //Monster Magnet
cm.teachSkill(1221002,0,5); // Power Stance
cm.teachSkill(1221003,0,5); //Holy Charge
cm.teachSkill(1221004,0,5); //Divine Charge
cm.teachSkill(1220005,0,5); //Achilles
cm.teachSkill(1220006,0,5); //Guardian
cm.teachSkill(1221007,0,5); //Rush
cm.teachSkill(1221009,0,5); //Blast
cm.teachSkill(1220010,0,5); //Advanced Charge
cm.teachSkill(1221011,0,5); //Sanctuary
cm.teachSkill(1221012,0,5); //Hero's Will
		   		   cm.sendNext("You now know your job skills.."); 
				   cm.dispose();
		   }
		   //dark night
		          else if (cm.getJob() ==  132 && cm.getSkillLevel(1321002) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		   
		   } 
		   else if (cm.getJob() ==  132 && cm.getSkillLevel(1321002) == 0){
		   cm.teachSkill(1321000,0,15); //Maple Warrior
cm.teachSkill(1321001,1,5); //Monster Magnet
cm.teachSkill(1321002,0,5); //Power Stance
cm.teachSkill(1321003,0,5); //Rush
cm.teachSkill(1320005,0,5); //Achilles
cm.teachSkill(1320006,0,5); //Berserk
cm.teachSkill(1321007,0,5); //Beholder
cm.teachSkill(1320008,0,5); //Beholder's Healing
cm.teachSkill(1320009,0,5); //Beholder's Buff
cm.teachSkill(1321010,0,5); //Hero's Will
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
		    
		   
		  }
		   
		   
		   		   // hero
		   		   
		        else if (cm.getJob() ==  112 && cm.getSkillLevel(1121001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		   
		   } 
		   else if (cm.getJob() ==  112 && cm.getSkillLevel(1121001) == 0){
		   cm.teachSkill(1121000,0,15); //Maple Warrior
cm.teachSkill(1121001,1,5); //Monster Magnet
cm.teachSkill(1121002,0,5); //Power Stance
cm.teachSkill(1120003,0,5); //Advanced Combo
cm.teachSkill(1120004,0,5); //Achilles
cm.teachSkill(1120005,0,5); //Guardian
cm.teachSkill(1121006,0,5); //Rush
cm.teachSkill(1121008,0,5); //Brandish
cm.teachSkill(1121010,0,5); //Enrage
cm.teachSkill(1121011,0,5); //Hero's Will
		   		   cm.sendNext("You now know your skills.");
cm.dispose();				   
		  } 
		  
		   // firemage
		   
		   		      else  if (cm.getJob() ==  212 && cm.getSkillLevel(2121001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		   
		   } 
		   else if (cm.getJob() ==  212 && cm.getSkillLevel(2121001) == 0){
		   cm.teachSkill(2121000,0,15); //Maple Warrior
cm.teachSkill(2121001,1,5); //Big Bang
cm.teachSkill(2121002,0,5); //Mana Reflection
cm.teachSkill(2121003,0,5); //Fire Demon
cm.teachSkill(2121004,0,5); //Infinity
cm.teachSkill(2121005,0,5); //Ifrit
cm.teachSkill(2121006,0,5); //Paralyze
cm.teachSkill(2121007,0,5); //Meteor Shower
cm.teachSkill(2121008,0,5); //Hero's Will
		   		   cm.sendNext("You now know your sill."); 
				   cm.dispose();
		  }
		   
		   //il
		   else if (cm.getJob() ==  222 && cm.getSkillLevel(2221001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  222 && cm.getSkillLevel(2221001) == 0){
		   cm.teachSkill(2221000,0,15); //Maple Warrior
cm.teachSkill(2221001,1,5); //Big Bang
cm.teachSkill(2221002,0,5); //Mana Reflection
cm.teachSkill(2221003,0,5); //Ice Demon
cm.teachSkill(2221004,0,5); //Infinity
cm.teachSkill(2221005,0,5); //Elquines
cm.teachSkill(2221006,0,5); //Chain Lightning
cm.teachSkill(2221007,0,5); //Blizzard
cm.teachSkill(2221008,0,5); //Hero's Will
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
		   }
		   
		   		   //bishop
		   else if (cm.getJob() ==  232 && cm.getSkillLevel(2321001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  232 && cm.getSkillLevel(2321001) == 0){
		   cm.teachSkill(2221000,0,15); //Maple Warrior
cm.teachSkill(2321000,0,5); //Maple Warrior
cm.teachSkill(2321001,1,5); //Big Bang
cm.teachSkill(2321002,0,5); //Mana Reflection
cm.teachSkill(2321003,0,5); //Bahamut
cm.teachSkill(2321004,0,5); //Infinity
cm.teachSkill(2321005,0,5); //Holy Shield
cm.teachSkill(2321006,0,5); //Resurrection
cm.teachSkill(2321007,0,5); //Angel's Ray
cm.teachSkill(2321008,0,5); //Genesis
cm.teachSkill(2321009,0,5); //Hero's Will
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
		   }
		   //bow master
		   else if (cm.getJob() ==  312 && cm.getSkillLevel(3121002) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  312 && cm.getSkillLevel(3121002) == 0){
cm.teachSkill(3121000,0,15); //Maple Warrior - Bow
cm.teachSkill(3121002,1,5); //Sharp Eyes - Bow
cm.teachSkill(3121003,0,5); //Dragon Breath - Bow
cm.teachSkill(3121004,0,5); //Hurricane - Bow
cm.teachSkill(3120005,0,5); //Bow Expert - Bow
cm.teachSkill(3121006,0,5); //Phoenix - Bow
cm.teachSkill(3121007,0,5); //Hamstring - Bow
cm.teachSkill(3121008,0,5); //Concentrate - Bow
cm.teachSkill(3121009,0,5); //Hero's Will - Bow
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
				   }
				   
				   		   //crossbow master
		   else if (cm.getJob() ==  322 && cm.getSkillLevel(3221001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  322 && cm.getSkillLevel(3221001) == 0){
cm.teachSkill(3221000,0,15); //Maple Warrior - Crossbow
cm.teachSkill(3221001,1,5); //Piercing - Crossbow
cm.teachSkill(3221002,0,5); //Sharp Eyes - Crossbow
cm.teachSkill(3221003,0,5); //Dragon Breath - Crossbow
cm.teachSkill(3220004,0,5); //Crossbow Expertness  - Crossbow
cm.teachSkill(3221005,0,5); //Freezer - Crossbow
cm.teachSkill(3221006,0,5); //Blind - Crossbow
cm.teachSkill(3221007,0,5); //Sniping - Crossbow
cm.teachSkill(3221008,0,5); //Hero's Will - Crossbow
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
				   }
				   
				   		   //night lord
		 else if (cm.getJob() ==  412 && cm.getSkillLevel(4120002) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  412 && cm.getSkillLevel(4120002) == 0){
cm.teachSkill(4121000,0,15); //Maple Warrior
cm.teachSkill(4120002,1,5); //Shadow Shifter
cm.teachSkill(4121003,0,5); //Taunt
cm.teachSkill(4121004,0,5); //Ninja Ambush
cm.teachSkill(4120005,0,5); //Venomous Star
cm.teachSkill(4121006,0,5); //Spirit Claw
cm.teachSkill(4121007,0,5); //Triple Throw
cm.teachSkill(4121008,0,5); //Ninja Storm
cm.teachSkill(4121009,0,5); //Hero's Will
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
				   }
				   
				   //shadower
		 else if (cm.getJob() ==  422 && cm.getSkillLevel(4220002) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  422 && cm.getSkillLevel(4220002) == 0){
cm.teachSkill(4221000,0,15); //Maple Warrior
cm.teachSkill(4220002,1,5); //Shadow Shifter
cm.teachSkill(4221003,0,5); //Taunt
cm.teachSkill(4221004,0,5); //Ninja Ambush
cm.teachSkill(4220005,0,5); //Venomous Stab
cm.teachSkill(4221006,0,5); //Smokescreen
cm.teachSkill(4221007,0,5); //Boomerang Step
cm.teachSkill(4221001,0,5); //Assassinate
cm.teachSkill(4221008,0,5); //Hero's Will
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
				   }
		
		
				   //bac
		 else if (cm.getJob() ==  512 && cm.getSkillLevel(5121001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  512 && cm.getSkillLevel(5121001) == 0){
cm.teachSkill(5121000,0,15); //Maple Warrior
cm.teachSkill(5121001,1,5); //Dragon Strike
cm.teachSkill(5121002,0,5); //Energy Orb
cm.teachSkill(5121003,0,5); //Super Transformation
cm.teachSkill(5121004,0,5); //Demolition
cm.teachSkill(5121005,0,5); //Snatch
cm.teachSkill(5121007,0,5); //Barrage
cm.teachSkill(5121008,0,5); //Pirate's Rage
cm.teachSkill(5121009,0,5); //Speed Infusion
cm.teachSkill(5121010,0,5); //Time Leap
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
				   }	

				   //cro
		 else if (cm.getJob() ==  522 && cm.getSkillLevel(5220001) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  522 && cm.getSkillLevel(5220001) == 0){
cm.teachSkill(5220001,1,5); //Elemental Boost
cm.teachSkill(5220002,0,5); //Wrath of the Octopi
cm.teachSkill(5220011,0,5); //Bullseye
cm.teachSkill(5221000,0,15); //Maple Warrior
cm.teachSkill(5221003,0,5); //Aerial Strike
cm.teachSkill(5221004,0,5); //Rapid Fire
cm.teachSkill(5221006,0,5); //Battleship
cm.teachSkill(5221007,0,5); //Battleship Cannon
cm.teachSkill(5221008,0,5); //Battleship Torpedo
cm.teachSkill(5221009,0,5); //Hypnotize
cm.teachSkill(5221010,0,5); //Speed Infusion
		   		   cm.sendNext("You now know your skills."); 
				   cm.dispose();
				   }				   
		
		//not 4th job
		   else if (cm.getJob() != (112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 512, 522)){
		   		cm.sendNext("You are not fourth job."); 
				cm.dispose();
		   }
		else if (status == 1) {
cm.dispose();

        
        } 
        
        }
    }




}

	