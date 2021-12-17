package net.runelite.rs.api;

import net.runelite.mapping.Import;

public interface RSLink
{
	@Import("previous")
	RSLink getPrevious$api();

	@Import("next")
	RSLink next$api();

	@Import("remove")
	void remove$api();
}
