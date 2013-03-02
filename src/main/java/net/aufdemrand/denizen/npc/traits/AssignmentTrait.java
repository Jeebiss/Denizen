package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.notables.Notable;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.AssignmentScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Paginator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AssignmentTrait extends Trait {

    // Saved to the C2 saves.yml
    @Persist
    private String assignment = "";

    public void buildLocationContext() {
        // TODO: finish this
    }

    public AssignmentTrait() {
        super("assignment");
    }

    /**
     * Checks to see if the NPCs assignment is still a valid script on load of NPC.
     *
     */
    @Override
    public void load(DataKey key) throws NPCLoadException {
        // Check to make sure assignment is still valid. Throw a dB error if not.
        if (hasAssignment())
            dB.echoError("Missing assignment '" + assignment + "' for NPC '"
                    + npc.getName() + "/" + npc.getId() + "! Perhaps the script has been removed?");
    }

    /**
     * Sets the NPCs Assignment Script and fires an 'On Assignment:' action. Can specify a player for
     * context with the action.
     *
     * @param assignment the name of the Assignment Script, case in-sensitive
     * @param player the player adding the assignment, can be null
     *
     * @return false if the assignment is invalid
     *
     */
    public boolean setAssignment(String assignment, Player player) {
        if (ScriptRegistry.containsScript(assignment)) {
            this.assignment = assignment.toUpperCase();
            // Add Constants/Trigger trait if not already added to the NPC.
            if (!npc.hasTrait(ConstantsTrait.class)) npc.addTrait(ConstantsTrait.class);
            if (!npc.hasTrait(TriggerTrait.class)) npc.addTrait(TriggerTrait.class);
            // Reset Constants
            npc.getTrait(ConstantsTrait.class).rebuildAssignmentConstants();
            // 'On Assignment' action.
            DenizenAPI.getCurrentInstance().getNPCRegistry().getDenizen(npc).action("assignment", player);
            return true;
        }

        else return false;
    }

    /**
     * Gets the name of the current Assignment Script assigned to this NPC.
     *
     * @return assignment script name, null if not set or assignment is invalid
     *
     */
    public AssignmentScriptContainer getAssignment() {
        if (hasAssignment() && ScriptRegistry.containsScript(assignment, AssignmentScriptContainer.class))
            return ScriptRegistry.getScriptContainerAs(assignment, AssignmentScriptContainer.class);
        else return null;
    }

    /**
     * Checks to see if this NPC currently has an assignment.
     *
     * @return true if NPC has an assignment and it is valid
     *
     */
    public boolean hasAssignment() {
        if (assignment == null || assignment.equals("")) return false;
        if (ScriptRegistry.containsScript(assignment)) return true;
        else return false;
    }

    /**
     * Removes the current assignment and fires an 'On Remove Assignment:' action. Can specify a player for
     * context with the action.
     *
     * @param player the player removing the assignment, can be null
     *
     */
    public void removeAssignment (Player player) {
        assignment = "";
        DenizenAPI.getCurrentInstance().getNPCRegistry().getDenizen(npc).action("remove assignment", player);
    }

    public void describe(CommandSender sender, int page) throws CommandException {

        AssignmentScriptContainer assignmentScript = ScriptRegistry
                .getScriptContainerAs(assignment, AssignmentScriptContainer.class);

        Paginator paginator = new Paginator().header("Assignment");
        paginator.addLine("<e>Current assignment: " + (hasAssignment() ? this.assignment : "None.") + "");
        paginator.addLine("");

        if (!hasAssignment()) {
            paginator.sendPage(sender, page);
            return;
        }

        // Interact Scripts
        boolean entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Interact Scripts:");
        paginator.addLine("<e>Key: <a>Priority  <b>Name");
        if (assignmentScript.contains("INTERACT SCRIPTS")) {
            entriesPresent = true;
            for (String scriptEntry : assignmentScript.getStringList("INTERACT SCRIPTS"))
                paginator.addLine("<a>" + scriptEntry.split(" ")[0] + "<b> " + scriptEntry.split(" ", 2)[1]);
        } if (!entriesPresent) paginator.addLine("<c>No Interact Scripts assigned.");
        paginator.addLine("");

        if (!entriesPresent) {
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
            return;
        }

        // Scheduled Activities
        entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Scheduled Scripts:");
        paginator.addLine("<e>Key: <a>Time  <b>Name");
        if (assignmentScript.contains("SCHEDULED ACTIVITIES")) {
            entriesPresent = true;
            for (String scriptEntry : assignmentScript.getStringList("SCHEDULED ACTIVITIES"))
                paginator.addLine("<a>" + scriptEntry.split(" ")[0] + "<b> " + scriptEntry.split(" ", 2)[1]);
        } if (!entriesPresent) paginator.addLine("<c>No scheduled scripts activities.");
        paginator.addLine("");

        // Linked Notable Locations/Blocks
        entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Linked Notable Locations:");
        paginator.addLine("<e>Key: <a>Name  <b>World  <c>Location");
        if (!DenizenAPI.getCurrentInstance().notableManager().getNotables().isEmpty()) entriesPresent = true;
        for (Notable notable : DenizenAPI.getCurrentInstance().notableManager().getNotables())
            if (notable.hasLink(npc.getId())) paginator.addLine(notable.describe());
        if (!entriesPresent) paginator.addLine("<c>No notable locations linked to this NPC.");
        paginator.addLine("");

        // Actions
        entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Actions:");
        paginator.addLine("<e>Key: <a>Action name  <b>Script Size");
        if (assignmentScript.contains("ACTIONS")) entriesPresent = true;
        if (entriesPresent)
            for (String action : assignmentScript.getConfigurationSection("ACTIONS").getKeys(false))
                paginator.addLine("<a>" + action + " <b>" + assignmentScript.getStringList("ACTIONS." + action).size());
        else paginator.addLine("<c>No actions defined in the assignment.");
        paginator.addLine("");

        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
    }

}