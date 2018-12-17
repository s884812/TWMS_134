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
        //  if (cm.getJob() !=  1211 || cm.getJob() !=  1111 || cm.getJob() !=  1411 || cm.getJob() !=  1511 || cm.getJob() !=  1311 ) {
		/*   if (cm.getJob() > 1000 || cm.getJob() < 2000) {
		   cm.sendNext("You are not a cygus knight."); 
		   } */
		   
		   // dawn warrior
		   		   
		   if (cm.getJob() ==  1111 && cm.getSkillLevel(11111004) > 0) {
		   cm.sendNext("You already know your skill."); 
		   		   cm.dispose();
		   } 
		   else if (cm.getJob() ==  1111 && cm.getSkillLevel(11111004) == 0){
		   cm.teachSkill(11111004, 1, 30);
		   		   cm.sendNext("You now know brandish."); 
				   cm.dispose();
		   }
		   //Blaze wizard
		          else if (cm.getJob() ==  1211 && cm.getSkillLevel(12111004) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		   
		   } 
		   else if (cm.getJob() ==  1211 && cm.getSkillLevel(12111004) == 0){
		   cm.teachSkill(12111004, 1, 20);
		   		   cm.sendNext("You now know ifrit."); 
				   cm.dispose();
		    
		   
		  }
		   
		   
		   		   // sin
		   		   
		        else if (cm.getJob() ==  1411 && cm.getSkillLevel(14111005) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		   
		   } 
		   else if (cm.getJob() ==  1411 ){
		   cm.teachSkill(14111005, 1, 20);
		   		   cm.sendNext("You now know triple throw.");
cm.dispose();				   
		  } 
		  
		   // thunder
		   
		   		      else  if (cm.getJob() ==  1511 && cm.getSkillLevel(15111004) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		   
		   } 
		   else if (cm.getJob() ==  1511 && cm.getSkillLevel(15111004) == 0){
		   cm.teachSkill(15111004, 1, 20);
		   		   cm.sendNext("You now know triple barrage."); 
				   cm.dispose();
		  }
		   
		   //wind
		   		   		        else if (cm.getJob() ==  1311 && cm.getSkillLevel(13111002) > 0) {
		   cm.sendNext("You already know your skill."); 
		   cm.dispose();
		  
		   } 
		   else if (cm.getJob() ==  1311 && cm.getSkillLevel(13111002) == 0){
		   cm.teachSkill(13111002, 1, 20);
		   		   cm.sendNext("You now know triple hurricane."); 
				   cm.dispose();
		   }
		
		   else if (cm.getJob() < 1000 || cm.getJob >= 2000){
		   cm.teachSkill(13111002, 1, 20);
		   		cm.sendNext("You are not a cyngus knight."); 
				cm.dispose();
		   }
		else if (status == 1) {
cm.dispose();

        
        } 
        
        }
    }




}


	