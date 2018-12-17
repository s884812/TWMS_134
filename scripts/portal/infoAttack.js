function enter(pi) {
	if (pi.getQuestStatus(1035) == 1)
		pi.ShowWZEffect("UI/tutorial.img/20");

	//pi.blockPortal();
	return true;
}