package krisko.acadyan;

public enum Strings
{
	// Menu
	PLAY("Play", "Spielen"),
	LEVEL_SELECT("Level select", "Levelauswahl"),
	SHOP("Shop", "Shop"),
	MULTIPLAYER("Multiplayer", "Mehrspieler"),
	OPTIONS("Options", "Optionen"),
	EXIT("Exit", "Beenden"),
	EDITOR("Editor", "Editor"),
	
	
	// Level select
	WORLD("World", "Welt"),
	
	
	// Shop
	BUY("Buy", "Kaufen"),
	BOUGHT("Bought", "Gekauft"),
	
	SKINS("Skins", "Aussehen"),
	STATS("Stats", "Status"),
	UPGRADES("Upgrades", "Upgrades"),
	ENCHANTMENTS("Entchantment", "Verzauberungen"),
	
	WEAPONS("Weapons", "Waffen"),
	ITEMS("Items", "Items"),
	GUN("Gun", "Pistole"),
	ROCKET("Rocket", "Raketenwerfer"),
	
	
	// Multiplayer
	JOIN_SERVER("Join Server", "Server beitreten"),
	HOST_SERVER("Host Server", "Server erstellen"),
	JOIN("Join", "Beitreten"),
	HOST("Host", "Erstellen"),
	
	
	// Editor
	N_MORE_SPACE("Press 'N' for more space", "'N' drücken, für mehr Platz"),
	
	SET_MAP_NAME("Set map name", "Map Name eingeben"),
	SAVE_MAP_AS("Save map as...", "Map speichern unter..."),
	OPEN_MAP("Open map", "Map öffnen"),
	MAP_EXISTS_REPLACE("Map already exists. Replace Map?", "Map existiert bereits. Map ersetzen?"),
	SAVE_MAP_BEFORE_TESTING("Map needs to be saved before testing", "Map muss vor dem Testen gespeichert werden"),
	
	COPY("Copy", "Kopieren"),
	
	
	// Options
	GENERAL("General", "Allgemein"),
	AUDIO("Audio", "Audio"),
	VIDEO("Video", "Video"),
	CONTROLS("Controls", "Steuerung"),
	
	LANGUAGE("Language", "Sprache"),
	CHANGE_LANGUAGE("Change Language", "Sprache wechseln"),
	BLOOD("Blood", "Blut"),
	CURSOR_SCALE("Cursor scale", "Cursor Skalierung"),
	
	VOLUME("Volume", "Lautstärke"),
	MUSIC("Music", "Musik"),
	EFFECTS("Effects", "Effekte"),
	
	
	// Controls
	MOUSE_SENSITIVITY("Mouse sensitivity", "Mausempfindlichkeit"),
	LEFT("Left", "Links"),
	RIGHT("Right", "Rechts"),
	JUMP("Jump", "Springen"),
	SPRINT("Sprint", "Sprinten"),
	SNEAK("Sneak", "Schleichen"),
	
	PUNCH("Punch", "Schlagen"),
	
	CHANGE_COLOR("Change color", "Farbe wechseln"),
	HIDE_PLAYER("Hide player", "Spieler verbergen"),
	INVENTORY("Inventory", "Inventar"),
	
	
	//
	BACK("Back", "Zurück"),
	OKAY("Okay", "Okay"),
	CURRENT_LANGUAGE("English", "Deutsch"),
	
	SPEED("Speed", "Geschwindigkeit"),
	OFFSET("Offset", "Offset"),
	STRENGTH("Strength", "Stärke"),
	TIME("Time", "Zeit"),
	RESPAWN("Respawn", "Respawn"),
	
	
	// texts
	TEXT_LVL_COMPLETE("Level complete", "Level geschafft"),
	TEXT_PRESS_SPACE_TO_CONTINUE("Press space to continue", "Leertaste drücken zum fortfahren"),
	
	LOGGEDIN_AS("Logged in as", "Eingeloggt als"),
	ACCOUNT_NAME("Account Name", "Account Name");
	
	private Strings(String english, String german)
	{
		eng = english;
		ger = german;
	}
	
	public String get()
	{
		if(Options.german())
			return ger;
		
		return eng;
	}
	
	private final String eng, ger;
}