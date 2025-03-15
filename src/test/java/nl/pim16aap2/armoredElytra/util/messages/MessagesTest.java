package nl.pim16aap2.armoredElytra.util.messages;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesTest
{
    @Test
    void insertVariable_shouldRestoreColor()
    {
        final String input = ChatColor.RED + "Test string with %VAR1% variable.";
        final String var1Value = ChatColor.GREEN + "a random";

        assertEquals(
            ChatColor.RED + "Test string with " + ChatColor.GREEN + "a random" + ChatColor.RED + " variable.",
            Messages.insertVariable(input, "%VAR1%", var1Value)
        );
    }

    @Test
    void insertVariable_shouldRestoreNonColor()
    {
        final String input = "Test string with %VAR1% variable.";
        final String var1Value = ChatColor.GREEN + "a random";

        assertEquals(
            "Test string with " + ChatColor.GREEN + "a random" + ChatColor.RESET + " variable.",
            Messages.insertVariable(input, "%VAR1%", var1Value)
        );
    }
}
