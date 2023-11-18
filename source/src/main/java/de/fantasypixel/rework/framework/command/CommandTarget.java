package de.fantasypixel.rework.framework.command;

/**
 * Enum representing the possible targets for a command.
 */
public enum CommandTarget {
    /**
     * The command can only be executed by players.
     */
    PLAYER,

    /**
     * The command can only be executed from the console.
     */
    CONSOLE,

    /**
     * The command can be executed by both players and from the console.
     */
    BOTH
}
