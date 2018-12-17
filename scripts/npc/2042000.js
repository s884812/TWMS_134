var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}


function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (status == 0 && mode == 1) {
        var selStr = "Sign up for Monster Carnival!"
        for (var i = 0; i < 6; i++){
            if (getCPQField(i+1) != "") {
                selStr += "\r\n#b#L" + i + "# " + getCPQField(i+1) + "#l#k";
            }
        }
        if (cm.getParty() == null) {
            cm.sendOk("You are not in a party.");
            cm.dispose();
        } else {
            if (cm.isLeader()) {
                cm.sendSimple(selStr);
            } else {
                cm.sendOk("Please tell your party leader to speak with me.");
                cm.dispose();
            }
        }
    } else if (status == 1)
        if (selection == 0) { //Field 1
            if (cm.getEventManager("cpq1").getInstance("cpq1") == null) {
                if (1 < cm.getParty().getMembers().size() < 5) {
                    if (checkLevelsAndMap(30, 200) == 1) {
                        cm.sendOk("A player in your party is not the appropriate level.");
                        cm.dispose();
                    } else if (checkLevelsAndMap(30, 200) == 2) {
                        cm.sendOk("Everyone in your party isnt in this map.");
                        cm.dispose();
                    } else {
                        var instance = cm.getEventManager("cpq1").startInstance();
                        instance.setOwner(cm.getChar());
                        instance.registerCarnivalParty(cm.getChar(), cm.getMap(), 0);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Your party is not the appropriate size.");
                }
            } else if (cm.getEventManager("cpq1").getInstance("cpq1").getPlayerCount() == cm.getParty().getMembers().size()) {
                if (checkLevelsAndMap(30, 200) == 1) {
                    cm.sendOk("A player in your party is not the appropriate level.");
                    cm.dispose();
                } else if (checkLevelsAndMap(30, 200) == 2) {
                    cm.sendOk("Everyone in your party isnt in this map.");
                    cm.dispose();
                } else {
                    //Send challenge packet here
                    var owner = cm.getEventManager("cpq1").getInstance("cpq1").getOwner();
                    owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getChar()));
                    //if (owner.getConversation() != 1) {
                        cm.getScriptManager().start(owner.getClient(), 2042006);
                    //}
                    cm.sendOk("Your challenge has been sent.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("The two parties participating in Monster Carnival must have an equal number of party member");
                cm.dispose();
            }
        } else if (selection == 1) { //Field 2
            if (cm.getEventManager("cpq2").getInstance("cpq2") == null) {
                if (1 < cm.getParty().getMembers().size() < 5) {
                    if (checkLevelsAndMap(30, 200) == 1) {
                        cm.sendOk("A player in your party is not the appropriate level.");
                        cm.dispose();
                    } else if (checkLevelsAndMap(30, 200) == 2) {
                        cm.sendOk("Everyone in your party isnt in this map.");
                        cm.dispose();
                    } else {
                        var instance = cm.getEventManager("cpq2").startInstance();
                        instance.setOwner(cm.getChar());
                        instance.registerCarnivalParty(cm.getChar(), cm.getMap(), 0);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Your party is not the appropriate size.");
                }
            } else if (cm.getEventManager("cpq2").getInstance("cpq2").getPlayerCount() == cm.getParty().getMembers().size()) {
                if (checkLevelsAndMap(30, 200) == 1) {
                    cm.sendOk("A player in your party is not the appropriate level.");
                    cm.dispose();
                } else if (checkLevelsAndMap(30, 200) == 2) {
                    cm.sendOk("Everyone in your party isnt in this map.");
                    cm.dispose();
                } else {
                    //Send challenge packet here
                    var owner = cm.getEventManager("cpq2").getInstance("cpq2").getOwner();
                    owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getChar()));
                    //if (owner.getConversation() != 1) {
                        cm.getScriptManager().start(owner.getClient(), 2042006);
                    //}
                    cm.sendOk("Your challenge has been sent.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("The two parties participating in Monster Carnival must have an equal number of party member");
                cm.dispose();
            }
        } else if (selection == 2) { //Field 3
            if (cm.getEventManager("cpq3").getInstance("cpq3") == null) {
                if (1 < cm.getParty().getMembers().size() < 5) {
                    if (checkLevelsAndMap(30, 200) == 1) {
                        cm.sendOk("A player in your party is not the appropriate level.");
                        cm.dispose();
                    } else if (checkLevelsAndMap(30, 200) == 2) {
                        cm.sendOk("Everyone in your party isnt in this map.");
                        cm.dispose();
                    } else {
                        var instance = cm.getEventManager("cpq3").startInstance();
                        instance.setOwner(cm.getChar());
                        instance.registerCarnivalParty(cm.getChar(), cm.getMap(), 0);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Your party is not the appropriate size.");
                }
            } else if (cm.getEventManager("cpq3").getInstance("cpq3").getPlayerCount() == cm.getParty().getMembers().size()) {
                if (checkLevelsAndMap(30, 200) == 1) {
                    cm.sendOk("A player in your party is not the appropriate level.");
                    cm.dispose();
                } else if (checkLevelsAndMap(30, 200) == 2) {
                    cm.sendOk("Everyone in your party isnt in this map.");
                    cm.dispose();
                } else {
                    //Send challenge packet here
                    var owner = cm.getEventManager("cpq3").getInstance("cpq3").getOwner();
                    owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getChar()));
                    //if (owner.getConversation() != 1) {
                        cm.getScriptManager().start(owner.getClient(), 2042006);
                    //}
                    cm.sendOk("Your challenge has been sent.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("The two parties participating in Monster Carnival must have an equal number of party member");
                cm.dispose();
            }
        } else if (selection == 3) { //Field 4
            if (cm.getEventManager("cpq4").getInstance("cpq4") == null) {
                if (1 < cm.getParty().getMembers().size() < 5) {
                    if (checkLevelsAndMap(30, 200) == 1) {
                        cm.sendOk("A player in your party is not the appropriate level.");
                        cm.dispose();
                    } else if (checkLevelsAndMap(30, 200) == 2) {
                        cm.sendOk("Everyone in your party isnt in this map.");
                        cm.dispose();
                    } else {
                        var instance = cm.getEventManager("cpq4").startInstance();
                        instance.setOwner(cm.getChar());
                        instance.registerCarnivalParty(cm.getChar(), cm.getMap(), 0);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Your party is not the appropriate size.");
                }
            } else if (cm.getEventManager("cpq4").getInstance("cpq4").getPlayerCount() == cm.getParty().getMembers().size()) {
                if (checkLevelsAndMap(30, 200) == 1) {
                    cm.sendOk("A player in your party is not the appropriate level.");
                    cm.dispose();
                } else if (checkLevelsAndMap(30, 200) == 2) {
                    cm.sendOk("Everyone in your party isnt in this map.");
                    cm.dispose();
                } else {
                    //Send challenge packet here
                    var owner = cm.getEventManager("cpq4").getInstance("cpq4").getOwner();
                    owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getChar()));
                    //if (owner.getConversation() != 1) {
                        cm.getScriptManager().start(owner.getClient(), 2042006);
                    //}
                    cm.sendOk("Your challenge has been sent.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("The two parties participating in Monster Carnival must have an equal number of party member");
                cm.dispose();
            }
        } else if (selection == 4) { //Field 5
            if (cm.getEventManager("cpq5").getInstance("cpq5") == null) {
                if (2 < cm.getParty().getMembers().size() < 7) {
                    if (checkLevelsAndMap(30, 200) == 1) {
                        cm.sendOk("A player in your party is not the appropriate level.");
                        cm.dispose();
                    } else if (checkLevelsAndMap(30, 200) == 2) {
                        cm.sendOk("Everyone in your party isnt in this map.");
                        cm.dispose();
                    } else {
                        var instance = cm.getEventManager("cpq5").startInstance();
                        instance.setOwner(cm.getChar());
                        instance.registerCarnivalParty(cm.getChar(), cm.getMap(), 0);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Your party is not the appropriate size.");
                }
            } else if (cm.getEventManager("cpq5").getInstance("cpq5").getPlayerCount() == cm.getParty().getMembers().size()) {
                if (checkLevelsAndMap(30, 200) == 1) {
                    cm.sendOk("A player in your party is not the appropriate level.");
                    cm.dispose();
                } else if (checkLevelsAndMap(30, 200) == 2) {
                    cm.sendOk("Everyone in your party isnt in this map.");
                    cm.dispose();
                } else {
                    //Send challenge packet here
                    var owner = cm.getEventManager("cpq5").getInstance("cpq5").getOwner();
                    owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getChar()));
                    //if (owner.getConversation() != 1) {
                        cm.getScriptManager().start(owner.getClient(), 2042006);
                    //}
                    cm.sendOk("Your challenge has been sent.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("The two parties participating in Monster Carnival must have an equal number of party member");
                cm.dispose();
            }
        } else if (selection == 5) { //Field 6
            if (cm.getEventManager("cpq6").getInstance("cpq6") == null) {
                if (2 < cm.getParty().getMembers().size() < 6) {
                    if (checkLevelsAndMap(30, 200) == 1) {
                        cm.sendOk("A player in your party is not the appropriate level.");
                        cm.dispose();
                    } else if (checkLevelsAndMap(30, 200) == 2) {
                        cm.sendOk("Everyone in your party isnt in this map.");
                        cm.dispose();
                    } else {
                        var instance = cm.getEventManager("cpq6").startInstance();
                        instance.setOwner(cm.getChar());
                        instance.registerCarnivalParty(cm.getChar(), cm.getMap(), 0);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Your party is not the appropriate size.");
                }
            } else if (cm.getEventManager("cpq6").getInstance("cpq6").getPlayerCount() == cm.getParty().getMembers().size()) {
                if (checkLevelsAndMap(30, 200) == 1) {
                    cm.sendOk("A player in your party is not the appropriate level.");
                    cm.dispose();
                } else if (checkLevelsAndMap(30, 200) == 2) {
                    cm.sendOk("Everyone in your party isnt in this map.");
                    cm.dispose();
                } else {
                    //Send challenge packet here
                    var owner = cm.getEventManager("cpq6").getInstance("cpq6").getOwner();
                    owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getChar()));
                    //if (owner.getConversation() != 1) {
                        cm.getScriptManager().start(owner.getClient(), 2042006);
                    //}
                    cm.sendOk("Your challenge has been sent.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("The two parties participating in Monster Carnival must have an equal number of party member");
                cm.dispose();
            }
        }
}

function checkLevelsAndMap(lowestlevel, highestlevel) {
    var party = cm.getParty().getMembers();
    var mapId = cm.getMapId();
    var valid = 0;
    var inMap = 0;

    var it = party.iterator();
    while (it.hasNext()) {
        var cPlayer = it.next();
        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) {
            valid = 1;
        }
        if (cPlayer.getMapid() != mapId) {
            valid = 2;
        }
    }

}

function getCPQField(fieldnumber) {
    var status = "";
    var event1 = cm.getEventManager("cpq"+fieldnumber);
    if (event1 != null) {
        var event = event1.getInstance("cpq"+fieldnumber);
        if (event == null && (0 < fieldnumber < 5)) {
            status = "Carnival Field "+fieldnumber+"(2~4) ppl";
        } else if (event == null && (4 < fieldnumber < 7)) {
            status = "Carnival Field "+fieldnumber+"(3~6) ppl";
        } else if (event != null && (event1.getProperty("started").equals("false"))) {
            var averagelevel = 0;
            for (i = 0; i < event.getPlayerCount(); i++) {
                averagelevel += event.getPlayers().get(i).getLevel();
            }
            averagelevel /= event.getPlayerCount();
            status = event.getPlayers().get(0).getParty().getLeader().getName()+"/"+event.getPlayerCount()+"users/Avg. Level "+averagelevel;
        }
    }
    return status;
}
