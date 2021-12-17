package net.runelite.rs.api;

import net.runelite.mapping.Import;

public interface RSParamComposition
{
	boolean isString$api();

	@Import("type")
	char getType$api();

	@Import("defaultInt")
	int getDefaultInt$api();

	@Import("defaultStr")
	String getDefaultStr$api();
}
